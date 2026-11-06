# 全项目代码规范

2026-11-03。适用于后端、公共组件、管理端、小程序和数据库脚本。规范以现有接口和业务契约为边界；源码静态检查、单元/集成测试、数据库执行、真实环境验收分别记录。

## 项目边界

| 工程 | 职责 | 规范入口 |
|---|---|---|
| lemnt-dependencies | 依赖管理与构建插件，不承载业务 | 本文、后端POM规范 |
| lemnt-common | 认证、序列化、数据库、RPC等公共能力 | 根AGENTS、后端分层与Java格式 |
| lemnt-server | auth、system、member、mall、trade、open及gateway | lemnt-server/AGENTS.md |
| lemnt-ui | React管理端，模块API和页面 | lemnt-ui/AGENTS.md |
| lemnt-miniapp | 微信原生会员端 | lemnt-miniapp/AGENTS.md |
| docs、work-items | 当前契约、迁移说明、分批交付和证据 | 本文的验证与交付约定 |

管理后台账号与会员身份分开。商城负责商品、优惠规则和活动执行；交易负责订单、支付、售后、金额及权益结果快照；会员负责账户与积分流水。跨域使用client契约，不跨库持有其他业务实体的写权限。

## 分层与契约

HTTP沿用`Req → Cmd/Qry → Executor → DTO → Res`。Adapter完成HTTP绑定、校验和格式转换；Application编排流程；Domain定义规则与Gateway；Infrastructure实现持久化、RPC、缓存和消息；Start装配组件。

- Application与Infrastructure互为兄弟，不能相互依赖；Domain不依赖它们，也不引入Spring、MyBatis、Jackson、Swagger或工具库。领域类可使用Lombok和JDK。
- 普通Executor不直接调用远端、Redis或Mapper。Dubbo提供者位于application/dubbo，消费者由GatewayImpl隔离。
- Req/Res不嵌套Application DTO、Domain Model或DO。公开字段及嵌套类型补齐中文Schema，明确枚举、单位、空值和范围。所有Java类型通过显式import引入并使用短名，包含注解、字段、泛型、参数、返回值、implements和静态调用，例如`@Schema(...)`、`Map<Long, Integer>`、`implements Serializable`。字符串、反射类名及XML中的真实包路径保留。纯数据载体优先record并实现Serializable。
- 新命令采用`{聚合}{动词}Cmd`/`CmdExe`，查询采用`{聚合}{查询类型}Qry`/`QueryExe`。不新增`QryExe`、`Resp`或应用层`Vo`。
- 跨服务接口沿用各上下文现有`*Client`；system旧`Remote*Service`及`api`包名保持兼容，不为统一外观重命名远端契约。
- 密码等敏感字段不得进入HTTP Res。UserDTO现有WRITE_ONLY防护保留，不以删除注解的方式弱化安全边界。
- 重构保留R包装、分页rows/total、数组形状、公开路径、权限标识及字段语义。内部对象替换需验证实际JSON，尤其NULL、空数组、false和大编号。

## 持久化与事务

- DO继承项目BaseDO，持久化注解仅在Infrastructure；字段使用明确业务名称，数据库表及列必须有中文COMMENT。
- 简单CRUD复用BaseMapper，条件构造和业务查询组合位于GatewayImpl。Mapper不新增默认实现或private条件构造器；复用条件留在对应Gateway中。
- 多表、数据库函数、原子upsert和复杂CAS放在`resources/mapper/*.xml`。Java中不写注解SQL，不用setSql拼接表达式；数字增减优先setIncrBy/setDecrBy。
- FOR UPDATE必须在事务内真正执行，XML同时设置`useCache="false"`、`flushCache="true"`。状态变更保留期望值或版本条件，并检查影响行数。
- 跨行操作统一锁顺序；远程调用不持有本地数据库锁。金额/数量状态和后续任务在同一事务保存，下游执行使用业务幂等。
- member/mall/trade每个服务只保留一个最终初始化SQL，只含CREATE TABLE及必要INSERT，不保留V*或upgrade脚本；system脚本维持现状。初始化不负责旧库升级；不能把准备好的SQL写成“数据库已执行”。详见[业务库初始化](database-initialization.md)。
- 历史未知保持NULL；只有经可审计核对的事实才回填。禁止按当前商品、活动、会员等级或积分配置推断历史快照。

## 数值、状态与身份

