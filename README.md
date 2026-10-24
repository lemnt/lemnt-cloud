# lemnt-cloud

基于 Spring Cloud 的通用微服务开发框架，保留 CQRS/DDD 分层，提供统一网关、认证、系统管理及公共组件，作为业务系统的开发底座。

当前仓库包含 Java 后端工程和 React 管理前端。第三方 SDK 仅保留一个通用 HTTP 调用示例。

## 项目结构

```text
lemnt-cloud
├── lemnt-ui                    React 管理前端
├── lemnt-dependencies          统一依赖版本、构建插件和环境配置
├── lemnt-common                通用组件
│   ├── lemnt-common-core        基础工具与通用常量
│   ├── lemnt-common-security    认证、令牌和权限支持
│   ├── lemnt-common-datascope   数据权限
│   ├── lemnt-common-mybatis     持久化支持
│   ├── lemnt-common-dubbo       RPC 支持
│   └── ...                     Redis、日志、文件、消息等组件
└── lemnt-server                服务聚合工程
    ├── lemnt-gateway           网关
    ├── lemnt-auth              认证中心
    ├── lemnt-system            系统管理
    ├── lemnt-open              开放接口示例
    ├── lemnt-third-sdk
    │   └── demo-sdk            Java HTTP Client 调用示例
    ├── sql
    │   ├── lemnt_system.sql    系统库初始化脚本
    │   └── nacos              Nacos 配置导出包
    └── AGENTS.md               项目开发规范
```

## 技术栈

以下框架版本来自当前依赖管理文件，Nacos Server 版本为项目约定的运行环境版本。

| 技术 | 版本 / 用途 |
| --- | --- |
| 前端 | React 19、TypeScript、Vite 8、Ant Design 6、Pro Components |
| 前端运行环境 | Node.js >=22.13.0 且 <23、Yarn 1.22.22 |
| Java | 17，构建规则限定 Java 17 |
| Spring Boot | 4.1.0 |
| Spring Cloud | 2025.1.2 |
| Spring Cloud Alibaba | 2025.1.0.0 |
| Nacos Server | 3.2.1，服务注册与配置管理 |
| Apache Dubbo | 3.3.6，服务间 RPC |
| MyBatis-Plus | 3.5.17 |
| MySQL / Redis | 数据存储与会话、缓存 |
| Spring Cloud Gateway | WebFlux 网关 |
| 其他组件 | Sentinel、消息队列、MinIO、XXL-Job 等，按实际模块配置准备 |

## 基础能力

- 统一网关、验证码、令牌校验和请求过滤。
- 登录、登出、令牌刷新及 Redis 会话管理。
- 用户、角色、菜单及接口权限管理。
- 部门、岗位和数据权限。
- 字典、系统参数、通知公告、操作日志和登录日志。
- 文件上传、Excel、缓存、消息和第三方调用等组件支持。

上述为代码提供的能力，完整使用仍需配置中间件，并进行实际联调。

## 架构约定

业务模块按职责拆分，`client` 按跨服务调用需要提供；认证和开放平台目前没有独立的 `client` 模块。

| 分层 | 职责 |
| --- | --- |
| client | Dubbo 接口及跨服务 DTO、Bo |
| adapter | HTTP Controller、Req/Res 与边界转换 |
| application | Cmd/Qry、Executor、应用编排及 Dubbo 服务实现 |
| domain | 领域模型、领域服务和 Gateway 接口 |
| infrastructure | Gateway 实现、持久化、缓存及远程调用 |
| start | 服务启动与组件装配 |

调用链为 `Controller → Executor → Domain Gateway → GatewayImpl`。`application` 与 `infrastructure` 都依赖 `domain`，两者不互相依赖。

详细规范见 [AGENTS.md](lemnt-server/AGENTS.md)。

## 本地运行

### 1. 准备环境

安装 JDK 17、Maven 3.x，并准备 Nacos Server 3.2.1、MySQL 和 Redis。其他中间件是否需要启动，取决于 Nacos 配置与所启用组件。

