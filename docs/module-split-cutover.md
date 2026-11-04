# 四服务配置与切流准备

P5-00/P5-01只交付源码、配置模板与自动化证据，不执行建库、迁移数据、发布Nacos、创建Topic、部署或切流。旧mall/trade仍保留在仓库，已从默认Maven Reactor移除；P5-01已实现网关内置端点映射；真实路由与数据切流尚未执行，当前不能直接撤下运行中的旧服务。

## 配置归属

| 服务 | HTTP默认端口 | Dubbo默认端口 | Nacos Data ID | 数据库环境变量前缀 |
|---|---:|---:|---|---|
| product | 9140 | 20884 | lemnt-product.yaml | PRODUCT_DB_ |
| promotion | 9150 | 20885 | lemnt-promotion.yaml | PROMOTION_DB_ |
| order | 9160 | 20886 | lemnt-order.yaml | ORDER_DB_ |
| pay | 9170 | 20887 | lemnt-pay.yaml | PAY_DB_ |

源模板位于docs/config/，每服务显式设置`URL`、`USERNAME`、`PASSWORD`。账号只授权所属库，迁入的数据仍按`work-items/module-split-migration/table-mapping.md`保留原编号及冻结字段。初始化SQL只用于空库，不可直接应用到现有业务库；本批没有执行数据库命令。

四服务按顺序导入`lemnt-common.yaml`、`commerce-mq.yaml`、本服务Data ID，使用同一`NACOS_NAMESPACE`及`NACOS_GROUP`。`NACOS_SERVER_ADDR`、`NACOS_USERNAME`、`NACOS_PASSWORD`由环境注入。member沿用既有profile命名空间和DEFAULT_GROUP，在原common/member导入之间增加commerce-mq；部署时必须核对它与四服务处于预期的同一命名空间/组。

`lemnt-common.yaml`继续提供Redis、认证、指标等公共组件配置；本批不改system配置、菜单或sys_config键。新服务本地模板覆盖独立数据源和Dubbo注册信息；不复制已有默认凭据。Dubbo提供者使用新Client全类名；消费者通过所属Infrastructure Gateway调用，写操作框架重试为0，结果未知按原业务号查询/恢复。

## 网关端点路由源

`docs/config/commerce-gateway-routes.yaml`是唯一内置业务路由源，Maven原样打包到网关，由`ServiceRouteConfiguration`使用Spring Gateway标准RouteDefinition解析。原313个显式端点分为product58、promotion122、order86、pay47个；按方法/服务组织为16个业务路由组，另有4个OPTIONS组。GET路由同时支持原隐式HEAD。浏览器CORS预检仍由既有CorsWebFilter在路由前处理。

`/mall`和`/trade`路径、请求方法、原查询编码保持；路由不StripPrefix或改写请求体。内置路由保留原DSL过滤器语义，不继承动态路由配置的默认改写过滤器；网关全局鉴权、跟踪、XSS等过滤器仍照常执行。member/auth/system既有路径、优先级和StripPrefix保持，open等其他动态配置不替换。原权限由目标Controller保留，并通过四个新服务的真实Controller映射核对。

**不要将这份清单再次合入Nacos routes。** 网关已内置加载同一文件，重复导入会形成重复定义。部署前审核当前Nacos中旧`/mall/**`、`/trade/**`业务粗路由及重复项并移除，只保留其他服务路由及既有鉴权、CORS和流控配置；旧`mall-gateway-route.yaml`、`trade-gateway-route.yaml`现在仅记录停用要求。代码中的内置业务路由没有旧服务兜底；运行环境是否还有旧动态路由须单独核验，本批未发布或删除在线配置。

现有Sentinel源、阈值和窗口未修改；导出基线中的auth/system规则保持。若部署环境另外按旧商城/交易routeId定义过规则，应在发布前按原规则审核新的`commerce-{服务}-{方法}`资源归属，保留原阈值/窗口，不能因新routeId绕过原限制；不在不了解实际规则时编造阈值。

## 消息与副作用开关

共享`commerce-mq.yaml`使用`ROCKETMQ_NAME_SERVER`；生产者组为`${spring.application.name}-commerce-producer-v1`。正式8个Topic为PAY_SUCCESS、PAY_REFUND_SUCCESS、ORDER_PAID、AFTER_SALE_COMPLETED、ORDER_CLOSED、ORDER_SHIPPED、ORDER_COMPLETED、STOCK_PRE_RESERVED。Topic与下列Consumer Group由生产常量/注解定义，不用新增一套不可生效的YAML映射替代。所有正式消费者最多重试16次，耗尽后由Broker死信保存；部署还须核对Broker重试/死信权限、告警与原信封重放。

