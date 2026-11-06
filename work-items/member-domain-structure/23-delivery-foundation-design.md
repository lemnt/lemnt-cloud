# 配送及交易配置复刻设计

2026-10-28 v1，责任 code-software-engineer。来源：22-mall-replication-baseline.md、docs/yudao-missing-tables.md、本地芋道 delivery/config DO 与服务及 TradeDeliveryPriceCalculator。参考技能 development-design.md、api-documentation.md、java.md（前序已读取），遵循 lemnt-server/AGENTS.md。

本批先交付普通快递的交易配置、快递公司、运费模板主表/计费/包邮 5 张表。保留芋道业务字段及金额分单位，DO 审计映射 system 的 create_by/update_by；集合列存 JSON，Gateway/Mapper 按表拆分。自提门店、商品配送字段、结算地址/运费快照是下一依赖，不以默认零运费冒充已接入。

API：/trade/admin/config GET/PUT；/trade/admin/delivery/expresses 与 /templates，POST 新增、PUT /{id} 修改、DELETE /{id} 删除、GET /{id} 详情、GET 分页、GET /simple 选择。独立 Req/Res，统一 R/PageResult，权限 trade:config:query/update、trade:delivery-express:create/update/delete/query/list、trade:delivery-template:create/update/delete/query/list。前端无现成原型，先交付可测试管理接口，不伪称页面完成。

模板保存一个事务锁定主表并同步子表，保留传入已有子项 id、验证归属；重复区域配置拒绝，首续计费数合法，金额非负且整数分。规则按区域精确匹配，首量内首价，超量按续量向上取整；包邮同时达到金额与数量/重量/体积门槛。全局包邮和活动包邮优先，分摊采用整数分，尾差归最后一行。纯计价服务不接受客户端价格作为正式订单依据，当前不提供面向会员的任意金额报价接口。

任务：D1 SQL+逐表模型/DO/Mapper/XML/Gateway → H2真实持久化；D2 配置、公司、模板应用事务及接口 → HTTP绑定/校验及保存/修改/删除；D3 运费计算 → 件/重量/体积、续费取整、包邮、区域缺失、行金额守恒测试；D4 文档/缺表清单更新/构建 → 不把尚未接入订单的计价器算作下单闭环。

存量仅新增 V7，不执行真实数据库；逻辑删除保留历史，模板子表外键及索引。配置沿用参考的分销字段以避免再次裁剪业务模型，本批仅保存配置，不宣称分销流程已实现。删除模板与 mall 商品引用的校验要在商品配送字段接入时补齐。SDK继续后置，不commit/push。后续结算接入时必须服务端读取会员地址、商品模板/重量/体积，再记录运费快照和订单分项金额，完成前不能宣称商城复刻完成。
