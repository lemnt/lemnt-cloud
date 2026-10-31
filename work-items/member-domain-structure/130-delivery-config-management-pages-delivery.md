# 快递公司、运费模板、交易配置后台入口交付

2026-10-31，本批补齐三个后台入口，复用现有trade业务接口和服务前缀路由，没有新增业务表、依赖或渠道SDK。

快递公司：名称/状态筛选分页、详情、新增编辑删除，状态0启用；编辑只发送接口字段。运费模板：名称分页、完整详情、新增编辑删除、计费及包邮区域规则、三种计费方式；保留原子规则ID，按件整数、其他六位小数、地区必填和同类规则地区互斥，数量与金额同时满足才包邮。真实地区树勾选，金额文本精确转分；读取详情失败不开编辑表单，失败保留草稿，false响应不报成功。

交易配置：仅退款/退货退款原因、满额包邮开关和门槛；全量PUT保留GET取得的自提及分佣字段，配置缺失时禁止保存。页面不会将未开放业务宣称已完成。关闭表单详情与配置的聚焦/重连刷新，避免异步读取覆盖编辑。

三个component注册，新增system库菜单脚本V20261031__trade_delivery_config_menu.sql，可重入、保留旧根名称和角色授权，不自动授予角色。trade-start显式包含测试SQL资源。接口/页面说明见docs/trade-delivery-api.md第6节；源前端8个文件映射更新为in_progress，浏览器交互及真实联调仍待验证。

验证结果见evidence/delivery-config-management-pages-verification.json。前端API合同与SSR覆盖金额、大ID、子规则身份、筛选边界、配置未开放字段保留、失败及权限；后端使用生产Jackson配置核对字符串精确数量/大ID及整数分，H2真实Mapper/事务计价和菜单重复执行。未执行真实库、部署、Nacos变更或浏览器验收，未commit/push。

最终247项前端测试/42文件、11项后端聚焦测试通过；build（含tsc）、lint、format:check与git diff --check通过，仅既有vendor大chunk提示。日志/tmp/delivery-ui-*.log、/tmp/delivery-management-backend.log。