Nacos 3.x 默认使用 `8080` 作为控制台端口，`8848` 作为服务主端口，`9848` 作为客户端 gRPC 端口。应用配置 `NACOS_PORT=8848`，并确保应用能够访问 gRPC 端口。控制台与本项目网关都默认使用 8080，同机部署时需要调整其中一个监听或映射端口。参见 [Nacos 官方部署说明](https://www.nacos.io/docs/latest/manual/admin/deployment/deployment-best-practices/)。

### 2. 初始化系统数据库

创建独立的系统数据库，并在该数据库中导入 [lemnt_system.sql](lemnt-server/sql/lemnt_system.sql)。

**脚本包含 `DROP TABLE`，只应用于新建或确认可以重建的数据库，不能直接用于已有业务库升级。** 数据库连接信息在 Nacos 的对应配置中设置。

### 3. 导入 Nacos 配置

配置包位于 [nacos_config_export.zip](lemnt-server/sql/nacos/nacos_config_export.zip)。导入前检查并替换数据库、Redis、文件存储、消息服务等环境地址及凭据。

当前默认使用 `dev` 命名空间 ID 和 `DEFAULT_GROUP`。先创建匹配的命名空间，再导入以下配置：

| Data ID | 用途 |
| --- | --- |
| `lemnt-common.yaml` | 公共配置 |
| `lemnt-system.yaml` | 系统管理配置 |
| `lemnt-auth.yaml` | 认证配置 |
| `lemnt-gateway.yaml` | 网关路由等配置 |
| `sentinel-lemnt-gateway` | 网关流控规则 |

开放平台配置入口还会读取 `lemnt-open.yaml`，当前导出包未提供该文件；需要使用该服务时，按其组件依赖补齐配置。

本地应用读取的环境变量如下：

| 变量 | 说明 |
| --- | --- |
| `NACOS_HOST` | Nacos 地址；本地运行时建议明确设置为 `127.0.0.1` |
| `NACOS_PORT` | 服务主端口，默认 `8848` |
| `NACOS_USERNAME` | Nacos 用户名 |
| `NACOS_PASSWORD` | Nacos 密码 |
| `SECURITY_TOKEN_SECRET` | JWT 签名密钥，UTF-8 编码至少 64 字节 |
| `GATEWAY_CORS_ALLOWED_ORIGINS` | 网关允许的前端跨域来源，多个来源用英文逗号分隔；默认不允许跨域 |

认证服务与网关必须使用相同 JWT 密钥。缺失或长度不足会导致相关服务启动失败。也可通过配置项 `security.token.secret` 提供密钥；不要将实际密钥提交到仓库。

在终端或 IDE 启动配置中设置环境变量。项目未配置自动读取 `.env` 文件。

登录会话有效期统一为 30 天（2592000 秒），Redis TTL 与登录响应 `expiresIn`（通用 Token 服务字段 `expires_in`）均使用秒。响应值表示本次签发的会话时长；后端处理认证请求时，剩余不超过 120 分钟（7200 秒）会续期至 30 天，后续有效期以 Redis 会话为准。JWT 不设置固定 `exp`，Redis 会话过期或登出删除后即失效。内部 `loginTime` / `expireTime` 分别为最近签发或续期时间、当前到期时间，使用 Unix 毫秒时间戳。原 `expiresIn` 使用分钟，调用方如有倒计时或过期换算，需要同步改为秒。

前端直接跨域访问网关时，设置 `GATEWAY_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173`；生产环境替换为实际前端来源（协议、主机及端口，不含路径）。也可通过网关配置项 `gateway.cors.allowed-origins` 设置白名单。允许携带凭据，不允许使用 `*`；预检显式允许 `Authorization`、`Content-Type`、`Accept` 请求头。使用 Vite 代理或同源反向代理时无需为前端开启跨域。

### 4. 构建

仓库根目录不提供聚合 `pom.xml`。在仓库根目录按顺序执行，先将依赖管理和公共组件安装到本地 Maven 仓库，再构建服务：

```bash
mvn -f lemnt-dependencies/pom.xml clean install -DskipTests
mvn -f lemnt-common/pom.xml clean install -DskipTests
mvn -f lemnt-server/pom.xml clean package -DskipTests
```

完成依赖管理和公共组件安装后，构建指定服务及其在服务工程内的依赖：

```bash
mvn -f lemnt-server/pom.xml -pl lemnt-system/lemnt-system-start -am package -DskipTests
```

默认构建环境为 `dev`，另有 `test`、`uat`、`prod`，例如 `mvn -f lemnt-server/pom.xml clean package -Ptest -DskipTests`。当前配置通过 Maven 替换 `@profile.active@`，同时用于 Spring Profile 和 Nacos 命名空间；切换环境时，上述各步均应使用相同的目标 Profile 重新构建。

### 5. 启动服务

在分别设置好环境变量的终端中启动，建议先启动系统管理，再启动认证和网关：

```bash
java -jar lemnt-server/lemnt-system/lemnt-system-start/target/lemnt-system-start.jar
java -jar lemnt-server/lemnt-auth/lemnt-auth-start/target/lemnt-auth-start.jar
java -jar lemnt-server/lemnt-gateway/target/lemnt-gateway.jar
```

每条命令会持续运行，需要使用独立终端或进程管理工具。开放接口示例按需启动：

```bash
java -jar lemnt-server/lemnt-open/lemnt-open-start/target/lemnt-open-start.jar
```

| 服务 | 默认端口 |
| --- | --- |
| lemnt-gateway | 8080 |
| lemnt-system | 9100 |
| lemnt-auth | 9200 |
| lemnt-open | 9300 |

检查服务日志及 Nacos 注册状态，确认服务处于同一命名空间。网关路由配置位于 Nacos，配置包使用 `/auth/**`、`/system/**`、`/open/**` 前缀转发请求。

## 前端运行与部署

前端工程位于 [lemnt-ui](lemnt-ui)，独立使用 yarn 构建，不参与 Maven 聚合构建。

```bash
cd lemnt-ui
yarn install --frozen-lockfile
yarn dev
```

开发地址默认为 `http://localhost:5173`。`.env.development` 使用 `VITE_APP_BASE_API=/dev-api`，Vite 将 `/dev-api` 请求转发到 `http://localhost:8080`，并移除该前缀。先启动后端网关及其依赖服务；如果网关改了端口，同步修改 `lemnt-ui/vite.config.ts` 中的代理目标。

生产构建：

```bash
cd lemnt-ui
yarn build
```

产物位于 `lemnt-ui/dist`。生产接口前缀为 `/prod-api`，需要由部署层反向代理至网关，并移除该前缀；Vite 开发代理不会进入生产构建。静态站点还需配置前端路由回退到 `index.html`。

共享的 `.env.development` 和 `.env.production` 仅配置接口前缀，可纳入版本管理；私有本地配置使用 `.env.local` 等忽略文件。`VITE_*` 变量会暴露给浏览器，不应存放密钥。

前端验证命令：

```bash
cd lemnt-ui
yarn typecheck
yarn lint
yarn format:check
yarn test
yarn build
```

前端已移除广告业务页面、路由、菜单编排、投放报表及广告主身份字段，首页提供基于授权菜单的快捷入口。本次整理不修改数据库中的菜单和业务数据。静态构建不等于完成真实登录、权限及系统接口联调。前端规范见 [lemnt-ui/AGENTS.md](lemnt-ui/AGENTS.md)。

## Docker

服务 Dockerfile 统一使用 Docker Hub 公网基础镜像 `eclipse-temurin:17-jre`。先完成 Maven 打包，再在根目录构建，例如：

```bash
docker build -t lemnt-gateway:local lemnt-server/lemnt-gateway
docker build -t lemnt-auth:local lemnt-server/lemnt-auth
```

运行容器时仍需传入环境配置，并确保容器可以访问 Nacos 和其他中间件；容器内的 `127.0.0.1` 指向容器自身。当前仓库未提供 Docker Compose 一键部署。

## 测试与验证

完成依赖管理和公共组件安装后，在仓库根目录分别执行公共组件和服务测试：

```bash
mvn -f lemnt-common/pom.xml test
mvn -f lemnt-server/pom.xml test
```

移除根聚合 POM 后的分步构建与测试尚未重新执行。文件上传校验测试已按现有 Problem Details 响应断言 HTTP 状态、错误码及错误详情。

打包成功不代表已完成真实部署验收；Nacos 3.2.1 连接、数据库、中间件、登录和接口权限等运行流程仍需在目标环境验证。

## 开发与贡献

开发前阅读 [项目规范](lemnt-server/AGENTS.md)，保持微服务与 CQRS/DDD 分层。新增 SDK 可参考 [demo-sdk](lemnt-server/lemnt-third-sdk/demo-sdk/README.md)。提交变更前检查构建、相关测试及 `git diff --check`，不要提交构建产物、本地配置或凭据。

许可证见 [LICENSE](LICENSE)，保留已有版权声明。
