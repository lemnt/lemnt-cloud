import { PageContainer } from '@ant-design/pro-components';
import { UserOutlined } from '@ant-design/icons';
import { Avatar, Card, Empty, Space, Typography } from 'antd';
import { Link } from 'react-router-dom';
import { pages } from '@/router/routes';
import { useSession } from '@/stores/userStore';
import defaultAvatar from '@/shared/assets/default-avatar.png';
import { homeShortcuts } from '../homeNavigation';
import styles from './homePage.module.css';

export default function HomePage() {
  const user = useSession((state) => state.user?.user);
  const menus = useSession((state) => state.menus);
  const shortcuts = homeShortcuts(menus, Object.keys(pages));
  return (
    <PageContainer
      className={styles.page}
      title={false}
      content={
        <div className={styles.welcome}>
          <Avatar
            size={72}
            src={user?.avatar || defaultAvatar}
            icon={<UserOutlined />}
            className={styles.avatar}
          />
          <div>
            <Typography.Title level={4} className={styles.greeting}>
              {user?.nickName || user?.userName}，欢迎回来！
            </Typography.Title>
            <Typography.Text type="secondary">
              lemnt-cloud 管理平台 · 管理系统配置与权限
            </Typography.Text>
          </div>
        </div>
      }
    >
      <Card title="快捷入口">
        {shortcuts.length ? (
          <Space wrap>
            {shortcuts.map((item) => (
              <Link key={item.path} to={item.path}>
                {item.title}
              </Link>
            ))}
          </Space>
        ) : (
          <Empty description="暂无可访问的菜单" />
        )}
      </Card>
    </PageContainer>
  );
}
