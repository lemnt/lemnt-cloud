# 本地运行验收只读预检

> 检查日期：2026-11-09；目标 HEAD：`12745a285d36869f2005d9f4ffdada93351ec1e4`。
> 当前工作区已有 B1a/B1b 未提交改动。本次仅检查环境，不启动中间件、不建库、不执行 DDL、不发布 Nacos 配置、不读取凭据值。

## 1. 工具链与服务探测

| 检查 | 实际结果 | 判定 |
|---|---|---|
| `java -version`、`mvn -version` | 默认 Temurin 11.0.29，Maven 3.9.12 | 默认 Java 不满足项目 Java 17 要求 |
| Java 17 安装路径 | `/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home/bin/java` 存在 | 本轮 Maven 显式设置 JAVA_HOME/PATH；不修改用户全局环境 |
| `node --version`、`yarn --version` | Node 22.23.3、Yarn 1.22.22 | 满足管理端 engines/packageManager |
| `docker version` | 客户端 29.1.3；无法连接本机 Docker socket | 守护进程不可达，不推断镜像或容器不存在 |
| `docker ps` | 无法连接守护进程 | 未获取容器清单，未启动 Docker Desktop |
| `command -v mysql redis-cli nacos mqadmin` | 当前 PATH 未找到这些命令 | 不等同于未安装，也不证明远端服务不可用 |
| `lsof -nP -iTCP -sTCP:LISTEN` | 未见 3306、6379、8848、9848、9876、10911、8080、9100、9110、9140–9170、9200、5173 监听 | 常用本地端口未就绪；自定义端口/远端部署仍未核实 |
| `/Applications` 名称检查 | 存在 Docker.app、WeChat.app；未匹配到微信开发者工具 | 微信聊天客户端不等于开发者工具，平台验收未执行 |

未验证任何数据库连接、Redis 会话、Nacos 注册、Dubbo 调用、RocketMQ 消费或支付渠道。没有已获批准的可丢弃实例、空库、namespace、数据目录和操作范围，因此 G-ENV 保持未通过。

## 2. 验收期间环境变化（同日复核）

初次探测之后，Docker状态发生变化；不是本次工具启动的结果。第二次`docker version`显示客户端/服务端均为29.1.3，`docker ps`显示mysql8（mysql:8.0）、redis7（redis:7.2）、nacos-server（nacos/nacos-server:v3.2.1），另有MinIO和PostgreSQL容器。3306、6379、8848、9848已监听，Nacos控制台映射8084；9876/10911及网关、业务服务、管理端默认端口仍未见监听。

上述只是容器名称、镜像标签与端口证据；不证明实际服务版本、健康、数据库为空或资源可丢弃。未读取容器环境变量、挂载数据、库表或凭据，未确认这些实例是否供其他业务使用。G-ENV仍待用户确认，不能对现有容器执行初始化或清理。

## 3. 配置契约核对

### 注册配置存在两种入口，不能只设置一套变量

- gateway/auth/system/member 使用 `NACOS_HOST` / `NACOS_PORT`。namespace 由构建占位符 `@profile.active@` 决定，group 为 `DEFAULT_GROUP`；配置导入 URL 也带 namespace/group。
- product/promotion/order/pay 使用 `NACOS_SERVER_ADDR` / `NACOS_NAMESPACE` / `NACOS_GROUP`。默认 namespace 为空，不能假设自动进入旧服务使用的 `dev`。
- 四个拆分服务的配置模板同时将上述 namespace/group 用于 Dubbo 注册。
- 在隔离验收中，必须显式让两套入口指向同一个已批准的 Nacos 实例及隔离空间，并核对最终构建资源和实际注册结果。只设置 `SPRING_PROFILES_ACTIVE` 或 `NACOS_NAMESPACE` 不足以证明所有服务一致。

来源：`lemnt-server/lemnt-gateway/src/main/resources/application.yaml`、各服务 `*-start/src/main/resources/application.yaml`、`docs/config/lemnt-{product,promotion,order,pay}.yaml`。

### 数据源变量按模板填写

四个拆分服务分别使用 `{PRODUCT|PROMOTION|ORDER|PAY}_DB_URL`、`{服务}_DB_USERNAME`、`{服务}_DB_PASSWORD`，不是通用 `DB_USERNAME` / `DB_PASSWORD`。member/auth/system 的数据源必须另行核对授权后的实际配置；不推断已有独立库。

