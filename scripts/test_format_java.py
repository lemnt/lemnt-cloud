"""注解短名化只改变类型引用，不处理文本或错误绑定同名注解。"""
import unittest

from format_java import normalize_annotations, normalize_types


class AnnotationFormatTest(unittest.TestCase):
    def test_qualified_annotation_uses_explicit_import(self):
        text = 'package example;\n@io.swagger.v3.oas.annotations.media.Schema(description="HTTP响应审计字段基类")\nclass Response {}'
        actual = normalize_annotations(text)
        self.assertIn('import io.swagger.v3.oas.annotations.media.Schema;', actual)
        self.assertIn('@Schema(description="HTTP响应审计字段基类")', actual)
        self.assertEqual(actual, normalize_annotations(actual))

    def test_documentation_and_strings_stay_unchanged(self):
        text = 'package example;\n// @org.junit.jupiter.api.Test\nclass Example { String value = "@org.junit.jupiter.api.Test"; }'
        self.assertEqual(text, normalize_annotations(text))

    def test_same_named_annotation_is_not_silently_replaced(self):
        text = 'package example;\nimport other.Schema;\n@io.swagger.v3.oas.annotations.media.Schema class Response {}'
        with self.assertRaisesRegex(ValueError, '同名冲突'):
            normalize_annotations(text)


class TypeImportFormatTest(unittest.TestCase):
    def test_wrapped_package_paths_and_serializable_are_shortened(self):
        source = ('package example;\nrecord Quote(java.util.\n Map<Long, Integer> gifts) '
                  'implements java.io.\n Serializable {}')
        result = normalize_types(source)
        self.assertIn('import java.util.Map;', result)
        self.assertIn('import java.io.Serializable;', result)
        self.assertIn('record Quote(Map<Long, Integer> gifts) implements Serializable', result)

    def test_generic_and_static_access_use_explicit_imports(self):
        source = ('package example;\nclass Quote { java.util.Map<Long, Integer> gifts; '
                  'Object values = java.util.List.of(); }')
        result = normalize_types(source)
        self.assertIn('import java.util.Map;', result)
        self.assertIn('import java.util.List;', result)
        self.assertIn('Map<Long, Integer> gifts;', result)
        self.assertIn('List.of()', result)
        self.assertEqual(result, normalize_types(result))

    def test_strings_comments_and_imports_are_preserved(self):
        source = ('package example;\nimport java.util.List;\n'
                  '// java.util.Map\nclass Quote { '
                  'String type = "com.example.OrderDTO"; List<String> values; }')
        self.assertEqual(source, normalize_types(source))

    def test_nested_types_keep_outer_qualification(self):
        result = normalize_types('package example; class Quote { java.util.Map.Entry<Long, Integer> value; }')
        self.assertIn('import java.util.Map;', result)
        self.assertIn('Map.Entry<Long, Integer>', result)
        self.assertNotIn('import java.util.Map.Entry;', result)

    def test_colliding_import_or_declared_type_requires_explicit_resolution(self):
        for source in (
                'package example; import other.Map; class Quote { java.util.Map<?, ?> values; }',
                'package example; class StringUtils extends org.apache.commons.lang3.StringUtils {}'):
            with self.assertRaisesRegex(ValueError, '类型同名冲突'):
                normalize_types(source)


if __name__ == '__main__':
    unittest.main()
