import { lazy, type ComponentType } from 'react';

/** 后端 component 仅作为注册键使用，不执行任意路径导入。 */
export const pages: Record<string, ComponentType> = {
  'mall/spec/index': lazy(() => import('../modules/mall/catalog/pages/SpecPage')),
  'mall/spec-value/index': lazy(() =>
    import('../modules/mall/catalog/pages/SpecPage').then((m) => ({ default: m.SpecValuePage })),
  ),
  'trade/delivery/express/index': lazy(() =>
    import('../modules/trade/delivery/pages/DeliveryPage').then((m) => ({
      default: m.ExpressPage,
    })),
  ),
  'trade/delivery/express-template/index': lazy(() =>
    import('../modules/trade/delivery/pages/DeliveryPage').then((m) => ({
      default: m.ExpressTemplatePage,
    })),
  ),
  'trade/config/index': lazy(() => import('../modules/trade/config/pages/TradeConfigPage')),
  'trade/payment/index': lazy(() =>
    import('../modules/trade/payment/pages/PaymentPage').then((m) => ({ default: m.PaymentPage })),
  ),
  'trade/payment-refund/index': lazy(() =>
    import('../modules/trade/payment/pages/PaymentPage').then((m) => ({
      default: m.PaymentRefundPage,
    })),
  ),
  'trade/payment-channel/index': lazy(() =>
    import('../modules/trade/payment/pages/PaymentPage').then((m) => ({
      default: m.PaymentChannelPage,
    })),
  ),
  'mall/promotion/reward-activity/index': lazy(() =>
    import('../modules/mall/promotion/pages/ActivityPage').then((m) => ({
      default: m.RewardActivityPage,
    })),
  ),
  'mall/promotion/discount-activity/index': lazy(() =>
    import('../modules/mall/promotion/pages/ActivityPage').then((m) => ({
      default: m.DiscountActivityPage,
    })),
  ),
  'mall/comment/index': lazy(() => import('../modules/mall/catalog/pages/CommentPage')),
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
  'mall/coupon/records': lazy(() => import('../modules/mall/checkout/pages/CouponRecordPage')),
  'mall/coupon/index': lazy(() => import('../modules/mall/checkout/pages/CouponTemplatePage')),
  'trade/order/index': lazy(
    () => import('../modules/trade/fulfillment/pages/OrderFulfillmentPage'),
  ),
  'trade/refund/index': lazy(() => import('../modules/trade/fulfillment/pages/RefundRequestPage')),
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
