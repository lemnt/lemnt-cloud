# lemnt-cloud

通用微服务框架后端项目。基于 Spring Cloud 微服务架构，使用 CQRS+DDD 六层分包模式。

---

## 模块结构

```
lemnt-server
├── lemnt-gateway         网关模块 [8080]
├── lemnt-auth            认证中心 [9200]
│   ├── lemnt-auth-adapter / application / domain / infrastructure / start
├── lemnt-system          系统管理 [9100]
│   ├── lemnt-system-client         Dubbo 接口定义（DTO/Bo，包名仍为 com.lemnt.system.api）
│   ├── lemnt-system-adapter        Web Controller 层
│   ├── lemnt-system-application    应用层（CQRS）
│   ├── lemnt-system-domain         领域层
│   ├── lemnt-system-infrastructure 基础设施层
│   └── lemnt-system-start          启动引导
├── lemnt-open            开放平台 [9300]
│   └── ...（无 client 模块）
└── lemnt-third-sdk       第三方 SDK
    └── demo-sdk           HTTP 调用示例
```

**注意：** 接口定义模块统一命名为 `xxx-client`，不是 `xxx-api`。lemnt-auth 与 lemnt-open 当前没有 client 模块。

**Dubbo 服务实现位置：** `@DubboService` 实现类放在 **application 层** 的 `application/dubbo/` 包下，不在 infrastructure
层。

---

## 技术栈

Spring Boot 4.1.0 + Spring Cloud 2025.1.2 + Spring Cloud Alibaba 2025.1.0.0 +
Spring Cloud Gateway (WebFlux) + Apache Dubbo + Nacos + MyBatis-Plus + MySQL + Redis +
RabbitMQ/RocketMQ + Sentinel + XXL-Job

---

## CQRS 架构规范（核心）

### 当前基准与存量代码

- `lemnt-system` 是新增业务模块的主要代码风格基准：Adapter、Cmd、Qry 以及适合不可变表达的值对象优先使用 Java `record`
- `lemnt-auth`、`lemnt-open` 以及 `lemnt-system` 的 Adapter Req/Res、Application Cmd/Qry 已按相同方向使用 `record`
- `lemnt-system` 的 DTO 等需要框架映射或可变组装的对象仍保留普通类；不做与需求无关的机械迁移
- 新增功能不得因为存量代码存在而继续复制已经废弃的分层方式

### 应用层分层

```
lemnt-{module}-application/{domain}/
  cmd/         命令对象（写操作入参）
  qry/         查询对象（读操作入参）
  dto/         应用层输出对象
  executor/    执行器（业务逻辑）
  service/     跨 Executor 复用的应用编排（按需）
```

### 命令（Command）

Cmd 是 Application 层写操作的输入契约，由 Adapter 层的 Req 转换得到，不直接作为 HTTP 请求体。

**命名：** `{聚合}{动词}Cmd`（ **聚合在前**）

```java
// Application 层命令：不可变数据载体，不放 HTTP、Swagger 或校验注解
public record AdSlotAddCmd(
        String adSlotName,
        Integer adSlotType,
        Integer width,
        Integer height,
        String remark
) implements Serializable {
}
```

**执行器命名：** `{聚合}{动词}CmdExe`

```java
// 执行器
executor/ConfigAddCmdExe.java                 // 新增配置
executor/AdvertiserUpdateCmdExe.java          // 修改广告主
executor/AdvertiserDeleteCmdExe.java          // 删除广告主
executor/AdSlotBatchUpdateStatusCmdExe.java   // 批量更新广告位状态
```

**规范：**

- 新增 Cmd 优先使用 `record`，仅包含应用执行所需字段
- Cmd 不添加 `@Schema`、`@NotBlank`、`@Size` 等 HTTP/文档/Bean Validation 注解；这些注解属于 Adapter 的 Req
- Executor 只接收 Cmd，不接收 DO/Bo
- Cmd 不含业务逻辑，纯数据容器
- 操作方法统一使用 `execute`
- **单字段入参可不封装 Cmd**：如按 ID 删除，Executor 可直接 `execute(Long id)`，无需包装为 `DeleteXxxCmd`

### 查询（Query）

Qry 是 Application 层读操作的输入契约，由 Adapter 层的 Req 转换得到，不直接承载 HTTP 参数绑定。

**命名：** `{查询对象}{查询类型}Qry`