| 值 | Java/数据库 | HTTP/两端 |
|---|---|---|
| 金额 | BigDecimal/DECIMAL，明确元或分 | 按既有接口约定解码；元文本不使用浮点计算退款 |
| Long/雪花编号 | Long/BIGINT | 全局序列化保留原安全数字/字符串契约，业务层统一为十进制字符串；旧数字仅接受安全整数 |
| BigInteger | 原整数比较范围 | 超出安全范围输出字符串，不能先longValue截断 |
| 积分 | 整数数量 | 保持Long精度，不混作现金单位；小程序不强依赖BigInt运行时 |
| NULL | 无法证明或未知 | 不自动变0、false、空字符串或空数组；按原契约展示“未记录” |
| 状态/权限 | 现有枚举和权限常量 | 集中映射，不能通过管理员角色名称代替接口权限 |

会员号来自可信会话，并在服务端校验订单/售后归属。会员入口缺失身份必须拒绝，不能落入管理员查询分支。客户端显示按钮不替代服务端权限和状态校验。

下单保存实际采用的优惠和必要计算因子；售后生成时冻结退款与权益方案。后续审批、退款重试、权益补偿不得按当前配置重新计算。现金成功与权益任务失败是不同事实。

## 格式与注释

Java使用固定`google-java-format 1.25.2`的AOSP四空格格式。导入分为项目内部、第三方、Lombok/Hutool工具、JDK/Jakarta、静态导入，各组字典序。格式化保留长字符串和既有Javadoc正文，不修改SQL、JSON、正则或消息文本来凑行宽。具体参数以`scripts/format_java.py`为准；工具来源见[官方说明](https://github.com/google/google-java-format)。

TS/TSX/JS/CJS/MJS/JSON/CSS/WXSS使用管理端已有Prettier配置：两空格、单引号、分号、100列和LF。小程序复用配置，不新增npm运行依赖。WXML使用`scripts/format_wxml.py`，只统一标签行缩进，不使用HTML格式化器改写正文、指令、属性和嵌入表达式。XML/SQL/Python采用四空格；复杂SQL按子句换行。`.editorconfig`统一编码和基础缩进。

接口、方法和复杂规则使用中文注释，说明为什么、业务约束和兼容原因。避免“查询查询”式占位说明，不用当前数据猜测缺失注释的业务含义。方法保持单一职责，不为一次使用新增通用框架或工具层。

## 两端约定

管理端严格TypeScript，不以any、ts-ignore、eslint-disable或删除测试避错。服务端状态使用TanStack Query，Query Key集中；Zustand只放客户端全局状态。API模块只处理请求和契约，不操作Modal、跳转或消息提示。独立查询的错误不能覆盖原详情；异步交互有loading并避免重复提交。

小程序保留原生CommonJS/Page结构及会员会话校验。请求中会员切换后拒绝旧响应；失败重试复用原请求号。页面不计算退款和尾差，不自动开启原本关闭的业务入口。Node测试、微信原生编译、支付渠道和真机验收分别留证。

## 验证命令

从项目根目录执行。JDK使用17，Node/Yarn使用README约定版本。

```bash
python3 scripts/check_architecture.py
python3 -m unittest discover -s scripts -p 'test_*.py'
python3 lemnt-server/sql/schema_comments.py
python3 -m unittest discover -s lemnt-server/sql -p 'test_*.py'
python3 scripts/format_wxml.py
JAVA_HOME=/path/to/jdk17 python3 scripts/format_java.py --jar /path/to/google-java-format-1.25.2-all-deps.jar
```

格式化工具下载为独立开发工具，不加入业务依赖。固定下载地址：`https://repo.maven.apache.org/maven2/com/google/googlejavaformat/google-java-format/1.25.2/google-java-format-1.25.2-all-deps.jar`；SHA256为`25157797a0a972c2290b5bc71530c4f7ad646458025e3484412a6e5a9b8c9aa6`。检查器自动验证校验和；加`--write`才写回Java或WXML文件。

```bash
mvn -f lemnt-common/pom.xml install
mvn -f lemnt-server/pom.xml package
```

需要显式Mockito agent的环境可在argLine传入当前Mockito依赖对应的JAR；Excel相关测试使用`-Djava.awt.headless=true`。不要把个人机器的绝对工具路径提交到POM。

```bash
cd lemnt-ui
yarn typecheck
yarn lint
yarn format:check
yarn test
yarn build
```

```bash
cd lemnt-miniapp
npm test
../lemnt-ui/node_modules/.bin/prettier --check '**/*.{js,cjs,mjs,json,wxss}'
```

静态检查覆盖明确的源码导入/POM依赖、Java全限定类型引用、HTTP模型Schema、注解SQL、Mapper默认方法、SQL片段和锁缓存规则；它不替代编译器、人工业务审查、安全审计和真实MySQL验证。类型短名规则同时检查测试源码。新增例外必须说明具体兼容原因，不增加无解释的忽略清单。

交付记录说明实际文件范围、行为变化、检查命令与结果、未验证层及迁移影响。批量格式化与业务修复分别说明依据，不自动commit或push。
