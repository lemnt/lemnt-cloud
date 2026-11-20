"""模块拆分检查器回归测试。"""
import unittest

import check_module_split as checker


class ModuleSplitTest(unittest.TestCase):
    """测试 SQL 拆分规则检查逻辑。"""

    def test_extract_tables_from_create_statements(self):
        """从 CREATE TABLE 语句中正确提取表名。"""
        sql = """
        CREATE TABLE IF NOT EXISTS product_category (
            id BIGINT PRIMARY KEY
        );
        CREATE TABLE product_brand (
            id BIGINT PRIMARY KEY
        );
        """
        tables = checker.extract_tables(sql)
        self.assertEqual(["product_category", "product_brand"], tables)

    def test_extract_tables_ignores_comments_and_strings(self):
        """提取表名时忽略注释和字符串中的伪 CREATE TABLE。"""
        sql = """
        -- CREATE TABLE fake_table (id INT);
        /* CREATE TABLE another_fake (id INT); */
        CREATE TABLE product_real (id INT);
        INSERT INTO log VALUES('CREATE TABLE not_a_table (id INT)');
        """
        tables = checker.extract_tables(sql)
        self.assertEqual(["product_real"], tables)

    def test_legacy_table_detection(self):
        """检测旧表名。"""
        findings = checker.check_no_legacy_tables("product", ["mall_product_category", "product_brand"])
        self.assertEqual(1, len(findings))
        self.assertEqual("legacy-table-found", findings[0]["rule"])
        self.assertEqual("mall_product_category", findings[0]["table"])

    def test_no_legacy_tables_passes(self):
        """新表名通过旧表名检查。"""
        findings = checker.check_no_legacy_tables("product", ["product_category", "product_brand"])
        self.assertEqual([], findings)

    def test_table_prefix_validation(self):
        """表名必须使用所属上下文前缀。"""
        findings = checker.check_table_prefix("product", ["product_category", "order_info"])
        self.assertEqual(1, len(findings))
        self.assertEqual("table-prefix-mismatch", findings[0]["rule"])
        self.assertEqual("order_info", findings[0]["table"])

    def test_message_table_prefix(self):
        """消息表也必须使用正确前缀。"""
        findings = checker.check_table_prefix("product", ["product_message_outbox", "wrong_message_inbox"])
        self.assertEqual(1, len(findings))
        self.assertEqual("message-table-prefix", findings[0]["rule"])

    def test_message_table_with_correct_prefix_passes(self):
        """正确前缀的消息表通过检查。"""
        findings = checker.check_table_prefix("order", ["order_info", "order_message_outbox", "order_message_inbox"])
        self.assertEqual([], findings)

    def test_cross_context_table_detection(self):
        """检测跨上下文表。"""
        findings = checker.check_no_cross_context_tables("order", ["order_info", "pay_payment_record"])
        self.assertEqual(1, len(findings))
        self.assertEqual("cross-context-table", findings[0]["rule"])
        self.assertIn("pay", findings[0]["detail"])

    def test_cross_context_ignores_message_tables(self):
        """跨上下文检查不检查消息表（消息表由前缀检查覆盖）。"""
        findings = checker.check_no_cross_context_tables("order", ["order_info", "order_message_outbox"])
        self.assertEqual([], findings)

    def test_all_contexts_have_correct_prefixes(self):
        """各上下文的允许前缀配置正确。"""
        self.assertEqual("product_", checker.CONTEXT_PREFIXES["product"])
        self.assertEqual("promotion_", checker.CONTEXT_PREFIXES["promotion"])
        self.assertEqual("order_", checker.CONTEXT_PREFIXES["order"])
        self.assertEqual("pay_", checker.CONTEXT_PREFIXES["pay"])

    def test_legacy_prefixes_cover_old_modules(self):
        """旧前缀覆盖 mall 和 trade。"""
        self.assertIn("mall_", checker.LEGACY_PREFIXES)
        self.assertIn("trade_", checker.LEGACY_PREFIXES)


if __name__ == "__main__":
    unittest.main()
