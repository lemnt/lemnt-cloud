# 会员接口迁移

版本 v5，2026-10-25；基础提交 8eac9f6。参考 YunaiV/yudao-cloud `8db9153bfe6bdc03baf93b74e244a2e74477c5c4` 的 member 控制器。本文按代码静态核对生成；JSON/curl 均为构造示例，供应商与部署环境尚未实测。设计依据见 work-items/yudao-member-mall-trade-migration/03-development-design.md。模板参考 code-software-engineer/references/api-documentation.md。

## 边界与运行

- `/member/app/**` 由小程序消费，会员 ID 来自独立 Bearer 会话；禁止把管理员身份当会员。`/member/admin/**` 为后台接口，使用现有管理员权限机制。旧 `/members/register` 现在要求 `member:user:create`，不再匿名注册。后台创建资料不自动设置密码或签发会话。
- 密码登录只接受手机号，BCrypt12，最短10字符、最长72 UTF-8字节。未知账号执行虚拟密码比对。每个手机号连续5次失败后15分钟窗口拒绝登录。
- 独立随机 accessToken 有效2小时，refreshToken 30天，仅保存 SHA256；刷新同时轮换两者，旧令牌不可重用。注销、密码修改/重置、手机号更新和停用撤销相关会话。令牌应保存到小程序安全会话存储，禁止写入日志。
- 短信场景 LOGIN/RESET_PASSWORD/UPDATE_OLD_MOBILE/UPDATE_NEW_MOBILE；有效5分钟、最多5次错误、60秒间隔、每手机号每场景20次/数据库日、全局默认100次/日。发送前预算提交，外部失败仍保留限流，避免不确定投递绕过预算。验证接口不消费验证码；登录/重置/换绑才消费。换绑须验证原号码及新号码。短信渠道目前仅支持中国大陆手机。
- 微信仅支持 WECHAT_MINI、WECHAT_MP；客户端提交临时 code，服务器通过 SDK 兑换，不接受客户端 openid/memberId/session_key。先生成 LOGIN/BIND state（5分钟、一次性、提供方+appId+目的+会员绑定），再携带 state 登录/绑定。解绑最后一个身份必须有手机号及密码。其他第三方提供方不支持；不是芋道所有第三方配置的逐字节兼容迁移。
- 地址最多20条，写操作锁会员；默认地址在事务内切换，他人地址详情返回 null。积分/经验变更使用 requestNo 幂等，重试须保持 delta/reason，余额不可负、不可溢出。等级变更记录经验重置；签到奖励与流水同事务，每数据库日一次，周期按启用规则最大天数循环。
- 状态采用 lemnt 1启用/0禁用（区别于参考项目）。金额配置为整数分，积分交易配置目前只管理持久化，**尚未接入订单积分抵扣与赠送**。数据库日期决定签到和限流日界；部署须统一时区。LocalDate 为 yyyy-MM-dd，LocalDateTime 按现有 Jackson yyyy-MM-dd HH:mm:ss。
- 应先审阅并依次执行 member/sql/V1__member.sql、V2__member_account.sql、V3__member_verification.sql；已有 V1 库只执行后两份。V2/V3 非重复执行脚本，不能重跑。这里未执行线上迁移。保留会话、身份、奖励流水并先回退应用，不能靠删表回滚。
- 需要 MySQL、Redis（微信令牌/分布式锁）、Nacos、Dubbo；网关和 trade 通过 MemberSessionClient 调用会员权威验证，超时失败关闭。网关须配置保留 `/member` 前缀的路由（docs/config/member-gateway-route.yaml），不可追加 StripPrefix。
- 可选配置 `member.sms.access-key-id/access-key-secret/sign-name/template-code/pepper/daily-limit`；pepper 至少32 UTF-8字节，生产密钥从密钥管理注入。微信配置 `member.wechat.mini.app-id/secret`、`member.wechat.mp.app-id/secret/redirect-uri/jsapi-hosts`（hosts逗号分隔，HTTPS允许域）。未配置返回503，禁止用假成功替代。SDK WxJava4.8.0、阿里云短信2.2.1/core4.7.6；超时与有界分布式锁等待已设置。未发送真实短信或调用真实微信。
- 各接口权限码见下文，部署须审阅角色授权；本次不自动给角色加权限。小程序 memberApiReady 仍为 false，待真实链路验证后再开启。本次不声称会员管理 React 页面、原生登录界面或生产联调完成。

