# 促销与支付五个后台入口交付

2026-10-31，本批一次补齐满减活动、折扣活动、支付单、支付退单、支付配置（支持渠道目录）五个后台入口。沿用React/Ant Design、TanStack Query、统一AdminModalForm及已有业务接口，没有新增依赖、业务表或渠道SDK。

促销支持分页筛选、详情、新增编辑、关闭及关闭后删除；满金额/满件数递增阶梯、全部/SPU/分类范围、真实SPU/SKU选择、减价/几折配置同批接通。金额字符串及BigInt精确比较，几折精确转基点，范围切换清空旧选项，SKU随商品切换清除，隐藏字段/状态/操作者不提交。读取失败重试、详情失败不打开编辑、成功后刷新失败提示、false响应不报成功。关闭不可重开，不编辑已关闭项。

支付单按单号/订单/渠道/状态分页并查看详情；支付退单按售后/支付单/状态分页并查看详情；状态集合独立、金额字符串及大ID安全解码。支持渠道仅显示code/name，不制造enabled或已接通状态。退款仍由售后流程发起，页面不提供手工造付款/退款交易。列表/详情/操作按各自权限控制，所有接口保留服务前缀。

五个菜单component均已注册。新增system库V20261031__mall_promotion_menu.sql与V20261031__trade_payment_menu.sql：可重入，保留旧根名称、现有角色绑定，不自动授予角色。trade-start测试资源显式包含新菜单脚本。只在H2执行，未修改真实库；部署需核对运行脚本并按现有流程授权。

最终237项前端测试、39文件通过，较上一批增加14项，涵盖金额/折扣精度、去除残留字段、请求路径/参数/响应、状态和ID、页面权限/关闭边界、五个component注册。build/typecheck/lint/format:check通过，仅既有大chunk提示。后端聚焦15项通过：促销真实Mapper/事务业务9、促销菜单1、支付真实Jackson HTTP契约4、支付菜单1；两个菜单均重复执行验证。git diff --check通过。源码业务服务未改，未重复全后端/小程序回归。

接口/操作说明见docs/promotion-management-pages.md、docs/payment-management-api.md；聚合证据evidence/backoffice-management-pages-verification.json；日志/tmp/promotion-management-backend.log、/tmp/payment-management-backend.log、/tmp/payment-ui-*.log。

本批完成已有业务能力的管理入口；源满减送赠券/赠积分/包邮、完整时间筛选、支付应用/钱包/通知管理等缺口仍存在，保持in_progress。前端验证为API合同及SSR，尚未浏览器交互/部署HTTP/Nacos/真实数据库或渠道联调。未commit/push。
