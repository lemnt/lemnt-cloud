# 完整迁移F01：优惠券

2026-10-28 v1，code-software-engineer。源：CouponTemplateDO/CouponDO、CouponConvert、CouponServiceImpl、CouponTemplateServiceImpl、TradeCouponPriceCalculator。父设计27-full-migration-design.md。

模板补description/take_type/take_limit_count/product_scope/product_scope_values/validity_type/fixed_start_term/fixed_end_term/use_count；保留现有元单位和字段别名并明确映射（threshold=use_price等）。发放总量/每人限量支持-1不限，DATE固定日期、TERM领取后起始天数+持续天数；日期由数据库时间计算。每次领取是一张新券，达到限额拒绝，不再用会员+模板唯一键强制一张。保留用户明确要求的LOCKED预占状态。

会员券落库完整名称、领取方式、金额/比例/范围/有效期快照和use_time，结算不依赖当前模板启停或改价。模板只能影响之后新发券。过期任务处理AVAILABLE，已锁定订单沿原锁定链路核销；关单/整单退款归还时按原券有效期转AVAILABLE/EXPIRED。撤回仅未用/过期，逻辑删除并退还领取计数；核销/退券维护模板使用计数。持券表新增deleted，索引member/template变普通索引。

商品范围在促销和会员折扣之后、运费前计算，仅对ALL/SPU_IDS/CATEGORY_IDS对应行校验门槛和分摊；价格快照保留行净额。会员券分页独立DTO/Res，不能用当前模板冒充原持券规则。后台新增模板修改/删除与发券列表/撤回，仍按app/web及逐接口Req/Res实现；同步React模板编辑/券管理和小程序券中心/券包。

变更SQL初始化+存量升级（发券快照从当前模板一次性回填，无法恢复已被改写的历史规则要标明）；无真实DB操作。事务锁顺序仍会员操作锁→模板锁→券，避免增添请求/额度表；不使用JPA或手写重复CRUD。验证模板变更不改旧券、固定/相对时间、多次领取限额并发、券适用范围、过期/退款/撤回计数和现有下单回归。
