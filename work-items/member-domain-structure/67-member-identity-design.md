# 会员后台姓名地区设计

2026-10-29，承接F04与66。源MemberUserDO.name/areaId及后台BaseVO有字段，App更新VO没有；源测试DDL为name varchar(30)、area_id int。新增可空列，原后台资料Req/Cmd/DTO/Res和表单贯通，App与认证契约不增加。

省略/null保留已有姓名地区，姓名空串明确清除；地区为正int编号，不接受0或溢出，不凭编号捏造地区名。暂不新增地区字典/表。源areaName依赖地区解析，本批无合法解析来源，明确未迁移。普通会员资料更新保留这两项，同原资料事务写入。升级仅生成，不执行。验证存储、省略保留、参数失败原值不变、App边界和前端参数，不commit/push。