```java
public record AdSlotPageQry(
        String adSlotName,
        Integer adSlotType,
        Integer status,
        Integer pageNum,
        Integer pageSize
) implements Serializable {
}
```

**执行器命名：** `{聚合}{查询类型}QueryExe`

```java
// 执行器
executor/ConfigPageQueryExe.java      // 分页查询配置
executor/ConfigDetailQueryExe.java    // 查询单个配置
executor/DeptListQueryExe.java        // 查询部门列表
executor/DeptTreeQueryExe.java        // 查询部门树
executor/AdSlotEnableListQueryExe.java // 查询启用的广告位列表
```

**查询类型：**

| 查询类型 | 用途     | Qry 示例          | 返回                    |
|----------|----------|-------------------|-------------------------|
| `Page`   | 分页查询 | `ConfigPageQry`   | `PageResult<ConfigDTO>` |
| `Detail` | 单个详情 | 单个 ID 可直接传参 | `ConfigDTO`           |
| `List`   | 列表查询 | `DeptListQry`     | `List<DeptDTO>`         |
| `Tree`   | 树结构   | `DeptTreeQry`     | `List<TreeSelect>`      |

`PageResult` 位于 `lemnt-common-domain`（`com.lemnt.common.domain.model.PageResult`）。

**规范：**

- Qry 命名 = `{聚合}{查询类型}Qry`， **不加动词前缀**（如 `Select`、`Get`）
- 新增 Qry 优先使用 `record`，只包含查询参数，不含业务逻辑和 Adapter 注解
- Executor 接收 Qry， **返回 DTO**（应用层输出对象是 `DTO`，不是 `Vo`）
- 分页 Qry 直接包含 `pageNum`/`pageSize` 字段，不继承公共分页对象

### 执行器（Executor）规范

```java
@Component
@RequiredArgsConstructor
public class ConfigAddCmdExe {

    private final ConfigGateway configGateway;

    // 主执行方法
    public Boolean execute(ConfigAddCmd cmd) {
        // 1. 参数校验 / 业务校验
        // 2. Cmd → Entity 转换
        // 3. 调用 Gateway 持久化
        // 4. 返回结果
    }

    // Cmd → Entity 转换（私有方法，不对外暴露）
    private Config toEntity(ConfigAddCmd cmd) { ... }
}
```

**依赖原则：**

- Executor 可注入 Domain 层 Gateway、Domain Service，以及 Application 层的校验器/编排服务
- Executor 不得直接依赖 Infrastructure 层的任何组件
- 远程调用（Dubbo）、缓存（Redis）、消息队列（MQ）等基础设施，全部 **封装在 GatewayImpl 中**，Gateway 接口返回领域模型结果，Executor
  不感知实现细节
- 写操作通常返回 `Boolean`，需要回传数据时返回 DTO 或明确的应用结果对象；不得返回 Entity 或 DO

---

## Controller 层规范

### 职责

Controller 只负责：

1. 接收并校验 Adapter 层 `Req`
2. 通过 `ReqConverter` 转换为 Application 层 `Cmd/Qry`
3. 调用 Executor 执行
4. 通过 `ResConverter` 将 DTO 转换为 `Res`，再包装为 `R`

```java
@PostMapping
public R<Boolean> add(@Validated @RequestBody AdSlotAddReq req) {
    return R.ok(adSlotAddCmdExe.execute(AdSlotReqConverter.toAddCmd(req)));
}

@GetMapping("/{adSlotId}")
public R<AdSlotRes> detail(@PathVariable Long adSlotId) {
    return R.ok(AdSlotResConverter.toRes(adSlotDetailQueryExe.execute(adSlotId)));
}
```

**说明：**

- Controller 不接收 Cmd/Qry、Bo、DTO、Entity 或 DO；HTTP 入参统一使用 Req，简单路径参数和 ID 集合可直接接收
- Req → Cmd/Qry 使用 `{聚合}ReqConverter`，DTO → Res 使用 `{聚合}ResConverter`
- 金额元/分换算、JSON 字符串/集合转换等边界格式转换放在 Adapter Converter，不放在 Controller 或 Executor
- Bo/跨服务 DTO 只用于 Dubbo 契约

### Req/Res 与 Swagger 规范

