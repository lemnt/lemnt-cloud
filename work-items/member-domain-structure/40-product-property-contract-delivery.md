# 商品规格列表合同收敛交付

2026-10-28，承接39设计及37/38规格快照链路；code-software-engineer。对照本地芋道SKU的Property列表，将商品管理临时双映射合同移除；不代表全部商城功能迁移完成。

## 实现

- ProductSku实体直接持有List<ProductSkuSpec>，字典编号及名称同一对象传递。创建草稿、DTO、独立创建/保存Req和web/app Res均使用各自Property列表，移除specValueIds字段。
- 请求properties必须显式传数组，无规格传[]；每个元素propertyId/valueId必填正数，名称可不传、由服务端读取；传入不一致名称会被拒绝。旧名称映射不再作为HTTP写入合同。
- 商品应用服务在事务内按编号锁字典，验证规格值归属、重复及各SKU规格项一致性，直接保存已验证的规格行。移除双Map重建、保存时再查一次字典名称、废弃的Gateway名值读写接口和仅用于临时编号映射的帮助方法。
- 变体唯一键仍沿用原名称/值摘要，不仅因合同调整而重建旧键。存量名值资料在领域内部通过明确转换保持缺号，不按名称推测历史编号；读取旧关系行不依赖当前字典。
- 后台编辑器提交规格对象列表，回显使用同一列表；旧未关联商品提示人工选择。详情展示规格名称和值，编号按字符串保存。小程序商品详情同步生成规格列表标签并验证成对编号，读取端保留旧名值响应兼容。
- 沿用现有SKU规格关系表、按表Gateway/BaseMapper和XML resultMap，没有新增表/列或执行DDL。接口文档原位更新，服务端与调用端需配套发布。此次未提交/推送/部署。

## 验证

- member/mall/trade及依赖275项测试通过，失败/错误/跳过均0。clean verify日志`/tmp/catalog-list-clean-verify.log`；最终修改与新增接口测试后的verify日志`/tmp/catalog-list-http-verify.log`。
- 新增隔离HTTP到真实数据库链路：实际Controller、Jackson配置、Bean Validation、统一异常、CQRS执行器、字典锁、本地事务与MyBatis-Plus落库均参与；只传编号返回规范名称并保存真实ID，响应不包含specValueIds。非法编号返回422、伪造名称返回400，失败未增加商品/规格行。
- 既有真实双库模式的商品→结算→订单→售后链路通过；原规格ID/名称保存、旧JSON/提交回执恢复、报价变化拒绝、优惠/积分/运费、库存/支付/售后及事务回滚全部回归。
- 后台171项测试通过；类型检查及构建、ESLint、Prettier通过。日志`/tmp/catalog-list-ui-tests-final.log`、`/tmp/catalog-list-ui-build-final.log`、`/tmp/catalog-list-ui-lint.log`、`/tmp/catalog-list-ui-format-check.log`。
- 小程序82项测试通过，新增完整规格列表标签、长ID和部分缺号拒绝用例，日志`/tmp/catalog-list-mini-tests.log`。未进行浏览器或真机验收。
- git diff --check通过。隔离HTTP测试没有启用网关认证或远程网络，不能作为部署后的认证/端到端证据。

## 尚未完成

本批仅收敛规格对象合同与保存流程。SKU规格仍保存在现有mall_product_sku_spec关系表，并未合并成芋道SKU的JSON列；SPU/SKU其余字段、规格模式及其他商品行为尚需逐项对齐。订单/售后其他字段、完整App业务及业务缺表继续按27完整设计推进。初始化46张表、仍缺36张业务对应表（支付demo排除），数量不因本批减少。真实支付/退款/物流渠道SDK继续按用户决定后置。

真实MySQL、Redis、Dubbo、MQ及部署HTTP/网关权限/浏览器真机为NOT RUN。前序数据库升级须按实际版本核对，不能把本批无额外DDL理解为旧库已具备前序新增列。QA重点验证真实调用端配套发布、旧商品人工关联、多SKU编辑及跨端商品详情展示。
