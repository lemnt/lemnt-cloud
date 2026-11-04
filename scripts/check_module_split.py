#!/usr/bin/env python3
"""检查模块拆分后的 SQL 归属与隔离规则。

规则：
1. 每个新业务服务（product/promotion/order/pay）只有一个初始化 SQL 文件。
2. SQL 文件中不包含旧表名（mall_*、trade_*）。
3. 业务表必须使用所属上下文前缀（product_/promotion_/order_/pay_）。
4. 消息表使用 {context}_message_outbox 或 {context}_message_inbox 格式。
5. 不允许出现跨上下文表（如 order 模块不应包含 pay_* 表）。
6. system/member 不参与拆分检查，保持原有结构。

默认只读，不修改任何文件。
"""
import argparse
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SQL_LEXER = re.compile(
    r"--[^\n]*|/\*.*?\*/|'(?:''|\\.|[^'\\])*'|\"(?:\"\"|\\.|[^\"\\])*\"|`(?:``|[^`])*`|[A-Za-z_][A-Za-z_0-9$]*|[^\s]",
    re.S,
)

# 各模块允许的表前缀
CONTEXT_PREFIXES = {
    "product": "product_",
    "promotion": "promotion_",
    "order": "order_",
    "pay": "pay_",
}

# 消息表的合法后缀
MESSAGE_SUFFIXES = ("_message_outbox", "_message_inbox")

# 旧表名前缀，不应出现在新模块 SQL 中
LEGACY_PREFIXES = ("mall_", "trade_")


def sql_tokens(sql):
    """忽略注释，返回 SQL 词法序列。"""
    return [m.group() for m in SQL_LEXER.finditer(sql) if not m.group().startswith(("--", "/*"))]


def extract_tables(sql):
    """从 CREATE TABLE 语句中提取表名。"""
    tokens = sql_tokens(sql)
    tables = []
    i = 0
    while i < len(tokens):
        if tokens[i].upper() == "CREATE" and i + 1 < len(tokens) and tokens[i + 1].upper() == "TABLE":
            j = i + 2
            if j < len(tokens) and tokens[j].upper() == "IF":
                j += 3  # 跳过 IF NOT EXISTS
            if j < len(tokens):
                table_name = tokens[j].strip("`")
                tables.append(table_name)
        i += 1
    return tables


def check_single_sql_file(module):
    """检查模块是否只有一个初始化 SQL 文件。"""
    sql_dir = ROOT / f"lemnt-server/lemnt-{module}/sql"
    if not sql_dir.exists():
        return [{"rule": "sql-directory-missing", "module": module, "detail": str(sql_dir)}]
    sql_files = sorted(sql_dir.rglob("*.sql"))
    expected = [sql_dir / f"lemnt_{module}.sql"]
    if sql_files != expected:
        return [{
            "rule": "multiple-sql-files",
            "module": module,
            "detail": f"期望 {expected[0].name}，实际 {[f.name for f in sql_files]}",
        }]
    return []


def check_no_legacy_tables(module, tables):
    """检查是否包含旧表名。"""
    findings = []
    for table in tables:
        for prefix in LEGACY_PREFIXES:
            if table.startswith(prefix):
                findings.append({
                    "rule": "legacy-table-found",
                    "module": module,
                    "table": table,
                    "detail": f"新模块 SQL 不应包含旧表名 {prefix}*",
                })
    return findings


def check_table_prefix(module, tables):
    """检查表名是否使用正确的前缀。"""
    expected_prefix = CONTEXT_PREFIXES[module]
    findings = []
    for table in tables:
        # 消息表允许
        if any(table.endswith(suffix) for suffix in MESSAGE_SUFFIXES):
            if not table.startswith(expected_prefix):
                findings.append({
                    "rule": "message-table-prefix",
                    "module": module,
                    "table": table,
                    "detail": f"消息表必须以 {expected_prefix} 开头",
                })
            continue
        # 业务表必须有上下文前缀
        if not table.startswith(expected_prefix):
            findings.append({
                "rule": "table-prefix-mismatch",
                "module": module,
                "table": table,
                "detail": f"表名必须以 {expected_prefix} 开头",
            })
    return findings


def check_no_cross_context_tables(module, tables):
    """检查是否包含其他上下文的表。"""
    findings = []
    allowed_prefix = CONTEXT_PREFIXES[module]
    for table in tables:
        # 消息表已经单独检查
        if any(table.endswith(suffix) for suffix in MESSAGE_SUFFIXES):
            continue
        # 检查是否包含其他上下文的前缀
        for other_module, other_prefix in CONTEXT_PREFIXES.items():
            if other_module != module and table.startswith(other_prefix):
                findings.append({
                    "rule": "cross-context-table",
                    "module": module,
                    "table": table,
                    "detail": f"属于 {other_module} 上下文的表不应出现在 {module} 模块",
                })
    return findings


def check_module(module):
    """检查单个模块的 SQL 拆分规则。"""
    findings = []
    findings.extend(check_single_sql_file(module))
    if findings:
        return findings  # SQL 文件不存在或数量不对，跳过后续检查

    sql_path = ROOT / f"lemnt-server/lemnt-{module}/sql/lemnt_{module}.sql"
    sql = sql_path.read_text()
    tables = extract_tables(sql)

    if not tables:
        findings.append({
            "rule": "no-tables-found",
            "module": module,
            "detail": "SQL 文件中未找到 CREATE TABLE 语句",
        })
        return findings

    findings.extend(check_no_legacy_tables(module, tables))
    findings.extend(check_table_prefix(module, tables))
    findings.extend(check_no_cross_context_tables(module, tables))
    return findings


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--json", type=Path, help="可选写入机器报告")
    args = parser.parse_args()

    all_findings = []
    summary = {"modules": [], "tablesByModule": {}, "totalTables": 0}

    for module in CONTEXT_PREFIXES:
        summary["modules"].append(module)
        findings = check_module(module)
        all_findings.extend(findings)

        # 统计表数量
        sql_path = ROOT / f"lemnt-server/lemnt-{module}/sql/lemnt_{module}.sql"
        if sql_path.exists():
            tables = extract_tables(sql_path.read_text())
            summary["tablesByModule"][module] = len(tables)
            summary["totalTables"] += len(tables)

    if args.json:
        args.json.write_text(json.dumps({
            "summary": summary,
            "violations": all_findings,
        }, ensure_ascii=False, indent=2) + "\n")

    print(json.dumps({
        "modules": summary["modules"],
        "tablesByModule": summary["tablesByModule"],
        "totalTables": summary["totalTables"],
    }, ensure_ascii=False))

    for finding in all_findings:
        module = finding.get("module", "")
        table = finding.get("table", "")
        detail = finding.get("detail", "")
        print(f"[{finding['rule']}] {module}/{table}: {detail}")

    print(f"违规：{len(all_findings)}")
    return bool(all_findings)


if __name__ == "__main__":
    raise SystemExit(main())
