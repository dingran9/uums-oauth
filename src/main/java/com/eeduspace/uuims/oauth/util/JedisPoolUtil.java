package com.eeduspace.uuims.oauth.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;

public class JedisPoolUtil {

    private static final Logger logger = LoggerFactory.getLogger(JedisPoolUtil.class);

    public static JedisPool pool;

    static {
        JedisPoolConfig config = new JedisPoolConfig();

        try {
            String redisIp = ConfigUtil.getProperty("redis.ip");
            int active = Integer.parseInt(ConfigUtil.getProperty("redis.maxactive"));
            int idle = Integer.parseInt(ConfigUtil.getProperty("redis.maxidle"));
            int wait = Integer.parseInt(ConfigUtil.getProperty("redis.maxwait"));
            config.setMaxTotal(active);
            config.setMaxIdle(idle);
            config.setMaxWaitMillis(wait);
            config.setTestOnBorrow(true);
            pool = new JedisPool(config, redisIp);
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.debug("JedisPoolUtil exception:{}",e);
        }

    }

    /**
     * 测试 是否能连接
     * @return
     */
    public java.lang.String ping() {
        Jedis jedis = pool.getResource();
        String ping= jedis.ping();
        pool.returnResource(jedis);
        return ping;
    }

    /**
     * 检查某个key是否在缓存中存在，如果存在返回true，否则返回false；需要注意的是，即使该key所对应的value是一个空字符串，
     * 也依然会返回true。
     *
     * @param key
     * @return
     */
    public static boolean exists(String key) {
        Jedis jedis = pool.getResource();
        boolean exists= jedis.exists(key);
        pool.returnResource(jedis);
        return exists;
    }

    /**
     * 存储数据到缓存中，若key已存在则覆盖 value的长度不能超过1073741824 bytes (1 GB)
     *
     * @param key
     * @param value
     * @return
     */
    public static void put(String key, String value) {
        Jedis jedis = pool.getResource();
        jedis.set(key, value);
        pool.returnResource(jedis);
    }
    /**
     * 存储数据到缓存中，若key已存在则覆盖 value的长度不能超过1073741824 bytes (1 GB)
     *
     * @param key
     * @param value
     * @return
     */
    public static void put(String key, String value, int seconds) {
        Jedis jedis = pool.getResource();
        jedis.set(key, value);
        jedis.expire(key,seconds);
        pool.returnResource(jedis);
    }

    /**
     *
     * @param key
     * @param seconds
     * @param value
     * @return
     */
    public static String setex(String key, int seconds, String value){
        Jedis jedis = pool.getResource();
        String setex= jedis.setex(key,seconds,value);
        pool.returnResource(jedis);
        return setex;

    }
    /**
     * 若key存在，将value追加到原有字符串的末尾。若key不存在，则创建一个新的空字符串。
     *
     * @param key
     * @param value
     * @return 返回字符串的总长度
     */
    public static Long append(String key, String value){
        Jedis jedis = pool.getResource();
        Long append= jedis.append(key,value);
        pool.returnResource(jedis);
        return append;
    }


    /**
     * 为key设置一个特定的过期时间，单位为秒。过期时间一到，redis将会从缓存中删除掉该key。
     * 即使是有过期时间的key，redis也会在持久化时将其写到硬盘中，并把相对过期时间改为绝对的Unix过期时间。
     * 在一个有设置过期时间的key上重复设置过期时间将会覆盖原先设置的过期时间。
     *
     * @param key
     * @param seconds
     * @return 返回1表示成功设置过期时间，返回0表示key不存在。
     */
    public static  Long expire(String key, int seconds){
        Jedis jedis = pool.getResource();
        long t=  jedis.expire(key,seconds);
        pool.returnResource(jedis);
        return t;
    }
    /**
     * 返回一个key还能活多久，单位为秒
     *
     * @param key
     * @return 如果该key本来并没有设置过期时间，则返回-1，如果该key不存在，则返回-2
     */
    public static  Long ttl(String key){
        Jedis jedis = pool.getResource();
        long t=  jedis.ttl(key);
        pool.returnResource(jedis);
        return t;
    }

    public static List<String> getList(String string) {
        Jedis jedis = pool.getResource();
        List<String> str = jedis.mget(string);
        pool.returnResource(jedis);
        return str;
    }
    /**
     * 从缓存中根据key取得其String类型的值，如果key不存在则返回null，如果key存在但value不是string类型的，
     * 则返回一个error。这个方法只能从缓存中取得value为string类型的值。
     *
     * @param string
     * @return
     */
    public static String get(String string) {
        Jedis jedis = pool.getResource();
        String result = jedis.get(string);
        pool.returnResource(jedis);
        return result;

    }

    public static void remove(String string) {
        Jedis jedis = pool.getResource();
        jedis.del(string);
        pool.returnResource(jedis);
    }

}
