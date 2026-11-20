# 流水基础审计规范设计

2026-10-30，承接88，参考system DO继承本项目BaseDO及MyMetaObjectHandler，源芋道BaseDO包含创建更新信息。本批积分/经验DO复用本地BaseDO，删除自有createTime避免遮蔽；新增create_by/update_by/update_time可空列，结果映射补齐，不新增HTTP字段。

新行操作者由既有安全上下文/统一填充器产生；无用户名时system表示服务执行身份，不反推会员姓名。createTime保留账本业务传入的数据库时间，updateTime初始化为同一事件时间；幂等重试不执行更新，审计不随重试变化。旧库新增字段和旧表copy都保持未知NULL，不回填当前时间/操作者。

不改公共BaseDO或公共填充器，不增加流水删除能力；源deleted与本地不可变财务事实的处理本批未引入。测试实际H2保存/填充/重试、原copy历史NULL及跨模块回归。只生成升级，不执行真实库，不commit/push。
