"""注解短名化只改变类型引用，不处理文本或错误绑定同名注解。"""
import unittest

from format_java import normalize_annotations


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


if __name__ == '__main__':
    unittest.main()