- 新增 Req/Res 优先使用 `record` 并实现 `Serializable`
- 每个 Req/Res 类型必须添加类级 `@Schema(description = "...")`；对需要固定模型名的类型可同时指定 `name`
- Req/Res 的每个对外字段或 record component 必须添加 `@Schema(description = "...")`；枚举值、单位、格式不直观时在描述中写清，并按需提供 `example`
- Bean Validation 注解只放在 Req：如 `@NotNull`、`@NotBlank`、`@NotEmpty`、`@Size`
- Controller 必须使用 `@Tag`，公开接口方法必须使用 `@Operation`
- Application 的 Cmd/Qry/DTO、Domain 模型、Infrastructure DO 不添加 Swagger 注解

```java
@Schema(description = "广告位新增请求")
public record AdSlotAddReq(
        @Schema(description = "广告位名称", example = "首页横幅")
        @NotBlank(message = "广告位名称不能为空")
        String adSlotName,
        @Schema(description = "广告位类型")
        @NotNull(message = "广告位类型不能为空")
        Integer adSlotType
) implements Serializable {
}
```

### 每层对象职责

| 层                             | 入参          | 出参   | 转换方式                                            |
|--------------------------------|---------------|--------|-----------------------------------------------------|
| **Adapter**（HTTP）            | `Req`         | `Res`  | ReqConverter / ResConverter                        |
| **Application**（CQRS）        | `Cmd` / `Qry` | `DTO`  | Executor 输入输出契约                              |
| **Domain**（领域）             | Domain Model  | Domain Model | 领域行为与契约，无 Web/持久化注解              |
| **Infrastructure**（基础设施） | Domain Model  | Domain Model | 内部使用 DO/第三方对象，通过 Converter 隔离    |
| **Client / Dubbo**             | `Bo`          | `DTO`  | 跨服务 RPC 契约，独立设计                          |

**绑定规则：**

```text
HTTP Req → Cmd/Qry → Executor → DTO → HTTP Res
Application → Domain → Cmd/DTO ↔ Domain Model 按需转换
Domain ↔ Infrastructure → Domain Model ↔ DO/第三方对象
Dubbo 接口 → Bo/DTO 独立设计
```

**禁止：**

- Adapter 层直接使用 DO、Entity
- Application 层直接使用 DO；普通 Executor 不得以 Dubbo Bo 作为业务输入
- Domain 层依赖 MyBatis-Plus 或其他框架注解
- Infrastructure 的 DO 被上层直接返回（必须通过 Converter 转换为 Domain Model，再由 Application 输出 DTO）

### 命名

| 类型           | 规则                                        | 示例                 | 所属模块                              |
|----------------|---------------------------------------------|----------------------|---------------------------------------|
| Controller     | `{聚合}Controller`                          | `RoleController`     | adapter                               |
| HTTP 请求      | `{聚合}{动词}Req`                           | `AdSlotAddReq`       | adapter                               |
| HTTP 响应      | `{聚合}Res`                                 | `AdSlotRes`          | adapter                               |
| Req/Res 转换器 | `{聚合}ReqConverter` / `{聚合}ResConverter` | `AdSlotReqConverter` | adapter                               |
| 命令对象       | `{聚合}{动词}Cmd`                           | `ConfigAddCmd`       | application                           |
| 查询对象       | `{聚合}{查询类型}Qry`                       | `ConfigPageQry`      | application                           |
| 传输对象       | `{聚合}DTO`                                 | `ConfigDTO`          | application（`{domain}/dto/`）        |
| 命令执行器     | `{聚合}{动词}CmdExe`                        | `ConfigAddCmdExe`    | application                           |
| 查询执行器     | `{聚合}{查询类型}QueryExe`                  | `ConfigPageQueryExe` | application                           |
| 领域实体       | `{聚合}`                                    | `Config`             | domain                                |
| 数据对象       | `{聚合}DO`                                  | `ConfigDO`           | infrastructure                        |
| Dubbo 接口     | `Remote{聚合}Service`                       | `RemoteUserService`  | `-client` 模块（包名 `com.lemnt.*.api`） |
| Dubbo 出参     | `{聚合}DTO`                                 | `UserInfoDTO`        | `-client` 模块                        |
| Dubbo 入参     | `{聚合}Bo`                                  | `UserBo`             | `-client` 模块                        |

**命名要点（易错）：**

