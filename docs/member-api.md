# 会员接口迁移

版本 v9，2026-10-26；业务Req/Res按接口独立，分页统一使用R.page；认证沿用公共TokenService；基础提交 8eac9f6。参考 YunaiV/yudao-cloud `8db9153bfe6bdc03baf93b74e244a2e74477c5c4` 的 member 控制器。本文按代码静态核对生成；JSON/curl 均为构造示例，部署环境尚未实测。设计依据见 work-items/yudao-member-mall-trade-migration/03-development-design.md。模板参考 code-software-engineer/references/api-documentation.md。

当前会员后台与小程序页面见 [member-pages.md](member-pages.md)。下文关于 API 切片未包含界面的说明保留为 v5 历史边界；v6 已新增页面，真实联调仍待验证。

短信验证码、微信登录、微信绑定及手机号授权接口已移除；仅保留密码登录。历史 V3 建表脚本不再作为部署前置条件，不执行删表操作。

## 边界与运行

- Java 适配层按入口分包：会员小程序 `member.adapter.app.user/address/growth`、密码登录/刷新/退出 `auth.adapter.app.member`，后台 `adapter.web.user/growth`，请求、响应、转换器和异常处理分别归入 `adapter.app` / `adapter.web` 下的域包；HTTP 路径不因 Java 分包改变。
- `/member/app/**` 由小程序消费，Bearer令牌统一在网关校验，会员 ID 从网关写入的用户头进入公共安全上下文；禁止把管理员身份当会员。`/member/admin/**` 为后台接口，使用现有管理员权限机制。旧 `/members/register` 现在要求 `member:user:create`，不再匿名注册。后台创建资料不自动设置密码或签发会话。
- 密码登录只接受手机号，沿用公共 SecurityUtils 的 BCrypt 默认配置，最短10字符、最长72 UTF-8字节。未知账号执行虚拟密码比对。每个手机号连续5次失败后15分钟窗口拒绝登录。
- 会员与后台复用 TokenService/JwtUtils 的 JWT + Redis 机制；会员签名与缓存对象的 user_type 为 member，两端统一复用 LOGIN_TOKEN_KEY + userKey，不能跨身份访问。会话有效期30天（2592000秒），刷新使用同一访问JWT续期；不再签发 refreshToken。注销仅撤销当前会话，不再维护全设备会话索引；改密、重置不删除其他已有会话。会员业务仍保留自身的停用状态校验，恢复启用后仍未过期的原会话可能再次可用。旧 mbr_a_/mbr_r_ 令牌失效，需要重新登录。
- 地址按源逻辑不额外限制20条，写操作锁会员；默认地址在事务内切换，他人地址详情返回 null。积分/经验变更使用 requestNo 幂等，重试须保持 delta/reason，余额不可负、不可溢出。等级变更记录经验重置；签到奖励与流水同事务，每数据库日一次，周期按启用规则最大天数循环。
- 状态采用 lemnt 1启用/0禁用（区别于参考项目）。金额配置为整数分，积分交易配置已用于结算抵扣与按实付赠送，抵扣单位价格为分、每单抵扣上限为积分数。数据库日期决定签到和限流日界；部署须统一时区。LocalDate 为 yyyy-MM-dd，LocalDateTime 按现有 Jackson yyyy-MM-dd HH:mm:ss。
- 新库先审阅并依次执行 member/sql/V1__member.sql、V2__member_account.sql；已有 V1 库只执行 V2。V2 非重复执行脚本，不能重跑。这里未执行线上迁移，不能靠删表回滚。
- member 不再通过 MemberSessionClient 或本地会话服务重复校验 token；应用接口使用 SecurityUtils.getUserId() 读取网关传递的身份。业务服务须通过内部网络限制直连，外部请求统一经过网关；网关先清理客户端身份头，再注入会员 ID，会员请求不传后台 userKey。
- 需要 MySQL、Redis（登录会话）、Nacos、Dubbo；网关由 AuthFilter 统一调用公共 TokenService 校验对应身份的 Redis 会话，不再有 MemberRequestSecurity，无 Dubbo 或远程HTTP校验；trade 保留 auth-client 的 `com.lemnt.auth.api.MemberSessionClient` 调用。网关须配置保留 `/member` 前缀的路由（docs/config/member-gateway-route.yaml），不可追加 StripPrefix。
- 认证迁移：登录、签发、刷新、注销在 lemnt-auth 执行，业务请求令牌校验统一在 gateway 执行；密码登录、刷新、退出 Controller 在 auth，网关精确路由到 auth，URL 保持不变。会员资料和密码维护仍在 member。部署与共享数据库配置见 [认证迁移说明](../work-items/member-auth-migration/03-development-design.md)。
- 各接口权限码见下文，部署须审阅角色授权；本次不自动给角色加权限。小程序 memberApiReady 仍为 false，待真实链路验证后再开启。本次不声称会员管理 React 页面、原生登录界面或生产联调完成。

## 响应与错误

通用 R 字段：code(integer，成功200/应用失败500)、msg(string|null)、data(接口结果|null)、rows(array|null)、total(integer|null)。分页统一使用 R.page：data=null，rows 为记录数组，total 为总数。业务请求与返回项按接口独立定义，分页容器统一复用。业务不存在通常 data=null，删除不存在返回false。完整对象字段见模型表；成功 msg 为 null。

会员接口复用项目 GlobalExceptionHandler。会员业务失败通过 ServiceException 显式保留 HTTP401（身份无效）、400（业务参数不合法）、409（业务冲突），返回 application/problem+json；错误体字段为 type、title、status、detail、instance、code，可附带 traceId。Bean Validation 使用统一校验响应 HTTP422。成功响应仍为 R。既有网关仍可能返回 HTTP200 + R.code401/503，客户端同时识别 HTTP 状态与 code。

业务认证失败的构造示例：

```json
{"type":"urn:lemnt:problem:business-rule-violation","title":"业务规则校验失败","status":401,"detail":"会员会话无效","instance":"/member/app/user","code":"BUSINESS_RULE_VIOLATION"}
```

## 能力对照

