package com.lemnt.member.infrastructure.account.gateway;

import me.chanjar.weixin.common.redis.RedisTemplateWxRedisOps;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.lemnt.member.domain.account.model.MemberProviderException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;

final class BoundedWxRedisOps extends RedisTemplateWxRedisOps {
    BoundedWxRedisOps(StringRedisTemplate redis) {
        super(redis);
    }

    @Override
    public Lock getLock(String key) {
        Lock delegate = super.getLock(key);
        return new Lock() {
            public void lock() {
                try {
                    if (!delegate.tryLock(3, TimeUnit.SECONDS)) throw new MemberProviderException();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new MemberProviderException();
                }
            }

            public void lockInterruptibly() throws InterruptedException {
                if (!delegate.tryLock(3, TimeUnit.SECONDS)) throw new MemberProviderException();
            }

            public boolean tryLock() {
                return delegate.tryLock();
            }

            public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
                return delegate.tryLock(Math.min(3000, unit.toMillis(time)), TimeUnit.MILLISECONDS);
            }

            public void unlock() {
                delegate.unlock();
            }

            public Condition newCondition() {
                return delegate.newCondition();
            }
        };
    }
}
