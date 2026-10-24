import { useState, type ReactNode } from 'react';
import { ProLayout } from '@ant-design/pro-components';
import { FullscreenOutlined, LockOutlined, LogoutOutlined, UserOutlined } from '@ant-design/icons';
import { App, Button, Dropdown, Grid, Result, Spin, Tooltip, Typography } from 'antd';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useSession } from '@/stores/userStore';
import { useScreenLock } from '@/stores/screenLockStore';
import { useLayoutSettings } from '@/stores/layoutSettingsStore';
import { ApplicationFooter } from '@/shared/components/ApplicationFooter';
import { BrandLogo } from '@/shared/components/BrandLogo';
import defaultAvatar from '@/shared/assets/default-avatar.png';
import HeaderNotice from '@/modules/system/notice/components/HeaderNotice';
import { getToken } from '../shared/auth/token';
import { findRoute, type MenuRoute } from '../router/menu';
import { pages } from '../router/routes';
import { PageCache } from './PageCache';
import { MenuIcon } from './MenuIcon';
import styles from './basicLayout.module.css';

function navigation(routes: MenuRoute[]): {
  path: string;
  name?: string;
  icon?: ReactNode;
  hideInMenu?: boolean;
  children?: ReturnType<typeof navigation>;
}[] {
  return routes.map((route) => ({
    path: route.path,
    name: route.meta?.title || route.name,
    icon:
      route.meta?.icon && route.meta.icon !== '#' ? <MenuIcon name={route.meta.icon} /> : undefined,
    hideInMenu: route.hidden,
    children: route.children ? navigation(route.children) : undefined,
  }));
}

export default function BasicLayout() {
  const session = useSession();
  const location = useLocation();
  const navigate = useNavigate();
  const screens = Grid.useBreakpoint();
  const [collapsed, setCollapsed] = useState<boolean>();
  const settings = useLayoutSettings((state) => state.settings);
  const { message } = App.useApp();
  if (!getToken())
    return (
      <Navigate
        to={`/login?redirect=${encodeURIComponent(location.pathname + location.search)}`}
        replace
      />
    );
  if (session.loading)
    return (
      <main className={styles.sessionState} aria-busy="true">
        <div className={styles.loadingContent} role="status" aria-live="polite">
          <BrandLogo />
          <Spin size="large" />
          <Typography.Paragraph type="secondary" className={styles.loadingDescription}>
            页面加载中…
          </Typography.Paragraph>
        </div>
      </main>
    );
  if (session.error || !session.user)
    return (
      <main className={styles.sessionState}>
        <Result
          status="error"
          title="加载失败"
          subTitle={session.error}
          extra={<Button onClick={session.initialize}>重试</Button>}
        />
      </main>
    );
  if (location.pathname === '/') return <Navigate to="/index" replace />;
  const route = findRoute(session.menus, location.pathname);
  const Page =
    location.pathname === '/index'
      ? pages['home/index']
      : location.pathname === '/user/profile'
        ? pages['system/user/profile/index']
        : route?.component
          ? pages[route.component]
          : undefined;
  return (
    <ProLayout
      {...settings}
      className={`app-shell ${styles.shell}`}
      title="lemnt-cloud 管理平台"
      logo={<BrandLogo />}
      locale="zh-CN"
      collapsed={collapsed}
      onCollapse={setCollapsed}
      onPageChange={() => {
        // 等路由实际切换后收起手机导航，未保存确认取消时仍保留菜单。
        if (screens.md === false) setCollapsed(true);
      }}
      siderWidth={256}
      menu={{ locale: false }}
      menuProps={{
        classNames: { popup: { root: styles.menuPopup } },
        selectedKeys: [location.pathname],
      }}
      token={{
        sider: {
          colorMenuBackground: '#fff',
          colorBgMenuItemCollapsedElevated: '#fff',
          colorBgMenuItemHover: '#f1f6ff',
          colorBgMenuItemActive: '#e6f0ff',
          colorBgMenuItemSelected: '#e6f0ff',
          colorTextMenu: '#475569',
          colorTextMenuSecondary: '#94a3b8',
          colorTextMenuTitle: '#1e293b',
          colorTextMenuSelected: '#1677ff',
          colorTextSubMenuSelected: '#1677ff',
          colorTextMenuItemHover: '#1677ff',
          colorTextMenuActive: '#1677ff',
          colorMenuItemDivider: '#edf0f5',
          colorBgCollapsedButton: '#fff',
          colorTextCollapsedButton: '#1677ff',
          colorTextCollapsedButtonHover: '#1454b8',
        },
        header: {
          colorTextMenuSelected: '#1677ff',
          colorBgMenuItemSelected: 'rgba(22, 119, 255, 0.08)',
        },
      }}
      onMenuHeaderClick={() => navigate('/index')}
      headerRender={settings.headerRender}
      menuRender={settings.menuRender}
      footerRender={settings.footerRender === false ? false : () => <ApplicationFooter />}
      location={location}
      actionsRender={() => [
        <HeaderNotice key="notice" />,
        <Tooltip key="fullscreen" title="全屏">
          <Button
            type="text"
            aria-label="切换全屏"
            icon={<FullscreenOutlined />}
            onClick={async () => {
              try {
                if (document.fullscreenElement) await document.exitFullscreen();
                else await document.documentElement.requestFullscreen();
              } catch {
                message.error('当前浏览器无法切换全屏');
              }
            }}
          />
        </Tooltip>,
      ]}
      route={{
        path: '/',
        routes: [
          { path: '/index', name: '工作台', icon: <MenuIcon name="dashboard" /> },
          ...navigation(session.menus),
        ],
      }}
      menuItemRender={(item, dom) => (item.path ? <Link to={item.path}>{dom}</Link> : dom)}
      avatarProps={{
        src: session.user.user.avatar || defaultAvatar,
        icon: <UserOutlined />,
        title: session.user.user.nickName || session.user.user.userName,
        render: (_, dom) => (
          <Dropdown
            menu={{
              items: [
                { key: 'profile', label: '个人中心', icon: <UserOutlined /> },
                { key: 'lock', label: '锁定屏幕', icon: <LockOutlined /> },
                { type: 'divider' },
                { key: 'logout', label: '退出登录', icon: <LogoutOutlined /> },
              ],
              onClick: async ({ key }) => {
                if (key === 'profile') {
                  navigate('/user/profile');
                  return;
                }
                if (key === 'lock') {
                  const user = session.user?.user;
                  if (!user) return;
                  try {
                    useScreenLock
                      .getState()
                      .lock(user.userId, location.pathname + location.search + location.hash);
                  } catch {
                    message.error('锁屏状态保存失败，请检查浏览器存储设置');
                  }
                  return;
                }
                try {
                  await session.logout();
                  navigate('/login');
                } catch (reason) {
                  message.error(String(reason));
                }
              },
            }}
          >
            {dom}
          </Dropdown>
        ),
      }}
    >
      <PageCache
        key={JSON.stringify([session.user.user.userId, session.user.permissions, session.menus])}
        menus={session.menus}
      >
        {Page ? (
          <Page key={location.pathname} />
        ) : route ? (
          <Result status="info" title="此页面尚在迁移中" subTitle={route.meta?.title} />
        ) : (
          <Result status="404" title="页面不存在或没有访问权限" />
        )}
      </PageCache>
    </ProLayout>
  );
}
