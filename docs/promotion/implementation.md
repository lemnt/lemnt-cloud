# 促销引擎 T1 实施记录

需求ID：yudao-member-mall-trade-migration；版本 promotion-v1/T1；2026-10-25（Asia/Shanghai）。
责任：code-software-engineer。输入：534048e 的 [promotion-v1 设计](design.md)、23个固定算例和仓库六层规范。

**已完成纯Java领域计价内核与测试，尚未接入实际结算。** T2..T7仍PLANNED。没有新增HTTP接口、数据库迁移、运行开关、Spring Bean或应用依赖；[api.md](api.md)的11个入口仍是拟定契约。原有V1报价、缓存提单、支付/关闭/Outbox行为不变。

## 文件与职责

代码根：`lemnt-server/lemnt-trade/lemnt-trade-domain/src/main/java/com/lemnt/trade/domain/promotion/`。

| 路径 | 实际职责 |
| --- | --- |
| model/FenAmounts.java | BigDecimal元精确转long分；加法/乘法溢出拒绝 |
| model/PricingLine.java | 权威SKU/SPU、正单价、数量1..999；行原价精确乘法 |
| model/PromotionScope.java | ALL、SPU_IDS、SKU_IDS；显式ID最多1000、正数、去重、排序及防御复制 |
| model/DiscountRule.java | 封闭的阶梯满减/支付比例/固定券规则；活动不接受固定券规则，券不接受阶梯规则 |
| model/PromotionVersion.java | 不可变活动规则、范围、优先级、版本、券策略和左闭右开窗口；不是持久实体 |
| model/SelectedCoupon.java | 已选券快照；fromLegacy校验本人AVAILABLE、未绑定订单、模板一致/启用、数据库本地时间窗口及原规则参数 |
| model/PricingContext.java | 服务端可信输入、BEST/NONE、一次数据库epoch采样；1..100不重复SKU及活动版本去重 |
| model/PricingResult.java | promo-1、总额、选中版本/券、行优惠/实付与单位分割；0元显示ZERO_PAY_UNSUPPORTED |
| model/PricingException.java | 显式错误码；尚无新HTTP映射 |
| service/DiscountAllocator.java | BigInteger中间乘积、最大余数、SKU数值并列顺序；零权重不获优惠 |
| service/PromotionPricingService.java | 至多一个活动+一张指定券；范围/窗口匹配、两阶段分摊及确定性择优 |

模型全部不可变；服务不访问系统时钟、Spring、数据库、Redis、Dubbo或渠道。它不能自行证明会员身份、商品可售/库存、发布状态或名额；这些仍由后续应用/Gateway负责获取和校验。`SelectedCoupon`构造器是内部可信快照契约，真实业务应经`fromLegacy`转换已查询的本人券；不能反序列化HTTP为领域上下文。

## 算法与错误

活动先减，券再以剩余总额判断门槛及计算。BEST不自动挑券或丢券；NONE保留已选券。EXCLUSIVE活动遇到已选券不参加组合。活动使券门槛失效或优惠不足1分时排除该组合，保留无活动+原券的候选。原价阶段券已不适用则COUPON_NOT_ELIGIBLE。不消费零优惠活动/券。

候选顺序：实付更少、资源占用更少、priority更高、数值活动ID更小、版本更小。活动时间/商品范围匹配后最多100候选，超过报PROMOTION_CANDIDATE_LIMIT，不截断；NONE不搜索活动。阶梯至多20档，门槛严格递增/优惠非递减，最高达标档只减一次。比例为基点；**优惠额向下取整**，不是应付取整；legacy券仅支持现有整数百分比1..99。

每阶段按剩余行金额分摊，余数相同按SKU数值升序，输入顺序不影响结果。每行`gross=campaignDiscount+couponDiscount+payable`，行汇总等于订单汇总。`unitBasePayableFen`和`extraFenUnitCount`稳定决定单位序号金额；这里只计算后续退款上限依据，没有执行部分退款。

| 领域错误/结果 | 触发 |
| --- | --- |
| INVALID_PROMOTION_REQUEST | 非法结构、ID/数量/范围、阶梯、比例、重复SKU/版本或不支持的规则组合 |
| AMOUNT_OUT_OF_RANGE | 负金额、元金额非整分、long转换/累计/乘法溢出、非正商品单价 |
| COUPON_NOT_ELIGIBLE | legacy归属/状态/模板/时间不合格，或原价下券优惠为0（含门槛失败） |
| PROMOTION_CANDIDATE_LIMIT | BEST中窗口/商品范围匹配活动超过100 |
| submittable=false、ZERO_PAY_UNSUPPORTED | 最低真实应付0；不替换成另一更贵组合或收取1分 |

