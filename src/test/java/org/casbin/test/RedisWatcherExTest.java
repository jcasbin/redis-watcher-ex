package org.casbin.test;

import io.lettuce.core.RedisURI;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;
import org.casbin.jcasbin.persist.WatcherEx;
import org.casbin.watcherEx.RedisWatcherEx;
import org.casbin.watcherEx.WatcherOptions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RedisWatcherExTest {
    private Enforcer enforcer;
    private WatcherEx watcher;

    @Before
    public void initWatcher() {
        WatcherOptions options = new WatcherOptions();
        options.setChannel("jcasbin-channel");
        options.setOptions(RedisURI.builder().withHost("192.168.101.65").withPort(6379).withPassword("redis").build());

        enforcer = new Enforcer("examples/rbac_model.conf", "examples/rbac_policy.csv");
        watcher = new RedisWatcherEx(options);
        enforcer.setWatcher(watcher);
    }

    @Test
    public void testUpdate() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        watcher.setUpdateCallback((msg)-> {
            countDownLatch.countDown();
            System.out.println("test method : " + msg);
        });
        watcher.update();
        Assert.assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testUpdateForAddPolicy() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        watcher.setUpdateCallback((msg)-> {
            countDownLatch.countDown();
            System.out.println("test method : " + msg);
        });
        watcher.updateForAddPolicy("alice", "data1", "read");
        Assert.assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));

    }

    @Test
    public void testUpdateForRemovePolicy() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        watcher.setUpdateCallback((msg)-> {
            countDownLatch.countDown();
            System.out.println("test method : " + msg);
        });
        watcher.updateForRemovePolicy("alice", "data1", "read");
        Assert.assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));

    }

    @Test
    public void testUpdateForRemoveFilteredPolicy() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        watcher.setUpdateCallback((msg)-> {
            countDownLatch.countDown();
            System.out.println("test method :" + msg);
        });
        watcher.updateForRemoveFilteredPolicy("alice", "data1", 1,"read");
        Assert.assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));

    }

    @Test
    public void testUpdateForSavePolicy() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        watcher.setUpdateCallback((msg)-> {
            countDownLatch.countDown();
            System.out.println("test method :" + msg);
        });
        watcher.updateForSavePolicy(new Model());
        Assert.assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));

    }

    @Test
    public void testUpdateForAddPolicies() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        watcher.setUpdateCallback((msg)-> {
            countDownLatch.countDown();
            System.out.println("test method :" + msg);
        });
        List<List<String>> rules = Arrays.asList(
                Arrays.asList("jack", "data4", "read"),
                Arrays.asList("katy", "data4", "write"),
                Arrays.asList("leyo", "data4", "read"),
                Arrays.asList("ham", "data4", "write")
        );
        watcher.updateForAddPolicies("alice", "data1", rules);
        Assert.assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testUpdateForRemovePolicies() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        watcher.setUpdateCallback((msg)-> {
            countDownLatch.countDown();
            System.out.println("test method :" + msg);
        });
        List<List<String>> rules = Arrays.asList(
                Arrays.asList("jack", "data4", "read"),
                Arrays.asList("katy", "data4", "write"),
                Arrays.asList("leyo", "data4", "read"),
                Arrays.asList("ham", "data4", "write")
        );
        watcher.updateForRemovePolicies("alice", "data1", rules);
        Assert.assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
    }
}
