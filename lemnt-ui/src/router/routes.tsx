import { lazy, type ComponentType } from 'react';

/** 后端 component 仅作为注册键使用，不执行任意路径导入。 */
export const pages: Record<string, ComponentType> = {
  'member/user/index': lazy(() => import('../modules/member/account/pages/MemberUserPage')),
  'member/level/index': lazy(() =>
    import('../modules/member/account/pages/MemberMetadataPage').then((m) => ({
      default: m.MemberLevelPage,
    })),
  ),
  'member/group/index': lazy(() =>
    import('../modules/member/account/pages/MemberMetadataPage').then((m) => ({
      default: m.MemberGroupPage,
    })),
  ),
  'member/tag/index': lazy(() =>
    import('../modules/member/account/pages/MemberMetadataPage').then((m) => ({
      default: m.MemberTagPage,
    })),
  ),
  'member/record/index': lazy(() => import('../modules/member/account/pages/MemberRecordPage')),
  'member/config/index': lazy(() => import('../modules/member/account/pages/MemberConfigPage')),
  'mall/category/index': lazy(() => import('../modules/mall/catalog/pages/CategoryPage')),
  'mall/brand/index': lazy(() => import('../modules/mall/catalog/pages/BrandPage')),
  'mall/spu/index': lazy(() => import('../modules/mall/catalog/pages/SpuPage')),
  'trade/coupon/index': lazy(() => import('../modules/trade/checkout/pages/CouponTemplatePage')),
  'trade/order/index': lazy(
    () => import('../modules/trade/fulfillment/pages/OrderFulfillmentPage'),
  ),
  'trade/refund/index': lazy(() => import('../modules/trade/fulfillment/pages/RefundRequestPage')),
  'trade/cart/index': lazy(() => import('../modules/trade/checkout/pages/MemberCartPage')),
  'home/index': lazy(() => import('../modules/home/pages/HomePage')),
  'system/operlog/index': lazy(() => import('../modules/system/log/pages/OperationLogPage')),
  'system/logininfor/index': lazy(() => import('../modules/system/log/pages/LoginLogPage')),
  'system/user/profile/index': lazy(() => import('../modules/system/profile/pages/ProfilePage')),
  'system/dict/index': lazy(() => import('../modules/system/dict/pages/DictPage')),
  'system/notice/index': lazy(() => import('../modules/system/notice/pages/NoticePage')),
  'system/user/index': lazy(() => import('../modules/system/user/pages/UserPage')),
  'system/role/index': lazy(() => import('../modules/system/role/pages/RolePage')),
  'system/menu/index': lazy(() => import('../modules/system/menu/pages/MenuPage')),
  'system/dept/index': lazy(() => import('../modules/system/dept/pages/DeptPage')),
  'system/post/index': lazy(() => import('../modules/system/post/pages/PostPage')),
  'system/config/index': lazy(() => import('../modules/system/config/pages/ConfigPage')),
};