| 参考会员控制器 | lemnt 入口 | 当前覆盖 |
| --- | --- | --- |
| app auth | /member/app/auth | 密码登录、刷新/注销 |
| app user | /member/app/user | 本人资料、密码修改 |
| app address | /member/app/addresses | 增删改查、默认地址、列表 |
| app level / experience / point | /member/app/levels、experience-records、point-records | 启用等级与本人分页流水 |
| app signin config / record | /member/app/signin | 规则、汇总、签到、分页（精确路径以下文为准） |
| admin user / address | /member/admin/users | 分页/详情/资料/状态、积分/经验/等级、分组/标签、地址 |
| admin level / group / tag | /member/admin/levels、groups、tags | CRUD、简洁列表、分组/标签分页 |
| admin level-record / experience-record / point-record | /member/admin/*-records | 跨会员分页、详情，按资产类型隔离；另保留单会员查询 |
| admin signin-config / signin-record / config | /member/admin | 规则按天数upsert/删除/详情，签到分页，积分配置 |

接口使用 lemnt REST 路径与字段，不是芋道原 URL 的兼容别名；筛选参数不保证与原项目完全一致。等级流水保留等级ID/经验/原因/时间，不复制参考项目完整等级快照。后台头像/邮箱等资料更新不代表联系方式已验证。交易、支付和售后剩余缺口仍按原迁移文档跟踪。

## 接口目录


### 1. 积分交易配置

接口地址：`/member/admin/config`  
请求方式：`GET`  
权限：`member:config:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | ConfigRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/config' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "pointTradeDeductEnable": true,
    "pointTradeDeductUnitPrice": 1,
    "pointTradeDeductMaxPrice": 1,
    "pointTradeGivePoint": 1
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 2. 保存积分交易配置

接口地址：`/member/admin/config`  
请求方式：`PUT`  
权限：`member:config:update`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| pointTradeDeductEnable | Boolean | 是 | Body | 无 | 积分抵扣及按实付赠送积分开关 |
| pointTradeDeductUnitPrice | Integer | 是 | Body | 无 | 每积分抵扣金额，分 |
| pointTradeDeductMaxPrice | Integer | 是 | Body | 无 | 每单最多使用积分数，0不限 |
| pointTradeGivePoint | Integer | 是 | Body | 无 | 每元赠送积分数 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Boolean | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request PUT 'https://<API_HOST>/member/admin/config' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"pointTradeDeductEnable": true, "pointTradeDeductUnitPrice": 1, "pointTradeDeductMaxPrice": 1, "pointTradeGivePoint": 1}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": true,
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 3. 所有签到规则

接口地址：`/member/admin/signin-configs`  
请求方式：`GET`  
权限：`member:sign-in-config:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | List<MemberConfigListRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/signin-configs' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": [
    {
      "day": 1,
      "point": 1,
      "experience": 1,
      "status": 1
    }
  ],
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 4. 按签到天数创建或更新规则，1启用0停用

接口地址：`/member/admin/signin-configs`  
请求方式：`PUT`  
权限：`member:sign-in-config:update`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| day | Integer | 是 | Body | 无 | 本轮连续签到第几天 |
| point | Long | 是 | Body | 无 | 奖励积分 |
| experience | Long | 是 | Body | 无 | 经验值或等级升级门槛 |
| status | Integer | 是 | Body | 无 | 状态：1启用，0停用 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Boolean | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request PUT 'https://<API_HOST>/member/admin/signin-configs' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"day": 1, "point": 1, "experience": 1, "status": 1}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": true,
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 5. 删除指定天数规则，历史奖励保留

接口地址：`/member/admin/signin-configs/{day}`  
请求方式：`DELETE`  
权限：`member:sign-in-config:delete`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| day | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Boolean | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request DELETE 'https://<API_HOST>/member/admin/signin-configs/1' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": true,
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 6. 指定签到天数规则

接口地址：`/member/admin/signin-configs/{day}`  
请求方式：`GET`  
权限：`member:sign-in-config:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| day | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | MemberConfigDetailRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/signin-configs/1' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "day": 1,
    "point": 1,
    "experience": 1,
    "status": 1
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 7. Group列表

接口地址：`/member/admin/groups`  
请求方式：`GET`  
权限：`member:group:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | List<MemberGroupListRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/groups' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": [
    {
      "id": 1,
      "name": "示例",
      "remark": "示例",
      "status": 1
    }
  ],
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 8. Group详情

接口地址：`/member/admin/groups/{id}`  
请求方式：`GET`  
权限：`member:group:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | MemberGroupDetailRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/groups/1' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "id": 1,
    "name": "示例",
    "remark": "示例",
    "status": 1
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 9. 创建Group

接口地址：`/member/admin/groups`  
请求方式：`POST`  
权限：`member:group:create`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| name | String | 是 | Body | 无 | 名称或收件人 |
| remark | String | 否 | Body | 无 | 备注 |
| status | Integer | 是 | Body | 无 | 状态：1启用，0停用 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Long | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/member/admin/groups' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"name": "示例", "remark": "示例", "status": 1}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": 1,
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 10. 修改Group

接口地址：`/member/admin/groups/{id}`  
请求方式：`PUT`  
权限：`member:group:update`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| name | String | 是 | Body | 无 | 名称或收件人 |
| remark | String | 否 | Body | 无 | 备注 |
| status | Integer | 是 | Body | 无 | 状态：1启用，0停用 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Long | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request PUT 'https://<API_HOST>/member/admin/groups/1' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"name": "示例", "remark": "示例", "status": 1}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": 1,
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 11. 删除未被会员使用的Group

接口地址：`/member/admin/groups/{id}`  
请求方式：`DELETE`  
权限：`member:group:delete`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Boolean | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request DELETE 'https://<API_HOST>/member/admin/groups/1' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": true,
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 12. Group简洁列表

接口地址：`/member/admin/groups/list-all-simple`  
请求方式：`GET`  
权限：`member:group:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | List<MemberGroupSimpleRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/groups/list-all-simple' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": [
    {
      "id": 1,
      "name": "示例",
      "remark": "示例",
      "status": 1
    }
  ],
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 13. Level列表

接口地址：`/member/admin/levels`  
请求方式：`GET`  
权限：`member:level:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | List<MemberLevelListRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/levels' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": [
    {
      "id": 1,
      "name": "示例",
      "level": 1,
      "experience": 1,
      "discountPercent": 1,
      "icon": "示例",
      "backgroundUrl": "示例",
      "status": 1
    }
  ],
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 14. Level详情

接口地址：`/member/admin/levels/{id}`  
请求方式：`GET`  
权限：`member:level:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | MemberLevelDetailRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/levels/1' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "id": 1,
    "name": "示例",
    "level": 1,
    "experience": 1,
    "discountPercent": 1,
    "icon": "示例",
    "backgroundUrl": "示例",
    "status": 1
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 15. 创建Level

接口地址：`/member/admin/levels`  
请求方式：`POST`  
权限：`member:level:create`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| name | String | 是 | Body | 无 | 名称或收件人 |
| level | Integer | 是 | Body | 无 | 等级序号 |
| experience | Long | 是 | Body | 无 | 经验值或等级升级门槛 |
| discountPercent | Integer | 是 | Body | 无 | 支付百分比，98为98折 |
| icon | String | 否 | Body | 无 | 等级图标URL |
| backgroundUrl | String | 否 | Body | 无 | 等级背景URL |
| status | Integer | 是 | Body | 无 | 状态：1启用，0停用 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Long | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/member/admin/levels' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"name": "示例", "level": 1, "experience": 1, "discountPercent": 1, "icon": "示例", "backgroundUrl": "示例", "status": 1}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": 1,
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 16. 修改Level

接口地址：`/member/admin/levels/{id}`  
请求方式：`PUT`  
权限：`member:level:update`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| name | String | 是 | Body | 无 | 名称或收件人 |
| level | Integer | 是 | Body | 无 | 等级序号 |
| experience | Long | 是 | Body | 无 | 经验值或等级升级门槛 |
| discountPercent | Integer | 是 | Body | 无 | 支付百分比，98为98折 |
| icon | String | 否 | Body | 无 | 等级图标URL |
| backgroundUrl | String | 否 | Body | 无 | 等级背景URL |
| status | Integer | 是 | Body | 无 | 状态：1启用，0停用 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Long | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request PUT 'https://<API_HOST>/member/admin/levels/1' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"name": "示例", "level": 1, "experience": 1, "discountPercent": 1, "icon": "示例", "backgroundUrl": "示例", "status": 1}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": 1,
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 17. 删除未被会员使用的Level

接口地址：`/member/admin/levels/{id}`  
请求方式：`DELETE`  
权限：`member:level:delete`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Boolean | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request DELETE 'https://<API_HOST>/member/admin/levels/1' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": true,
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 18. Level简洁列表

接口地址：`/member/admin/levels/list-all-simple`  
请求方式：`GET`  
权限：`member:level:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | List<MemberLevelSimpleRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/levels/list-all-simple' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": [
    {
      "id": 1,
      "name": "示例",
      "level": 1,
      "experience": 1,
      "discountPercent": 1,
      "icon": "示例",
      "backgroundUrl": "示例",
      "status": 1
    }
  ],
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 19. groups/page分页

接口地址：`/member/admin/groups/page`  
请求方式：`GET`  
权限：`member:group:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| memberId | Long | 否 | Query | 无 | 可选会员ID；未填查询所有会员 |
| name | String | 否 | Query | 无 | 名称包含匹配 |
| status | Integer | 否 | Query | 无 | 状态：0禁用，1启用 |
| pageNum | Integer | 否 | Query | 1 | 页码，默认1 |
| pageSize | Integer | 否 | Query | 20 | 页大小，默认20，最大100 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | null | 分页使用统一 rows/total |
| rows | MemberCatalogQueryGroupsPageRes[] | 本接口独立的返回项数组 |
| total | integer | 总记录数 |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/groups/page?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": null,
  "rows": [
    {
      "id": 1,
      "name": "示例",
      "remark": "示例",
      "status": 1
    }
  ],
  "total": 1
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 20. tags/page分页

接口地址：`/member/admin/tags/page`  
请求方式：`GET`  
权限：`member:tag:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| memberId | Long | 否 | Query | 无 | 可选会员ID；未填查询所有会员 |
| name | String | 否 | Query | 无 | 名称包含匹配 |
| status | Integer | 否 | Query | 无 | 状态：0禁用，1启用 |
| pageNum | Integer | 否 | Query | 1 | 页码，默认1 |
| pageSize | Integer | 否 | Query | 20 | 页大小，默认20，最大100 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | null | 分页使用统一 rows/total |
| rows | MemberCatalogQueryTagsPageRes[] | 本接口独立的返回项数组 |
| total | integer | 总记录数 |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/tags/page?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": null,
  "rows": [
    {
      "id": 1,
      "name": "示例",
      "remark": "示例"
    }
  ],
  "total": 1
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 21. point-records分页

接口地址：`/member/admin/point-records`  
请求方式：`GET`  
权限：`member:point:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| memberId | Long | 否 | Query | 无 | 可选会员ID；未填查询所有会员 |
| name | String | 否 | Query | 无 | 名称包含匹配 |
| status | Integer | 否 | Query | 无 | 状态：0禁用，1启用 |
| pageNum | Integer | 否 | Query | 1 | 页码，默认1 |
| pageSize | Integer | 否 | Query | 20 | 页大小，默认20，最大100 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | null | 分页使用统一 rows/total |
| rows | MemberRecordPointRecordsRes[] | 本接口独立的返回项数组 |
| total | integer | 总记录数 |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/point-records?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": null,
  "rows": [
    {
      "id": 1,
      "memberId": 1,
      "assetType": "示例",
      "delta": 1,
      "balance": 1,
      "bizType": "示例",
      "bizNo": "示例",
      "reason": "人工调整",
      "createTime": "2026-10-25 12:00:00"
    }
  ],
  "total": 1
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 22. point-records详情

接口地址：`/member/admin/point-records/{id}`  
请求方式：`GET`  
权限：`member:point:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | MemberRecordPointRecordsDetailRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/point-records/1' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "id": 1,
    "memberId": 1,
    "assetType": "示例",
    "delta": 1,
    "balance": 1,
    "bizType": "示例",
    "bizNo": "示例",
    "reason": "人工调整",
    "createTime": "2026-10-25 12:00:00"
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 23. experience-records分页

接口地址：`/member/admin/experience-records`  
请求方式：`GET`  
权限：`member:experience:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| memberId | Long | 否 | Query | 无 | 可选会员ID；未填查询所有会员 |
| name | String | 否 | Query | 无 | 名称包含匹配 |
| status | Integer | 否 | Query | 无 | 状态：0禁用，1启用 |
| pageNum | Integer | 否 | Query | 1 | 页码，默认1 |
| pageSize | Integer | 否 | Query | 20 | 页大小，默认20，最大100 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | null | 分页使用统一 rows/total |
| rows | MemberRecordExperienceRecordsRes[] | 本接口独立的返回项数组 |
| total | integer | 总记录数 |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/experience-records?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": null,
  "rows": [
    {
      "id": 1,
      "memberId": 1,
      "assetType": "示例",
      "delta": 1,
      "balance": 1,
      "bizType": "示例",
      "bizNo": "示例",
      "reason": "人工调整",
      "createTime": "2026-10-25 12:00:00"
    }
  ],
  "total": 1
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 24. experience-records详情

接口地址：`/member/admin/experience-records/{id}`  
请求方式：`GET`  
权限：`member:experience:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | MemberRecordExperienceRecordsDetailRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/experience-records/1' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "id": 1,
    "memberId": 1,
    "assetType": "示例",
    "delta": 1,
    "balance": 1,
    "bizType": "示例",
    "bizNo": "示例",
    "reason": "人工调整",
    "createTime": "2026-10-25 12:00:00"
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 25. level-records分页

接口地址：`/member/admin/level-records`  
请求方式：`GET`  
权限：`member:level:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| memberId | Long | 否 | Query | 无 | 可选会员ID；未填查询所有会员 |
| name | String | 否 | Query | 无 | 名称包含匹配 |
| status | Integer | 否 | Query | 无 | 状态：0禁用，1启用 |
| pageNum | Integer | 否 | Query | 1 | 页码，默认1 |
| pageSize | Integer | 否 | Query | 20 | 页大小，默认20，最大100 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | null | 分页使用统一 rows/total |
| rows | MemberRecordLevelRecordsRes[] | 本接口独立的返回项数组 |
| total | integer | 总记录数 |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/level-records?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": null,
  "rows": [
    {
      "id": 1,
      "memberId": 1,
      "levelId": 1,
      "experience": 1,
      "reason": "人工调整",
      "createTime": "2026-10-25 12:00:00"
    }
  ],
  "total": 1
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 26. level-records详情

接口地址：`/member/admin/level-records/{id}`  
请求方式：`GET`  
权限：`member:level:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | MemberRecordLevelRecordsDetailRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/level-records/1' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "id": 1,
    "memberId": 1,
    "levelId": 1,
    "experience": 1,
    "reason": "人工调整",
    "createTime": "2026-10-25 12:00:00"
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 27. signin-records分页

接口地址：`/member/admin/signin-records`  
请求方式：`GET`  
权限：`member:signin:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| memberId | Long | 否 | Query | 无 | 可选会员ID；未填查询所有会员 |
| name | String | 否 | Query | 无 | 名称包含匹配 |
| status | Integer | 否 | Query | 无 | 状态：0禁用，1启用 |
| pageNum | Integer | 否 | Query | 1 | 页码，默认1 |
| pageSize | Integer | 否 | Query | 20 | 页大小，默认20，最大100 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | null | 分页使用统一 rows/total |
| rows | MemberRecordSigninRecordsRes[] | 本接口独立的返回项数组 |
| total | integer | 总记录数 |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/signin-records?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": null,
  "rows": [
    {
      "id": 1,
      "memberId": 1,
      "signDate": "2026-10-25",
      "day": 1,
      "point": 1,
      "experience": 1
    }
  ],
  "total": 1
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 28. Tag列表

接口地址：`/member/admin/tags`  
请求方式：`GET`  
权限：`member:tag:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | List<MemberTagListRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/tags' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": [
    {
      "id": 1,
      "name": "示例",
      "remark": "示例"
    }
  ],
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 29. Tag详情

接口地址：`/member/admin/tags/{id}`  
请求方式：`GET`  
权限：`member:tag:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | MemberTagDetailRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/tags/1' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "id": 1,
    "name": "示例",
    "remark": "示例"
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 30. 创建Tag

接口地址：`/member/admin/tags`  
请求方式：`POST`  
权限：`member:tag:create`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| name | String | 是 | Body | 无 | 名称或收件人 |
| remark | String | 否 | Body | 无 | 备注 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Long | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/member/admin/tags' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"name": "示例", "remark": "示例"}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": 1,
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 31. 修改Tag

接口地址：`/member/admin/tags/{id}`  
请求方式：`PUT`  
权限：`member:tag:update`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| name | String | 是 | Body | 无 | 名称或收件人 |
| remark | String | 否 | Body | 无 | 备注 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Long | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request PUT 'https://<API_HOST>/member/admin/tags/1' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"name": "示例", "remark": "示例"}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": 1,
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 32. 删除未被会员使用的Tag

接口地址：`/member/admin/tags/{id}`  
请求方式：`DELETE`  
权限：`member:tag:delete`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Boolean | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request DELETE 'https://<API_HOST>/member/admin/tags/1' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": true,
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 33. Tag简洁列表

接口地址：`/member/admin/tags/list-all-simple`  
请求方式：`GET`  
权限：`member:tag:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | List<MemberTagSimpleRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/tags/list-all-simple' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": [
    {
      "id": 1,
      "name": "示例",
      "remark": "示例"
    }
  ],
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 34. 后台创建会员，不验证联系信息且不签发会话

接口地址：`/member/admin/users`  
请求方式：`POST`  
权限：`member:user:create`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| nickname | String | 是 | Body | 无 | 昵称 |
| mobile | String | 是 | Body | 无 | 手机号 |
| email | String | 否 | Body | 无 | 邮箱 |
| avatar | String | 否 | Body | 无 | 头像HTTPS地址 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | MemberUserCreateRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/member/admin/users' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"nickname": "测试会员", "mobile": "13800000001", "email": "member@example.test", "avatar": "https://example.test/avatar.png"}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "id": 1,
    "memberNo": "示例",
    "nickname": "测试会员",
    "mobile": "13800000001",
    "email": "member@example.test",
    "avatar": "https://example.test/avatar.png",
    "status": 1,
    "points": 1,
    "experience": 1,
    "levelId": 1,
    "groupId": 1,
    "sex": 1,
    "birthday": "2026-10-25",
    "createTime": "2026-10-25 12:00:00"
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 35. 会员分页，手机号精确筛选

接口地址：`/member/admin/users`  
请求方式：`GET`  
权限：`member:user:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| mobile | String | 否 | Query | 无 | 手机号 |
| pageNum | Integer | 否 | Query | 1 | 页码，默认1 |
| pageSize | Integer | 否 | Query | 无 | 每页条数，默认20，最大100 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | null | 分页使用统一 rows/total |
| rows | MemberUserPageRes[] | 本接口独立的返回项数组 |
| total | integer | 总记录数 |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/users?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": null,
  "rows": [
    {
      "id": 1,
      "memberNo": "示例",
      "nickname": "测试会员",
      "mobile": "13800000001",
      "email": "member@example.test",
      "avatar": "https://example.test/avatar.png",
      "status": 1,
      "points": 1,
      "experience": 1,
      "levelId": 1,
      "groupId": 1,
      "sex": 1,
      "birthday": "2026-10-25",
      "createTime": "2026-10-25 12:00:00"
    }
  ],
  "total": 1
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 36. 会员详情

接口地址：`/member/admin/users/{id}`  
请求方式：`GET`  
权限：`member:user:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | MemberUserDetailRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/users/1' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "id": 1,
    "memberNo": "示例",
    "nickname": "测试会员",
    "mobile": "13800000001",
    "email": "member@example.test",
    "avatar": "https://example.test/avatar.png",
    "status": 1,
    "points": 1,
    "experience": 1,
    "levelId": 1,
    "groupId": 1,
    "sex": 1,
    "birthday": "2026-10-25",
    "createTime": "2026-10-25 12:00:00"
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 37. 后台更新资料和状态，校验会员状态

接口地址：`/member/admin/users/{id}`  
请求方式：`PUT`  
权限：`member:user:update`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| nickname | String | 是 | Body | 无 | 昵称 |
| email | String | 否 | Body | 无 | 邮箱 |
| avatar | String | 否 | Body | 无 | 头像HTTPS地址 |
| sex | Integer | 否 | Body | 无 | 性别：0未知，1男，2女 |
| birthday | java.time.LocalDate | 否 | Body | 无 | 出生日期 |
| status | Integer | 否 | Body | 无 | 状态：1启用，0停用 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Boolean | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request PUT 'https://<API_HOST>/member/admin/users/1' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"nickname": "测试会员", "email": "member@example.test", "avatar": "https://example.test/avatar.png", "sex": 1, "birthday": "2026-10-25", "status": 1}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": true,
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 38. 后台重置密码

接口地址：`/member/admin/users/{id}/password`  
请求方式：`PUT`  
权限：`member:user:reset-password`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| password | String | 是 | Body | 无 | 密码，至少10字符且最多72 UTF-8字节 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Boolean | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request PUT 'https://<API_HOST>/member/admin/users/1/password' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"password": "Example-password-2026"}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": true,
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 39. 调整等级及对应经验，幂等业务编号

接口地址：`/member/admin/users/{id}/level`  
请求方式：`PUT`  
权限：`member:user:update-level`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| levelId | Long | 否 | Body | 无 | 等级编号，可空 |
| requestNo | String | 是 | Body | 无 | 幂等业务编号，重试不可更改内容 |
| reason | String | 是 | Body | 无 | 原因 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Boolean | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request PUT 'https://<API_HOST>/member/admin/users/1/level' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"levelId": 1, "requestNo": "member-request-001", "reason": "人工调整"}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": true,
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 40. 分配或清空分组

接口地址：`/member/admin/users/{id}/group`  
请求方式：`PUT`  
权限：`member:user:update`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| groupId | Long | 否 | Body | 无 | 分组编号，可空 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Boolean | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request PUT 'https://<API_HOST>/member/admin/users/1/group' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"groupId": 1}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": true,
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 41. 会员标签编号

接口地址：`/member/admin/users/{id}/tags`  
请求方式：`GET`  
权限：`member:user:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | List<Long> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/users/1/tags' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": [
    1
  ],
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 42. 设置标签集合，空集合清空

接口地址：`/member/admin/users/{id}/tags`  
请求方式：`PUT`  
权限：`member:user:update`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| tagIds | java.util.List<Long> | 是 | Body | 无 | 标签编号，空集合清空标签 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Boolean | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request PUT 'https://<API_HOST>/member/admin/users/1/tags' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"tagIds": [1]}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": true,
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 43. 后台查看会员地址簿，包含个人信息

接口地址：`/member/admin/users/{id}/addresses`  
请求方式：`GET`  
权限：`member:address:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | List<AddressRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/users/1/addresses' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": [
    {
      "id": 1,
      "memberId": 1,
      "name": "示例",
      "mobile": "13800000001",
      "areaId": 1,
      "detailAddress": "示例",
      "defaultStatus": true
    }
  ],
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 44. 等级变更记录分页

接口地址：`/member/admin/users/{id}/level-records`  
请求方式：`GET`  
权限：`member:level-record:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| mobile | String | 否 | Query | 无 | 手机号 |
| pageNum | Integer | 否 | Query | 1 | 页码，默认1 |
| pageSize | Integer | 否 | Query | 无 | 每页条数，默认20，最大100 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | null | 分页使用统一 rows/total |
| rows | MemberUserLevelRecordsRes[] | 本接口独立的返回项数组 |
| total | integer | 总记录数 |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/users/1/level-records?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": null,
  "rows": [
    {
      "id": 1,
      "memberId": 1,
      "levelId": 1,
      "experience": 1,
      "reason": "人工调整",
      "createTime": "2026-10-25 12:00:00"
    }
  ],
  "total": 1
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 45. 会员签到记录分页

接口地址：`/member/admin/users/{id}/signin-records`  
请求方式：`GET`  
权限：`member:sign-in-record:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| mobile | String | 否 | Query | 无 | 手机号 |
| pageNum | Integer | 否 | Query | 1 | 页码，默认1 |
| pageSize | Integer | 否 | Query | 无 | 每页条数，默认20，最大100 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | null | 分页使用统一 rows/total |
| rows | MemberUserSignsRes[] | 本接口独立的返回项数组 |
| total | integer | 总记录数 |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/users/1/signin-records?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": null,
  "rows": [
    {
      "id": 1,
      "memberId": 1,
      "signDate": "2026-10-25",
      "day": 1,
      "point": 1,
      "experience": 1
    }
  ],
  "total": 1
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 46. 幂等变更会员points，余额不能为负

接口地址：`/member/admin/users/{id}/points`  
请求方式：`PUT`  
权限：`member:user:update-point`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| delta | Long | 是 | Body | 无 | 本次有符号变更量 |
| requestNo | String | 是 | Body | 无 | 幂等业务编号，重试不可更改内容 |
| reason | String | 是 | Body | 无 | 原因 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | MemberUserPointsRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request PUT 'https://<API_HOST>/member/admin/users/1/points' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"delta": 1, "requestNo": "member-request-001", "reason": "人工调整"}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "id": 1,
    "memberId": 1,
    "assetType": "示例",
    "delta": 1,
    "balance": 1,
    "bizType": "示例",
    "bizNo": "示例",
    "reason": "人工调整",
    "createTime": "2026-10-25 12:00:00"
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 47. 会员points流水分页

接口地址：`/member/admin/users/{id}/points-records`  
请求方式：`GET`  
权限：`member:point-record:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| mobile | String | 否 | Query | 无 | 手机号 |
| pageNum | Integer | 否 | Query | 1 | 页码，默认1 |
| pageSize | Integer | 否 | Query | 无 | 每页条数，默认20，最大100 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | null | 分页使用统一 rows/total |
| rows | MemberUserPointsRecordsRes[] | 本接口独立的返回项数组 |
| total | integer | 总记录数 |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/users/1/points-records?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": null,
  "rows": [
    {
      "id": 1,
      "memberId": 1,
      "assetType": "示例",
      "delta": 1,
      "balance": 1,
      "bizType": "示例",
      "bizNo": "示例",
      "reason": "人工调整",
      "createTime": "2026-10-25 12:00:00"
    }
  ],
  "total": 1
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 48. 幂等变更会员experience，余额不能为负

接口地址：`/member/admin/users/{id}/experience`  
请求方式：`PUT`  
权限：`member:user:update-level`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| delta | Long | 是 | Body | 无 | 本次有符号变更量 |
| requestNo | String | 是 | Body | 无 | 幂等业务编号，重试不可更改内容 |
| reason | String | 是 | Body | 无 | 原因 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | MemberUserExperienceRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request PUT 'https://<API_HOST>/member/admin/users/1/experience' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"delta": 1, "requestNo": "member-request-001", "reason": "人工调整"}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "id": 1,
    "memberId": 1,
    "assetType": "示例",
    "delta": 1,
    "balance": 1,
    "bizType": "示例",
    "bizNo": "示例",
    "reason": "人工调整",
    "createTime": "2026-10-25 12:00:00"
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 49. 会员experience流水分页

接口地址：`/member/admin/users/{id}/experience-records`  
请求方式：`GET`  
权限：`member:experience-record:query`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| mobile | String | 否 | Query | 无 | 手机号 |
| pageNum | Integer | 否 | Query | 1 | 页码，默认1 |
| pageSize | Integer | 否 | Query | 无 | 每页条数，默认20，最大100 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | null | 分页使用统一 rows/total |
| rows | MemberUserExperienceRecordsRes[] | 本接口独立的返回项数组 |
| total | integer | 总记录数 |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/users/1/experience-records?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": null,
  "rows": [
    {
      "id": 1,
      "memberId": 1,
      "assetType": "示例",
      "delta": 1,
      "balance": 1,
      "bizType": "示例",
      "bizNo": "示例",
      "reason": "人工调整",
      "createTime": "2026-10-25 12:00:00"
    }
  ],
  "total": 1
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 50. 当前会员资料

接口地址：`/member/app/user`  
请求方式：`GET`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | ProfileRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/app/user' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "id": 1,
    "memberNo": "示例",
    "nickname": "测试会员",
    "mobile": "13800000001",
    "email": "member@example.test",
    "avatar": "https://example.test/avatar.png",
    "status": 1,
    "points": 1,
    "experience": 1,
    "levelId": 1,
    "groupId": 1,
    "sex": 1,
    "birthday": "2026-10-25",
    "createTime": "2026-10-25 12:00:00"
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 51. 更新本人资料，不接受会员ID、手机号、积分、等级或状态

接口地址：`/member/app/user`  
请求方式：`PUT`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| nickname | String | 是 | Body | 无 | 昵称 |
| email | String | 否 | Body | 无 | 邮箱 |
| avatar | String | 否 | Body | 无 | 头像HTTPS地址 |
| sex | Integer | 否 | Body | 无 | 性别：0未知，1男，2女 |
| birthday | java.time.LocalDate | 否 | Body | 无 | 出生日期 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Boolean | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request PUT 'https://<API_HOST>/member/app/user' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"nickname": "测试会员", "email": "member@example.test", "avatar": "https://example.test/avatar.png", "sex": 1, "birthday": "2026-10-25"}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": true,
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 52. 验证原密码并修改

接口地址：`/member/app/user/password`  
请求方式：`PUT`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| oldPassword | String | 是 | Body | 无 | 当前密码 |
| newPassword | String | 是 | Body | 无 | 新密码，至少10字符且最多72 UTF-8字节 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Boolean | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request PUT 'https://<API_HOST>/member/app/user/password' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"oldPassword": "Example-old-password-2026", "newPassword": "Example-new-password-2026"}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": true,
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 53. 已启用会员等级

接口地址：`/member/app/levels`  
请求方式：`GET`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | List<LevelRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/app/levels' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": [
    {
      "id": 1,
      "name": "示例",
      "level": 1,
      "experience": 1,
      "discountPercent": 1,
      "icon": "示例",
      "backgroundUrl": "示例",
      "status": 1
    }
  ],
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 54. 已启用签到规则

接口地址：`/member/app/signin/config`  
请求方式：`GET`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | List<SignConfigRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/app/signin/config' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": [
    {
      "day": 1,
      "point": 1,
      "experience": 1,
      "status": 1
    }
  ],
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 55. 本人签到汇总

接口地址：`/member/app/signin/summary`  
请求方式：`GET`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | SignSummaryRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/app/signin/summary' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "totalDay": 1,
    "continuousDay": 1,
    "todaySignIn": true
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 56. 数据库日期每日幂等签到，积分与经验同事务发放

接口地址：`/member/app/signin`  
请求方式：`POST`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | AppMemberGrowthSignRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/member/app/signin' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "id": 1,
    "memberId": 1,
    "signDate": "2026-10-25",
    "day": 1,
    "point": 1,
    "experience": 1
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 57. 本人签到记录分页

接口地址：`/member/app/signin/records`  
请求方式：`GET`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| pageNum | Integer | 否 | Query | 1 | 页码，默认1 |
| pageSize | Integer | 否 | Query | 无 | 每页条数，默认20，最大100 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | null | 分页使用统一 rows/total |
| rows | AppMemberGrowthSignsRes[] | 本接口独立的返回项数组 |
| total | integer | 总记录数 |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/app/signin/records?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": null,
  "rows": [
    {
      "id": 1,
      "memberId": 1,
      "signDate": "2026-10-25",
      "day": 1,
      "point": 1,
      "experience": 1
    }
  ],
  "total": 1
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 58. 本人POINT流水分页

接口地址：`/member/app/point-records`  
请求方式：`GET`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| pageNum | Integer | 否 | Query | 1 | 页码，默认1 |
| pageSize | Integer | 否 | Query | 无 | 每页条数，默认20，最大100 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | null | 分页使用统一 rows/total |
| rows | AppMemberGrowthPointRes[] | 本接口独立的返回项数组 |
| total | integer | 总记录数 |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/app/point-records?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": null,
  "rows": [
    {
      "id": 1,
      "memberId": 1,
      "assetType": "示例",
      "delta": 1,
      "balance": 1,
      "bizType": "示例",
      "bizNo": "示例",
      "reason": "人工调整",
      "createTime": "2026-10-25 12:00:00"
    }
  ],
  "total": 1
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 59. 本人EXPERIENCE流水分页

接口地址：`/member/app/experience-records`  
请求方式：`GET`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| pageNum | Integer | 否 | Query | 1 | 页码，默认1 |
| pageSize | Integer | 否 | Query | 无 | 每页条数，默认20，最大100 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | null | 分页使用统一 rows/total |
| rows | AppMemberGrowthExperienceRes[] | 本接口独立的返回项数组 |
| total | integer | 总记录数 |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/app/experience-records?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": null,
  "rows": [
    {
      "id": 1,
      "memberId": 1,
      "assetType": "示例",
      "delta": 1,
      "balance": 1,
      "bizType": "示例",
      "bizNo": "示例",
      "reason": "人工调整",
      "createTime": "2026-10-25 12:00:00"
    }
  ],
  "total": 1
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 60. 本人地址列表

接口地址：`/member/app/addresses`  
请求方式：`GET`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | List<AppMemberAddressListRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/app/addresses' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": [
    {
      "id": 1,
      "memberId": 1,
      "name": "示例",
      "mobile": "13800000001",
      "areaId": 1,
      "detailAddress": "示例",
      "defaultStatus": true
    }
  ],
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 61. 本人默认地址

接口地址：`/member/app/addresses/default`  
请求方式：`GET`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | AppMemberAddressDefaultAddressRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/app/addresses/default' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "id": 1,
    "memberId": 1,
    "name": "示例",
    "mobile": "13800000001",
    "areaId": 1,
    "detailAddress": "示例",
    "defaultStatus": true
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 62. 本人地址详情，不存在和他人地址均为空

接口地址：`/member/app/addresses/{id}`  
请求方式：`GET`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | AppMemberAddressDetailRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/app/addresses/1' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "id": 1,
    "memberId": 1,
    "name": "示例",
    "mobile": "13800000001",
    "areaId": 1,
    "detailAddress": "示例",
    "defaultStatus": true
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 63. 新增本人地址

接口地址：`/member/app/addresses`  
请求方式：`POST`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| name | String | 是 | Body | 无 | 名称或收件人 |
| mobile | String | 是 | Body | 无 | 手机号 |
| areaId | Long | 是 | Body | 无 | 地区编号 |
| detailAddress | String | 是 | Body | 无 | 详细地址 |
| defaultStatus | Boolean | 是 | Body | 无 | 是否默认地址 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | AppMemberAddressCreateRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/member/app/addresses' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"name": "示例", "mobile": "13800000001", "areaId": 1, "detailAddress": "示例", "defaultStatus": true}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "id": 1,
    "memberId": 1,
    "name": "示例",
    "mobile": "13800000001",
    "areaId": 1,
    "detailAddress": "示例",
    "defaultStatus": true
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 64. 更新本人地址，默认地址修改串行化

接口地址：`/member/app/addresses/{id}`  
请求方式：`PUT`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| name | String | 是 | Body | 无 | 名称或收件人 |
| mobile | String | 是 | Body | 无 | 手机号 |
| areaId | Long | 是 | Body | 无 | 地区编号 |
| detailAddress | String | 是 | Body | 无 | 详细地址 |
| defaultStatus | Boolean | 是 | Body | 无 | 是否默认地址 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | AppMemberAddressUpdateRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request PUT 'https://<API_HOST>/member/app/addresses/1' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"name": "示例", "mobile": "13800000001", "areaId": 1, "detailAddress": "示例", "defaultStatus": true}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "id": 1,
    "memberId": 1,
    "name": "示例",
    "mobile": "13800000001",
    "areaId": 1,
    "detailAddress": "示例",
    "defaultStatus": true
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 65. 删除本人地址

接口地址：`/member/app/addresses/{id}`  
请求方式：`DELETE`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| id | integer | 是 | Path | 无 | 正整数标识，day为签到天数1..366 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Boolean | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request DELETE 'https://<API_HOST>/member/app/addresses/1' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": true,
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 66. 会员手机号密码登录，独立令牌域

接口地址：`/member/app/auth/login`  
请求方式：`POST`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| mobile | String | 是 | Body | 无 | 手机号 |
| password | String | 是 | Body | 无 | 密码，至少10字符且最多72 UTF-8字节 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | MemberTokenRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/member/app/auth/login' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"mobile": "13800000001", "password": "Example-password-2026"}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "memberId": 1,
    "accessToken": "<MEMBER_JWT>",
    "expiresIn": 2592000
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 67. 使用当前访问JWT续期会员会话

接口地址：`/member/app/auth/refresh-token`  
请求方式：`POST`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | String | 是 | Header | 无 | Bearer 会员访问JWT，用于会话续期 |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | MemberTokenRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/member/app/auth/refresh-token' --header 'Authorization: Bearer <MEMBER_JWT>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "memberId": 1,
    "accessToken": "<MEMBER_JWT>",
    "expiresIn": 2592000
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。


### 68. 撤销当前会员会话，重复退出幂等

接口地址：`/member/app/auth/logout`  
请求方式：`POST`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Boolean | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/member/app/auth/logout' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": true,
  "rows": null,
  "total": null
}
```

适用错误：缺失或无效凭证401；参数400；状态或重复业务冲突409。

## 模型字段

业务 Req/Res 均按接口独立定义；字段以当前代码为准。分页统一返回 R 的 rows/total，不再定义 AccountPageRes。

### AuthLoginReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| mobile | String | 手机号 |
| password | String | 密码，至少10字符且最多72 UTF-8字节 |

### MemberTokenRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| memberId | Long | 会员编号 |
| accessToken | String | 会员访问令牌，禁止记录到日志 |
| expiresIn | Long | 访问令牌有效期，秒 |

### AppMemberAddressCreateReq

包：`com.lemnt.member.adapter.app.address.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| mobile | String | \u624b\u673a\u53f7 |
| areaId | Long | \u5730\u533a\u7f16\u53f7 |
| detailAddress | String | \u8be6\u7ec6\u5730\u5740 |
| defaultStatus | Boolean | \u662f\u5426\u9ed8\u8ba4\u5730\u5740 |

### AppMemberAddressUpdateReq

包：`com.lemnt.member.adapter.app.address.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| mobile | String | \u624b\u673a\u53f7 |
| areaId | Long | \u5730\u533a\u7f16\u53f7 |
| detailAddress | String | \u8be6\u7ec6\u5730\u5740 |
| defaultStatus | Boolean | \u662f\u5426\u9ed8\u8ba4\u5730\u5740 |

### AppMemberAddressCreateRes

包：`com.lemnt.member.adapter.app.address.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberId | Long | \u4f1a\u5458\u7f16\u53f7 |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| mobile | String | \u624b\u673a\u53f7 |
| areaId | Long | \u5730\u533a\u7f16\u53f7 |
| detailAddress | String | \u8be6\u7ec6\u5730\u5740 |
| defaultStatus | Boolean | \u662f\u5426\u9ed8\u8ba4\u5730\u5740 |

### AppMemberAddressDefaultAddressRes

包：`com.lemnt.member.adapter.app.address.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberId | Long | \u4f1a\u5458\u7f16\u53f7 |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| mobile | String | \u624b\u673a\u53f7 |
| areaId | Long | \u5730\u533a\u7f16\u53f7 |
| detailAddress | String | \u8be6\u7ec6\u5730\u5740 |
| defaultStatus | Boolean | \u662f\u5426\u9ed8\u8ba4\u5730\u5740 |

### AppMemberAddressDetailRes

包：`com.lemnt.member.adapter.app.address.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberId | Long | \u4f1a\u5458\u7f16\u53f7 |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| mobile | String | \u624b\u673a\u53f7 |
| areaId | Long | \u5730\u533a\u7f16\u53f7 |
| detailAddress | String | \u8be6\u7ec6\u5730\u5740 |
| defaultStatus | Boolean | \u662f\u5426\u9ed8\u8ba4\u5730\u5740 |

### AppMemberAddressListRes

包：`com.lemnt.member.adapter.app.address.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberId | Long | \u4f1a\u5458\u7f16\u53f7 |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| mobile | String | \u624b\u673a\u53f7 |
| areaId | Long | \u5730\u533a\u7f16\u53f7 |
| detailAddress | String | \u8be6\u7ec6\u5730\u5740 |
| defaultStatus | Boolean | \u662f\u5426\u9ed8\u8ba4\u5730\u5740 |

### AppMemberAddressUpdateRes

包：`com.lemnt.member.adapter.app.address.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberId | Long | \u4f1a\u5458\u7f16\u53f7 |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| mobile | String | \u624b\u673a\u53f7 |
| areaId | Long | \u5730\u533a\u7f16\u53f7 |
| detailAddress | String | \u8be6\u7ec6\u5730\u5740 |
| defaultStatus | Boolean | \u662f\u5426\u9ed8\u8ba4\u5730\u5740 |

### ExperienceRecordPageReq

包：`com.lemnt.member.adapter.app.growth.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| pageNum | Integer | \u9875\u7801\uff0c\u9ed8\u8ba41 |
| pageSize | Integer | \u6bcf\u9875\u6761\u6570\uff0c\u9ed8\u8ba420\uff0c\u6700\u5927100 |

### PointRecordPageReq

包：`com.lemnt.member.adapter.app.growth.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| pageNum | Integer | \u9875\u7801\uff0c\u9ed8\u8ba41 |
| pageSize | Integer | \u6bcf\u9875\u6761\u6570\uff0c\u9ed8\u8ba420\uff0c\u6700\u5927100 |

### SignRecordPageReq

包：`com.lemnt.member.adapter.app.growth.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| pageNum | Integer | \u9875\u7801\uff0c\u9ed8\u8ba41 |
| pageSize | Integer | \u6bcf\u9875\u6761\u6570\uff0c\u9ed8\u8ba420\uff0c\u6700\u5927100 |

### AppMemberGrowthExperienceRes

包：`com.lemnt.member.adapter.app.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberId | Long | \u4f1a\u5458\u7f16\u53f7 |
| assetType | String | \u8d44\u4ea7\uff1aPOINT\u79ef\u5206\uff0cEXPERIENCE\u7ecf\u9a8c |
| delta | Long | \u672c\u6b21\u6709\u7b26\u53f7\u53d8\u66f4\u91cf |
| balance | Long | \u53d8\u66f4\u540e\u4f59\u989d |
| bizType | String | \u4e1a\u52a1\u7c7b\u578b |
| bizNo | String | \u5e42\u7b49\u4e1a\u52a1\u7f16\u53f7 |
| reason | String | \u539f\u56e0 |
| createTime | LocalDateTime | \u6570\u636e\u5e93\u65f6\u95f4 |

### AppMemberGrowthPointRes

包：`com.lemnt.member.adapter.app.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberId | Long | \u4f1a\u5458\u7f16\u53f7 |
| assetType | String | \u8d44\u4ea7\uff1aPOINT\u79ef\u5206\uff0cEXPERIENCE\u7ecf\u9a8c |
| delta | Long | \u672c\u6b21\u6709\u7b26\u53f7\u53d8\u66f4\u91cf |
| balance | Long | \u53d8\u66f4\u540e\u4f59\u989d |
| bizType | String | \u4e1a\u52a1\u7c7b\u578b |
| bizNo | String | \u5e42\u7b49\u4e1a\u52a1\u7f16\u53f7 |
| reason | String | \u539f\u56e0 |
| createTime | LocalDateTime | \u6570\u636e\u5e93\u65f6\u95f4 |

### AppMemberGrowthSignRes

包：`com.lemnt.member.adapter.app.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberId | Long | \u4f1a\u5458\u7f16\u53f7 |
| signDate | LocalDate | \u7b7e\u5230\u4e1a\u52a1\u65e5\u671f |
| day | Integer | \u672c\u8f6e\u8fde\u7eed\u7b7e\u5230\u7b2c\u51e0\u5929 |
| point | Long | \u5956\u52b1\u79ef\u5206 |
| experience | Long | \u7ecf\u9a8c\u503c\u6216\u7b49\u7ea7\u5347\u7ea7\u95e8\u69db |

### AppMemberGrowthSignsRes

包：`com.lemnt.member.adapter.app.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberId | Long | \u4f1a\u5458\u7f16\u53f7 |
| signDate | LocalDate | \u7b7e\u5230\u4e1a\u52a1\u65e5\u671f |
| day | Integer | \u672c\u8f6e\u8fde\u7eed\u7b7e\u5230\u7b2c\u51e0\u5929 |
| point | Long | \u5956\u52b1\u79ef\u5206 |
| experience | Long | \u7ecf\u9a8c\u503c\u6216\u7b49\u7ea7\u5347\u7ea7\u95e8\u69db |

### LevelRes

包：`com.lemnt.member.adapter.app.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| level | Integer | \u7b49\u7ea7\u5e8f\u53f7 |
| experience | Long | \u7ecf\u9a8c\u503c\u6216\u7b49\u7ea7\u5347\u7ea7\u95e8\u69db |
| discountPercent | Integer | \u652f\u4ed8\u767e\u5206\u6bd4\uff0c98\u4e3a98\u6298 |
| icon | String | \u7b49\u7ea7\u56fe\u6807URL |
| backgroundUrl | String | \u7b49\u7ea7\u80cc\u666fURL |
| status | Integer | \u72b6\u6001\uff1a1\u542f\u7528\uff0c0\u505c\u7528 |

### SignConfigRes

包：`com.lemnt.member.adapter.app.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| day | Integer | \u672c\u8f6e\u8fde\u7eed\u7b7e\u5230\u7b2c\u51e0\u5929 |
| point | Long | \u5956\u52b1\u79ef\u5206 |
| experience | Long | \u7ecf\u9a8c\u503c\u6216\u7b49\u7ea7\u5347\u7ea7\u95e8\u69db |
| status | Integer | \u72b6\u6001\uff1a1\u542f\u7528\uff0c0\u505c\u7528 |

### SignSummaryRes

包：`com.lemnt.member.adapter.app.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| totalDay | long | \u7d2f\u8ba1\u7b7e\u5230\u5929\u6570 |
| continuousDay | int | \u5f53\u524d\u5468\u671f\u8fde\u7eed\u5929\u6570 |
| todaySignIn | boolean | \u4eca\u65e5\u662f\u5426\u7b7e\u5230 |

### PasswordChangeReq

包：`com.lemnt.member.adapter.app.user.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| oldPassword | String | \u5f53\u524d\u5bc6\u7801 |
| newPassword | String | \u65b0\u5bc6\u7801\uff0c\u81f3\u5c1110\u5b57\u7b26\u4e14\u6700\u591a72 UTF-8\u5b57\u8282 |

### ProfileUpdateReq

包：`com.lemnt.member.adapter.app.user.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| nickname | String | \u6635\u79f0 |
| email | String | \u90ae\u7bb1 |
| avatar | String | \u5934\u50cfHTTPS\u5730\u5740 |
| sex | Integer | \u6027\u522b\uff1a0\u672a\u77e5\uff0c1\u7537\uff0c2\u5973 |
| birthday | LocalDate | \u51fa\u751f\u65e5\u671f |

### ProfileRes

包：`com.lemnt.member.adapter.app.user.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberNo | String | \u4f1a\u5458\u4e1a\u52a1\u7f16\u53f7 |
| nickname | String | \u6635\u79f0 |
| mobile | String | \u624b\u673a\u53f7 |
| email | String | \u90ae\u7bb1 |
| avatar | String | \u5934\u50cfHTTPS\u5730\u5740 |
| status | Integer | \u72b6\u6001\uff1a1\u542f\u7528\uff0c0\u505c\u7528 |
| points | Long | \u5f53\u524d\u79ef\u5206 |
| experience | Long | \u7ecf\u9a8c\u503c\u6216\u7b49\u7ea7\u5347\u7ea7\u95e8\u69db |
| levelId | Long | \u7b49\u7ea7\u7f16\u53f7\uff0c\u53ef\u7a7a |
| groupId | Long | \u5206\u7ec4\u7f16\u53f7\uff0c\u53ef\u7a7a |
| sex | Integer | \u6027\u522b\uff1a0\u672a\u77e5\uff0c1\u7537\uff0c2\u5973 |
| birthday | LocalDate | \u51fa\u751f\u65e5\u671f |
| createTime | LocalDateTime | \u6570\u636e\u5e93\u65f6\u95f4 |

### AddressRes

包：`com.lemnt.member.adapter.web.address.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberId | Long | \u4f1a\u5458\u7f16\u53f7 |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| mobile | String | \u624b\u673a\u53f7 |
| areaId | Long | \u5730\u533a\u7f16\u53f7 |
| detailAddress | String | \u8be6\u7ec6\u5730\u5740 |
| defaultStatus | Boolean | \u662f\u5426\u9ed8\u8ba4\u5730\u5740 |

### ConfigSaveReq

包：`com.lemnt.member.adapter.web.growth.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| pointTradeDeductEnable | Boolean | \u79ef\u5206\u62b5\u6263\u5f00\u5173\uff0c\u4ea4\u6613\u96c6\u6210\u53e6\u884c\u63a5\u5165 |
| pointTradeDeductUnitPrice | Integer | \u6bcf\u79ef\u5206\u62b5\u6263\u91d1\u989d\uff0c\u5206 |
| pointTradeDeductMaxPrice | Integer | \u79ef\u5206\u62b5\u6263\u91d1\u989d\u4e0a\u9650\uff0c\u5206 |
| pointTradeGivePoint | Integer | \u6bcf\u5143\u8d60\u9001\u79ef\u5206\u6570 |

### LevelAssignReq

包：`com.lemnt.member.adapter.web.growth.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| levelId | Long | \u7b49\u7ea7\u7f16\u53f7\uff0c\u53ef\u7a7a |
| requestNo | String | \u5e42\u7b49\u4e1a\u52a1\u7f16\u53f7\uff0c\u91cd\u8bd5\u4e0d\u53ef\u66f4\u6539\u5185\u5bb9 |
| reason | String | \u539f\u56e0 |

### MemberCatalogQueryGroupsPageReq

包：`com.lemnt.member.adapter.web.growth.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| memberId | Long | \u53ef\u9009\u4f1a\u5458ID\uff1b\u672a\u586b\u67e5\u8be2\u6240\u6709\u4f1a\u5458 |
| name | String | \u540d\u79f0\u5305\u542b\u5339\u914d |
| status | Integer | \u72b6\u6001\uff1a0\u7981\u7528\uff0c1\u542f\u7528 |
| pageNum | Integer | \u9875\u7801\uff0c\u9ed8\u8ba41 |
| pageSize | Integer | \u9875\u5927\u5c0f\uff0c\u9ed8\u8ba420\uff0c\u6700\u5927100 |

### MemberCatalogQueryTagsPageReq

包：`com.lemnt.member.adapter.web.growth.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| memberId | Long | \u53ef\u9009\u4f1a\u5458ID\uff1b\u672a\u586b\u67e5\u8be2\u6240\u6709\u4f1a\u5458 |
| name | String | \u540d\u79f0\u5305\u542b\u5339\u914d |
| status | Integer | \u72b6\u6001\uff1a0\u7981\u7528\uff0c1\u542f\u7528 |
| pageNum | Integer | \u9875\u7801\uff0c\u9ed8\u8ba41 |
| pageSize | Integer | \u9875\u5927\u5c0f\uff0c\u9ed8\u8ba420\uff0c\u6700\u5927100 |

### MemberLevelCreateReq

包：`com.lemnt.member.adapter.web.growth.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| level | Integer | \u7b49\u7ea7\u5e8f\u53f7 |
| experience | Long | \u7ecf\u9a8c\u503c\u6216\u7b49\u7ea7\u5347\u7ea7\u95e8\u69db |
| discountPercent | Integer | \u652f\u4ed8\u767e\u5206\u6bd4\uff0c98\u4e3a98\u6298 |
| icon | String | \u7b49\u7ea7\u56fe\u6807URL |
| backgroundUrl | String | \u7b49\u7ea7\u80cc\u666fURL |
| status | Integer | \u72b6\u6001\uff1a1\u542f\u7528\uff0c0\u505c\u7528 |

### MemberLevelUpdateReq

包：`com.lemnt.member.adapter.web.growth.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| level | Integer | \u7b49\u7ea7\u5e8f\u53f7 |
| experience | Long | \u7ecf\u9a8c\u503c\u6216\u7b49\u7ea7\u5347\u7ea7\u95e8\u69db |
| discountPercent | Integer | \u652f\u4ed8\u767e\u5206\u6bd4\uff0c98\u4e3a98\u6298 |
| icon | String | \u7b49\u7ea7\u56fe\u6807URL |
| backgroundUrl | String | \u7b49\u7ea7\u80cc\u666fURL |
| status | Integer | \u72b6\u6001\uff1a1\u542f\u7528\uff0c0\u505c\u7528 |

### MemberRecordExperienceRecordsReq

包：`com.lemnt.member.adapter.web.growth.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| memberId | Long | \u53ef\u9009\u4f1a\u5458ID\uff1b\u672a\u586b\u67e5\u8be2\u6240\u6709\u4f1a\u5458 |
| name | String | \u540d\u79f0\u5305\u542b\u5339\u914d |
| status | Integer | \u72b6\u6001\uff1a0\u7981\u7528\uff0c1\u542f\u7528 |
| pageNum | Integer | \u9875\u7801\uff0c\u9ed8\u8ba41 |
| pageSize | Integer | \u9875\u5927\u5c0f\uff0c\u9ed8\u8ba420\uff0c\u6700\u5927100 |

### MemberRecordLevelRecordsReq

包：`com.lemnt.member.adapter.web.growth.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| memberId | Long | \u53ef\u9009\u4f1a\u5458ID\uff1b\u672a\u586b\u67e5\u8be2\u6240\u6709\u4f1a\u5458 |
| name | String | \u540d\u79f0\u5305\u542b\u5339\u914d |
| status | Integer | \u72b6\u6001\uff1a0\u7981\u7528\uff0c1\u542f\u7528 |
| pageNum | Integer | \u9875\u7801\uff0c\u9ed8\u8ba41 |
| pageSize | Integer | \u9875\u5927\u5c0f\uff0c\u9ed8\u8ba420\uff0c\u6700\u5927100 |

### MemberRecordPointRecordsReq

包：`com.lemnt.member.adapter.web.growth.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| memberId | Long | \u53ef\u9009\u4f1a\u5458ID\uff1b\u672a\u586b\u67e5\u8be2\u6240\u6709\u4f1a\u5458 |
| name | String | \u540d\u79f0\u5305\u542b\u5339\u914d |
| status | Integer | \u72b6\u6001\uff1a0\u7981\u7528\uff0c1\u542f\u7528 |
| pageNum | Integer | \u9875\u7801\uff0c\u9ed8\u8ba41 |
| pageSize | Integer | \u9875\u5927\u5c0f\uff0c\u9ed8\u8ba420\uff0c\u6700\u5927100 |

### MemberRecordSigninRecordsReq

包：`com.lemnt.member.adapter.web.growth.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| memberId | Long | \u53ef\u9009\u4f1a\u5458ID\uff1b\u672a\u586b\u67e5\u8be2\u6240\u6709\u4f1a\u5458 |
| name | String | \u540d\u79f0\u5305\u542b\u5339\u914d |
| status | Integer | \u72b6\u6001\uff1a0\u7981\u7528\uff0c1\u542f\u7528 |
| pageNum | Integer | \u9875\u7801\uff0c\u9ed8\u8ba41 |
| pageSize | Integer | \u9875\u5927\u5c0f\uff0c\u9ed8\u8ba420\uff0c\u6700\u5927100 |

### MemberUserExperienceReq

包：`com.lemnt.member.adapter.web.growth.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| delta | Long | \u672c\u6b21\u6709\u7b26\u53f7\u53d8\u66f4\u91cf |
| requestNo | String | \u5e42\u7b49\u4e1a\u52a1\u7f16\u53f7\uff0c\u91cd\u8bd5\u4e0d\u53ef\u66f4\u6539\u5185\u5bb9 |
| reason | String | \u539f\u56e0 |

### MemberUserPointsReq

包：`com.lemnt.member.adapter.web.growth.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| delta | Long | \u672c\u6b21\u6709\u7b26\u53f7\u53d8\u66f4\u91cf |
| requestNo | String | \u5e42\u7b49\u4e1a\u52a1\u7f16\u53f7\uff0c\u91cd\u8bd5\u4e0d\u53ef\u66f4\u6539\u5185\u5bb9 |
| reason | String | \u539f\u56e0 |

### SignConfigSaveReq

包：`com.lemnt.member.adapter.web.growth.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| day | Integer | \u672c\u8f6e\u8fde\u7eed\u7b7e\u5230\u7b2c\u51e0\u5929 |
| point | Long | \u5956\u52b1\u79ef\u5206 |
| experience | Long | \u7ecf\u9a8c\u503c\u6216\u7b49\u7ea7\u5347\u7ea7\u95e8\u69db |
| status | Integer | \u72b6\u6001\uff1a1\u542f\u7528\uff0c0\u505c\u7528 |

### ConfigRes

包：`com.lemnt.member.adapter.web.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| pointTradeDeductEnable | Boolean | \u79ef\u5206\u62b5\u6263\u5f00\u5173\uff0c\u4ea4\u6613\u96c6\u6210\u53e6\u884c\u63a5\u5165 |
| pointTradeDeductUnitPrice | Integer | \u6bcf\u79ef\u5206\u62b5\u6263\u91d1\u989d\uff0c\u5206 |
| pointTradeDeductMaxPrice | Integer | \u79ef\u5206\u62b5\u6263\u91d1\u989d\u4e0a\u9650\uff0c\u5206 |
| pointTradeGivePoint | Integer | \u6bcf\u5143\u8d60\u9001\u79ef\u5206\u6570 |

### MemberConfigDetailRes

包：`com.lemnt.member.adapter.web.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| day | Integer | \u672c\u8f6e\u8fde\u7eed\u7b7e\u5230\u7b2c\u51e0\u5929 |
| point | Long | \u5956\u52b1\u79ef\u5206 |
| experience | Long | \u7ecf\u9a8c\u503c\u6216\u7b49\u7ea7\u5347\u7ea7\u95e8\u69db |
| status | Integer | \u72b6\u6001\uff1a1\u542f\u7528\uff0c0\u505c\u7528 |

### MemberConfigListRes

包：`com.lemnt.member.adapter.web.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| day | Integer | \u672c\u8f6e\u8fde\u7eed\u7b7e\u5230\u7b2c\u51e0\u5929 |
| point | Long | \u5956\u52b1\u79ef\u5206 |
| experience | Long | \u7ecf\u9a8c\u503c\u6216\u7b49\u7ea7\u5347\u7ea7\u95e8\u69db |
| status | Integer | \u72b6\u6001\uff1a1\u542f\u7528\uff0c0\u505c\u7528 |

### MemberLevelDetailRes

包：`com.lemnt.member.adapter.web.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| level | Integer | \u7b49\u7ea7\u5e8f\u53f7 |
| experience | Long | \u7ecf\u9a8c\u503c\u6216\u7b49\u7ea7\u5347\u7ea7\u95e8\u69db |
| discountPercent | Integer | \u652f\u4ed8\u767e\u5206\u6bd4\uff0c98\u4e3a98\u6298 |
| icon | String | \u7b49\u7ea7\u56fe\u6807URL |
| backgroundUrl | String | \u7b49\u7ea7\u80cc\u666fURL |
| status | Integer | \u72b6\u6001\uff1a1\u542f\u7528\uff0c0\u505c\u7528 |

### MemberLevelListRes

包：`com.lemnt.member.adapter.web.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| level | Integer | \u7b49\u7ea7\u5e8f\u53f7 |
| experience | Long | \u7ecf\u9a8c\u503c\u6216\u7b49\u7ea7\u5347\u7ea7\u95e8\u69db |
| discountPercent | Integer | \u652f\u4ed8\u767e\u5206\u6bd4\uff0c98\u4e3a98\u6298 |
| icon | String | \u7b49\u7ea7\u56fe\u6807URL |
| backgroundUrl | String | \u7b49\u7ea7\u80cc\u666fURL |
| status | Integer | \u72b6\u6001\uff1a1\u542f\u7528\uff0c0\u505c\u7528 |

### MemberLevelSimpleRes

包：`com.lemnt.member.adapter.web.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| level | Integer | \u7b49\u7ea7\u5e8f\u53f7 |
| experience | Long | \u7ecf\u9a8c\u503c\u6216\u7b49\u7ea7\u5347\u7ea7\u95e8\u69db |
| discountPercent | Integer | \u652f\u4ed8\u767e\u5206\u6bd4\uff0c98\u4e3a98\u6298 |
| icon | String | \u7b49\u7ea7\u56fe\u6807URL |
| backgroundUrl | String | \u7b49\u7ea7\u80cc\u666fURL |
| status | Integer | \u72b6\u6001\uff1a1\u542f\u7528\uff0c0\u505c\u7528 |

### MemberRecordExperienceRecordsDetailRes

包：`com.lemnt.member.adapter.web.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberId | Long | \u4f1a\u5458\u7f16\u53f7 |
| assetType | String | \u8d44\u4ea7\uff1aPOINT\u79ef\u5206\uff0cEXPERIENCE\u7ecf\u9a8c |
| delta | Long | \u672c\u6b21\u6709\u7b26\u53f7\u53d8\u66f4\u91cf |
| balance | Long | \u53d8\u66f4\u540e\u4f59\u989d |
| bizType | String | \u4e1a\u52a1\u7c7b\u578b |
| bizNo | String | \u5e42\u7b49\u4e1a\u52a1\u7f16\u53f7 |
| reason | String | \u539f\u56e0 |
| createTime | LocalDateTime | \u6570\u636e\u5e93\u65f6\u95f4 |

### MemberRecordExperienceRecordsRes

包：`com.lemnt.member.adapter.web.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberId | Long | \u4f1a\u5458\u7f16\u53f7 |
| assetType | String | \u8d44\u4ea7\uff1aPOINT\u79ef\u5206\uff0cEXPERIENCE\u7ecf\u9a8c |
| delta | Long | \u672c\u6b21\u6709\u7b26\u53f7\u53d8\u66f4\u91cf |
| balance | Long | \u53d8\u66f4\u540e\u4f59\u989d |
| bizType | String | \u4e1a\u52a1\u7c7b\u578b |
| bizNo | String | \u5e42\u7b49\u4e1a\u52a1\u7f16\u53f7 |
| reason | String | \u539f\u56e0 |
| createTime | LocalDateTime | \u6570\u636e\u5e93\u65f6\u95f4 |

### MemberRecordLevelRecordsDetailRes

包：`com.lemnt.member.adapter.web.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberId | Long | \u4f1a\u5458\u7f16\u53f7 |
| levelId | Long | \u7b49\u7ea7\u7f16\u53f7\uff0c\u53ef\u7a7a |
| experience | Long | \u7ecf\u9a8c\u503c\u6216\u7b49\u7ea7\u5347\u7ea7\u95e8\u69db |
| reason | String | \u539f\u56e0 |
| createTime | LocalDateTime | \u6570\u636e\u5e93\u65f6\u95f4 |

### MemberRecordLevelRecordsRes

包：`com.lemnt.member.adapter.web.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberId | Long | \u4f1a\u5458\u7f16\u53f7 |
| levelId | Long | \u7b49\u7ea7\u7f16\u53f7\uff0c\u53ef\u7a7a |
| experience | Long | \u7ecf\u9a8c\u503c\u6216\u7b49\u7ea7\u5347\u7ea7\u95e8\u69db |
| reason | String | \u539f\u56e0 |
| createTime | LocalDateTime | \u6570\u636e\u5e93\u65f6\u95f4 |

### MemberRecordPointRecordsDetailRes

包：`com.lemnt.member.adapter.web.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberId | Long | \u4f1a\u5458\u7f16\u53f7 |
| assetType | String | \u8d44\u4ea7\uff1aPOINT\u79ef\u5206\uff0cEXPERIENCE\u7ecf\u9a8c |
| delta | Long | \u672c\u6b21\u6709\u7b26\u53f7\u53d8\u66f4\u91cf |
| balance | Long | \u53d8\u66f4\u540e\u4f59\u989d |
| bizType | String | \u4e1a\u52a1\u7c7b\u578b |
| bizNo | String | \u5e42\u7b49\u4e1a\u52a1\u7f16\u53f7 |
| reason | String | \u539f\u56e0 |
| createTime | LocalDateTime | \u6570\u636e\u5e93\u65f6\u95f4 |

### MemberRecordPointRecordsRes

包：`com.lemnt.member.adapter.web.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberId | Long | \u4f1a\u5458\u7f16\u53f7 |
| assetType | String | \u8d44\u4ea7\uff1aPOINT\u79ef\u5206\uff0cEXPERIENCE\u7ecf\u9a8c |
| delta | Long | \u672c\u6b21\u6709\u7b26\u53f7\u53d8\u66f4\u91cf |
| balance | Long | \u53d8\u66f4\u540e\u4f59\u989d |
| bizType | String | \u4e1a\u52a1\u7c7b\u578b |
| bizNo | String | \u5e42\u7b49\u4e1a\u52a1\u7f16\u53f7 |
| reason | String | \u539f\u56e0 |
| createTime | LocalDateTime | \u6570\u636e\u5e93\u65f6\u95f4 |

### MemberRecordSigninRecordsRes

包：`com.lemnt.member.adapter.web.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberId | Long | \u4f1a\u5458\u7f16\u53f7 |
| signDate | LocalDate | \u7b7e\u5230\u4e1a\u52a1\u65e5\u671f |
| day | Integer | \u672c\u8f6e\u8fde\u7eed\u7b7e\u5230\u7b2c\u51e0\u5929 |
| point | Long | \u5956\u52b1\u79ef\u5206 |
| experience | Long | \u7ecf\u9a8c\u503c\u6216\u7b49\u7ea7\u5347\u7ea7\u95e8\u69db |

### MemberUserExperienceRecordsRes

包：`com.lemnt.member.adapter.web.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberId | Long | \u4f1a\u5458\u7f16\u53f7 |
| assetType | String | \u8d44\u4ea7\uff1aPOINT\u79ef\u5206\uff0cEXPERIENCE\u7ecf\u9a8c |
| delta | Long | \u672c\u6b21\u6709\u7b26\u53f7\u53d8\u66f4\u91cf |
| balance | Long | \u53d8\u66f4\u540e\u4f59\u989d |
| bizType | String | \u4e1a\u52a1\u7c7b\u578b |
| bizNo | String | \u5e42\u7b49\u4e1a\u52a1\u7f16\u53f7 |
| reason | String | \u539f\u56e0 |
| createTime | LocalDateTime | \u6570\u636e\u5e93\u65f6\u95f4 |

### MemberUserExperienceRes

包：`com.lemnt.member.adapter.web.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberId | Long | \u4f1a\u5458\u7f16\u53f7 |
| assetType | String | \u8d44\u4ea7\uff1aPOINT\u79ef\u5206\uff0cEXPERIENCE\u7ecf\u9a8c |
| delta | Long | \u672c\u6b21\u6709\u7b26\u53f7\u53d8\u66f4\u91cf |
| balance | Long | \u53d8\u66f4\u540e\u4f59\u989d |
| bizType | String | \u4e1a\u52a1\u7c7b\u578b |
| bizNo | String | \u5e42\u7b49\u4e1a\u52a1\u7f16\u53f7 |
| reason | String | \u539f\u56e0 |
| createTime | LocalDateTime | \u6570\u636e\u5e93\u65f6\u95f4 |

### MemberUserLevelRecordsRes

包：`com.lemnt.member.adapter.web.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberId | Long | \u4f1a\u5458\u7f16\u53f7 |
| levelId | Long | \u7b49\u7ea7\u7f16\u53f7\uff0c\u53ef\u7a7a |
| experience | Long | \u7ecf\u9a8c\u503c\u6216\u7b49\u7ea7\u5347\u7ea7\u95e8\u69db |
| reason | String | \u539f\u56e0 |
| createTime | LocalDateTime | \u6570\u636e\u5e93\u65f6\u95f4 |

### MemberUserPointsRecordsRes

包：`com.lemnt.member.adapter.web.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberId | Long | \u4f1a\u5458\u7f16\u53f7 |
| assetType | String | \u8d44\u4ea7\uff1aPOINT\u79ef\u5206\uff0cEXPERIENCE\u7ecf\u9a8c |
| delta | Long | \u672c\u6b21\u6709\u7b26\u53f7\u53d8\u66f4\u91cf |
| balance | Long | \u53d8\u66f4\u540e\u4f59\u989d |
| bizType | String | \u4e1a\u52a1\u7c7b\u578b |
| bizNo | String | \u5e42\u7b49\u4e1a\u52a1\u7f16\u53f7 |
| reason | String | \u539f\u56e0 |
| createTime | LocalDateTime | \u6570\u636e\u5e93\u65f6\u95f4 |

### MemberUserPointsRes

包：`com.lemnt.member.adapter.web.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberId | Long | \u4f1a\u5458\u7f16\u53f7 |
| assetType | String | \u8d44\u4ea7\uff1aPOINT\u79ef\u5206\uff0cEXPERIENCE\u7ecf\u9a8c |
| delta | Long | \u672c\u6b21\u6709\u7b26\u53f7\u53d8\u66f4\u91cf |
| balance | Long | \u53d8\u66f4\u540e\u4f59\u989d |
| bizType | String | \u4e1a\u52a1\u7c7b\u578b |
| bizNo | String | \u5e42\u7b49\u4e1a\u52a1\u7f16\u53f7 |
| reason | String | \u539f\u56e0 |
| createTime | LocalDateTime | \u6570\u636e\u5e93\u65f6\u95f4 |

### MemberUserSignsRes

包：`com.lemnt.member.adapter.web.growth.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberId | Long | \u4f1a\u5458\u7f16\u53f7 |
| signDate | LocalDate | \u7b7e\u5230\u4e1a\u52a1\u65e5\u671f |
| day | Integer | \u672c\u8f6e\u8fde\u7eed\u7b7e\u5230\u7b2c\u51e0\u5929 |
| point | Long | \u5956\u52b1\u79ef\u5206 |
| experience | Long | \u7ecf\u9a8c\u503c\u6216\u7b49\u7ea7\u5347\u7ea7\u95e8\u69db |

### GroupAssignReq

包：`com.lemnt.member.adapter.web.user.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| groupId | Long | \u5206\u7ec4\u7f16\u53f7\uff0c\u53ef\u7a7a |

### MemberCreateReq

包：`com.lemnt.member.adapter.web.user.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| nickname | String | \u6635\u79f0 |
| mobile | String | \u624b\u673a\u53f7 |
| email | String | \u90ae\u7bb1 |
| avatar | String | \u5934\u50cfHTTPS\u5730\u5740 |

### MemberGroupCreateReq

包：`com.lemnt.member.adapter.web.user.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| remark | String | \u5907\u6ce8 |
| status | Integer | \u72b6\u6001\uff1a1\u542f\u7528\uff0c0\u505c\u7528 |

### MemberGroupUpdateReq

包：`com.lemnt.member.adapter.web.user.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| remark | String | \u5907\u6ce8 |
| status | Integer | \u72b6\u6001\uff1a1\u542f\u7528\uff0c0\u505c\u7528 |

### MemberProfileUpdateReq

包：`com.lemnt.member.adapter.web.user.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| nickname | String | \u6635\u79f0 |
| email | String | \u90ae\u7bb1 |
| avatar | String | \u5934\u50cfHTTPS\u5730\u5740 |
| sex | Integer | \u6027\u522b\uff1a0\u672a\u77e5\uff0c1\u7537\uff0c2\u5973 |
| birthday | LocalDate | \u51fa\u751f\u65e5\u671f |
| status | Integer | \u72b6\u6001\uff1a1\u542f\u7528\uff0c0\u505c\u7528 |

### MemberRegisterReq

包：`com.lemnt.member.adapter.web.user.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| nickname | String | \u6635\u79f0 |
| mobile | String | \u624b\u673a\u53f7 |
| email | String | \u90ae\u7bb1 |
| avatar | String | \u5934\u50cf\u5730\u5740 |

### MemberTagCreateReq

包：`com.lemnt.member.adapter.web.user.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| remark | String | \u5907\u6ce8 |

### MemberTagUpdateReq

包：`com.lemnt.member.adapter.web.user.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| remark | String | \u5907\u6ce8 |

### MemberUserExperienceRecordsReq

包：`com.lemnt.member.adapter.web.user.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| mobile | String | \u624b\u673a\u53f7 |
| pageNum | Integer | \u9875\u7801\uff0c\u9ed8\u8ba41 |
| pageSize | Integer | \u6bcf\u9875\u6761\u6570\uff0c\u9ed8\u8ba420\uff0c\u6700\u5927100 |

### MemberUserLevelRecordsReq

包：`com.lemnt.member.adapter.web.user.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| mobile | String | \u624b\u673a\u53f7 |
| pageNum | Integer | \u9875\u7801\uff0c\u9ed8\u8ba41 |
| pageSize | Integer | \u6bcf\u9875\u6761\u6570\uff0c\u9ed8\u8ba420\uff0c\u6700\u5927100 |

### MemberUserPageReq

包：`com.lemnt.member.adapter.web.user.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| mobile | String | \u624b\u673a\u53f7 |
| pageNum | Integer | \u9875\u7801\uff0c\u9ed8\u8ba41 |
| pageSize | Integer | \u6bcf\u9875\u6761\u6570\uff0c\u9ed8\u8ba420\uff0c\u6700\u5927100 |

### MemberUserPointsRecordsReq

包：`com.lemnt.member.adapter.web.user.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| mobile | String | \u624b\u673a\u53f7 |
| pageNum | Integer | \u9875\u7801\uff0c\u9ed8\u8ba41 |
| pageSize | Integer | \u6bcf\u9875\u6761\u6570\uff0c\u9ed8\u8ba420\uff0c\u6700\u5927100 |

### MemberUserSignsReq

包：`com.lemnt.member.adapter.web.user.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| mobile | String | \u624b\u673a\u53f7 |
| pageNum | Integer | \u9875\u7801\uff0c\u9ed8\u8ba41 |
| pageSize | Integer | \u6bcf\u9875\u6761\u6570\uff0c\u9ed8\u8ba420\uff0c\u6700\u5927100 |

### PasswordResetReq

包：`com.lemnt.member.adapter.web.user.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| password | String | \u5bc6\u7801\uff0c\u81f3\u5c1110\u5b57\u7b26\u4e14\u6700\u591a72 UTF-8\u5b57\u8282 |

### TagsAssignReq

包：`com.lemnt.member.adapter.web.user.req`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| tagIds | List<Long> | \u6807\u7b7e\u7f16\u53f7\uff0c\u7a7a\u96c6\u5408\u6e05\u7a7a\u6807\u7b7e |

### MemberCatalogQueryGroupsPageRes

包：`com.lemnt.member.adapter.web.user.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| remark | String | \u5907\u6ce8 |
| status | Integer | \u72b6\u6001\uff1a1\u542f\u7528\uff0c0\u505c\u7528 |

### MemberCatalogQueryTagsPageRes

包：`com.lemnt.member.adapter.web.user.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| remark | String | \u5907\u6ce8 |

### MemberDetailRes

包：`com.lemnt.member.adapter.web.user.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u4f1a\u5458\u7f16\u53f7 |
| memberNo | String | \u4f1a\u5458\u4e1a\u52a1\u7f16\u53f7 |
| nickname | String | \u6635\u79f0 |
| mobile | String | \u624b\u673a\u53f7 |
| email | String | \u90ae\u7bb1 |
| avatar | String | \u5934\u50cf\u5730\u5740 |
| status | Integer | \u72b6\u6001 |
| points | Long | \u79ef\u5206 |

### MemberGroupDetailRes

包：`com.lemnt.member.adapter.web.user.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| remark | String | \u5907\u6ce8 |
| status | Integer | \u72b6\u6001\uff1a1\u542f\u7528\uff0c0\u505c\u7528 |

### MemberGroupListRes

包：`com.lemnt.member.adapter.web.user.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| remark | String | \u5907\u6ce8 |
| status | Integer | \u72b6\u6001\uff1a1\u542f\u7528\uff0c0\u505c\u7528 |

### MemberGroupSimpleRes

包：`com.lemnt.member.adapter.web.user.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| remark | String | \u5907\u6ce8 |
| status | Integer | \u72b6\u6001\uff1a1\u542f\u7528\uff0c0\u505c\u7528 |

### MemberRegisterRes

包：`com.lemnt.member.adapter.web.user.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u4f1a\u5458\u7f16\u53f7 |
| memberNo | String | \u4f1a\u5458\u4e1a\u52a1\u7f16\u53f7 |
| nickname | String | \u6635\u79f0 |
| mobile | String | \u624b\u673a\u53f7 |
| email | String | \u90ae\u7bb1 |
| avatar | String | \u5934\u50cf\u5730\u5740 |
| status | Integer | \u72b6\u6001 |
| points | Long | \u79ef\u5206 |

### MemberTagDetailRes

包：`com.lemnt.member.adapter.web.user.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| remark | String | \u5907\u6ce8 |

### MemberTagListRes

包：`com.lemnt.member.adapter.web.user.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| remark | String | \u5907\u6ce8 |

### MemberTagSimpleRes

包：`com.lemnt.member.adapter.web.user.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| name | String | \u540d\u79f0\u6216\u6536\u4ef6\u4eba |
| remark | String | \u5907\u6ce8 |

### MemberUserCreateRes

包：`com.lemnt.member.adapter.web.user.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberNo | String | \u4f1a\u5458\u4e1a\u52a1\u7f16\u53f7 |
| nickname | String | \u6635\u79f0 |
| mobile | String | \u624b\u673a\u53f7 |
| email | String | \u90ae\u7bb1 |
| avatar | String | \u5934\u50cfHTTPS\u5730\u5740 |
| status | Integer | \u72b6\u6001\uff1a1\u542f\u7528\uff0c0\u505c\u7528 |
| points | Long | \u5f53\u524d\u79ef\u5206 |
| experience | Long | \u7ecf\u9a8c\u503c\u6216\u7b49\u7ea7\u5347\u7ea7\u95e8\u69db |
| levelId | Long | \u7b49\u7ea7\u7f16\u53f7\uff0c\u53ef\u7a7a |
| groupId | Long | \u5206\u7ec4\u7f16\u53f7\uff0c\u53ef\u7a7a |
| sex | Integer | \u6027\u522b\uff1a0\u672a\u77e5\uff0c1\u7537\uff0c2\u5973 |
| birthday | LocalDate | \u51fa\u751f\u65e5\u671f |
| createTime | LocalDateTime | \u6570\u636e\u5e93\u65f6\u95f4 |

### MemberUserDetailRes

包：`com.lemnt.member.adapter.web.user.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberNo | String | \u4f1a\u5458\u4e1a\u52a1\u7f16\u53f7 |
| nickname | String | \u6635\u79f0 |
| mobile | String | \u624b\u673a\u53f7 |
| email | String | \u90ae\u7bb1 |
| avatar | String | \u5934\u50cfHTTPS\u5730\u5740 |
| status | Integer | \u72b6\u6001\uff1a1\u542f\u7528\uff0c0\u505c\u7528 |
| points | Long | \u5f53\u524d\u79ef\u5206 |
| experience | Long | \u7ecf\u9a8c\u503c\u6216\u7b49\u7ea7\u5347\u7ea7\u95e8\u69db |
| levelId | Long | \u7b49\u7ea7\u7f16\u53f7\uff0c\u53ef\u7a7a |
| groupId | Long | \u5206\u7ec4\u7f16\u53f7\uff0c\u53ef\u7a7a |
| sex | Integer | \u6027\u522b\uff1a0\u672a\u77e5\uff0c1\u7537\uff0c2\u5973 |
| birthday | LocalDate | \u51fa\u751f\u65e5\u671f |
| createTime | LocalDateTime | \u6570\u636e\u5e93\u65f6\u95f4 |

### MemberUserPageRes

包：`com.lemnt.member.adapter.web.user.res`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | \u8bb0\u5f55\u7f16\u53f7 |
| memberNo | String | \u4f1a\u5458\u4e1a\u52a1\u7f16\u53f7 |
| nickname | String | \u6635\u79f0 |
| mobile | String | \u624b\u673a\u53f7 |
| email | String | \u90ae\u7bb1 |
| avatar | String | \u5934\u50cfHTTPS\u5730\u5740 |
| status | Integer | \u72b6\u6001\uff1a1\u542f\u7528\uff0c0\u505c\u7528 |
| points | Long | \u5f53\u524d\u79ef\u5206 |
| experience | Long | \u7ecf\u9a8c\u503c\u6216\u7b49\u7ea7\u5347\u7ea7\u95e8\u69db |
| levelId | Long | \u7b49\u7ea7\u7f16\u53f7\uff0c\u53ef\u7a7a |
| groupId | Long | \u5206\u7ec4\u7f16\u53f7\uff0c\u53ef\u7a7a |
| sex | Integer | \u6027\u522b\uff1a0\u672a\u77e5\uff0c1\u7537\uff0c2\u5973 |
| birthday | LocalDate | \u51fa\u751f\u65e5\u671f |
| createTime | LocalDateTime | \u6570\u636e\u5e93\u65f6\u95f4 |
