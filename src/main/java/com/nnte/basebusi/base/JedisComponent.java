package com.nnte.basebusi.base;

import com.nnte.basebusi.annotation.BusiLogAttr;
import com.nnte.framework.base.BaseNnte;
import com.nnte.framework.base.NameLockComponent;
import com.nnte.framework.utils.JsonUtil;
import com.nnte.framework.utils.SerializeUtil;
import com.nnte.framework.utils.ThreadUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Component
@ConfigurationProperties(prefix = "redis")
@PropertySource(value = "classpath:redis.properties")
@BusiLogAttr(BaseBusi.Logger_Name)
public class JedisComponent extends BaseNnte {

    @Autowired
    private NameLockComponent nameLock;
    private static JedisPool jedis_rmq;
    private static JedisPool pool;

    @Getter
    @Setter
    private String redisServer;
    @Getter
    @Setter
    private String redisPws;
    @Getter
    @Setter
    private String localHostAbstractName;

    public void initJedisCom() {
        outLogInfo("开始初始化Jedis.......");
        JedisPoolConfig config = new JedisPoolConfig();
        //连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
        config.setBlockWhenExhausted(true);
        //设置的逐出策略类名, 默认DefaultEvictionPolicy(当连接超过最大空闲时间,或连接数超过最大空闲连接数)
        config.setEvictionPolicyClassName("org.apache.commons.pool2.impl.DefaultEvictionPolicy");
        //是否启用pool的jmx管理功能, 默认true config.setJmxEnabled(true);
        //MBean ObjectName = new ObjectName("org.apache.commons.pool2:type=GenericObjectPool,name=" + "pool" + i); 默认为"pool", JMX不熟,具体不知道是干啥的...默认就好. config.setJmxNamePrefix("pool");
        //是否启用后进先出, 默认true
        config.setLifo(true);
        //最大空闲连接数, 默认8个
        config.setMaxIdle(8);
        //最大连接数, 默认8个
        config.setMaxTotal(800);
        //获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
        config.setMaxWaitMillis(-1);
        //逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
        config.setMinEvictableIdleTimeMillis(1800000);
        //最小空闲连接数, 默认0
        config.setMinIdle(0);
        //每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
        config.setNumTestsPerEvictionRun(3);
        //对象空闲多久后逐出, 当空闲时间>该值 且 空闲连接>最大空闲数 时直接逐出,不再根据MinEvictableIdleTimeMillis判断  (默认逐出策略)
        config.setSoftMinEvictableIdleTimeMillis(1800000);
        //在获取连接的时候检查有效性, 默认false
        config.setTestOnBorrow(true);
        //在空闲时检查有效性, 默认false
        config.setTestWhileIdle(true);
        config.setTestOnCreate(true);
        //逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
        config.setTimeBetweenEvictionRunsMillis(100000);
        try {
            pool = new JedisPool(config, getRedisServer(),
                    Protocol.DEFAULT_PORT, Protocol.DEFAULT_TIMEOUT, getRedisPws());
        } catch (Exception e) {
            BaseLog.outLogExp(e);
        }

        //以下是消息服务器专用pool
        JedisPoolConfig config2 = new JedisPoolConfig();
        //连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
        config2.setBlockWhenExhausted(true);
        //设置的逐出策略类名, 默认DefaultEvictionPolicy(当连接超过最大空闲时间,或连接数超过最大空闲连接数)
        config2.setEvictionPolicyClassName("org.apache.commons.pool2.impl.DefaultEvictionPolicy");
        //是否启用pool的jmx管理功能, 默认true config.setJmxEnabled(true);
        //MBean ObjectName = new ObjectName("org.apache.commons.pool2:type=GenericObjectPool,name=" + "pool" + i); 默认为"pool", JMX不熟,具体不知道是干啥的...默认就好. config.setJmxNamePrefix("pool");
        //是否启用后进先出, 默认true
        config2.setLifo(true);
        //最大空闲连接数, 默认8个
        config2.setMaxIdle(4);
        //最大连接数, 默认8个
        config2.setMaxTotal(50);
        //获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
        config2.setMaxWaitMillis(-1);
        //逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
        config2.setMinEvictableIdleTimeMillis(1800000);
        //最小空闲连接数, 默认0
        config2.setMinIdle(0);
        //每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
        config2.setNumTestsPerEvictionRun(3);
        //对象空闲多久后逐出, 当空闲时间>该值 且 空闲连接>最大空闲数 时直接逐出,不再根据MinEvictableIdleTimeMillis判断  (默认逐出策略)
        config2.setSoftMinEvictableIdleTimeMillis(1800000);
        //在获取连接的时候检查有效性, 默认false
        config2.setTestOnBorrow(true);
        //在空闲时检查有效性, 默认false
        config2.setTestWhileIdle(true);
        config2.setTestOnCreate(true);
        //逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
        config2.setTimeBetweenEvictionRunsMillis(100000);
        try {
            jedis_rmq = new JedisPool(config2, getRedisServer(),
                    Protocol.DEFAULT_PORT, Protocol.DEFAULT_TIMEOUT, getRedisPws());
        } catch (Exception e) {
            BaseLog.outLogExp(e);
        }
        outLogInfo("Jedis init success!IP=" + getRedisServer());
    }

