package com.mycomany.community;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testStrings(){
        String redisKey= "test:count";

        redisTemplate.opsForValue().set(redisKey, 1);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));

    }

    @Test
    public void testHashes(){
        String redisKey ="test:user";
        redisTemplate.opsForHash().put(redisKey, "id",1);
        redisTemplate.opsForHash().put(redisKey, "username", "allen");

        System.out.println(redisTemplate.opsForHash().get(redisKey,"id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey,"username"));
    }

    @Test
    public void testLists(){
        String redisKey ="test:ids";

        redisTemplate.opsForList().leftPush(redisKey,"101");
        redisTemplate.opsForList().leftPush(redisKey,"102");
        redisTemplate.opsForList().leftPush(redisKey,"103");

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey,0));
        System.out.println(redisTemplate.opsForList().range(redisKey,0,2));

        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }


    @Test
    public void testSets(){
        String redisKey = "test:teachers";

        redisTemplate.opsForSet().add(redisKey,"allen","lina","bob");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));

    }

    @Test
    public void testSortedSets(){
        String redisKey ="test:students";

        redisTemplate.opsForZSet().add(redisKey,"allen",80);
        redisTemplate.opsForZSet().add(redisKey,"lina",90);
        redisTemplate.opsForZSet().add(redisKey,"bob",50);
        redisTemplate.opsForZSet().add(redisKey,"jame",60);
        redisTemplate.opsForZSet().add(redisKey,"henry",30);

        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println(redisTemplate.opsForZSet().score(redisKey,"bob"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey,"jame"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey,"allen"));
    }

    @Test
    public void testKeys(){
        redisTemplate.delete("test:user");

        System.out.println(redisTemplate.hasKey("test:user"));

        redisTemplate.expire("test:students",10, TimeUnit.SECONDS);
    }

    @Test
    public void testBoundOperations(){
        String redisKey = "test:teachers";
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        operations.add("sam","tom");
        System.out.println(operations.members());

    }

    @Test
    public void testTransactional(){
     Object obj = redisTemplate.execute(new SessionCallback() {
           @Override
           public Object execute(RedisOperations operations) throws DataAccessException {
              String redisKey= "test:tx";
               operations.multi();

               operations.opsForSet().add(redisKey,"huangxin");
               operations.opsForSet().add(redisKey,"remix");
               operations.opsForSet().add(redisKey,"kobe");

               System.out.println(operations.opsForSet().members(redisKey));

               return operations.exec();
           }
       });

     System.out.println(obj);

    }
}
