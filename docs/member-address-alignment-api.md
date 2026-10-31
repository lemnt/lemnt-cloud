# 会员收货地址收敛

接口路径和原独立Req/Res保持：App `/app/addresses` 列表/创建，`/{id}` 详情/修改/删除，`/default` 默认地址；网关对外仍加 `/member` 前缀。本人编号仅来自网关上下文，HTTP请求和RPC的memberId映射保持，不因数据库列改名而改变。

按本地芋道AddressServiceImpl移除额外20条限制；不存在或非本人地址修改/删除统一ServiceException404，使用项目全局Problem Details响应。详情查询缺失或他人仍为null。删除默认地址不擅自把其他地址改成默认，首次新增非默认项也不会自动变默认。

默认切换保留原会员行锁与事务，只清除本人其他true项，使用实体更新触发统一更新审计；普通地址、其他会员地址不被清默认写操作触碰。保存失败时，先清除的默认标志和审计一同回滚。默认查询直接按归属/默认标志过滤，按原ID倒序确定一条，不加载全部列表再筛。

member_address按源改为user_id，AddressDO继承项目BaseDO，新增可空create_time/create_by/update_time/update_by；新写入/更新沿用项目统一审计填充。Mapper只继承BaseMapper，Gateway使用Lambda，不重复包装内置insert/update/delete，没有SQL注解或新的XML。

新库用已同步的V2；现有旧库仅在核对结构、停写备份后执行一次 `lemnt-server/lemnt-member/sql/upgrade/address_owner_audit.sql`。脚本重命名原member_id并新增四个可空审计列，保留原编号、归属、默认状态及原索引/外键；旧审计未知不回填。不能对旧库重跑初始化V2。本批未执行真实库升级，H2的MySQL模式通过不等同真实MySQLDDL验收。

源BaseDO逻辑删除等跨项目基础字段仍按现项目BaseDO规范逐项核对；本批未新增deleted/tenant_id或更改原物理删除语义。源areaName等最终展示契约仍待核对，不宣称地址完整迁移完成。
