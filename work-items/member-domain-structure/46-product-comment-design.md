# 商品评价链路迁移设计

2026-10-29 v1，责任 code-software-engineer。承接完整迁移F03，文章/文章分类继续按用户最新指令排除。参考已读取的技能Java/API/开发设计规范、项目AGENTS、本地芋道ProductCommentDO/Service/Mapper/Convert、App评价请求和响应及后台/小程序页面。沿用现有CQRS、独立HTTP合同、BaseMapper/Lambda及含resultMap的锁XML。

## 现状与本批目标

现有mall_product_comment仅单评分/正文，缺公开分页和实际调用页面，创建从当前SKU取SPU。补描述/服务评分、匿名、图片、会员昵称头像快照、订单编号及商品快照；原score维持现有字段，采用源floor((descriptionScores+benefitScores)/2)计算，公开页只显示visible=true，按好评>=4、中评=3、差评<3过滤。仅扩展既有表，不新增辅助表；逻辑删除字段沿源通过MyBatis Plus过滤，visible仍独立表示前台显隐，不新增删除接口。

写入会员只取网关上下文、必须本人COMPLETED订单内SKU；订单商品已存快照优先使用，不以当前商品改名或删除覆盖历史。旧订单无快照的字段保留null；不伪造orderItemId。当前交易明细模型使用订单+SKU定位、未向client暴露独立行ID，本批保留这一边界，源订单项ID及订单评价标记联动仍待迁移。会员资料从现有MemberUserClient读取并扩展avatar，复用同一查询，不新建专用会员接口。失败不落评价，重试先比对已保存全部评价内容，不能改写已评商品。

HTTP创建新增descriptionScores/benefitScores、anonymous和picUrls（最多9个HTTPS地址）；兼容原score输入：仅传score时将其作为两项评分，提供两项时综合分由服务端计算，同时传score必须一致；历史数据的两项评分/图片/匿名和会员商品快照未知保持null，不用旧score反推历史两项。新记录replyStatus=false，后台回复更新为true并保留完整快照。

## 页面与API

- 后台现有 GET /mall/comments、详情、回复和显隐沿用权限码；补独立CommentVisibleRes，管理页支持评分/会员/SPU/显隐/回复过滤、真实详情、图片、规格和回复。菜单只生成不自动授权角色。
- GET /mall/app/comments 公开分页，必须spuId，type=0全部/1好/2中/3差，标准页码；独立App查询Executor/Qry/Req/Res。禁止客户端覆写visible或查询后台条件。公开响应不含订单编号、会员ID、回复员工ID；匿名时昵称“匿名用户”、头像省略，未记录昵称不伪造。非匿名只展示保存时合法资料。
- POST同路径仍受保护；公开GET必须网关精确匹配，不能开放POST或子路径。
- 小程序商品详情跳评价列表；订单内已完成商品提供评分入口、描述/服务1至5、正文、可选最多9个HTTPS图片地址、匿名开关；创建后真实查询公开列表。网络未知结果保留输入，用户原内容重试由后端核验，不在本地假设成功。源图片上传SDK/相机选择没有对应会员文件链路，本批仅提交已具备的HTTPS地址，不能宣称原生图片上传迁移完成。

后台与App继续使用不同Req/Res和统一R.page。空值遵循NON_NULL；编号可为安全整数或长字符串；日期沿用全局yyyy-MM-dd HH:mm:ss；错误用统一Problem Details，前端读detail，不自建异常处理器。JSON列表使用String DO字段及显式Converter，应用/domain不依赖Jackson/MyBatis。

## 任务及验收

1. 表字段与历史兼容、按表存储转换/锁resultMap→生产DDL H2升级、JSON往返、旧行保持未知值、回复不丢字段。
2. 会员client快照、真实订单写入、公开分页/隐私→归属/完成状态/重试/并发/平均分/隐藏与评分tab/匿名HTTP合同。
3. React管理及小程序列表/创建→筛选false、长ID、0/空数据、未知结果、身份切换和输入保留回归。
4. 接口文档、源清单与交付证据→受影响Maven clean verify、UI typecheck/lint/format/test/build、miniapp测试、diff检查。

源虚拟评价创建、订单项ID及评价状态联动、照片上传、本批未完成的源字段不能标迁移完成。真实MySQL/Redis/Dubbo/部署HTTP/浏览器/真机分别标NOT RUN。SQL只生成，不执行真实库升级、不commit/push，不更改用户已有暂存状态。

原型核对补充：已读取源ReplyForm.vue、App add.vue/list.vue；源App批量循环订单项投稿、图片上传、8条上拉分页，本批适配为当前单SKU入口、HTTPS地址填写及20条标准分页。源orderItemId/订单标记及批量页面后续仍待迁移。源后台额外昵称/商品名称/订单ID/创建时间筛选和导出不因本批已有基本管理页就标完成。
