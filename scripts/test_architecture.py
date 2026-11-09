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
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.util.List;
@Schema(description="分页")
public record PageReq(@Size(max=9) List<@Size(max=20) String> ids) {}''')
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
    def external_client(self):
        path = self.root / 'lemnt-server/lemnt-remote/lemnt-remote-client/src/main/java/Event.java'
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text('package remote.api; public record Event(String orderNo) {}')

    def test_external_client_import_is_only_allowed_in_infrastructure(self):
        self.external_client()
        for layer in ('application', 'domain', 'adapter', 'client', 'start'):
            with self.subTest(layer=layer):
                self.java(layer, 'Use', 'package sample;\nimport remote.api.Event;\nclass Use {}')
                findings = scan(self.root)['violations']
                self.assertTrue(any(f['rule'] == 'external-client-import'
                                    and f'lemnt-demo-{layer}/' in f['path'] for f in findings))
        self.java('infrastructure', 'Use', 'package sample;\nimport remote.api.Event;\nclass Use {}')
        self.assertFalse(any(f['rule'] == 'external-client-import'
                             and 'lemnt-demo-infrastructure/' in f['path']
                             for f in scan(self.root)['violations']))

    def test_external_client_wildcard_static_and_nested_imports_are_checked(self):
        self.external_client()
        for statement in ('remote.api.*', 'static remote.api.Event.*',
                          'static remote.api.Event.VALUE', 'remote.api.Event.Nested'):
            with self.subTest(statement=statement):
                self.java('application', 'Use', f'package sample;\nimport {statement};\nclass Use {{}}')
                self.assertIn('external-client-import', self.rules())

    def test_own_and_common_client_and_test_source_remain_allowed(self):
        self.java('client', 'OwnClient', 'package sample.api; public interface OwnClient {}')
        common = self.root / 'lemnt-common/lemnt-common-client/src/main/java/Envelope.java'
        common.parent.mkdir(parents=True)
        common.write_text('package common.api; public record Envelope(String payload) {}')
        self.java('application', 'Use', 'package sample;\nimport sample.api.OwnClient;\nimport common.api.Envelope;\nclass Use {}')
        self.external_client()
        test = self.root / 'lemnt-server/lemnt-demo/lemnt-demo-application/src/test/java/UseTest.java'
        test.parent.mkdir(parents=True)
        test.write_text('package sample;\nimport remote.api.Event;\nclass UseTest {}')
        self.assertEqual([], self.rules())

    def test_fully_qualified_external_client_reference_is_also_checked(self):
        self.external_client()
        self.java('application', 'Use', 'package sample; class Use { remote.api.Event event; }')
        self.assertIn('external-client-import', self.rules())

    def test_external_client_production_pom_scopes_are_rejected(self):
        path = self.root / 'lemnt-server/lemnt-demo/lemnt-demo-application/pom.xml'
        path.parent.mkdir(parents=True)
        pom = '''<project xmlns="http://maven.apache.org/POM/4.0.0"><artifactId>lemnt-demo-application</artifactId>
<dependencies><dependency><groupId>com.lemnt</groupId><artifactId>lemnt-remote-client</artifactId>%s</dependency></dependencies></project>'''
        for scope in ('', '<scope>compile</scope>', '<scope>provided</scope>',
                      '<scope>runtime</scope>', '<optional>true</optional>'):
            with self.subTest(scope=scope):
                path.write_text(pom % scope)
                self.assertIn('pom-external-client', self.rules())
        path.write_text(pom % '<scope>test</scope>')
        self.assertEqual([], self.rules())

    def test_own_common_and_infrastructure_client_pom_dependencies_remain_allowed(self):
        pom = '''<project xmlns="http://maven.apache.org/POM/4.0.0"><artifactId>lemnt-demo-%s</artifactId>
<dependencies><dependency><groupId>com.lemnt</groupId><artifactId>%s</artifactId></dependency></dependencies></project>'''
        for layer, dependency in (('application', 'lemnt-demo-client'),
                                  ('domain', 'lemnt-demo-client'),
                                  ('application', 'lemnt-common-client'),
                                  ('infrastructure', 'lemnt-remote-client')):
            path = self.root / f'lemnt-server/lemnt-demo/lemnt-demo-{layer}/pom.xml'
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(pom % (layer, dependency))
        self.assertEqual([], self.rules())


if __name__ == '__main__':
    unittest.main()
