# 配送贯通下单交付

2026-10-28，member-domain-structure，code-software-engineer。依据25-delivery-checkout-design.md v2；v1保存在同目录。源参考本地芋道配送计价、商品、会员地址、订单及售后实现，保持先复刻普通商城链路的目标。

## 已实现

| 设计任务 | 变更范围 | 证据 |
| --- | --- | --- |
| C1 商品配送字段 | SPU配送方式/模板，SKU重量/体积，SQL/DO/resultMap/Domain/Cmd/DTO/Req/Res完整传递；后台商品编辑和详情支持配置，十进制stringMode | CatalogPersistenceTest真实新增/修改往返；HTTP校验和前端解码用例 |
| C2 会员地址与运费RPC | MemberAddressClient按memberId+addressId读取；Mall网关隔离RPC并复核归属；TradeDeliveryClient使用真实运费应用服务；元/分精确转换和远程分摊守恒 | MemberAccountIntegrationTest地址归属；DeliveryPriceGatewayTest金额边界；两库集成使用实际RPC适配器和provider |
| C3 结算、缓存、订单 | 必填本人地址；补齐pointStatus输入传递；冻结收件/行运费；摘要和提交重新校验；交易订单主子表保存独立列，RPC和幂等比较完整快照 | 20+6.50=26.50实际持久化，地址/运费改变409；旧请求保持旧地址；生产RedisCodec往返 |
| C4 页面与发货 | 小程序刷新/选择本人地址、确认页显示收件及运费，切号清理；后台模板选择、重量/体积和详情；新订单发货使用冻结地址，旧无快照订单允许补填 | 小程序74项、前端162项；接口和发货集成验证；未做真实浏览器/微信验收 |
| C5 售后和回归 | 行实付包含行运费；售后按原快照和数量计算，不用当前模板重算；赠送积分在运费后计算 | 支付后发货/部分售后退款13.25元；245项受影响后端及依赖测试通过 |

完整接口变更见 docs/delivery-checkout-api.md，证据见 evidence/delivery-checkout-verification.json。后端scoped clean verify通过后又补充运费RPC整数分越界用例，最终verify为245项全通过；最后配送字段空元素校验另做聚焦HTTP回归。

## 升级与兼容

新库同步mall V1/V2、trade V1结构；存量升级使用两服务sql/upgrade/delivery_checkout_columns.sql，先演练、停写并备份，不重跑新库初始化。旧商品不自动分配默认运费模板。新预览键mall:settlement:v2使无地址旧预览失效，30天提交恢复索引继续保留。客户端须同步更新addressId参数。

历史订单收件列可为NULL，行运费为0；旧记录的读取构造器保持兼容。当前公开预览必须提供本人地址，不能由客户端伪造运费。新订单发货的收件字段来自订单，忽略请求体的替代收件人；历史无快照才补填。

模板删除与参考保持一致；已保存订单不受模板后续编辑或删除影响，尚未提交的结算重新计价。没有新建额外技术表。原缺表进度仍为38张业务表和2张演示表，本批主要贯通字段及流程，不把字段增加计作补表完成。

## 验证限制与后续

H2 MySQL模式、生产Mapper/XML和Spring事务代理验证；会员RPC本身用真实会员Gateway独立验证，商城/交易集成通过直接provider调用，无实际Dubbo网络。Redis验证实际序列化和已有测试边界，未连接真实Redis。支付退款SDK用测试适配器，真实渠道按用户要求后续接入。本轮没有执行真实数据库迁移、部署、提交或推送。

商城整体复刻尚未完成。接下来仍需持券规则快照/完整有效期和范围、订单与售后日志、完整价格分项及其余缺失业务表，不能把本轮普通快递链路通过等同于全部商城完成。
