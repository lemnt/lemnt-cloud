#!/usr/bin/env python3
"""按固定AOSP格式统一Java源码；工具JAR独立于业务依赖，默认只检查。"""
import argparse
import hashlib
import os
from pathlib import Path
import re
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
SHA256 = "25157797a0a972c2290b5bc71530c4f7ad646458025e3484412a6e5a9b8c9aa6"
IMPORT = re.compile(r"^import (static )?([\w.*]+);[ \t]*$", re.M)
ANNOTATION = re.compile(r"@((?:[a-z]\w*\.)+[A-Z]\w*(?:\.[A-Z]\w*)*)")


def normalize_annotations(text):
    """将全限定注解改为显式导入；同名冲突必须人工解决，不能换错注解。"""
    from check_architecture import mask_java
    imports = {match.group(2).split('.')[-1]: match.group(2)
               for match in IMPORT.finditer(text) if not match.group(1)}
    declarations = set(re.findall(r'\b(?:class|record|interface|enum)\s+(\w+)', mask_java(text)))
    additions = set()
    edits = []
    for match in ANNOTATION.finditer(mask_java(text)):
        parts = match.group(1).split('.')
        index = next(index for index, part in enumerate(parts) if part[0].isupper())
        simple = parts[index]
        qualified = '.'.join(parts[:index + 1])
        if simple in declarations or (simple in imports and imports[simple] != qualified):
            raise ValueError(f'注解同名冲突：{qualified}')
        imports[simple] = qualified
        line = 'import ' + qualified + ';'
        if line not in text:
            additions.add(line)
        edits.append((match.start(), match.end(), '@' + '.'.join(parts[index:])))
    for start, end, value in reversed(edits):
        text = text[:start] + value + text[end:]
    if additions:
        package = re.search(r'\bpackage\s+[\w.]+\s*;', mask_java(text))
        if package is None:
            raise ValueError('缺少package声明')
        text = text[:package.end()] + '\n\n' + '\n'.join(sorted(additions)) + text[package.end():]
    return text


def source_files():
    """只处理维护中的源码，不包含target、IDE、日志与生成目录。"""
    files = []
    for directory in ("lemnt-common", "lemnt-server"):
        files.extend(p for p in (ROOT / directory).rglob("*.java")
                     if "/src/" in p.as_posix() and "target" not in p.parts)
    return sorted(files)


def import_key(match):
    """沿用项目内部、第三方、工具、标准库、静态导入的分组。"""
    name = match.group(2)
    if match.group(1):
        return 4, name
    if name.startswith("com.lemnt."):
        return 0, name
    if name.startswith(("lombok.", "cn.hutool.")):
        return 2, name
    if name.startswith(("java.", "javax.", "jakarta.")):
        return 3, name
    return 1, name


def sort_imports(text):
    matches = list(IMPORT.finditer(text))
    if not matches:
        return text
    # 不跨越带注释的导入块，防止移动其解释；单独报告供人工处理。
    between = IMPORT.sub("", text[matches[0].start():matches[-1].end()])
    if between.strip():
        raise ValueError("导入块含额外内容，需人工整理")
    groups = []
    previous = None
    for match in sorted(matches, key=import_key):
        group = import_key(match)[0]
        if previous is not None and previous != group:
            groups.append("")
        groups.append(match.group())
        previous = group
    return text[:matches[0].start()] + "\n".join(groups) + text[matches[-1].end():]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--jar", required=True, type=Path, help="google-java-format 1.25.2 all-deps JAR")
    parser.add_argument("--write", action="store_true", help="写回格式；默认只检查")
    args = parser.parse_args()
    if hashlib.sha256(args.jar.read_bytes()).hexdigest() != SHA256:
        parser.error("格式化工具版本/校验和不匹配")
    files = source_files()
    imports_changed = []
    for path in files:
        text = path.read_text()
        ordered = sort_imports(normalize_annotations(text))
        if text != ordered:
            imports_changed.append(path)
            if args.write:
                path.write_text(ordered)
    java_home = os.environ.get("JAVA_HOME")
    java = str(Path(java_home) / "bin/java") if java_home else "java"
    options = ["--aosp", "--skip-sorting-imports", "--skip-removing-unused-imports",
               "--skip-reflowing-long-strings", "--skip-javadoc-formatting"]
    options += ["--replace"] if args.write else ["--dry-run", "--set-exit-if-changed"]
    with tempfile.NamedTemporaryFile(mode="w", suffix=".args") as arguments:
        arguments.write("\n".join(str(path) for path in files))
        arguments.flush()
        result = subprocess.run([java, "-jar", str(args.jar.resolve()), *options, "@" + arguments.name])
    print(f"Java源码 {len(files)} 个，导入/注解规范差异 {len(imports_changed)} 个")
    return result.returncode or (1 if imports_changed and not args.write else 0)


if __name__ == "__main__":
    raise SystemExit(main())