| Topic | 消费服务 | Consumer Group |
|---|---|---|
| AFTER_SALE_COMPLETED | member | lemnt-member-refunded-v1 |
| AFTER_SALE_COMPLETED | product | lemnt-product-refunded-v1 |
| AFTER_SALE_COMPLETED | promotion | lemnt-promotion-after-sale-completed-v1 |
| ORDER_CLOSED | member | lemnt-member-closed-v1 |
| ORDER_CLOSED | pay | lemnt-pay-order-closed-v1 |
| ORDER_CLOSED | product | lemnt-product-closed-v1 |
| ORDER_CLOSED | promotion | lemnt-promotion-order-closed-v1 |
| ORDER_COMPLETED | promotion | lemnt-promotion-order-completed-v1 |
| ORDER_PAID | member | lemnt-member-paid-v1 |
| ORDER_PAID | product | lemnt-product-paid-v1 |
| ORDER_PAID | promotion | lemnt-promotion-order-paid-v1 |
| ORDER_SHIPPED | member | lemnt-member-shipped-v1 |
| PAY_REFUND_SUCCESS | order | lemnt-order-pay-refund-success-v1 |
| PAY_SUCCESS | order | lemnt-order-pay-success-v1 |
| STOCK_PRE_RESERVED | promotion | lemnt-promotion-stock-pre-reserved-v1 |

`COMMERCE_MQ_ENABLED`默认false，控制全部15个正式消费者；`COMMERCE_OUTBOX_ENABLED`默认false，控制order/pay两个Relay。配置缺失或显式false都不装配这些副作用组件。启用后按原Outbox记录发送，不重建事件编号或载荷。product出站不使用本地消息表：库存预占事实经RocketMQ事务消息（半消息+台账回查）直发，MQ不可用时预占同步失败。promotion只有Inbox，不新增Outbox。

其他开关均默认关闭：`PROMOTION_SCHEDULER_ENABLED`、`ORDER_SCHEDULER_ENABLED`、`ORDER_AUTO_RECEIVE_ENABLED`、`ORDER_AUTO_COMMENT_ENABLED`、`ORDER_RECOVERY_ENABLED`、`PAY_LEGACY_NOTIFY_ENABLED`。既有`lemnt.mall.scheduler.*`与`lemnt.trade.*`配置键保持兼容，分别只在新的实际归属服务配置。order旧补偿Topic入口`lemnt.order.legacy-rollback.enabled`固定false；没有审核过历史未决任务时不得随意开启。

## 实际切流前的检查顺序（本批未执行）

1. 核对P5-01端点唯一归属证据，并完成P5-02保护检查、P5-03全量回归。记录当前服务版本、路由、配置和数据备份；禁止仅凭本批构建结果发布。
2. 暂停旧mall/trade写入、调度器及消费者，等待在途RPC完成，记录未决订单、渠道结果、退款、预占、优惠券、活动、提现及通知任务；只读核对，不以当前配置补造缺失历史。
3. 在独立目标库完成审核后的数据迁移，保留主键、业务号、快照与幂等凭证；按64表映射逐表计数并核对现金、数量、积分、券、库存。跨库外键由原业务号和可信Client核验替代，不能丢失关联。
4. Redis沿用原提交恢复命名空间`mall:{settlement}:v2:`和新预览`mall:settlement:v5:`；保留原requestNo/orderNo/afterSaleNo、锁和30天恢复记录。旧未提交预览版本不变造为新报价，已提交请求必须能按原编号查询。不要清空共享Redis或按新服务名重写已有键。
5. 历史PAY/REFUND/AFTER_SALE任务分别核验真实资金、订单冻结方案及权益回执，再登记明确的新归属。原支付应用标识（例如已存在的lemnt-mall appKey）是业务配置，不随部署名重命名。旧库存/券补偿消息逐条核对，不能同时重放为两套不同副作用。
6. 准备Nacos源、独立账号、Dubbo服务、8个Topic和15个独立消费组。新服务先保持所有上述开关关闭，核对服务发现、数据源、实际调用方和回调地址；再按已审定顺序开启消费者、Relay、恢复与调度，最后开放新写入/切换网关。
7. 在真实环境验证普通/零现金订单、部分退款与尾差、重复/乱序、MQ中断恢复、支付验签、库存确认、积分/券和佣金。没有证据时仍标记待验收。

## 回滚边界

切换前可保持旧版本继续运行。新服务产生写入或发布事件后，先停止新写入、消费者与调度，再按原业务编号对账并制定反向数据同步；不能直接把旧服务重新指向滞后库，也不能以删除Outbox/Inbox消除未决事实。路由回退、数据库恢复和历史任务处置须形成单独审核记录，本文件不执行这些操作。