小程序 `lemnt-miniapp/config.js` 仍为 apiBaseUrl 空、memberApiReady=false、paymentAppKey 空。本次保持不变，不把单元测试通过当作可启用生产入口的依据。

## 4. 后续执行与回退边界

1. 用户明确确认可丢弃/隔离环境、实例或容器、端口、独立空库、数据目录、Nacos namespace/group 及允许操作范围后，才进入 E003。
2. 只对批准的空库应用初始化 SQL；现有脚本包含 DROP，不能用于已有业务库。配置发布前核对 Data ID、变量和副作用开关，不保存秘密到版本库。
3. E003 实际连接验证通过后才进入 E004。HTTP 服务使用 preview 启动入口；检查 Nacos 注册、真实 RPC/HTTP 和身份域，不以进程存活或 optional 配置导入代替就绪。
4. 核心业务验收仍需 V-B2a、V-B2b、B3 实际实现/测试等前置完成；不能只批准 G-ENV 就把 L01–L08 记为通过。
5. 回退只停止本次获授权启动的进程、撤销本次在专用空间发布的配置。删除容器/数据目录、清库或清理消息需另行明确授权；不碰共享 Redis、已有消息积压或现有业务库。

当前结论：`static_verified`（配置入口已核对）；本地基础设施和真实业务验收为 `blocked`，不是 `local_runtime_passed`。

## 5. 数据库与Redis使用授权后的探测（2026-11-09）

用户已明确“允许使用本地docker启动的数据库和redis”。已核实mysql8、redis7运行中，主机127.0.0.1的3306/6379 TCP连接成功；容器未配置Docker健康检查。

在未提供凭据的情况下，MySQL只读`SELECT 1`返回1045 Access denied，Redis `PING`返回NOAUTH Authentication required。mysqladmin ping和redis-cli虽退出0，但业务响应明确未认证，不能记为连接验收通过。未尝试猜测密码，也未读取容器环境或业务数据。

当时仍需明确认证方式和专用空库/Redis隔离策略；后续认证结果见第6节。使用授权不等于允许现有库执行DROP初始化、清理Redis或操作其他中间件，G-ENV保持未整体通过。证据见[evidence/local-db-redis-preflight-20261109-114646.json](evidence/local-db-redis-preflight-20261109-114646.json)。

## 6. Nacos在线配置读取与认证验证（2026-11-09）

用户要求“读取nacos 的配置”，并明确同意“使用项目默认账号”。使用gateway配置中的默认账号登录成功；Token及数据源/Redis密码仅保留在进程内存和认证传输中，不保存原始配置或秘密到证据。

- `dev/DEFAULT_GROUP`：已读取common、system、auth、gateway、member配置。system数据源指向本机3306的lemnt_system，member指向lemnt_member；共享Redis为127.0.0.1:6379、DB 0。共享配置同时包含spring.redis与spring.data.redis两种字段路径。
- `lemnt/DEFAULT_GROUP`：另有common、system、auth、gateway配置，system指向ad_system；不能与dev混用。本轮未使用该空间的数据库凭据进行认证。
- 使用dev中system/member凭据，对本地mysql8执行不选择业务schema的`SELECT 1`，两次均返回1；使用dev共享Redis凭据执行AUTH/PING，返回成功。没有查询任何业务表、扫描Redis键或执行写入。
- 对dev空间精确查询lemnt-product.yaml、lemnt-promotion.yaml、lemnt-order.yaml、lemnt-pay.yaml、commerce-mq.yaml，均返回HTTP 404；配置缺失只针对该namespace/group/dataId组合，不推断其他空间也不存在。
- dev共享RocketMQ指向非本机地址，未连接、探测或发送消息。当前默认构建使用dev，拆分服务的namespace仍需显式对齐，不能直接依赖空namespace默认值。

当前已通过的范围仅为本地MySQL/Redis认证连通性，不代表lemnt_system/lemnt_member存在、为空、可重建或业务运行验收通过。专用验收空库、Redis隔离、Nacos配置发布及RocketMQ仍未获完整操作授权，G-ENV及E003不自动标记完成。

证据：[脱敏配置摘录](evidence/nacos-config-read-20261109-115031.json)、[只读认证检查](evidence/nacos-local-connectivity-20261109-115150.json)。