这些是内部规则，不能据此声称HTTP状态、未知JSON字段拒绝、鉴权或并发事务已经实现。原始券参数损坏产生结构/金额错误时也拒绝，不能当作不用券继续下单。

## 验收与实际验证

`src/test/java/com/lemnt/trade/domain/promotion/PromotionVectorsTest.java`通过Maven testResource直接读取docs/promotion/pricing-vectors.json；未复制、重算或修改人工固定预期值。`PromotionPricingTest.java`承载额外边界、随机性质及legacy差分。testResource只包括固定算例，不进入生产资源；无新增测试依赖。

| 验收 | 实际覆盖 |
| --- | --- |
| P-01守恒/余数 | 两阶段固定分摊、数值并列/零权重、2000组独立有理份额与余数排名断言 |
| P-02舍入/0分券 | 固定边界、1500组实际CheckoutRules.discount百分比差分+1500组固定券差分；不是Python模拟Java |
| P-03范围 | ALL/SKU/SPU、同SPU多SKU、不触及其他SPU及活动后零权重行 |
| P-04券约束 | 原价门槛拒绝、活动后门槛失效、互斥、NONE保留券、活动后0分券排除组合 |
| P-05零元 | 真实0元、状态原因、零实付行/单位，无自动收1分 |
| P-06边界 | Long.MAX_VALUE合法结果/BigInteger中间积、累加/乘法溢出、非整分/负值、零单价拒绝 |
| P-07阶梯/上限 | 最高达标档一次、比例优惠上限、不合法阶梯、20/100/1000边界及数量999 |
| P-08确定性 | 固定算例乱序、500组完整计价守恒/单位分割/输入排列不变、优先级极值/数值ID/版本并列、防御复制 |
| 额外边界 | legacy归属/状态/绑定/模板启用和时窗，活动左闭右开/未匹配不计上限，错误结构/空值 |

PASS：Java17、Maven3.9.11，**完整71项目verify、431项测试，0 failures/errors/skips**。其中新增39项：23动态固定算例+16额外测试方法；循环内的随机/差分组数不计为独立Maven测试数量。原392项仍通过。未发现促销源码编译问题。

```bash
/tmp/apache-maven-3.9.11/bin/mvn -o -s /tmp/member-sdk-settings.xml -B \
  -DargLine=-javaagent:/root/.m2/repository/net/bytebuddy/byte-buddy-agent/1.18.10/byte-buddy-agent-1.18.10.jar verify
PYTHONPATH=/tmp/promotion-schema-validation python docs/promotion/validation/validate_design.py
```

上述临时路径属于本次实际环境；其他环境使用正常可用的Maven设置和相应测试代理。独立设计检查PASS：15 Schema、16例、13文档JSON、13反例、4跨字段草稿、23算例、2000随机分摊、1500 Python公式对比及14历史源码hash保持。该脚本只评估设计附件，输出不再硬编码运行时“未实现”；原设计历史证据保留。

证据：[promotion-kernel](../../work-items/yudao-member-mall-trade-migration/evidence/promotion-kernel/)。包括实际verify结果摘要、当前运行的测试名/结果、源码SHA256及设计校验结果。存量测试的网络接口警告、预期异常日志/空JAR警告未引起构建失败；存量集成测试仍有deprecated提示。

React/小程序未改动，本轮NOT RERUN；169/69仅为历史结果。目标MySQL/Redis/Nacos/Dubbo、支付渠道、浏览器/原生编译/设备均NOT RUN，不以本次纯领域测试替代。没有线上配置、角色授权、SQL执行或生产可用性声明。

## 后续与回滚

T1可交code QA/人工按P系列验收；下一步T2持久化不可变版本、root跨版本使用计数和事务/并发测试。T3权威事实RPC与管理API、T4缓存结算V2、T5支付/关闭/补偿、T6页面、T7真实集成仍未完成。计价只输出结果，不占用次数，不写缓存，不创建订单；提单仍须从服务端缓存生成订单并重新核验，不能静默重新定价。

目前没有生产调用者，回滚可删除T1新增类及测试资源配置，无数据迁移。保留迁移分支和Draft PR；不修改main、不merge、不部署。
