import {
  PageContainer,
  ProForm,
  ProFormText,
  ProFormTextArea,
  ProFormSwitch,
} from '@ant-design/pro-components';
import { Alert, App, Button, Result } from 'antd';
import { useMutation, useQuery } from '@tanstack/react-query';
import { usePermission } from '@/shared/hooks/usePermission';
import * as api from '../api/tradeConfigApi';
/** 售后原因与全场包邮设置；其他全量配置字段保留读取值。 */
export default function TradeConfigPage() {
  const { can } = usePermission(),
    { message } = App.useApp();
  const config = useQuery({
    queryKey: ['trade-config'],
    queryFn: api.getTradeConfig,
    enabled: can('trade:config:query'),
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
  });
  const save = useMutation({
    mutationFn: (v: api.ConfigValues) => {
      if (!config.data) throw Error('配置尚未读取');
      return api.saveTradeConfig(config.data, v);
    },
  });
  if (!can('trade:config:query')) return <Result status="403" title="没有交易配置查询权限" />;
  return (
    <PageContainer title="交易配置">
      {config.isError && (
        <Alert
          type="error"
          title="交易配置读取失败"
          description={String(config.error)}
          action={<Button onClick={() => config.refetch()}>重试</Button>}
        />
      )}
      {config.isPending && <Alert type="info" title="正在读取交易配置…" />}
      {config.isSuccess && (
        <ProForm<api.ConfigValues>
          key={config.dataUpdatedAt}
          initialValues={api.configValues(config.data)}
          disabled={!can('trade:config:update') || save.isPending}
          submitter={can('trade:config:update') ? undefined : false}
          onFinish={async (values) => {
            if (save.isPending || !can('trade:config:update')) return false;
            try {
              await save.mutateAsync(values);
              message.success('交易配置已保存');
            } catch (e) {
              message.error(String(e));
              return false;
            }
            const refreshed = await config.refetch();
            if (refreshed.isError) message.warning('已保存，刷新失败请重试读取');
            return true;
          }}
        >
          <ProFormTextArea
            name="refundReasons"
            label="仅退款原因"
            extra="每行一个原因，最多100条，每条不超过200字；留空表示清空。"
            fieldProps={{ rows: 6 }}
          />
          <ProFormTextArea
            name="returnReasons"
            label="退货退款原因"
            extra="每行一个原因，最多100条，每条不超过200字；留空表示清空。"
            fieldProps={{ rows: 6 }}
          />
          <ProFormSwitch name="freeEnabled" label="全场满额包邮" />
          <ProFormText name="freePrice" label="包邮金额门槛（元）" rules={[{ required: true }]} />
        </ProForm>
      )}
    </PageContainer>
  );
}