- 命令与执行器一律 **聚合在前、动词在后**：`ConfigAddCmd`，不是 `CreateConfigCmd`
- 查询执行器是 **`QueryExe`**，不是 `QryExe`（查询对象才是 `Qry`）
- HTTP 响应是 **`Res`**，不是 `Resp`
- 应用层输出对象统一使用 **`DTO`**；`lemnt-open` 的 `IndexVo` 是存量命名，新代码不新增 `Vo`
- 新增 Req/Res/Cmd/Qry 等纯数据载体优先使用 `record`；DTO、Entity、DO 是否使用普通类由可变性和映射需求决定

---

## 领域层规范

### 分层依赖

```
Controller → Executor(Cmd/Qry) → Gateway(domain 接口) → GatewayImpl(infrastructure 实现)
```

- **Controller**（adapter 层）：接收请求，组装 Cmd/Qry，调用 Executor
- **Executor**（application 层）：业务编排，依赖 Domain Gateway/Service 或 Application 内部协作者
- **Gateway 接口**（domain 层）：定义领域契约
- **GatewayImpl**（infrastructure 层）：实现持久化、缓存、RPC 等基础设施逻辑
- **Entity**（domain 层）：纯领域对象，不依赖任何框架

**核心约束：** 箭头方向不能反向。application 层不能直接引入 infrastructure 层的任何类（RedisTemplate、Mapper、MQ 等）。

### Domain Model 规范

```java
@Data
@Accessors(chain = true)
public class Config implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long configId;
    private String configName;
    private String configKey;
    // ...
}
```

- 领域模型位于 `domain/{domain}/model`；`lemnt-auth` 现有 `entity` 包属于存量结构
- 聚合根、需要状态变化或承载领域行为的实体使用普通类；不可变且按值比较的值对象优先使用 `record`
- Domain Model 只包含领域字段和领域行为，不含 HTTP、Swagger、持久化注解
- 不使用 `@TableName`、`@TableId` 等 MyBatis-Plus 注解，这些注解只放在 Infrastructure DO
- `LoginUser` 是会话状态模型，登录时间、过期时间、Token 等字段需要刷新，因此保留可变类
- `UserInfo` 承载 `isDisabled`、`verifyPassword` 等领域行为，因此保留领域类
- `UserInfoRole` 语义上是值对象，但当前 MapStruct 映射依赖 setter；在映射链整体调整前保留类，不做单文件机械迁移

### Domain Service 规范

- Domain Service 只承载需要 Entity 或 Gateway 协作的领域规则，保持纯 Java，不使用 Spring 注解
- 需要注入的 Domain Service 由 application 层的 `configuration` 包通过 `@Bean` 注册
- 菜单树、部门树、前端路由等查询结果编排属于 application 层，不放在 domain 层

---

## 包命名规范

### Java 包

```
com.lemnt.{module}.application.{domain}.cmd       // 命令对象
com.lemnt.{module}.application.{domain}.qry       // 查询对象
com.lemnt.{module}.application.{domain}.executor  // 执行器
com.lemnt.{module}.application.{domain}.service   // 查询结果编排等应用服务
com.lemnt.{module}.adapter.web.{domain}            // Controller
com.lemnt.{module}.adapter.web.{domain}.req        // HTTP 请求对象
com.lemnt.{module}.adapter.web.{domain}.res        // HTTP 响应对象
com.lemnt.{module}.adapter.web.{domain}.converter  // HTTP 边界转换器
com.lemnt.{module}.domain.{domain}.model           // 领域模型（当前推荐）
com.lemnt.{module}.domain.{domain}.gateway         // 领域 Gateway 接口
com.lemnt.{module}.domain.{domain}.service         // 纯 Java Domain Service
com.lemnt.{module}.infrastructure.{domain}.dataobject // DO
com.lemnt.{module}.infrastructure.{domain}.mapper     // MyBatis-Plus Mapper
com.lemnt.{module}.infrastructure.{domain}.gateway    // Gateway 实现
com.lemnt.{module}.infrastructure.{domain}.converter  // DO/远程 DTO 与领域模型转换
```

Controller 当前直接放在 `adapter.web.{domain}` 包下，不额外创建 `controller` 子包。`lemnt-auth` 的
`adapter.web.auth.controller` 是存量目录，修改时保持局部一致即可。

### 文件命名

| 模式                       | 示例                         |
|----------------------------|------------------------------|
| `{聚合}{动词}CmdExe`       | `ConfigAddCmdExe`            |
| `{聚合}{查询类型}QueryExe` | `ConfigPageQueryExe`         |
| `{聚合}{动词}Cmd`          | `ConfigAddCmd`               |
| `{聚合}{查询类型}Qry`      | `ConfigPageQry`              |
| `{聚合}DTO`                | `ConfigDTO`                  |
| `{聚合}Res` / `{聚合}Req`  | `AdSlotRes` / `AdSlotAddReq` |

