import { PageContainer } from '@ant-design/pro-components';
import { Result } from 'antd';
import { usePermission } from '@/shared/hooks/usePermission';
import CouponRecordsPanel from './CouponRecordsPanel';

/** 会员持券管理独立于模板编辑权限。 */
export default function CouponRecordPage() {
  const { can } = usePermission();
  if (!can('mall:coupon:list')) return <Result status="403" title="没有优惠券查询权限" />;
  return (
    <PageContainer title="会员优惠券">
      <CouponRecordsPanel />
    </PageContainer>
  );
}
