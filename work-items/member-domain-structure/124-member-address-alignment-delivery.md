# 收货地址源逻辑收敛交付

2026-10-31，承接123，依据本地芋道AddressServiceImpl、MemberAddressDO及MemberAddressMapper。

移除额外最多20条限制，新增21条验证通过；本人归属条件继续用于所有读写，缺失/他人修改或删除统一404，删除默认不指定替代项，详情与默认查询缺失仍null。App编号来自网关上下文，不接收请求体或参数覆盖。

member_address属主改为源user_id，AddressDO继承项目BaseDO并具备四项可空审计列，Domain/HTTP/RPC memberId保持。V2同步新库，一次性旧库升级sql/upgrade/address_owner_audit.sql保留编号、归属、默认状态、索引/外键，历史未知审计NULL不补造。未执行真实库，部署已有库需先核对并执行升级，不能重跑V2。

Mapper仅继承BaseMapper，不再重复insert/update/delete等包装；Gateway以Lambda按表操作，无XML或SQL注解。保存与清默认使用实体触发统一审计，清默认限定本人其他true项，不写普通地址/其他会员。默认查询直接过滤返回一条，原会员行锁和保存事务保留。

新增5项H2/真实Mapper验证：创建/更新审计和默认切换选择性、旧结构升级及FK保留、缺失/他人/重复删除和无自动替代默认、强制插入失败后的默认标志/审计回滚、MockMvc贯通真实保存/默认查询/删除且忽略外部属主并返回统一404。原并发默认、RPC归属、现有用例保留；旧20条测试改为21条允许。没有删除测试规避失败。

member verify、common+member/auth/gateway/mall/trade联合verify通过：member113/auth20/mall119/trade158，共410业务测试；gateway34/common-security29均失败/错误/跳过0，网关禁止Dubbo规则继续通过。小程序123项通过，git diff --check通过。前端未修改，本批未重复React检查。日志/tmp/member-address-*.log，证据evidence/member-address-alignment-verification.json；接口docs/member-address-alignment-api.md，主接口文档已删除20条限制。

未commit/push，未真实MySQL/Redis/Nacos/Dubbo/部署HTTP/浏览器或微信验收。H2 MySQL模式成功不等同真实MySQL升级验收。源逻辑删除基础字段及最终地址展示契约继续核对，完整迁移仍in_progress，文章排除。
