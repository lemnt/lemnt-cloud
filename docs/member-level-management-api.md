# 会员等级管理

2026-10-30。服务内路径/growth/levels，前端已同步；分组与标签元数据实际路径分别/groups、/tags，已一并修正旧/member/admin调用。网关示例补相应路径，尚未应用Nacos。

GET /growth/levels使用独立MemberLevelListReq：name可选最多80字包含匹配，status可选0停用/1启用，空白名称忽略；返回等级升序的独立列表Res，权限member:level:query。GET /growth/levels/list-all-simple与App列表只查启用，附加status=0不能把简单列表改成停用列表。原创建/修改/详情路径及Req/Res、权限保持。

名称、等级序号唯一及等级经验严格递增沿现有源校验；状态仍项目1启用0停用。删除使用基础配置事务锁，缺失404、当前有会员使用409，成功Boolean true，保留原SQL NOT EXISTS及会员外键保护。删除后历史等级记录快照保留，不因当前等级消失而丢历史。

LevelDO继承本地BaseDO，增可空审计列；新记录沿统一填充器，旧记录不补创建时间或操作者。icon/backgroundUrl可显式清空，BaseMapper更新策略保持原null语义；简单读写直接BaseMapper，复杂删除在含resultMap的XML，无新增注解SQL。

新库V2已更新；已有旧表核对缺失列后一次执行sql/upgrade/level_audit_fields.sql，不能对新版表重复执行。H2旧结构升级保留原唯一键/会员外键和未知审计通过；真实MySQL未执行，需停写备份核对。部署HTTP/浏览器E2E/微信端、所有并发分配删除竞争未验收，不能把测试当作已部署。
