# 会员后台备注设计

2026-10-29，F04，参考源MemberUserDO.mark及后台会员资料编辑。现member_user缺mark；新增可空VARCHAR(255)，旧值不补造。沿现有后台资料PUT与分页/详情，新增可选mark：省略/null保留原值，空字符串明确清除，长度最多255。同原资料更新事务写入，不影响密码/积分/等级。

domain Member、application DTO/Cmd、web独立Req/Res映射mark，MyBatis Plus Lambda写入；原App调用兼容，App Req/Res、client登录DTO不含mark，会员不能修改/查看内部备注。后台编辑表单增加备注。仅生成旧库脚本，不执行真实DDL。验证存储与省略/清除、会员边界、前端请求和回归；未commit/push。
