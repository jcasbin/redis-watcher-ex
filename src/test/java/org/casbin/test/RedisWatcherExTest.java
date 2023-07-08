package org.casbin.test;

import org.casbin.jcasbin.main.Enforcer;
import org.casbin.watcherEx.RedisWatcherEx;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.JedisPoolConfig;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

public class RedisWatcherExTest {
    private RedisWatcherEx redisWatcher,redisConfigWatcher;
    private final String expect="update msg";
    private final String expectConfig="update msg for config";

    /**
     * You should replace the initWatcher() method's content with your own Redis instance.
     */
    @Before
    public void initWatcher(){
        String redisTopic = "jcasbin-topic";
        String redisConfig = "jcasbin-config";
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(2);
        config.setMaxWaitMillis(100 * 1000);
        redisWatcher = new RedisWatcherEx("192.168.101.65",6379, redisTopic, 2000, "redis");
        redisConfigWatcher = new RedisWatcherEx(config,"192.168.101.65",6379, redisConfig, 2000, "redis");
        Enforcer enforcer = new Enforcer();
        enforcer.setWatcher(redisWatcher);
        Enforcer configEnforcer = new Enforcer();
        configEnforcer.setWatcher(redisConfigWatcher);
    }

    @Test
    public void testConfigUpdate() throws InterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        redisConfigWatcher.setUpdateCallback(()-> System.out.print(expectConfig) );
        redisConfigWatcher.update();
        Thread.sleep(100);
        Assert.assertEquals(expectConfig, expectConfig);
    }

    @Test
    public void testUpdate() throws InterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        redisWatcher.setUpdateCallback(()-> System.out.print(expect) );
        redisWatcher.update();
        Thread.sleep(100);
        Assert.assertEquals(expect, expect);
    }

    @Test
    public void testConsumerCallback() throws InterruptedException {
        redisWatcher.setUpdateCallback((s) -> {
            System.out.print(s);
        });
        redisWatcher.update();
        Thread.sleep(100);

        redisConfigWatcher.setUpdateCallback((s) -> {
            System.out.print(s);
        });
        redisConfigWatcher.update();
        Thread.sleep(100);
    }

    @Test
    public void testConnectWatcherWithoutPassword() {
        String redisTopic = "jcasbin-topic";
        RedisWatcherEx redisWatcherWithoutPassword = new RedisWatcherEx("127.0.0.1", 6379, redisTopic);
        Assert.assertNotNull(redisWatcherWithoutPassword);

        String redisConfig = "jcasbin-config";
        RedisWatcherEx redisConfigWatcherWithoutPassword = new RedisWatcherEx("127.0.0.1", 6379, redisConfig);
        Assert.assertNotNull(redisConfigWatcherWithoutPassword);
    }

    @Test
    public void testUpdateForAddPolicy() throws InterruptedException {
        redisWatcher.setUpdateCallback((s) -> {
            System.out.println(s);
        });

        Thread.sleep(500);
        Enforcer enforcer = new Enforcer("example/rbac_model.conf","example/rbac_policy.csv");
        Enforcer enforcer2 = new Enforcer("example/rbac_model.conf","example/rbac_policy.csv");
        enforcer.setWatcher(redisWatcher);
        enforcer2.setWatcher(redisWatcher);
        enforcer.addPolicy("alice", "book1", "write");
        Thread.sleep(500);
        Assert.assertNotEquals(enforcer2.getPolicy(), enforcer.getPolicy());
    }

    @Test
    public void testUpdateForRemovePolicy() throws InterruptedException {
        redisWatcher.setUpdateCallback((s) -> {
            System.out.println(s);
        });

        Enforcer enforcer = new Enforcer("example/rbac_model.conf","example/rbac_policy.csv");
        Enforcer enforcer2 = new Enforcer("example/rbac_model.conf","example/rbac_policy.csv");
        enforcer.setWatcher(redisWatcher);
        enforcer2.setWatcher(redisWatcher);

        Thread.sleep(500);
        enforcer.removePolicy("alice", "data1", "read");
        Thread.sleep(500);
        Assert.assertNotEquals(enforcer2.getPolicy(), enforcer.getPolicy());
    }

    @Test
    public void testUpdateForRemoveFilteredPolicy() throws InterruptedException {
        redisWatcher.setUpdateCallback((s) -> {
            System.out.println(s);
        });

        Enforcer enforcer = new Enforcer("example/rbac_model.conf","example/rbac_policy.csv");
        Enforcer enforcer2 = new Enforcer("example/rbac_model.conf","example/rbac_policy.csv");
        enforcer.setWatcher(redisWatcher);
        enforcer2.setWatcher(redisWatcher);

        Thread.sleep(500);
        enforcer.removeFilteredPolicy(1, "data1", "read");
        Thread.sleep(500);
        Assert.assertNotEquals(enforcer2.getPolicy(), enforcer.getPolicy());
    }

    @Test
    public void testUpdateSavePolicy() throws InterruptedException {
        redisWatcher.setUpdateCallback((s) -> {
            System.out.println(s);
        });

        Enforcer enforcer = new Enforcer("example/rbac_model.conf","example/rbac_policy.csv");
        Enforcer enforcer2 = new Enforcer("example/rbac_model.conf","example/rbac_policy.csv");
        enforcer.setWatcher(redisWatcher);
        enforcer2.setWatcher(redisWatcher);

        Thread.sleep(500);
        enforcer.savePolicy();
        Thread.sleep(500);
        Assert.assertEquals(enforcer2.getPolicy(), enforcer.getPolicy());
    }

    @Test
    public void testUpdateForAddPolicies() throws InterruptedException {
        List<List<String>> rules = Arrays.asList(
                Arrays.asList("jack", "data4", "read"),
                Arrays.asList("katy", "data4", "write"),
                Arrays.asList("leyo", "data4", "read"),
                Arrays.asList("ham", "data4", "write")
        );

        redisWatcher.setUpdateCallback((s) -> {
            System.out.println(s);
        });

        Enforcer enforcer = new Enforcer("example/rbac_model.conf","example/rbac_policy.csv");
        Enforcer enforcer2 = new Enforcer("example/rbac_model.conf","example/rbac_policy.csv");
        enforcer.setWatcher(redisWatcher);
        enforcer2.setWatcher(redisWatcher);

        Thread.sleep(500);
        enforcer.addPolicies(rules);
        Thread.sleep(500);
        Assert.assertNotEquals(enforcer2.getPolicy(), enforcer.getPolicy());
    }

    @Test
    public void testUpdateForRemovePolicies() throws InterruptedException {
        List<List<String>> rules = Arrays.asList(
                Arrays.asList("jack", "data4", "read"),
                Arrays.asList("katy", "data4", "write"),
                Arrays.asList("leyo", "data4", "read"),
                Arrays.asList("ham", "data4", "write")
        );

        redisWatcher.setUpdateCallback((s) -> {
            System.out.println(s);
        });

        Enforcer enforcer = new Enforcer("example/rbac_model.conf","example/rbac_policy.csv");
        Enforcer enforcer2 = new Enforcer("example/rbac_model.conf","example/rbac_policy.csv");
        enforcer.setWatcher(redisWatcher);
        enforcer2.setWatcher(redisWatcher);

        Thread.sleep(500);
        enforcer.removePolicies(rules);
        Thread.sleep(500);
        Assert.assertEquals(enforcer2.getPolicy(), enforcer.getPolicy());
    }

    @Test
    public void testUpdateForUpdatePolicy() throws InterruptedException {
        ByteArrayOutputStream dizzzy = new ByteArrayOutputStream();
        System.setOut(new PrintStream(dizzzy));
        redisWatcher.setUpdateCallback(() -> System.out.print(expect));
        redisWatcher.update();

        Enforcer enforcer = new Enforcer("example/rbac_model.conf", "example/rbac_policy.csv");
        enforcer.setWatcher(redisWatcher);

        Thread.sleep(500);
        enforcer.updatePolicy(Arrays.asList("alice", "data1", "read"), Arrays.asList("alice", "data1", "write"));
        Thread.sleep(500);

        Assert.assertEquals(expect, dizzzy.toString().trim());
    }

    @Test
    public void testUpdateForUpdatePolicies() throws InterruptedException {
        List<List<String>> rules = Arrays.asList(
                Arrays.asList("alice", "data1", "read"),
                Arrays.asList("katy", "data4", "write"),
                Arrays.asList("leyo", "data4", "read"),
                Arrays.asList("ham", "data4", "write")
        );
        ByteArrayOutputStream dizzzy = new ByteArrayOutputStream();
        System.setOut(new PrintStream(dizzzy));
        redisWatcher.setUpdateCallback(() -> System.out.print(expect));
        redisWatcher.update();

        Enforcer enforcer = new Enforcer("example/rbac_model.conf", "example/rbac_policy.csv");
        enforcer.setWatcher(redisWatcher);

        Thread.sleep(500);
        enforcer.addPolicies(rules);
        Thread.sleep(500);

        Assert.assertEquals(expect, dizzzy.toString().trim());
    }
}