---

## POM 规范

### 模块依赖链

依赖是 **树状**的，不是一条线性链。`application` 与 `infrastructure` 是兄弟，两者都依赖 `domain`，彼此 **不互相依赖**。

```
                        start
                       ╱     ╲
                adapter       infrastructure
                   │               │
             application           │
                    ╲             ╱
                     ╲           ╱
                       domain
                          │
                        client
```

以 lemnt-system 为例的实际依赖：

| 模块                    | 依赖的内部模块                                                  |
|-------------------------|-----------------------------------------------------------------|
| `lemnt-system-client`         | 无（仅 `lemnt-common-client`）                                     |
| `lemnt-system-domain`         | `lemnt-system-client`、`lemnt-common-domain`、`lemnt-common-core`           |
| `lemnt-system-application`    | `lemnt-system-domain`、`lemnt-system-client`、`lemnt-common-application`       |
| `lemnt-system-infrastructure` | `lemnt-system-domain`、`lemnt-common-mybatis`、`lemnt-common-datasource` 等 |
| `lemnt-system-adapter`        | `lemnt-system-application`、`lemnt-common-adapter`                       |
| `lemnt-system-start`          | `lemnt-system-adapter`、`lemnt-system-infrastructure`、`lemnt-common-start`    |

**依赖方向：** 箭头单向， **`application` 绝不依赖 `infrastructure`**。

**各模块职责与依赖：**

| 模块                 | 依赖                         | 说明                                                               |
|----------------------|------------------------------|--------------------------------------------------------------------|
| `xxx-client`         | 无内部依赖                   | `Remote*Service` Dubbo 接口、DTO、Bo（包名 `com.lemnt.{module}.api`） |
| `xxx-domain`         | `client`                     | Entity、Gateway 接口                                               |
| `xxx-infrastructure` | `domain`                     | GatewayImpl、DO、Mapper                                            |
| `xxx-application`    | `domain`                     | Cmd/Qry、Executor、`@DubboService` 实现                            |
| `xxx-adapter`        | `application`                | Controller、Req/Res                                                |
| `xxx-start`          | `adapter` + `infrastructure` | Spring Boot 启动类                                                 |

**注意：** 接口定义模块叫 `xxx-client`，不是 `xxx-api`。lemnt-auth 与 lemnt-open 目前没有 client 模块。

### POM 书写规范

```xml
<!-- 1. 依赖按模块分组，组内按字母排序 -->
<dependencies>
    <!-- 内部模块 -->
    <dependency>
        <groupId>com.lemnt</groupId>
        <artifactId>lemnt-xxx-client</artifactId>
    </dependency>

    <!-- Spring / 框架 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- 工具 -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

**禁止：**

- 模块间循环依赖（如 adapter 依赖 application，application 不能反向依赖 adapter）
- `xxx-start` 模块依赖非启动必要的模块（如 mapper、redis 只放在 infrastructure）
- 子模块遗漏 `lombok` 依赖

---

## 代码格式规范

### 换行

```java
// ✅ 多字段换行对齐，每行一个字段
private String configName;
private String configKey;
private String configValue;

// ✅ 方法链换行缩进 4 空格
user.setDeptId(cmd.getDeptId())
    .setUserName(cmd.getUserName());

