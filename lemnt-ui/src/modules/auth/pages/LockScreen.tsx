import { useEffect, useRef, useState } from 'react';
import { Alert, Avatar, Button, Form, Input } from 'antd';
import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useSession } from '@/stores/userStore';
import { useScreenLock } from '@/stores/screenLockStore';
import { getToken } from '@/shared/auth/token';
import { unlockScreen } from '../api/screenLockApi';
import type { InputRef } from 'antd';
import type { ScreenLockRecord } from '../utils/screenLock';
import './lockScreen.css';

interface LockScreenProps {
  record: ScreenLockRecord;
}

interface UnlockFormValues {
  password: string;
}

export default function LockScreen({ record }: LockScreenProps) {
  const session = useSession();
  const navigate = useNavigate();
  const [form] = Form.useForm<UnlockFormValues>();
  const [now, setNow] = useState(() => new Date());
  const [isBusy, setIsBusy] = useState(false);
  const [error, setError] = useState<string>();
  const input = useRef<InputRef>(null);
  const pending = useRef<AbortController | null>(null);
  const isSubmitting = useRef(false);

  useEffect(() => {
    input.current?.focus();
    const timer = window.setInterval(() => setNow(new Date()), 1000);
    return () => {
      window.clearInterval(timer);
      pending.current?.abort();
    };
  }, []);

  async function handleUnlock(values: UnlockFormValues) {
    if (isSubmitting.current) return;
    isSubmitting.current = true;
    setIsBusy(true);
    setError(undefined);
    const token = getToken();
    const controller = new AbortController();
    pending.current = controller;
    try {
      const result = await unlockScreen(values.password, controller.signal);
      if (controller.signal.aborted || token !== getToken()) return;
      if (useScreenLock.getState().record !== record) return;
      if (result.data !== true) throw new Error(result.msg || '解锁失败，请重试');
      if (useScreenLock.getState().unlock(record, token)) {
        navigate(record.path, { replace: true });
      }
    } catch (reason) {
      if (controller.signal.aborted) return;
      setError(reason instanceof Error ? reason.message : '解锁失败，请重试');
      form.resetFields(['password']);
      input.current?.focus();
    } finally {
      isSubmitting.current = false;
      if (!controller.signal.aborted) setIsBusy(false);
    }
  }

  async function handleLogout() {
    if (isSubmitting.current) return;
    isSubmitting.current = true;
    setIsBusy(true);
    try {
      await session.logout();
    } catch {
      // logout 无论请求结果如何均清理本地会话。
    } finally {
      navigate('/login', { replace: true });
    }
  }

  const user = session.user?.user;
  return (
    <main className="screen-lock" aria-label="系统锁屏">
      <div className="screen-lock-time">{now.toLocaleTimeString('zh-CN', { hour12: false })}</div>
      <div className="screen-lock-date">
        {now.toLocaleDateString('zh-CN', {
          year: 'numeric',
          month: 'long',
          day: 'numeric',
          weekday: 'long',
        })}
      </div>
      <section className="screen-lock-card">
        <Avatar size={80} src={user?.avatar} icon={<UserOutlined />} />
        <h1>{user?.nickName || user?.userName}</h1>
        <p>
          <LockOutlined /> 系统已锁定，请输入登录密码解锁
        </p>
        <Form<UnlockFormValues> form={form} onFinish={handleUnlock} disabled={isBusy}>
          <Form.Item name="password" rules={[{ required: true, message: '请输入登录密码' }]}>
            <Input.Password
              ref={input}
              aria-label="登录密码"
              placeholder="请输入登录密码"
              autoComplete="current-password"
              size="large"
            />
          </Form.Item>
          {error && <Alert type="error" title={error} showIcon className="screen-lock-error" />}
          <Button type="primary" htmlType="submit" block size="large" loading={isBusy}>
            解锁
          </Button>
        </Form>
        <Button type="link" onClick={handleLogout} disabled={isBusy}>
          退出重新登录
        </Button>
      </section>
    </main>
  );
}
