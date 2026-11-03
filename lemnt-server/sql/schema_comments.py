#!/usr/bin/env python3
"""补齐初始化COMMENT、检查覆盖率，并按已有库真实DDL生成缺失注释升级计划。"""
import argparse
from dataclasses import dataclass
import json
import os
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
CATALOG = Path(__file__).with_name("schema-comments.json")
LEXER = re.compile(r"--[^\n]*|/\*.*?\*/|'(?:''|\\.|[^'\\])*'|\"(?:\"\"|\\.|[^\"\\])*\"|`(?:``|[^`])*`|[A-Za-z_][A-Za-z_0-9$]*|[^\s]", re.S)
CONSTRAINTS = {"PRIMARY", "UNIQUE", "KEY", "INDEX", "CONSTRAINT", "FOREIGN", "CHECK"}


@dataclass
class Token:
    value: str
    start: int
    end: int


def tokens(sql):
    """忽略行/块注释，字符串和反引号整体保留，防止拆开类型或表达式。"""
    return [Token(m.group(), m.start(), m.end()) for m in LEXER.finditer(sql)
            if not m.group().startswith(("--", "/*"))]


def name(token):
    return token.value.strip("`")


def comment(parts):
    for i, token in enumerate(parts):
        if token.value.upper() == "COMMENT":
            value = parts[i + 1]
            if value.value == "=":
                value = parts[i + 2]
            return value, value.value[1:-1].replace("''", "'").strip()
    return None, ""


def segments(parts):
    """只在顶层逗号处拆列，保留DECIMAL、枚举、函数和约束内部逗号。"""
    start = depth = 0
    for i, token in enumerate(parts):
        depth += (token.value == "(") - (token.value == ")")
        if token.value == "," and depth == 0:
            yield parts[start:i]
            start = i + 1
    if parts[start:]:
        yield parts[start:]


def definitions(sql):
    """返回CREATE TABLE和ADD COLUMN的表/列位置，不把索引当字段。"""
    statements = []
    start = 0
    all_tokens = tokens(sql)
    for i, token in enumerate(all_tokens):
        if token.value == ";":
            statements.append(all_tokens[start:i])
            start = i + 1
    if all_tokens[start:]:
        statements.append(all_tokens[start:])
    for statement in statements:
        if len(statement) < 4:
            continue
        first = [p.value.upper() for p in statement[:2]]
        if first == ["CREATE", "TABLE"]:
            i = 5 if statement[2].value.upper() == "IF" else 2
            table = name(statement[i])
            opening = i + 1
            if statement[opening].value != "(":
                raise ValueError("不支持的表定义: " + table)
            depth = 1
            closing = opening + 1
            while depth:
                depth += (statement[closing].value == "(") - (statement[closing].value == ")")
                closing += 1
            yield table, None, statement[closing:], statement[closing - 1].end
            for part in segments(statement[opening + 1:closing - 1]):
                if part and part[0].value.upper() not in CONSTRAINTS:
                    yield table, name(part[0]), part, part[-1].end
        elif first == ["ALTER", "TABLE"]:
            table = name(statement[2])
            for part in segments(statement[3:]):
                if not part or part[0].value.upper() != "ADD":
                    continue
                i = 2 if part[1].value.upper() == "COLUMN" else 1
                if part[i].value.upper() not in CONSTRAINTS:
                    yield table, name(part[i]), part[i:], part[-1].end


def source_files():
    yield "system", ROOT / "sql/lemnt_system.sql"
    for domain in ("member", "mall", "trade"):
        for path in sorted((ROOT / f"lemnt-{domain}/sql").glob("V*.sql")):
            yield domain, path


def inventory():
    result = {}
    for domain, path in source_files():
        for table, column, parts, _ in definitions(path.read_text()):
            item = result.setdefault(domain, {}).setdefault(table, {"comment": "", "columns": {}})
            _, value = comment(parts)
            if column is None:
                item["comment"] = value
            else:
                item["columns"][column] = value
    return result


def literal(value):
    return "'" + value.replace("'", "''") + "'"


def without_comments(sql):
    """结构校验：仅剔除COMMENT属性及其值，其他DDL词法序列必须不变。"""
    parts = tokens(sql)
    result = []
    i = 0
    while i < len(parts):
        if parts[i].value.upper() == "COMMENT":
            i += 1
            if parts[i].value == "=":
                i += 1
            i += 1
        else:
            result.append(parts[i].value)
            i += 1
    return result


