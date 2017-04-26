package com.eeduspace.uuims.oauth.redis;

import com.eeduspace.uuims.oauth.BaseTest;
import com.eeduspace.uuims.oauth.model.UserModel;
import com.eeduspace.uuims.oauth.service.ProductService;
import com.eeduspace.uuims.oauth.service.UserService;
import com.eeduspace.uuims.oauth.util.RandomUtils;
import com.google.gson.Gson;
import net.sf.ehcache.store.compound.factories.AATreeSet;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.*;

/**
 * Author: dingran
 * Date: 2015/10/27
 * Description:
 */
public class RedisTest extends BaseTest {

    private static Gson gson = new Gson();
    @Inject
    private RedisClientTemplate redisClientTemplate;
    @Inject
    private UserService userService;
    @Inject
    private ProductService productService;
    
    @Value("${online.pidKey}")
	private String onlinePidKey;
    @Value("${online.uidKey}")
	private String onlineUidKey;
    @Value("${online.sidKey}")
	private String onlineSidKey;
	@Value("${online.xing}")
	private String onlineXing;
	
    @Test
    public void testRedis() {
  /*      JedisPoolConfig config = new JedisPoolConfig();

        String redisIp = "192.168.1.13";
        int active = 600;
        int idle = 300;
        int wait = 1000;
        config.setMaxActive(active);
        config.setMaxIdle(idle);
        config.setMaxWait(wait);
        config.setTestOnBorrow(true);
        pool = new JedisPool(config, redisIp);


        Jedis jedis = pool.getResource();
        jedis.set("testdr", "test");
        pool.returnResource(jedis);*/
    }


    //@Test
    public void test() {

//        redisClientTemplate.getKeyTag("TK"+"*");
//        ShardedJedis shardedJedis = redisDataSource.getRedisClient();
//        Map<String,String> map=new HashMap<>();
        Set<String> set = new AATreeSet<>();
        set=redisClientTemplate.keys("TK*");
//        set=redisClientTemplate.hkeys("TK*");
//        set= shardedJedis.hkeys("TK*");
//        logger.debug(gson.toJson(set));
//        redisClientTemplate.set("res","rest");
//        logger.debug("------->"+redisClientTemplate.get("res"));
//
//        Jedis jedis = RedisPoolUtils.getJedis();
//        set = jedis.keys("TK*");
        logger.debug(gson.toJson(set));

 /*       Jedis jedis = RedisPoolUtils.getJedis();

        Set<String> onlineSet = jedis.keys("TK*");

        List<String> onlines = new ArrayList<>(onlineSet);

        Pipeline pip = jedis.pipelined();
        for (String key : onlines) {
            pip.get(getKey(key));
        }
        List<Object> result = pip.syncAndReturnAll();
        RedisPoolUtils.release(jedis);

        for (int i = 0; i < result.size(); i++) {
            logger.debug(gson.toJson(result.get(i)));
        }*/

    }

    /**
     * 152      *
     * 153      * @Title: online
     * 154      * @Description: 分页显示在线列表
     * 155      * @return List
     * 156      * @throws
     * 157
     */
    public static List onlineByPage(int page, int pageSize) throws Exception {

        Jedis jedis = RedisPoolUtils.getJedis();

        Set onlineSet = jedis.keys("TK*");

        List onlines = new ArrayList(onlineSet);

        if (onlines.size() == 0) {
            return null;
        }

        Pipeline pip = jedis.pipelined();
        for (Object key : onlines) {
            pip.get(getKey(key));
        }
        List result = pip.syncAndReturnAll();
        RedisPoolUtils.release(jedis);

        List<UserModel> listUser = new ArrayList<UserModel>();
        for (int i = 0; i < result.size(); i++) {
           listUser.add(gson.fromJson(gson.toJson(result.get(i)),UserModel.class));
        }
        //排序-比较
        Collections.sort(listUser, new Comparator<UserModel>() {
            public int compare(UserModel o1, UserModel o2) {
                return o2.getCreateDate().compareTo(o1.getCreateDate());
            }
        });
        onlines = listUser;
        int start = (page - 1) * pageSize;
        int toIndex = (start + pageSize) > onlines.size() ? onlines.size() : start + pageSize;
        List list = onlines.subList(start, toIndex);

        return list;
    }

    private static String getKey(Object obj) {

        String temp = String.valueOf(obj);
        String key[] = temp.split(":");

        return "TK" + key[key.length - 1];
    }
    
  //  @Test
    public void changeOPK(){
    	System.out.println("从配置文件中取得的pidKey：" + onlinePidKey);
    	String productId = "1";
    	String equipment = "Web";
    	String openId = "fjdosqaf4e65w4fd1sa6f";
    	String onlinePidKey = this.onlinePidKey;
    	//利用MessageFormat.format()方法来替换掉从配置文件取得的可变的字符串
		/*if(StringUtils.isBlank(productId)){
			if(StringUtils.isBlank(equipment)){
				onlinePidKey = MessageFormat.format(this.onlinePidKey, this.onlineXing,this.onlineXing);
			}else{
				onlinePidKey = MessageFormat.format(this.onlinePidKey, this.onlineXing,equipment);
			}
		}else{
			if(StringUtils.isBlank(equipment)){
				onlinePidKey = MessageFormat.format(this.onlinePidKey, productId,this.onlineXing);
			}else{
				onlinePidKey = MessageFormat.format(this.onlinePidKey, productId,equipment);
			}
		}*/
		//测试替换字符串
		String onlineCookie = MessageFormat.format(this.onlineSidKey, openId, RandomUtils.getRandom(6));
		System.out.println(this.onlineSidKey + "-----onlineCookie：" + onlineCookie);
		String onlineUser = MessageFormat.format(this.onlineUidKey, openId);
		System.out.println(this.onlineUidKey + "-----onlineUser：" + onlineUser);
		String onlineProduct = MessageFormat.format(this.onlinePidKey, String.valueOf(productId),equipment,openId);
		System.out.println(this.onlinePidKey + "-----onlineProduct：" + onlineProduct);
		//replace来替换掉从配置文件取得的可变的字符串
		/*if(StringUtils.isBlank(productId)){
			onlinePidKey = onlinePidKey.replace("{productId}", onlineXing);
		}else{
			onlinePidKey = onlinePidKey.replace("{productId}", productId);
		}
		if(StringUtils.isBlank(equipment)){
			onlinePidKey = onlinePidKey.replace("{equipment}", onlineXing);
		}else{
			onlinePidKey = onlinePidKey.replace("{equipment}", equipment);
		}*/
		System.out.println("转化的onlinePidKey：" + onlinePidKey);
	}
    
}

