# 配送基础批次交付

2026-10-28，需求 member-domain-structure，责任 code-software-engineer。输入：23-delivery-foundation-design.md v1、本地芋道 config/delivery DO 及运费计算器。复刻目标仍以 22-mall-replication-baseline.md 为准，本批没有宣称商城已复刻完成。

## 本批实现与设计对应

| 任务 | 实际变更 | 验证 |
| --- | --- | --- |
| D1 表和持久化 | trade/sql/V7__delivery_config.sql；config/delivery 各层逐表模型、DO、BaseMapper、resultMap XML、Gateway；保留5表参考业务字段，审计列映射system规范 | 生产建表、Mapper/XML、JSON往返、软删除 |
| D2 业务和管理接口 | config单例配置；快递公司及模板CQRS、独立Req/Res、14个HTTP映射；模板主从事务、子项归属与ID保留、地区去重、配置行锁防并发重名 | 事务回滚、并发创建、HTTP嵌套校验、接口/权限映射 |
| D3 运费计算及发货 | DeliveryPriceService 件/重量/体积、向上续费、全场/活动/地区包邮、整数分行分摊；OrderFulfillmentService 新发货检查启用公司，历史重放先返回原结果 | 行金额守恒、包邮双门槛、缺地区/重量、停用发货和历史重放 |
| D4 文档和升级 | docs/trade-delivery-api.md、缺表进度、仅增加权限的种子脚本；权限脚本不自动授权角色 | 两次种子执行不重复、不授角色；git diff --check |

建表后初始化配置不默认开启包邮/自提/分销；配置保留分销字段仅用于结构与配置契约，不表示分销功能实现。集合存JSON，金额整数分。模板只做逻辑删除，旧数据仍在；接入商品配送字段时须补被引用模板的删除策略。

当前39张初始化表增加到44张，参考无对应表从45降至40（38业务+2演示），这是表映射进度，不是流程完成率。详细进度见 evidence/yudao-table-progress-20261028.json。

## 验证证据与环境

证据统一写入 evidence/delivery-foundation-verification.json。Java17、离线Maven、Mockito显式agent；生产持久化/H2 MySQL模式与Spring事务代理，远程支付仍沿用原测试边界。没有真实MySQL迁移、没有部署、没有提交或推送。HTTP测试验证公共ProblemDetail错误结构（校验422、业务400），未另写异常处理。

## 未完成依赖与下一步

- 商品 delivery_types/delivery_template_id、SKU weight/volume 尚未接入；地址所属会员校验、结算和订单的运费与价格分项快照仍需迁移。
- DeliveryPriceService 尚未加入现有结算总价，不能声称下单金额已包含运费；本批没有开放允许会员自行传价格的报价接口。
- 未新增后台管理页面，已交付可验证的管理接口及权限定义。
- 真实渠道SDK按用户要求后续接入；目前仍有38张业务表及对应流程待迁移。
- 上线前先演练V7，并核对已有发货编码、通过管理接口导入启用公司；不将历史未知编码自动变成有效渠道。

下一纵向链路：商品配送字段 → 会员地址 → 快递计价 → 结算缓存快照 → 订单主子表价格分项及配送信息 → 部分售后运费分摊。需继续按原参考补齐，不能用零运费默认值掩盖缺失。
