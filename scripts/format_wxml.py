#!/usr/bin/env python3
"""统一WXML标签行的两空格缩进，不重写标签、属性、表达式或正文。"""
import argparse
from html.parser import HTMLParser
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]


class TagIndentation(HTMLParser):
    def __init__(self):
        super().__init__(convert_charrefs=False)
        self.depth = 0
        self.lines = {}

    def mark(self):
        line, _ = self.getpos()
        self.lines.setdefault(line - 1, self.depth)

    def handle_starttag(self, tag, attrs):
        self.mark()
        self.depth += 1

    def handle_startendtag(self, tag, attrs):
        self.mark()

    def handle_endtag(self, tag):
        self.depth = max(0, self.depth - 1)
        self.mark()


def format_text(text):
    parser = TagIndentation()
    parser.feed(text)
    lines = text.splitlines(keepends=True)
    for index, depth in parser.lines.items():
        # 只改标签前的缩进，纯正文和多行属性的内容保持原样。
        if re.match(r'^[ \t]*<', lines[index]):
            lines[index] = '  ' * depth + lines[index].lstrip(' \t')
    return ''.join(lines)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--write', action='store_true')
    args = parser.parse_args()
    files = sorted((ROOT / 'lemnt-miniapp').rglob('*.wxml'))
    changed = []
    for path in files:
        text = path.read_text()
        formatted = format_text(text)
        if text != formatted:
            changed.append(path)
            if args.write:
                path.write_text(formatted)
    print(f'WXML文件 {len(files)} 个，缩进差异 {len(changed)} 个')
    return bool(changed) and not args.write


if __name__ == '__main__':
    raise SystemExit(main())
