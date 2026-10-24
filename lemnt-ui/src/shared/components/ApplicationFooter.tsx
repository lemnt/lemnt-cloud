import { DefaultFooter } from '@ant-design/pro-components';

export function ApplicationFooter() {
  return (
    <DefaultFooter links={[]} copyright={`${new Date().getFullYear()} lemnt-cloud 管理平台`} />
  );
}
