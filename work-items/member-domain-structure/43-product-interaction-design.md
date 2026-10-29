# 商品收藏与浏览足迹迁移设计

2026-10-29，code-software-engineer，沿用已读取的设计/API/Java示例、项目AGENTS与27完整设计F03。对照本地芋道ProductFavoriteDO/Service/AppFavoriteController及ProductBrowseHistoryDO/Service/Controller，迁移两张业务缺表及可操作页面，不只建空表。

目标：新增mall_product_favorite（user_id/spu_id）、mall_product_browse_history（user_id/spu_id/user_deleted），继承当前BaseDO审计字段并补源逻辑删除字段；在基础会员锁内保证每会员+SPU仅一条有效记录，收藏重复添加报冲突、取消不存在报不存在。足迹每会员每SPU只保留最新有效行，最多100条有效记录（含已隐藏），旧行按源逻辑删除保留；删除/清空只对本人标记user_deleted，重新浏览恢复可见。复用现有MemberLockGateway/基础LockTemplate，持有至数据库事务结束，不能新增请求表或会员账户辅助表。

各表独立DO/BaseMapper/Gateway与领域记录，简单查询使用Lambda，无需空XML。应用层独立Cmd/Qry/DTO/Executor和两个业务Service；app/web独立Controller/Req/Res/Converter，app会员身份只取SecurityUtils，Body/Query不接受userId；web只读需权限及可选会员/SPU筛选。分页统一R.page顶层rows/total，不造新分页对象。

收藏App：POST /mall/app/favorites，DELETE /mall/app/favorites/{spuId}，GET /mall/app/favorites、/exists、/count。足迹App：POST /mall/app/browse-history记录，GET分页，DELETE批量及DELETE /clear清空。公开商品详情在网关会清除身份并跳过认证，不能在那里猜测会员；采用登录后独立受保护POST记录，触发时机仍是详情加载成功。匿名不调用。无Token认证代码进入mall。

读卡片批量取当前SPU/SKU名称、图片及真实最低价格，价格按现有元字符串，资料缺失保留null，不捏造销量/库存。管理员只读收藏/足迹嵌入商品详情，不新增无关后台主菜单。小程序详情收藏/取消及足迹记录、会员收藏/足迹列表/移除/清空，处理加载、无数据、错误及换号/未知写入结果核验。

任务：1) 两表和按表存储；2) 事务/锁/分页/额度及所有独立合同；3) admin权限种子及商品详情面板；4) miniapp传输/详情/列表页面；5) 生产DDL+真实事务/基础锁模板边界测试并发有效记录唯一、跨会员隔离、足迹上限及隐藏恢复/回滚，HTTP防伪身份及前端回归。新数据库SQL只生成不执行，现有SDK后置，不commit/push。

成功标准：真实存储与接口到调用页面形成闭环，两表可从缺表清单移出；初始化45→47，缺业务36→34。真实Redis/MySQL/网关部署/浏览器真机未验证则标NOT RUN。与源基类逻辑删除/租户字段等仍按目标BaseDO体系适配，不虚报逐列完全一致。

页面对照补充：已读取源 goods-collect.vue、goods-log.vue，迁移当前页选择/批量移除。收藏按源逐条取消，部分失败停止并刷新真实结果，不增加批量数据库接口；足迹批量单请求。单条、批量失败信息在重新查询后保留，查询失败清空旧列表。
