# SKU规格JSON存储合并交付

2026-10-28，承接41设计；code-software-engineer。依据本地芋道ProductSkuDO及字典/SKU名称同步逻辑，完成本批存储与行为收敛，不代表完整商城迁移完成。

## 实现

- SKU主体增加properties字段，四字段Property列表直接随SKU读写，DO为String、Converter负责JSON编码/解析，初始化物理列为LONGTEXT。ProductSkuMapper XML resultMap包含该列，原锁查询继续禁用缓存；简单读写使用BaseMapper/Lambda。
- 商品创建、更新、详情、批量详情、App商品及结算目录不再读写单独关系表；价格/库存/配送/规格在SKU事务内保存。移除mall_product_sku_spec的DO、Mapper、Gateway与XML，保留ProductSkuSpec作为规格值对象。
- 按芋道行为允许字典改名：更新当前SKU对应ID的规格名称/值名称，原订单/售后/提交回执不变。筛出实际引用行后按SKU编号锁定并重读，避免旧扫描结果覆盖并发写入；只更新properties及派生变体键，不覆盖价格库存。冲突或JSON写入失败时字典与SKU事务整体回滚。
- 删除字典值保留SKU已有快照；规格项仍有子值时不能删除。旧未关联编号不按同名猜测同步。初始化移除从旧规格自动创建字典的额外逻辑，避免为历史资料推测字典身份。
- 后台已有规格名称作为编辑快照标签保留，字典值被删除后仍可展示并选择替代值；查询失败仍报错，实际保存必须通过服务端存在性/归属校验，不将历史标签冒充有效字典。
- API列表形状不变，更新名称和删除字典的行为文档已同步。未新增表，初始化从46张降至45张；36张业务缺表数量不变。

## 存量迁移与限制

`lemnt-server/lemnt-mall/sql/upgrade/sku_properties_json.sql`在停旧写者、备份并核对版本后复制关系行到SKU JSON；前置是旧表已有property_id/value_id列，未知ID保持NULL。脚本给出编号成对、JSON合法、总数量及每条旧规格在对应SKU内完整存在的核对查询，不会删除旧表。确认核对与备份后单独审核`retire_sku_spec_table.sql`。不要将两份文件不经核对串行执行。

本代理未执行真实DDL、提交、推送或部署。迁移SQL使用MySQL 8 JSON函数，未在真实MySQL验证，不能用H2业务回归替代该项。源参考SQL未包含product_sku建表，当前只对照源DO/业务行为，没有声称源列长度与本地LONGTEXT完全一致。

## 验证

- member/mall/trade及依赖276项测试通过，失败/错误/跳过均0。clean verify：`/tmp/sku-json-clean-verify.log`；最终verify：`/tmp/sku-json-final-verify.log`；接口描述修订后打包检查：`/tmp/sku-json-package-final.log`。
- 生产初始化DDL、MyBatis-Plus、H2 MySQL模式和真实事务：SKU JSON保存/读取及批量详情；字典改名同步当前SKU，价格库存不变；删除字典保留SKU快照；未知旧ID不按同名改写；SKU JSON更新失败时字典和SKU均回滚。
- 现有Controller到数据库合同、商品到结算/订单/售后链路、规格编号变化拒绝报价、旧JSON/提交回执恢复、优惠/积分/运费、库存/支付/售后全部回归。原订单及售后在真实字典改名后仍保留旧规格。
- 生产及测试运行时代码没有mall_product_sku_spec或旧DO/Mapper/Gateway引用；旧表名只保留在历史/存量升级脚本与记录中。初始化45张表已核对。
- 后台171项测试、类型检查及构建、ESLint、Prettier通过。日志`/tmp/sku-json-ui-tests.log`、`/tmp/sku-json-ui-build.log`、`/tmp/sku-json-ui-lint.log`、`/tmp/sku-json-ui-format.log`。
- 小程序82项测试通过，日志`/tmp/sku-json-mini-tests.log`。git diff --check通过。

## 仍待完成

SKU其他字段、商品规格模式、字典重复创建返回原ID等仍需逐项对齐；其余业务缺表和完整App流程按27设计继续推进。真实支付/退款/物流渠道SDK按用户决定后置。真实MySQL迁移、Redis/Dubbo/MQ及部署HTTP/网关认证/浏览器真机端到端均NOT RUN。

QA重点：备份与逐SKU迁移核对、旧写者停写、新服务配套发布、字典改名对当前SKU/历史订单的隔离、删除字典后编辑已有商品及跨端规格显示。隔离自动化测试不等同于上线验收。