## 响应与错误

通用 R 字段：code(integer，成功200/应用失败500)、msg(string|null)、data(接口结果|null)、rows(array|null)、total(integer|null)。本组接口分页放在 data.records/data.total，外层 rows/total 为 null。业务不存在通常 data=null，删除不存在返回false。完整对象字段见模型表；成功 msg 为 null。

会员业务 advice：认证失败HTTP401，参数失败HTTP400，状态冲突HTTP409，未配置/外部服务不可用HTTP503，R.fail 的 code 仍为500。Bean Validation 错误由框架/全局处理器生成，不能仅按 R.code 判断。既有网关约定是HTTP200+R.code401/503，客户端须同时检查HTTP及code。管理员权限错误沿用公共安全处理器。下例为业务认证失败的构造示例：

```json
{"code":500,"msg":"会员会话无效","data":null,"rows":null,"total":null}
```

## 能力对照

| 参考会员控制器 | lemnt 入口 | 当前覆盖 |
| --- | --- | --- |
| app auth | /member/app/auth | 密码/短信登录、刷新/注销、验证码、微信登录/跳转/JSAPI |
| app user | /member/app/user | 本人资料、密码、短信重置密码、短信/微信换绑手机号 |
| app address | /member/app/addresses | 增删改查、默认地址、列表 |
| app level / experience / point | /member/app/levels、experience-records、point-records | 启用等级与本人分页流水 |
| app signin config / record | /member/app/signin | 规则、汇总、签到、分页（精确路径以下文为准） |
| app social | /member/app/social | 微信绑定/解绑、本人绑定提供方、小程序码、订阅模板 |
| admin user / address | /member/admin/users | 分页/详情/资料/状态、积分/经验/等级、分组/标签、地址 |
| admin level / group / tag | /member/admin/levels、groups、tags | CRUD、简洁列表、分组/标签分页 |
| admin level-record / experience-record / point-record | /member/admin/*-records | 跨会员分页、详情，按资产类型隔离；另保留单会员查询 |
| admin signin-config / signin-record / config | /member/admin | 规则按天数upsert/删除/详情，签到分页，积分配置 |

接口使用 lemnt REST 路径与字段，不是芋道原 URL 的兼容别名；筛选参数不保证与原项目完全一致。绑定列表只返回提供方，刻意不泄露 openid；等级流水保留等级ID/经验/原因/时间，不复制参考项目完整等级快照。后台头像/邮箱等资料更新不代表联系方式已验证。交易、支付和售后剩余缺口仍按原迁移文档跟踪。

## 接口目录

### 1. 积分交易配置，交易应用另行接入

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 2. 保存积分交易配置

接口地址：`/member/admin/config`  
请求方式：`PUT`  
权限：`member:config:update`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| pointTradeDeductEnable | Boolean | 是 | Body | 无 | 积分抵扣开关，交易集成另行接入 |
| pointTradeDeductUnitPrice | Integer | 是 | Body | 无 | 每积分抵扣金额，分 |
| pointTradeDeductMaxPrice | Integer | 是 | Body | 无 | 积分抵扣金额上限，分 |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | List<SignConfigRes> | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | SignConfigRes | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | List<GroupRes> | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | GroupRes | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | List<GroupRes> | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | List<LevelRes> | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | LevelRes | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | List<LevelRes> | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | AccountPageRes<GroupRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/groups/page?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "records": [
      {
        "id": 1,
        "name": "示例",
        "remark": "示例",
        "status": 1
      }
    ],
    "total": 1
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | AccountPageRes<TagRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/tags/page?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "records": [
      {
        "id": 1,
        "name": "示例",
        "remark": "示例"
      }
    ],
    "total": 1
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | AccountPageRes<AssetRecordRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/point-records?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "records": [
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
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | AssetRecordRes | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | AccountPageRes<AssetRecordRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/experience-records?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "records": [
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
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | AssetRecordRes | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | AccountPageRes<LevelRecordRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/level-records?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "records": [
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
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | LevelRecordRes | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | AccountPageRes<SignRecordRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/signin-records?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "records": [
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
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | List<TagRes> | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | TagRes | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | List<TagRes> | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | ProfileRes | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | AccountPageRes<ProfileRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/users?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "records": [
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
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | ProfileRes | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 37. 后台更新资料和状态，停用立即撤销会话

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 38. 后台重置密码，撤销全部会话

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | AccountPageRes<LevelRecordRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/users/1/level-records?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "records": [
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
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | AccountPageRes<SignRecordRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/users/1/signin-records?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "records": [
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
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | AssetRecordRes | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | AccountPageRes<AssetRecordRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/users/1/points-records?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "records": [
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
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | AssetRecordRes | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | AccountPageRes<AssetRecordRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/admin/users/1/experience-records?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "records": [
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
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 52. 验证原密码并修改，撤销全部会员会话

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | SignRecordRes | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 57. 本人签到记录分页

接口地址：`/member/app/signin/records`  
请求方式：`GET`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| mobile | String | 否 | Query | 无 | 手机号 |
| pageNum | Integer | 否 | Query | 1 | 页码，默认1 |
| pageSize | Integer | 否 | Query | 无 | 每页条数，默认20，最大100 |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | AccountPageRes<SignRecordRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/app/signin/records?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "records": [
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
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 58. 本人POINT流水分页

接口地址：`/member/app/point-records`  
请求方式：`GET`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| mobile | String | 否 | Query | 无 | 手机号 |
| pageNum | Integer | 否 | Query | 1 | 页码，默认1 |
| pageSize | Integer | 否 | Query | 无 | 每页条数，默认20，最大100 |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | AccountPageRes<AssetRecordRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/app/point-records?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "records": [
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
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 59. 本人EXPERIENCE流水分页

接口地址：`/member/app/experience-records`  
请求方式：`GET`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| mobile | String | 否 | Query | 无 | 手机号 |
| pageNum | Integer | 否 | Query | 1 | 页码，默认1 |
| pageSize | Integer | 否 | Query | 无 | 每页条数，默认20，最大100 |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | AccountPageRes<AssetRecordRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/app/experience-records?pageNum=1&pageSize=20' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "records": [
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
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | List<AddressRes> | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | AddressRes | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | AddressRes | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 63. 新增本人地址，最多20个

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
| data | AddressRes | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | AddressRes | 字段详见下方响应模型，允许不存在为null |
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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
| data | TokenPairRes | 字段详见下方响应模型，允许不存在为null |
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
    "accessToken": "mbr_a_<43_CHAR_RANDOM>",
    "refreshToken": "mbr_r_<43_CHAR_RANDOM>",
    "expiresIn": 7200
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 67. 一次性刷新轮换，旧访问和刷新令牌同时失效

接口地址：`/member/app/auth/refresh-token`  
请求方式：`POST`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| refreshToken | String | 是 | Body | 无 | 会员刷新令牌，禁止记录到日志 |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | TokenPairRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/member/app/auth/refresh-token' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"refreshToken": "mbr_r_<43_CHAR_RANDOM>"}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "memberId": 1,
    "accessToken": "mbr_a_<43_CHAR_RANDOM>",
    "refreshToken": "mbr_r_<43_CHAR_RANDOM>",
    "expiresIn": 7200
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 69. 发送场景验证码，默认未配置时拒绝，日额度及频率受限

接口地址：`/member/app/auth/send-sms-code`  
请求方式：`POST`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| mobile | String | 是 | Body | 无 | mobile |
| scene | String | 是 | Body | 无 | scene |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Boolean | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/member/app/auth/send-sms-code' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"mobile": "13800000001", "scene": "LOGIN"}'
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 70. 验证验证码但不消费，错误次数受限

接口地址：`/member/app/auth/validate-sms-code`  
请求方式：`POST`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| mobile | String | 是 | Body | 无 | mobile |
| scene | String | 是 | Body | 无 | scene |
| code | String | 是 | Body | 无 | code |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Boolean | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/member/app/auth/validate-sms-code' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"mobile": "13800000001", "scene": "LOGIN", "code": "123456"}'
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 71. 消费LOGIN验证码并登录，首次验证手机号后创建会员

接口地址：`/member/app/auth/sms-login`  
请求方式：`POST`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| mobile | String | 是 | Body | 无 | mobile |
| code | String | 是 | Body | 无 | code |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | TokenPairRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/member/app/auth/sms-login' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"mobile": "13800000001", "code": "123456"}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "memberId": 1,
    "accessToken": "mbr_a_<43_CHAR_RANDOM>",
    "refreshToken": "mbr_r_<43_CHAR_RANDOM>",
    "expiresIn": 7200
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 72. 消费RESET_PASSWORD验证码并重置密码，撤销全部会话

接口地址：`/member/app/user/reset-password`  
请求方式：`PUT`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| mobile | String | 是 | Body | 无 | mobile |
| code | String | 是 | Body | 无 | code |
| password | String | 是 | Body | 无 | password |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Boolean | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request PUT 'https://<API_HOST>/member/app/user/reset-password' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"mobile": "13800000001", "code": "123456", "password": "Example-password-2026"}'
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 73. 验证原手机和新手机验证码后修改手机号，撤销全部会话

接口地址：`/member/app/user/mobile`  
请求方式：`PUT`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| mobile | String | 是 | Body | 无 | mobile |
| code | String | 是 | Body | 无 | code |
| oldCode | String | 否 | Body | 无 | oldCode |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Boolean | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request PUT 'https://<API_HOST>/member/app/user/mobile' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"mobile": "13800000001", "code": "123456", "oldCode": "123456"}'
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 74. 服务端兑换微信手机code后修改，不接受明文手机号

接口地址：`/member/app/user/mobile-by-weixin`  
请求方式：`PUT`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| code | String | 是 | Body | 无 | code |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Boolean | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request PUT 'https://<API_HOST>/member/app/user/mobile-by-weixin' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"code": "123456"}'
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 75. 生成绑定或登录用的一次性state，BIND必须当前会员登录

接口地址：`/member/app/auth/social-state`  
请求方式：`POST`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| provider | String | 是 | Body | 无 | provider |
| purpose | String | 是 | Body | 无 | purpose |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | SocialStateRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/member/app/auth/social-state' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"provider": "WECHAT_MINI", "purpose": "LOGIN"}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "state": "<ONE_TIME_STATE>",
    "authorizationUrl": "示例"
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 76. 公众号登录授权地址，固定服务端回调，返回state

接口地址：`/member/app/auth/social-auth-redirect`  
请求方式：`GET`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | SocialStateRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/app/auth/social-auth-redirect' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "state": "<ONE_TIME_STATE>",
    "authorizationUrl": "示例"
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 77. 服务器兑换第三方code和一次性state后登录，只支持配置的微信提供方

接口地址：`/member/app/auth/social-login`  
请求方式：`POST`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| provider | String | 是 | Body | 无 | provider |
| loginCode | String | 是 | Body | 无 | loginCode |
| state | String | 是 | Body | 无 | state |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | TokenPairRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/member/app/auth/social-login' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"provider": "WECHAT_MINI", "loginCode": "<WECHAT_LOGIN_CODE>", "state": "<ONE_TIME_STATE>"}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "memberId": 1,
    "accessToken": "mbr_a_<43_CHAR_RANDOM>",
    "refreshToken": "mbr_r_<43_CHAR_RANDOM>",
    "expiresIn": 7200
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 78. 服务器校验微信登录code和手机号code后登录

接口地址：`/member/app/auth/weixin-mini-app-login`  
请求方式：`POST`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| loginCode | String | 是 | Body | 无 | loginCode |
| phoneCode | String | 是 | Body | 无 | phoneCode |
| state | String | 是 | Body | 无 | state |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | TokenPairRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/member/app/auth/weixin-mini-app-login' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"loginCode": "<WECHAT_LOGIN_CODE>", "phoneCode": "<WECHAT_PHONE_CODE>", "state": "<ONE_TIME_STATE>"}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "memberId": 1,
    "accessToken": "mbr_a_<43_CHAR_RANDOM>",
    "refreshToken": "mbr_r_<43_CHAR_RANDOM>",
    "expiresIn": 7200
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 79. 允许域名内的公众号JSAPI签名，不返回公众号密钥

接口地址：`/member/app/auth/create-weixin-jsapi-signature`  
请求方式：`POST`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| url | String | 是 | Body | 无 | url |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | JsapiConfigRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/member/app/auth/create-weixin-jsapi-signature' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"url": "https://example.test/page"}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "appId": "示例",
    "nonceStr": "示例",
    "timestamp": 1,
    "signature": "示例"
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 80. 本人已绑定的提供方名称，不返回openid/session_key

接口地址：`/member/app/social`  
请求方式：`GET`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | List<String> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/app/social' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": [
    "示例"
  ],
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 81. 服务端验证code和绑定state，禁止绑定他人的第三方身份

接口地址：`/member/app/social/bind`  
请求方式：`POST`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| provider | String | 是 | Body | 无 | provider |
| code | String | 是 | Body | 无 | code |
| state | String | 是 | Body | 无 | state |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Boolean | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/member/app/social/bind' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"provider": "WECHAT_MINI", "code": "123456", "state": "<ONE_TIME_STATE>"}'
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 82. 解绑本人身份，最后一种登录方式不可解绑

接口地址：`/member/app/social/bind`  
请求方式：`DELETE`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| provider | String | 是 | Body | 无 | provider |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | Boolean | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request DELETE 'https://<API_HOST>/member/app/social/bind' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"provider": "WECHAT_MINI"}'
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

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 83. 生成正式版小程序码，返回PNG base64

接口地址：`/member/app/social/wxa-qrcode`  
请求方式：`POST`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |
| scene | String | 是 | Body | 无 | scene |
| page | String | 是 | Body | 无 | page |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | QrcodeRes | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request POST 'https://<API_HOST>/member/app/social/wxa-qrcode' --header 'Authorization: Bearer <TOKEN>' --header 'Content-Type: application/json' --data '{"scene": "LOGIN", "page": "pages/index/index"}'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "base64": "示例",
    "contentType": "示例"
  },
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

### 84. 配置小程序的订阅消息模板列表

接口地址：`/member/app/social/subscribe-template-list`  
请求方式：`GET`  
权限：`会员令牌或凭证（参见边界）`

| 参数名 | 类型 | 必填 | 位置 | 默认值 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Authorization | string | 私有操作必填 | Header | 无 | Bearer会员accessToken |

| 响应参数 | 类型 | 说明 |
| --- | --- | --- |
| code | integer | 成功200；错误见响应与错误节 |
| msg | string/null | 成功null |
| data | List<SubscribeTemplateRes> | 字段详见下方响应模型，允许不存在为null |
| rows | array/null | 本接口null |
| total | integer/null | 本接口null |

请求示例（构造，未实测）：

```bash
curl --request GET 'https://<API_HOST>/member/app/social/subscribe-template-list' --header 'Authorization: Bearer <TOKEN>'
```

响应示例（构造，未实测）：

```json
{
  "code": 200,
  "msg": null,
  "data": [
    {
      "id": "示例",
      "title": "示例",
      "content": "示例",
      "type": 1
    }
  ],
  "rows": null,
  "total": null
}
```

适用错误：缺失凭证/无效验证码401；参数400；状态或重复业务冲突409；未配置的供应商503（按本接口涉及的场景适用）。

## 模型字段

请求字段的必填性与范围以源码 Bean Validation 和业务验证为准，空字段不等于清空所有可选字段的通用 PATCH 语义。以下响应表完整列出每个独立模型，分页 records 的元素类型由接口返回声明确定。

### AccountPageReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| mobile | String | 手机号 |
| pageNum | Integer | 页码，默认1 |
| pageSize | Integer | 每页条数，默认20，最大100 |

### AccountPageRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| records | java.util.List<T> | 记录 |
| total | long | 总数 |

### AddressRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 记录编号 |
| memberId | Long | 会员编号 |
| name | String | 名称或收件人 |
| mobile | String | 手机号 |
| areaId | Long | 地区编号 |
| detailAddress | String | 详细地址 |
| defaultStatus | Boolean | 是否默认地址 |

### AddressSaveReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| name | String | 名称或收件人 |
| mobile | String | 手机号 |
| areaId | Long | 地区编号 |
| detailAddress | String | 详细地址 |
| defaultStatus | Boolean | 是否默认地址 |

### AdminMemberPageReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| memberId | Long | 可选会员ID；未填查询所有会员 |
| name | String | 名称包含匹配 |
| status | Integer | 状态：0禁用，1启用 |
| pageNum | Integer | 页码，默认1 |
| pageSize | Integer | 页大小，默认20，最大100 |

### AdminProfileUpdateReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| nickname | String | 昵称 |
| email | String | 邮箱 |
| avatar | String | 头像HTTPS地址 |
| sex | Integer | 性别：0未知，1男，2女 |
| birthday | java.time.LocalDate | 出生日期 |
| status | Integer | 状态：1启用，0停用 |

### AssetAdjustReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| delta | Long | 本次有符号变更量 |
| requestNo | String | 幂等业务编号，重试不可更改内容 |
| reason | String | 原因 |

### AssetRecordRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 记录编号 |
| memberId | Long | 会员编号 |
| assetType | String | 资产：POINT积分，EXPERIENCE经验 |
| delta | Long | 本次有符号变更量 |
| balance | Long | 变更后余额 |
| bizType | String | 业务类型 |
| bizNo | String | 幂等业务编号 |
| reason | String | 原因 |
| createTime | java.time.LocalDateTime | 数据库时间 |

### AuthLoginReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| mobile | String | 手机号 |
| password | String | 密码，至少10字符且最多72 UTF-8字节 |

### AuthRefreshReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| refreshToken | String | 会员刷新令牌，禁止记录到日志 |

### ConfigRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| pointTradeDeductEnable | Boolean | 积分抵扣开关，交易集成另行接入 |
| pointTradeDeductUnitPrice | Integer | 每积分抵扣金额，分 |
| pointTradeDeductMaxPrice | Integer | 积分抵扣金额上限，分 |
| pointTradeGivePoint | Integer | 每元赠送积分数 |

### ConfigSaveReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| pointTradeDeductEnable | Boolean | 积分抵扣开关，交易集成另行接入 |
| pointTradeDeductUnitPrice | Integer | 每积分抵扣金额，分 |
| pointTradeDeductMaxPrice | Integer | 积分抵扣金额上限，分 |
| pointTradeGivePoint | Integer | 每元赠送积分数 |

### GroupAssignReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| groupId | Long | 分组编号，可空 |

### GroupRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 记录编号 |
| name | String | 名称或收件人 |
| remark | String | 备注 |
| status | Integer | 状态：1启用，0停用 |

### GroupSaveReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| name | String | 名称或收件人 |
| remark | String | 备注 |
| status | Integer | 状态：1启用，0停用 |

### JsapiConfigRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| appId | String | appId |
| nonceStr | String | nonceStr |
| timestamp | Long | timestamp |
| signature | String | signature |

### JsapiReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| url | String | url |

### LevelAssignReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| levelId | Long | 等级编号，可空 |
| requestNo | String | 幂等业务编号，重试不可更改内容 |
| reason | String | 原因 |

### LevelRecordRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 记录编号 |
| memberId | Long | 会员编号 |
| levelId | Long | 等级编号，可空 |
| experience | Long | 经验值或等级升级门槛 |
| reason | String | 原因 |
| createTime | java.time.LocalDateTime | 数据库时间 |

### LevelRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 记录编号 |
| name | String | 名称或收件人 |
| level | Integer | 等级序号 |
| experience | Long | 经验值或等级升级门槛 |
| discountPercent | Integer | 支付百分比，98为98折 |
| icon | String | 等级图标URL |
| backgroundUrl | String | 等级背景URL |
| status | Integer | 状态：1启用，0停用 |

### LevelSaveReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| name | String | 名称或收件人 |
| level | Integer | 等级序号 |
| experience | Long | 经验值或等级升级门槛 |
| discountPercent | Integer | 支付百分比，98为98折 |
| icon | String | 等级图标URL |
| backgroundUrl | String | 等级背景URL |
| status | Integer | 状态：1启用，0停用 |

### MemberCreateReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| nickname | String | 昵称 |
| mobile | String | 手机号 |
| email | String | 邮箱 |
| avatar | String | 头像HTTPS地址 |

### MobileUpdateReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| mobile | String | mobile |
| code | String | code |
| oldCode | String | oldCode |

### PasswordChangeReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| oldPassword | String | 当前密码 |
| newPassword | String | 新密码，至少10字符且最多72 UTF-8字节 |

### PasswordResetReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| password | String | 密码，至少10字符且最多72 UTF-8字节 |

### ProfileRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 记录编号 |
| memberNo | String | 会员业务编号 |
| nickname | String | 昵称 |
| mobile | String | 手机号 |
| email | String | 邮箱 |
| avatar | String | 头像HTTPS地址 |
| status | Integer | 状态：1启用，0停用 |
| points | Long | 当前积分 |
| experience | Long | 经验值或等级升级门槛 |
| levelId | Long | 等级编号，可空 |
| groupId | Long | 分组编号，可空 |
| sex | Integer | 性别：0未知，1男，2女 |
| birthday | java.time.LocalDate | 出生日期 |
| createTime | java.time.LocalDateTime | 数据库时间 |

### ProfileUpdateReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| nickname | String | 昵称 |
| email | String | 邮箱 |
| avatar | String | 头像HTTPS地址 |
| sex | Integer | 性别：0未知，1男，2女 |
| birthday | java.time.LocalDate | 出生日期 |

### QrcodeReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| scene | String | scene |
| page | String | page |

### QrcodeRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| base64 | String | base64 |
| contentType | String | contentType |

### SignConfigRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| day | Integer | 本轮连续签到第几天 |
| point | Long | 奖励积分 |
| experience | Long | 经验值或等级升级门槛 |
| status | Integer | 状态：1启用，0停用 |

### SignConfigSaveReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| day | Integer | 本轮连续签到第几天 |
| point | Long | 奖励积分 |
| experience | Long | 经验值或等级升级门槛 |
| status | Integer | 状态：1启用，0停用 |

### SignRecordRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 记录编号 |
| memberId | Long | 会员编号 |
| signDate | java.time.LocalDate | 签到业务日期 |
| day | Integer | 本轮连续签到第几天 |
| point | Long | 奖励积分 |
| experience | Long | 经验值或等级升级门槛 |

### SignSummaryRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| totalDay | long | 累计签到天数 |
| continuousDay | int | 当前周期连续天数 |
| todaySignIn | boolean | 今日是否签到 |

### SmsLoginReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| mobile | String | mobile |
| code | String | code |

### SmsPasswordResetReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| mobile | String | mobile |
| code | String | code |
| password | String | password |

### SmsSendReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| mobile | String | mobile |
| scene | String | scene |

### SmsValidateReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| mobile | String | mobile |
| scene | String | scene |
| code | String | code |

### SocialBindReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| provider | String | provider |
| code | String | code |
| state | String | state |

### SocialLoginReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| provider | String | provider |
| loginCode | String | loginCode |
| state | String | state |

### SocialStateReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| provider | String | provider |
| purpose | String | purpose |

### SocialStateRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| state | String | state |
| authorizationUrl | String | authorizationUrl |

### SocialUnbindReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| provider | String | provider |

### SubscribeTemplateRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| id | String | id |
| title | String | title |
| content | String | content |
| type | Integer | type |

### TagRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 记录编号 |
| name | String | 名称或收件人 |
| remark | String | 备注 |

### TagSaveReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| name | String | 名称或收件人 |
| remark | String | 备注 |

### TagsAssignReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| tagIds | java.util.List<Long> | 标签编号，空集合清空标签 |

### TokenPairRes

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| memberId | Long | 会员编号 |
| accessToken | String | 会员访问令牌，禁止记录到日志 |
| refreshToken | String | 会员刷新令牌，禁止记录到日志 |
| expiresIn | Long | 访问令牌有效期，秒 |

### WechatLoginReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| loginCode | String | loginCode |
| phoneCode | String | phoneCode |
| state | String | state |

### WechatMobileUpdateReq

| 参数名 | 类型 | 说明 |
| --- | --- | --- |
| code | String | code |

