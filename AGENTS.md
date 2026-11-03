# lemnt-cloud 统一开发入口

适用于整个仓库；子目录规范补充具体约定。用户明确指令优先。修改后不commit、不push。

- 项目结构与验证命令见[README](README.md)；统一边界、格式和发布标准见[代码规范](docs/code-standards.md)。
- 后端遵循[后端规范](lemnt-server/AGENTS.md)，管理端遵循[管理端规范](lemnt-ui/AGENTS.md)，小程序遵循[小程序规范](lemnt-miniapp/AGENTS.md)。公共组件采用相同Java格式与分层约束。
- 改动前核对git状态、工作项和实际代码，保留已有未提交改动；不把文档示例或历史进度当作已执行事实。
- 注解使用显式import和短名，如`@Schema(...)`，不写全限定注解路径。
- 接口、方法和复杂业务规则使用中文说明，解释单位、状态、并发约束、幂等与兼容原因；不批量生成无业务含义的占位注释。
- HTTP使用Req/Res，应用层使用Cmd/Qry/DTO，领域层保持纯Java，基础设施在GatewayImpl隔离；不因格式重构改变公共路径、字段和权限。
- Java采用固定版本AOSP四空格格式及项目导入顺序；TS/JS/JSON/WXSS使用已有Prettier配置。WXML只统一标签行缩进，保留正文、指令和属性值。
- 金额、ID、历史NULL、积分、业务状态按现有边界契约转换，不使用浮点计算财务金额，不按当前配置补造历史快照。
- 普通持久化条件在GatewayImpl中使用Lambda；复杂SQL放XML。行锁必须在事务中真正执行，XML明确`useCache=false`、`flushCache=true`。
- 复用现有依赖和工具，不引入业务依赖来解决格式问题；格式化工具独立下载并验证固定校验和。
- 使用`scripts/check_architecture.py`检查分层和持久化规则，使用格式检查及对应模块测试验证修改。测试结果、数据库执行、真实环境验收分别记录。
- 不删除测试或放宽断言来通过构建，不自动执行现有库DDL、提交代码或改写用户的git索引。
