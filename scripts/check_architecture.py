#!/usr/bin/env python3
"""检查源码与POM分层、HTTP模型、Mapper职责和行锁缓存；默认只读。"""
import argparse
from collections import Counter
import json
from pathlib import Path
import re
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
JAVA_LITERAL = re.compile(r'/\*.*?\*/|//[^\n]*|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'', re.S)
SCHEMA = re.compile(r'@(?:io\.swagger\.v3\.oas\.annotations\.media\.)?Schema\b')
IMPORT = re.compile(r'^import\s+(?:static\s+)?([\w.*]+)\s*;', re.M)
NS = {'m': 'http://maven.apache.org/POM/4.0.0'}
LAYERS = {'adapter', 'application', 'domain', 'infrastructure', 'client', 'start'}
FORBIDDEN = {
    'domain': {'application', 'infrastructure', 'adapter', 'start'},
    'application': {'infrastructure', 'adapter', 'start'},
    'infrastructure': {'application', 'adapter', 'start'},
    'adapter': {'infrastructure', 'start'},
    'client': {'domain', 'application', 'infrastructure', 'adapter', 'start'},
}


def mask_java(text):
    """忽略注释与字面量，保留位置，避免把示例代码误判为真实依赖。"""
    return JAVA_LITERAL.sub(lambda match: ' ' * len(match.group()), text)


def layer_of(path):
    before = path.as_posix().split('/src/main/')[0]
    return before.split('/')[-1].split('-')[-1]


def record_components(text, masked):
    """按括号/泛型层级拆record组件，支持嵌套注解与泛型集合。"""
    for match in re.finditer(r'\brecord\s+(\w+)\s*\(', masked):
        start = position = match.end()
        depth = 1
        while depth:
            depth += (masked[position] == '(') - (masked[position] == ')')
            position += 1
        last = start
        parentheses = generics = 0
        for index in range(start, position):
            char = masked[index] if index < position - 1 else ','
            parentheses += (char == '(') - (char == ')')
            generics += (char == '<') - (char == '>')
            if char == ',' and parentheses == 0 and generics == 0:
                if masked[last:index].strip():
                    name = re.findall(r'\w+', masked[last:index])[-1]
                    yield match.group(1), name, text[last:index], last
                last = index + 1


def scan(root):
    findings = []
    files = []
    for directory in ('lemnt-common', 'lemnt-server'):
        files.extend(path for path in (root / directory).rglob('*.java')
                     if '/src/main/java/' in path.as_posix() and 'target' not in path.parts)
    contents = {path: path.read_text() for path in sorted(files)}
    classes = {}
    for path, text in contents.items():
        package = re.search(r'\bpackage\s+([\w.]+)\s*;', mask_java(text))
        if package:
            classes[package.group(1) + '.' + path.stem] = layer_of(path)

    def issue(rule, path, offset, detail):
        text = contents.get(path, '')
        findings.append({'rule': rule, 'path': path.relative_to(root).as_posix(),
                         'line': text[:offset].count('\n') + 1, 'detail': detail})

    for path, text in contents.items():
        layer = layer_of(path)
        masked = mask_java(text)
        for match in IMPORT.finditer(masked):
            name = match.group(1)
            targets = []
            if name.endswith('.*'):
                targets = [target for cls, target in classes.items() if cls.startswith(name[:-1])]
            else:
                cls = name
                while cls and cls not in classes:
                    cls = cls.rpartition('.')[0]
                if cls:
                    targets = [classes[cls]]
            if any(target in FORBIDDEN.get(layer, set()) for target in targets):
                issue('layer-import', path, match.start(), name)
            if layer == 'domain' and not name.startswith(('java.', 'com.lemnt.', 'lombok.')):
                issue('domain-purity', path, match.start(), name)
            if layer == 'application' and name.startswith((
                    'com.baomidou.', 'org.apache.ibatis.', 'org.mybatis.',
                    'org.springframework.data.redis.', 'org.apache.dubbo.config.annotation.DubboReference',
                    'io.swagger.')):
                issue('application-infrastructure', path, match.start(), name)
            if layer == 'adapter' and ('/req/' in path.as_posix() or '/res/' in path.as_posix()):
                if any(target in {'domain', 'application', 'infrastructure'} for target in targets):
                    issue('http-model-boundary', path, match.start(), name)
        if layer == 'infrastructure':
            for match in re.finditer(r'@(?:org\.apache\.ibatis\.annotations\.)?(?:Select|Insert|Update|Delete)(?:Provider)?\s*\(', masked):
                issue('sql-annotation', path, match.start(), 'SQL须放在Mapper XML')
            for match in re.finditer(r'\.setSql\s*\(', masked):
                issue('sql-fragment', path, match.start(), '使用类型安全更新或Mapper XML')
            if path.name.endswith('Mapper.java'):
                for match in re.finditer(r'\bdefault\s+', masked):
                    issue('mapper-default', path, match.start(), '条件构造应放在GatewayImpl')
        if layer == 'adapter' and path.stem.endswith(('Req', 'Res')):
            for match in re.finditer(r'\b(?:record|class)\s+(\w+)\b', masked):
                start = max(masked.rfind('}', 0, match.start()), masked.rfind('{', 0, match.start()),
                            masked.rfind(';', 0, match.start())) + 1
                if not SCHEMA.search(text[start:match.start()]):
                    issue('http-type-schema', path, match.start(), match.group(1))
            for record, name, component, offset in record_components(text, masked):
                if not SCHEMA.search(component):
                    issue('http-field-schema', path, offset, record + '.' + name)
        if path.name.endswith('Controller.java'):
            for match in re.finditer(r'public\s+[^;{]+?\([^;{]*?\b(\w+(?:Cmd|Qry|DTO|Bo))\s+\w+', masked):
                issue('http-input', path, match.start(), match.group(1) + '须经Req转换')

    xml_count = 0
    for path in sorted((root / 'lemnt-server').rglob('*.xml')):
        if '/src/main/resources/mapper/' not in path.as_posix() or 'target' in path.parts:
            continue
        xml_count += 1
        tree = ET.parse(path).getroot()
        for select in tree.findall('select'):
            sql = ' '.join(select.itertext())
            if re.search(r'\bFOR\s+UPDATE\b', sql, re.I):
                if select.get('useCache') != 'false' or select.get('flushCache') != 'true':
                    issue('lock-cache', path, 0, select.get('id'))
        if '${' in path.read_text():
            issue('raw-sql-substitution', path, 0, '禁止原样拼接HTTP输入')

    poms = []
    for directory in ('lemnt-common', 'lemnt-server'):
        poms.extend(path for path in (root / directory).rglob('pom.xml') if 'target' not in path.parts)
    graph = {}
    for path in sorted(poms):
        tree = ET.parse(path).getroot()
        artifact = tree.findtext('m:artifactId', namespaces=NS)
        layer = artifact.split('-')[-1]
        dependencies = []
        for dependency in tree.findall('m:dependencies/m:dependency', NS):
            if dependency.findtext('m:scope', namespaces=NS) == 'test':
                continue
            name = dependency.findtext('m:artifactId', namespaces=NS) or ''
            if dependency.findtext('m:groupId', namespaces=NS) != 'com.lemnt':
                continue
            dependencies.append(name)
            if name.split('-')[-1] in FORBIDDEN.get(layer, set()):
                issue('pom-layer', path, 0, artifact + ' -> ' + name)
        graph[artifact] = dependencies
    visited = set()

    def visit(node, chain):
        if node in chain:
            findings.append({'rule': 'pom-cycle', 'path': 'lemnt-server/pom.xml', 'line': 1,
                             'detail': ' -> '.join(chain + [node])})
            return
        if node in visited:
            return
        visited.add(node)
        for dependency in graph.get(node, []):
            visit(dependency, chain + [node])

    for artifact in graph:
        visit(artifact, [])
    return {'javaMainFiles': len(files), 'mapperXmlFiles': xml_count, 'pomFiles': len(poms),
            'byLayer': dict(sorted(Counter(layer_of(path) for path in files).items())),
            'violations': findings}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--json', type=Path, help='可选写入机器报告')
    args = parser.parse_args()
    report = scan(ROOT)
    if args.json:
        args.json.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n')
    print(json.dumps({key: value for key, value in report.items() if key != 'violations'}, ensure_ascii=False))
    for finding in report['violations']:
        print(f"{finding['path']}:{finding['line']} [{finding['rule']}] {finding['detail']}")
    print(f"规范违规：{len(report['violations'])}")
    return bool(report['violations'])


if __name__ == '__main__':
    raise SystemExit(main())