    public Map<String, String> getByPattern(String pkey) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            Set<String> keys = jedis.keys(pkey);
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                String value = jedis.get(key);
                //System.out.println(key + "=" + value);
                map.put(key, value);
            }
        } catch (Exception e) {
            BaseLog.outLogExp(e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
        return map;
    }

    public void flushAll() {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.flushAll();
        } catch (Exception e) {
            BaseLog.outLogExp(e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }

    //返回Redis服务器时间(以 UNIX 时间戳格式表示)
    public Long getRedisTime() {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            List<String> listTime = jedis.time();
            if (listTime != null && listTime.size() > 0)
                return Long.valueOf(listTime.get(0));
        } catch (Exception e) {
            BaseLog.outLogExp(e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
        return 0L;
    }

    public String get(String pkey) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.get(pkey);
        } catch (Exception e) {
            BaseLog.outLogExp(e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
        return null;
    }

    /**
     * @param pkey
     * @param expired_time 秒
     * @param v
     * @throws Exception
     */
    public void setex(String pkey, int expired_time, String v) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.setex(pkey, expired_time, v);
        } catch (Exception e) {
            BaseLog.outLogExp(e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }

    public void del(String pkey) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.del(pkey);
        } catch (Exception e) {
            BaseLog.outLogExp(e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }

    public String setObj(String key, int seconds, Object o) {
        String r = "";
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            r = jedis.setex(key, seconds, JsonUtil.beanToJson(o));
        } catch (Exception e) {
            BaseLog.outLogExp(e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
        return r;
    }

    public <T> T getObj(String key, Class<T> pojoCalss) {
        String json = null;
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            json = jedis.get(key);
        } catch (Exception e) {
            BaseLog.outLogExp(e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
        if (json != null)
            try {
                return JsonUtil.jsonToBean(json, pojoCalss);
            } catch (Exception e) {
                BaseLog.outLogExp(e);
            }
        return null;
    }

    public <T> String setObjList(String key, int seconds, List<T> o) {
        String r = "";
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            r = jedis.setex(key, seconds, JsonUtil.beanToJson(o));
        } catch (Exception e) {
            BaseLog.outLogExp(e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
        return r;
    }

    public <T> List<T> getObjList(String key) {
        String json = null;
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            json = jedis.get(key);
        } catch (Exception e) {
            BaseLog.outLogExp(e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
        if (json != null)
            try {
                List<T> tmpList = new ArrayList<T>();
                return JsonUtil.jsonToBean(json, tmpList.getClass());
            } catch (Exception e) {
                BaseLog.outLogExp(e);
            }
        return null;
    }

    public String setObjByte(String key, int seconds, Object o) {
        String r = "";
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            r = jedis.setex(key.getBytes(), seconds, SerializeUtil.serialize(o));
        } catch (Exception e) {
            BaseLog.outLogExp(e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
        return r;
    }

    public Object getObjByte(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return SerializeUtil.unserialize(jedis.get(key.getBytes()));
        } catch (Exception e) {
            BaseLog.outLogExp(e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
        return null;
    }

    public void delByte(String pkey) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            jedis.del(pkey.getBytes());
        } catch (Exception e) {
            BaseLog.outLogExp(e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }

    public long incrBy(String key, long num) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.incrBy(key, num);
        } catch (Exception e) {
            BaseLog.outLogExp(e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
        return 0;
    }

    //安全设置Redis锁
    public boolean setRedisLockSafe(String lockName, int seconds) {
        ReentrantLock safeLock = nameLock.getLockByName(lockName, true);//公平锁
        if (safeLock == null)
            return false;
        boolean sucSetLock = false;
        try {
            sucSetLock = safeLock.tryLock(Long.valueOf(seconds), TimeUnit.SECONDS);
            if (sucSetLock)
                return setRedisLock(lockName, seconds);
        } catch (Exception e) {
            BaseLog.outLogExp(e);
        } finally {
            if (sucSetLock)
                safeLock.unlock();
        }
        return false;
    }

    //返回true加锁成功，可以继续操作，返回false说明加锁失败，不可继续操作
    public boolean setRedisLock(String lock, int seconds) {
        Jedis jedis = null;
        Long expired = seconds * 1000l;//锁过期时间设置5秒
        try {
            int reqTimes = 0;
            int interval = 250;//默认250毫秒试着加锁一次
            do {
                //服务器绝对名称+线程号+锁名=锁值，表示锁只能被当前服务器的加锁线程释放
                String value = getLocalHostAbstractName() + "_" + ThreadUtil.getThreadCode() + "_" + lock;
                jedis = pool.getResource();
                long acquired = jedis.setnx(lock, String.valueOf(value));
                //SETNX成功，则成功获取一个锁
                if (acquired == 1) {
                    jedis.expire(lock, seconds);//设置5秒超时
                    return true;
                }
                ThreadUtil.Sleep(interval);
                reqTimes++;
            } while (reqTimes * interval < seconds * 1000);
        } catch (Exception e) {
            BaseLog.outLogExp(e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
        return false;//默认是枷锁失败
    }

    //释放锁 非强制释放
    public void releaseRedisLock(String lock) {
        releaseRedisLock(lock, false);
    }

    //释放锁 isForce强制释放标志，默认应为false, isForce=true表示强制释放，
    // 即不判断锁是否是本服务器本线程加上的，一律释放
    public void releaseRedisLock(String lock, boolean isForce) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (jedis != null) {
                if (isForce)
                    jedis.del(lock);
                else {
                    String value = getLocalHostAbstractName() + "_" + ThreadUtil.getThreadCode() + "_" + lock;
                    String lockVal = jedis.get(lock);
                    if (lockVal.equals(value))
                        jedis.del(lock);
                }
            }
        } catch (Exception e) {
            BaseLog.outLogExp(e);
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }
}