def fill(sql, catalog):
    changes = []
    for table, column, parts, end in definitions(sql):
        previous, value = comment(parts)
        if value:
            continue
        item = catalog[table]
        description = item["comment"] if column is None else item["columns"][column]
        if not description.strip():
            raise ValueError(f"注释未定义: {table}.{column}")
        if previous:
            changes.append((previous.start, previous.end, literal(description)))
        else:
            position = parts[-1].end if parts else end
            changes.append((position, position, " COMMENT " + literal(description)))
    for start, end, value in sorted(changes, reverse=True):
        sql = sql[:start] + value + sql[end:]
    return sql


def upgrade_plan(ddl, table_catalog):
    """从SHOW CREATE TABLE保留实际列定义，仅补缺失注释，不复制仓库字段类型。"""
    plan = []
    for table, column, parts, _ in definitions(ddl):
        if column is not None and column not in table_catalog["columns"]:
            continue
        previous, value = comment(parts)
        if value:
            continue
        description = table_catalog["comment"] if column is None else table_catalog["columns"][column]
        if column is None:
            plan.append(f"ALTER TABLE `{table}` COMMENT = {literal(description)};")
        else:
            original = ddl[parts[0].start:parts[-1].end]
            updated = (original[:previous.start - parts[0].start] + literal(description)
                       + original[previous.end - parts[0].start:]) if previous else original + " COMMENT " + literal(description)
            if without_comments(original) != without_comments(updated):
                raise ValueError("字段定义发生变化: " + column)
            plan.append(f"ALTER TABLE `{table}` MODIFY COLUMN {updated};")
    return plan


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--fill", action="store_true", help="只补初始化SQL缺失注释")
    parser.add_argument("--domain", choices=["system", "member", "mall", "trade"], help="读取已有库生成升级计划")
    parser.add_argument("--apply", action="store_true", help="执行已有库升级；默认只打印SQL")
    parser.add_argument("--database", help="覆盖lemnt_<domain>库名")
    args = parser.parse_args()
    catalog = json.loads(CATALOG.read_text())
    if args.apply and not args.domain:
        parser.error("--apply必须指定--domain")
    if args.domain:
        legacy = json.loads(CATALOG.with_name("schema-comments-legacy.json").read_text())
        catalog[args.domain] = {**catalog[args.domain], **legacy.get(args.domain, {})}
        user = os.environ.get("DB_USER")
        if not user:
            parser.error("请设置DB_USER，密码可通过DB_PASSWORD环境变量传入")
        database = args.database or "lemnt_" + args.domain
        command = ["mysql", "--protocol=TCP", "--batch", "--raw", "--skip-column-names", "--default-character-set=utf8mb4",
                   "-h", os.environ.get("DB_HOST", "127.0.0.1"), "-P", os.environ.get("DB_PORT", "3306"), "-u", user, database]
        environment = dict(os.environ)
        environment["MYSQL_PWD"] = environment.pop("DB_PASSWORD", "")

        def query(sql):
            # 查询和写入都走stdin，凭证不出现在命令行或生成计划。
            return subprocess.run(command, input=sql, text=True, env=environment, capture_output=True, check=True).stdout

        existing = set(query("SHOW TABLES").splitlines())
        count = 0
        for table, item in catalog[args.domain].items():
            if table not in existing:
                print("-- 跳过不存在的表: " + table)
                continue
            ddl = query(f"SHOW CREATE TABLE `{table}`").split("\t", 1)[1]
            plan = upgrade_plan(ddl, item)
            for sql in plan:
                print(sql)
                if args.apply:
                    query(sql)
                count += 1
        print(f"-- {'已执行' if args.apply else '待审核计划'}: {count} 条；已有非空注释保持不变")
        return
    for domain, path in source_files():
        original = path.read_text()
        if args.fill:
            updated = fill(original, catalog[domain])
            if without_comments(original) != without_comments(updated):
                raise ValueError("DDL结构发生变化: " + str(path))
            path.write_text(updated)
    actual = inventory()
    missing = [(domain, table, column) for domain, tables in actual.items() for table, item in tables.items()
               for column, value in [(None, item["comment"]), *item["columns"].items()] if not value.strip()]
    print(json.dumps({"tables": sum(map(len, actual.values())), "columns": sum(len(t["columns"]) for v in actual.values() for t in v.values()),
                      "missing": missing}, ensure_ascii=False, indent=2))
    if missing:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
