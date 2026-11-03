"""规范检查器的正反例，防止扫描漏掉真实边界或误伤兼容包名。"""
from pathlib import Path
import tempfile
import unittest

from check_architecture import scan


class ArchitectureTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.root = Path(self.directory.name)

    def java(self, module, name, text):
        path = self.root / f'lemnt-server/lemnt-demo/lemnt-demo-{module}/src/main/java/{name}.java'
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text)

    def rules(self):
        return [finding['rule'] for finding in scan(self.root)['violations']]

    def test_application_cannot_import_infrastructure_even_with_wildcard(self):
        self.java('infrastructure', 'Row', 'package sample.infrastructure; public class Row {}')
        self.java('application', 'Use', 'package sample.application;\nimport sample.infrastructure.*;\nclass Use {}')
        self.assertIn('layer-import', self.rules())

    def test_api_domain_package_is_client_contract_and_comments_are_not_imports(self):
        self.java('client', 'UserBo', 'package sample.api.domain; public record UserBo(String name) {}')
        self.java('client', 'Client', 'package sample.api;\nimport sample.api.domain.UserBo;\n// import org.springframework.Bean;\ninterface Client {}')
        self.assertEqual([], self.rules())

    def test_domain_cannot_keep_json_framework_annotations(self):
        self.java('domain', 'Value', 'package sample.domain;\nimport com.fasterxml.jackson.annotation.JsonInclude;\nclass Value {}')
        self.assertIn('domain-purity', self.rules())

    def test_mapper_rejects_sql_annotations_and_default_methods(self):
        self.java('infrastructure', 'ValueMapper', 'package sample; interface ValueMapper { @org.apache.ibatis.annotations.Select("SELECT 1") int find(); default int count() { return 0; } }')
        self.assertIn('sql-annotation', self.rules())
        self.assertIn('mapper-default', self.rules())

    def test_record_components_with_nested_validation_and_generics_require_schema(self):
        self.java('adapter', 'PageReq', '''package sample.adapter.req;
@io.swagger.v3.oas.annotations.media.Schema(description="分页")
public record PageReq(@Size(max=9) java.util.List<@Size(max=20) String> ids) {}''')
        self.assertEqual(['http-field-schema'], self.rules())

    def test_for_update_requires_both_cache_controls(self):
        path = self.root / 'lemnt-server/lemnt-demo/src/main/resources/mapper/ValueMapper.xml'
        path.parent.mkdir(parents=True)
        path.write_text('<mapper><select id="lock" useCache="false">select * from value for update</select></mapper>')
        self.assertIn('lock-cache', self.rules())
        path.write_text('<mapper><select id="lock" useCache="false" flushCache="true">select * from value for update</select></mapper>')
        self.assertNotIn('lock-cache', self.rules())

    def test_pom_production_boundary_is_checked_and_test_dependency_is_allowed(self):
        path = self.root / 'lemnt-server/lemnt-demo/lemnt-demo-application/pom.xml'
        path.parent.mkdir(parents=True)
        pom = '''<project xmlns="http://maven.apache.org/POM/4.0.0"><artifactId>lemnt-demo-application</artifactId>
<dependencies><dependency><groupId>com.lemnt</groupId><artifactId>lemnt-demo-infrastructure</artifactId>%s</dependency></dependencies></project>'''
        path.write_text(pom % '')
        self.assertIn('pom-layer', self.rules())
        path.write_text(pom % '<scope>test</scope>')
        self.assertNotIn('pom-layer', self.rules())


if __name__ == '__main__':
    unittest.main()
