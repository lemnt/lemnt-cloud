import React from 'react';
import ReactDOM from 'react-dom/client';
import { ProConfigProvider } from '@ant-design/pro-components';
import { App, ConfigProvider, theme } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { useLayoutSettings } from '@/stores/layoutSettingsStore';
import Application from './App';
import 'antd/dist/reset.css';
import './styles/application.css';
import './styles/overlays.css';

function ApplicationRoot() {
  const settings = useLayoutSettings((state) => state.settings);
  const isDark = settings.navTheme === 'realDark';
  return (
    <ConfigProvider
      locale={zhCN}
      modal={{
        centered: true,
        classNames: {
          container: 'admin-overlay-container',
          header: 'admin-overlay-header',
          body: 'admin-overlay-body',
          footer: 'admin-overlay-footer',
          close: 'admin-overlay-close',
        },
      }}
      drawer={{
        classNames: {
          section: 'admin-drawer-container',
          header: 'admin-drawer-header',
          body: 'admin-drawer-body',
          footer: 'admin-drawer-footer',
        },
      }}
      popconfirm={{ classNames: { root: 'admin-popconfirm' } }}
      theme={{
        cssVar: { key: 'lemnt' },
        algorithm: isDark ? theme.darkAlgorithm : theme.defaultAlgorithm,
        token: {
          colorPrimary: settings.colorPrimary,
          borderRadius: 6,
          fontSize: 14,
          controlHeight: 32,
        },
        components: {
          Card: { headerFontSize: 16, headerHeight: 56 },
          Table: { headerBg: isDark ? '#1d1d1d' : '#fafafa' },
          Modal: { titleFontSize: 18, titleLineHeight: 1.5 },
          Drawer: { footerPaddingBlock: 16, footerPaddingInline: 24 },
        },
      }}
    >
      <ProConfigProvider dark={isDark}>
        <App className={settings.colorWeak ? 'application color-weak' : 'application'}>
          <Application />
        </App>
      </ProConfigProvider>
    </ConfigProvider>
  );
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ApplicationRoot />
  </React.StrictMode>,
);