// ✅ 方法参数过多时换行
public IPage<Config> execute(
        ConfigPageQry qry,
        PageQuery pageQuery) {
```

```java
// ❌ 禁止单行多语句
data.setDictLabel(cmd.getDictLabel()); data.setDictValue(cmd.getDictValue()); data.setDictType(cmd.getDictType());

// ❌ 禁止方法声明写在一行超过 120 字符
```

### 空行

```java
// ✅ 方法之间空 1 行
public Boolean execute(ConfigAddCmd cmd) {
    return true;
}

public Boolean execute(ConfigUpdateCmd cmd) {
    return true;
}

// ✅ import 分组空 1 行
import com.lemnt.system.domain.config.gateway.ConfigGateway;
import com.lemnt.common.core.constant.CacheConstants;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
```

### import 顺序

```
1. 内部模块（com.lemnt.*）
2. 第三方框架（org.springframework.*, org.apache.*）
3. 工具库（lombok.*, cn.hutool.*）
4. 标准库（java.*, jakarta.*）
各组之间空 1 行。
```

---

## 配置与安全

### JWT 密钥（启动强依赖）

JWT 签名密钥 **不在代码中硬编码**，由 `TokenProperties`（`security.token.secret`）注入，并由 `TokenConfig` 注册 `JwtUtils`
Bean。 **密钥缺失或不足 64 字节时应用启动直接失败**（HS512 要求 512 位密钥）。

- 配置键：`security.token.secret`，或环境变量 `SECURITY_TOKEN_SECRET`
- **lemnt-auth（签发）与 lemnt-gateway（校验）必须配置同一个密钥**，否则所有令牌校验失败
- 密钥轮换会使所有已登录用户立即失效
- 令牌 **不携带 `exp` 声明**，有效期由 Redis 会话控制（30 天 TTL，即 2592000 秒）。网关每次请求都校验
  Redis；后端用户上下文拦截器在剩余不超过 120 分钟（7200 秒）时续期至 30 天，登出即删除会话键
- Redis TTL 与登录响应 `expires_in`（HTTP 字段 `expiresIn`）统一使用秒；该响应值表示本次签发的会话时长，
  后续自动续期以 Redis 会话为准，不是 JWT 的固定到期时间。`loginTime` / `expireTime` 为 Unix 毫秒时间戳

### 数据权限

在实际执行 SQL 的 Mapper 方法上标注 `@DataScope`（不能标在 Gateway 实现方法上），
`CustomDataPermissionHandler` 根据 MyBatis 的 mappedStatementId 读取 Mapper 及其继承方法上的注解，
按角色 `sys_role.data_scope` 动态改写 SQL 的 WHERE 条件：

| 值 | 含义         | 生成的条件                                                                                   |
|----|--------------|----------------------------------------------------------------------------------------------|
| 1  | 全部数据     | 不过滤                                                                                       |
| 2  | 自定义       | `d.dept_id IN (SELECT dept_id FROM sys_role_dept WHERE role_id IN (...))`                    |
| 3  | 本部门       | `d.dept_id = {deptId}`                                                                       |
| 4  | 本部门及以下 | `d.dept_id IN (SELECT dept_id FROM sys_dept WHERE dept_id = ? OR FIND_IN_SET(?, ancestors))` |
| 5  | 仅本人       | `u.user_id = {userId}`                                                                       |

- 多角色时各数据范围取 **并集**；任一角色为「全部数据」则不过滤
- 部门/用户维度分别依赖注解的 `deptAlias` / `userAlias`， **未配置对应别名时该维度不生成条件**（避免多表关联下列名歧义）
- 无可用角色或无法生成任何有效范围条件时返回零条记录；未获取登录用户上下文时拒绝查询
- 数据权限拦截器必须位于分页拦截器之前，确保查询记录与分页总数使用同一范围；XML 不再拼接 `${dataScope}` SQL 片段
- SQL 集成测试使用生产 Mapper/XML、实际拦截器链和 H2 MySQL 模式；`FIND_IN_SET` 由测试别名模拟，仍需在目标 MySQL 验证方言行为

### 数据库脚本（sql 目录）

| 文件                | 内容                                                                                        |
|---------------------|---------------------------------------------------------------------------------------------|
| `lemnt_system.sql`     | 系统库表结构（`sys_*`）与初始化数据，**建表使用 DROP + CREATE**                             |

### 单元测试

测试放在各模块的 `src/test/java`，需在 pom 中声明 `spring-boot-starter-test`（test scope）。约定：

- 数据权限：断言生成的条件表达式内容
- JWT：断言签名校验、篡改拒绝、密钥隔离
- 配置类：用 `ApplicationContextRunner` 验证启动期的失败/成功

---

## 禁止事项

- **Application 层直接依赖 Infrastructure 层**（如 Executor 注入 RedisTemplate、Mapper 等）
- Controller 直接调用 Gateway 或 Mapper
- Executor 之间互相调用；需要复用逻辑时提取 Application Service、校验器或 Domain Service
- Cmd/Qry 包含业务逻辑（纯数据容器）
- Entity 包含持久化注解或继承 MyBatis-Plus 基类
- 多字段写入/查询直接以散参数传递；应封装为 Cmd/Qry（单 ID、ID 集合等简单参数除外）
