package com.eeduspace.uuims.oauth.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

/**
 * Author: dingran
 * Date: 2015/10/29
 * Description:Redis实现
 */
@Service("redisDataSource")
public class RedisDataSourceImpl implements RedisDataSource {

    private static final Logger logger = LoggerFactory.getLogger(RedisDataSourceImpl.class);

    @Autowired
    private ShardedJedisPool shardedJedisPool;

    @Autowired
    private JedisPool jedisPool;
    @Override
    public ShardedJedis getRedisClient() {
        try {
            ShardedJedis shardJedis = shardedJedisPool.getResource();
            return shardJedis;
        } catch (Exception e) {
            logger.error("getRedisClent error", e);
        }
        return null;
    }

    @Override
    public void returnResource(ShardedJedis shardedJedis) {
        shardedJedisPool.returnResource(shardedJedis);
    }

    @Override
    public void returnResource(ShardedJedis shardedJedis, boolean broken) {
        if (broken) {
            shardedJedisPool.returnBrokenResource(shardedJedis);
        } else {
            shardedJedisPool.returnResource(shardedJedis);
        }
    }

    @Override
    public Jedis getJedisClient() {
        try {
            Jedis Jedis = jedisPool.getResource();
            return Jedis;
        } catch (Exception e) {
            logger.error("getRedisClent error", e);
        }
        return null;
    }

    @Override
    public void returnResource(Jedis jedis) {
        jedisPool.returnResource(jedis);
    }

    @Override
    public void returnResource(Jedis jedis, boolean broken) {
        if (broken) {
            jedisPool.returnBrokenResource(jedis);
        } else {
            jedisPool.returnResource(jedis);
        }
    }
}
