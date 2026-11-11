# 会员地区选择交付

2026-10-29，承接69设计。原样迁移芋道biz-ip的area.csv到system只读资源，按area域拆domain Gateway/模型、infrastructure内存加载、application DTO/执行器、adapter web独立Res/控制器。GET /areas/tree需登录，返回中国子树；数据启动加载一次并保持不可变。不新建表或依赖，不声称此快照为最新行政区。

会员编辑改为地区树选择，详情按真实树路径展示全称。加载中/失败/未识别旧编号分别显示，失败可重试并禁止地区修改，其他资料可编辑；旧编号不猜测替换。member响应仍只有areaId，没有新增跨服务RPC或member API areaName。

system原133项verify通过，新增2项实际源资源/HTTP映射测试单独通过；合计现135项测试证据。新测试核对北京→北京市→东城区、不可变、排除其他国家、真实执行器到HTTP映射及RequiresLogin注解；standalone不代替部署权限验收。React189项通过，类型/代码/格式与构建验证见证据。本轮最终UI文案修正后再次构建/测试/规范检查。

未部署外部路由、未浏览器验收，后端存储已有测试使用授权本地端口运行；未执行DDL、未commit/push。member服务端areaName解析、注册登录审计仍未迁移。文章继续排除。

接口docs/member-profile-mark-api.md；证据evidence/member-area-verification.json。
