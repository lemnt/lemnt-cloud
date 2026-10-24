import { useEffect, useState } from 'react';
import { LoginForm, ProFormText } from '@ant-design/pro-components';
import { Alert, App, Button, ConfigProvider, theme } from 'antd';
import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { Navigate, useNavigate, useSearchParams } from 'react-router-dom';
import { request } from '@/api/request';
import { useSession } from '@/stores/userStore';
import { safeRedirect } from '@/shared/auth/permissions';
import { BrandLogo } from '@/shared/components/BrandLogo';
import { ApplicationFooter } from '@/shared/components/ApplicationFooter';
import styles from './loginPage.module.css';

interface Captcha {
  captchaEnabled?: boolean;
  img?: string;
  uuid?: string;
}
interface LoginFormValues {
  username: string;
  password: string;
  code?: string;
}

export default function LoginPage() {
  const session = useSession();
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const { message } = App.useApp();
  const [captcha, setCaptcha] = useState<Captcha>();
  const [error, setError] = useState<string>();
  async function refreshCaptcha() {
    try {
      const result = await request<Captcha>({ url: '/code', timeout: 20000 }, true);
      setCaptcha(result.data || (result as unknown as Captcha));
      setError(undefined);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : '验证码加载失败');
    }
  }
  useEffect(() => {
    let isActive = true;
    // 初次请求在异步回调中更新状态，卸载后不再写入组件状态。
    void request<Captcha>({ url: '/code', timeout: 20000 }, true).then(
      (result) => {
        if (isActive) setCaptcha(result.data || (result as unknown as Captcha));
      },
      (reason: unknown) => {
        if (isActive) setError(reason instanceof Error ? reason.message : '验证码加载失败');
      },
    );
    return () => {
      isActive = false;
    };
  }, []);
  if (session.user) return <Navigate to={safeRedirect(params.get('redirect'))} replace />;
  return (
    <ConfigProvider
      theme={{
        algorithm: theme.defaultAlgorithm,
        cssVar: { key: 'login' },
        token: { colorPrimary: '#6863f5', borderRadius: 10, controlHeightLG: 44 },
      }}
    >
      <div className={styles.page}>
        <div className={styles.scene} aria-hidden="true">
          <div className={styles.dotField} />
          <div className={styles.orbit} />
          <div className={styles.orbLarge} />
          <div className={styles.orbSmall} />
          <div className={styles.orbTiny} />
          <div className={styles.glassPanel}>
            <div className={styles.panelHeading}>
              <span />
              <span />
              <span />
            </div>
            <div className={styles.panelChart}>
              <span />
              <span />
              <span />
              <span />
              <span />
              <span />
            </div>
            <div className={styles.panelLines}>
              <span />
              <span />
            </div>
          </div>
          <div className={styles.miniPanel}>
            <span />
            <span />
            <span />
          </div>
        </div>
        <main className={styles.main}>
          <LoginForm<LoginFormValues>
            logo={<BrandLogo />}
            title="lemnt-cloud 管理平台"
            contentStyle={{ width: '100%', minWidth: 0, maxWidth: '100%' }}
            submitter={{ searchConfig: { submitText: '登 录' } }}
            onFinish={async (values) => {
              try {
                await session.login({ ...values, uuid: captcha?.uuid || '' });
                navigate(safeRedirect(params.get('redirect')), { replace: true });
                return true;
              } catch (reason) {
                message.error(reason instanceof Error ? reason.message : '登录失败');
                await refreshCaptcha();
                return false;
              }
            }}
          >
            {error && (
              <Alert
                type="error"
                title={error}
                action={<Button onClick={refreshCaptcha}>重试</Button>}
              />
            )}
            <ProFormText
              name="username"
              fieldProps={{ size: 'large', prefix: <UserOutlined />, autoComplete: 'username' }}
              placeholder="账号"
              rules={[{ required: true, message: '请输入账号' }]}
            />
            <ProFormText.Password
              name="password"
              fieldProps={{
                size: 'large',
                prefix: <LockOutlined />,
                autoComplete: 'current-password',
              }}
              placeholder="密码"
              rules={[{ required: true, message: '请输入密码' }]}
            />
            {captcha?.captchaEnabled !== false && (
              <div className={styles.captcha}>
                <ProFormText
                  name="code"
                  formItemProps={{ className: styles.captchaInput }}
                  fieldProps={{ size: 'large', autoComplete: 'off' }}
                  placeholder="验证码"
                  rules={[{ required: true, message: '请输入验证码' }]}
                />
                <Button
                  type="text"
                  size="large"
                  className={styles.captchaButton}
                  aria-label="刷新验证码"
                  onClick={refreshCaptcha}
                >
                  {captcha?.img ? (
                    <span className={styles.captchaImageFrame}>
                      <img
                        className={styles.captchaImage}
                        alt="验证码"
                        src={`data:image/jpeg;base64,${captcha.img}`}
                      />
                    </span>
                  ) : (
                    '刷新'
                  )}
                </Button>
              </div>
            )}
          </LoginForm>
        </main>
        <ApplicationFooter />
      </div>
    </ConfigProvider>
  );
}
