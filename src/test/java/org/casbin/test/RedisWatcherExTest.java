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


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;


public class RedisWatcherExTest {
    private Enforcer enforcer;
    private WatcherEx watcher;

    private final String expect="update various types of messages";

    @Before
    public void initWatcher() {
        WatcherOptions options = new WatcherOptions();
        options.setChannel("jcasbin-channel");
        options.setOptions(RedisURI.builder()
                .withHost("127.0.0.1")
                .withPort(6379)
                .withPassword("foobared")
                .build());
        enforcer = new Enforcer("examples/rbac_model.conf", "examples/rbac_policy.csv");
        watcher = new RedisWatcherEx(options);
        enforcer.setWatcher(watcher);
    }

    @Test
    public void testUpdate() throws InterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        watcher.setUpdateCallback(()-> {
            System.out.println(expect);
        });
        watcher.update();

        Thread.sleep(100);
        Assert.assertEquals(expect, expect);
    }

    @Test
    public void testUpdateForAddPolicy() throws InterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        watcher.setUpdateCallback(()-> {
            System.out.println(expect);
        });

        watcher.updateForAddPolicy("alice", "data1", "read");

        Thread.sleep(100);
        Assert.assertEquals(expect, expect);
    }

    @Test
    public void testUpdateForRemovePolicy() throws InterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        watcher.setUpdateCallback(()-> {
            System.out.println(expect);
        });

        watcher.updateForRemovePolicy("alice", "data1", "read");

        Thread.sleep(100);
        Assert.assertEquals(expect, expect);
    }

    @Test
    public void testUpdateForRemoveFilteredPolicy() throws InterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        watcher.setUpdateCallback(()-> {
            System.out.println(expect);
        });
        watcher.updateForRemoveFilteredPolicy("alice", "data1", 1,"read");

        Thread.sleep(100);
        Assert.assertEquals(expect, expect);
    }

    @Test
    public void testUpdateForSavePolicy() throws InterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        watcher.setUpdateCallback(()-> {
            System.out.println(expect);
        });
        watcher.updateForSavePolicy(new Model());

        Thread.sleep(100);
        Assert.assertEquals(expect, expect);

    }

    @Test
    public void testUpdateForAddPolicies() throws InterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        watcher.setUpdateCallback(()-> {
            System.out.println(expect);
        });
        List<List<String>> rules = Arrays.asList(
                Arrays.asList("jack", "data4", "read"),
                Arrays.asList("katy", "data4", "write"),
                Arrays.asList("leyo", "data4", "read"),
                Arrays.asList("ham", "data4", "write")
        );
        watcher.updateForAddPolicies("alice", "data1", rules);

        Thread.sleep(100);
        Assert.assertEquals(expect, expect);
    }

    @Test
    public void testUpdateForRemovePolicies() throws InterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        watcher.setUpdateCallback(()-> {
            System.out.println(expect);
        });
        List<List<String>> rules = Arrays.asList(
                Arrays.asList("jack", "data4", "read"),
                Arrays.asList("katy", "data4", "write"),
                Arrays.asList("leyo", "data4", "read"),
                Arrays.asList("ham", "data4", "write")
        );
        watcher.updateForRemovePolicies("alice", "data1", rules);

        Thread.sleep(100);
        Assert.assertEquals(expect, expect);
    }
}
