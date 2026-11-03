"""注释工具回归：覆盖SQL词法边界及已有库字段定义保真。"""
import unittest

import schema_comments as schema


class SchemaCommentsTest(unittest.TestCase):
    def setUp(self):
        self.catalog = {"sample": {"comment": "示例业务表", "columns": {
            "id": "主键编号", "amount": "金额，单位元", "state": "状态", "stamp": "更新时间",
            "derived": "计算值", "label": "标签", "kept": "不能替换已有注释"}}}

    def test_single_line_nested_commas_and_quoted_literals(self):
        sql = "CREATE TABLE sample(id BIGINT PRIMARY KEY,amount DECIMAL(19,2) DEFAULT 0,state ENUM('a,b','c') DEFAULT 'a,b',CHECK(amount>=0));"
        result = schema.fill(sql, self.catalog)
        self.assertEqual(schema.without_comments(sql), schema.without_comments(result))
        self.assertEqual(4, result.count("COMMENT"))
        self.assertEqual(result, schema.fill(result, self.catalog))

    def test_preserves_nonempty_and_replaces_empty_comment(self):
        sql = "CREATE TABLE sample(id INT COMMENT '原说明',label VARCHAR(10) COMMENT '') COMMENT='原表说明';"
        result = schema.fill(sql, self.catalog)
        self.assertIn("COMMENT '原说明'", result)
        self.assertIn("COMMENT='原表说明'", result)
        self.assertIn("COMMENT '标签'", result)
        self.assertEqual(schema.without_comments(sql), schema.without_comments(result))

    def test_add_column_does_not_treat_indexes_as_columns(self):
        sql = "ALTER TABLE sample ADD COLUMN amount DECIMAL(19,2) DEFAULT 0, ADD CONSTRAINT ck CHECK(amount>=0);"
        result = schema.fill(sql, self.catalog)
        self.assertEqual(1, result.count("COMMENT"))
        self.assertEqual(schema.without_comments(sql), schema.without_comments(result))

    def test_show_create_plan_preserves_real_column_attributes(self):
        ddl = """CREATE TABLE `sample` (
          `id` bigint unsigned NOT NULL AUTO_INCREMENT,
          `amount` decimal(23,6) NOT NULL DEFAULT '0.000000',
          `label` varchar(99) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT 'a,b',
          `stamp` timestamp(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
          `derived` bigint GENERATED ALWAYS AS ((id + 1)) STORED,
          `kept` int DEFAULT 1 COMMENT '已有说明',
          PRIMARY KEY (`id`)
        ) ENGINE=InnoDB"""
        plan = schema.upgrade_plan(ddl, self.catalog["sample"])
        result = "\n".join(plan)
        self.assertEqual(6, len(plan))
        self.assertNotIn("MODIFY COLUMN `kept`", result)
        for part in ["unsigned NOT NULL AUTO_INCREMENT", "decimal(23,6)", "COLLATE utf8mb4_bin",
                     "DEFAULT 'a,b'", "ON UPDATE CURRENT_TIMESTAMP(6)", "GENERATED ALWAYS AS ((id + 1)) STORED"]:
            self.assertIn(part, result)
        self.assertNotIn("PRIMARY KEY", result)

    def test_upgrade_skips_unknown_live_columns_and_existing_comments(self):
        ddl = "CREATE TABLE sample(id INT COMMENT '现有',extra_column VARCHAR(50)) COMMENT='现有表说明';"
        self.assertEqual([], schema.upgrade_plan(ddl, self.catalog["sample"]))

    def test_sql_comments_and_escaped_quotes_are_not_definitions(self):
        sql = "-- CREATE TABLE fake(x INT);\nCREATE TABLE sample(label VARCHAR(20) DEFAULT 'x;it''s,y',id INT); /* COMMENT 'fake' */"
        result = schema.fill(sql, self.catalog)
        self.assertEqual(schema.without_comments(sql), schema.without_comments(result))
        self.assertEqual(["sample", "sample", "sample"], [t for t, _, _, _ in schema.definitions(result)])

    def test_current_schema_catalog_is_complete(self):
        import json
        catalog = json.loads(schema.CATALOG.read_text())
        for domain, tables in schema.inventory().items():
            for table, item in tables.items():
                self.assertTrue(item["comment"], table)
                self.assertEqual(catalog[domain][table]["comment"], item["comment"])
                self.assertEqual(catalog[domain][table]["columns"], item["columns"])
                self.assertTrue(all(item["columns"].values()), table)

    def business_statements(self, domain):
        path = schema.ROOT / f"lemnt-{domain}/sql/lemnt_{domain}.sql"
        values = schema.tokens(path.read_text())
        start = 0
        for index, token in enumerate(values):
            if token.value == ";":
                if values[start:index]:
                    yield values[start:index]
                start = index + 1
        self.assertFalse(values[start:], "初始化语句必须以分号结束")

    def test_business_services_have_one_initialization_file_with_only_create_and_insert(self):
        for domain in ("member", "mall", "trade"):
            directory = schema.ROOT / f"lemnt-{domain}/sql"
            self.assertEqual([directory / f"lemnt_{domain}.sql"], sorted(directory.rglob("*.sql")))
            for statement in self.business_statements(domain):
                self.assertIn([part.value.upper() for part in statement[:2]],
                              [["CREATE", "TABLE"], ["INSERT", "INTO"]])

    def test_business_foreign_keys_reference_already_created_tables(self):
        for domain in ("member", "mall", "trade"):
            created = set()
            for statement in self.business_statements(domain):
                if statement[0].value.upper() != "CREATE":
                    continue
                table_index = 5 if statement[2].value.upper() == "IF" else 2
                table = schema.name(statement[table_index])
                self.assertNotIn(table, created)
                references = {schema.name(statement[index + 1])
                              for index, token in enumerate(statement[:-1])
                              if token.value.upper() == "REFERENCES"} - {table}
                self.assertTrue(references <= created, f"{table}: {references - created}")
                created.add(table)
            self.assertTrue(created, domain)


if __name__ == "__main__":
    unittest.main()
