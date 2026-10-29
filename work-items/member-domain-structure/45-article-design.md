# 商城文章与分类迁移设计

2026-10-29 v1，责任 code-software-engineer，承接27完整迁移F10。已沿用本技能 development-design/api-documentation/java/typescript-web 规范和项目AGENTS；参考本地芋道 ArticleDO/ArticleCategoryDO、ServiceImpl、Mapper、admin/app控制器、后台ArticleForm及App richtext.vue。保持现有Java17、Spring Boot、MyBatis Plus、React/Ant Design和原生小程序，不新增依赖。

## 范围与契约

新增 `mall_promotion_article_category`、`mall_promotion_article`，业务包归 promotion/article，Adapter按app/web→promotion。各表独立DO/BaseMapper/Gateway；简单读写Lambda，分类行锁用XML且含resultMap。逻辑删除沿源行为，审计字段按当前BaseDO；不增认证或辅助表。分类删除时有有效文章报冲突；文章新增/修改必须分类存在，锁分类记录与事务共同保护引用。文章初始browseCount=0，修改资料不覆盖浏览次数；增加浏览量原子递增。源关联商品spuId允许0（未关联），不伪造商品，正数作为当前商品跳转编号。

后台：分类新增/修改/删除/详情/分页/启用列表；文章新增/修改/删除/详情/分页。独立Req/Res、Cmd/Qry/DTO/Executor/Converter，分页统一R.page，不共用写入HTTP对象。状态源0启用1禁用，推荐false是有效值，分类App列表仅启用按sort升序；分类后台分页sort降序，文章后台分页id降序。按源文章标题/作者为精确匹配，分类名称模糊；创建时间按显式起止筛选，不隐式扩大范围。

App：GET `/mall/app/article-categories` 启用分类；GET `/mall/app/articles` 按分类分页；GET `/mall/app/articles/recommended` 可选recommendHot/recommendBanner；GET `/mall/app/articles/detail` 按id优先或title精确查询最后一篇（明确id降序）；PUT `/mall/app/articles/{id}/browse-count` 增量。源文章App接口未按文章status过滤，本轮保留并在文档注明，不擅自增加发布状态机。GET不隐式增加浏览次数。上述精确路由按源允许匿名，网关不扩大其他私有路径白名单。

## 页面输入输出

后台两页映射源文章/分类：名称/标题、作者、分类、关联商品、图片、简介、排序、状态、热门/轮播、富文本内容；表格及详情显示实际浏览量/创建时间。图片使用现有上传能力，富文本复用现有编辑器及安全预览，不能直接输出未消毒HTML。编辑成功后刷新实际列表；错误保留表单，不伪造成功。菜单和权限种子幂等，无自动角色授权。

App文章列表及阅读页：分类选择、分页、标题/简介/封面，详情作者/日期/内容及关联商品；支持从源richtext相同id/title输入打开。阅读成功再请求增量，失败保留阅读内容并显示计数未确认，不自动重试增量。页面为空/失败清空旧文章。原生rich-text不执行任意HTML脚本，不新增H5 WebView。

## 任务与验证

1. 两表/按表组件及事务规则→H2生产DDL、分类引用保护/逻辑删除/禁用列表/原子计数、资料修改不覆盖次数。
2. 独立应用和HTTP合同/权限/公开精确路径→控制器合同、网关公共路由测试。
3. React两页/API/权限及miniapp列表/阅读→API筛选false、错误/空态、长ID/读取计数与未知结果回归。
4. 接口文档/源清单/证据→Maven verify、前端测试/构建/lint/format/typecheck、小程序测试和diff检查。

SQL只生成，不执行真实数据库升级；禁止commit/push。数据库物理长度采用目标约束，不虚报源DDL完全一致；源源码字段可确认，源物理DDL尚不完备。现有身份相关历史表不属于本批清理。真实MySQL/Redis/网关部署/浏览器/真机未执行必须分别记录。
