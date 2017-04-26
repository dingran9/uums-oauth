package com.eeduspace.uuims.oauth.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Author: dingran
 * Date: 2016/3/24
 * Description:
 */
public class RedisPoolUtils {
    private static final JedisPool pool;

    static {
        JedisPoolConfig config = new JedisPoolConfig();
        //设置池配置项值
        config.setMaxTotal(1024);
        config.setMaxIdle(200);
        config.setMaxWaitMillis(1000);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);

        pool = new JedisPool(config,"127.0.0.1", 6379);
    }

    /**
     * 28      *
     * 29      * @Title: release
     * 30      * @Description: 释放连接
     * 31      * @param @param jedis
     * 32      * @return void
     * 33      * @throws
     * 34
     */
    public static void release(Jedis jedis) {
        pool.returnResource(jedis);
    }

    public static Jedis getJedis() {
        return pool.getResource();
    }
}
