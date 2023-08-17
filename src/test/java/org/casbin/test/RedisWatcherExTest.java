package org.casbin.test;

import io.lettuce.core.RedisURI;
import org.awaitility.Awaitility;
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
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Durations.TEN_SECONDS;


public class RedisWatcherExTest {
    private Enforcer enforcer;
    private WatcherEx watcher;

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
    public void testUpdate() {
        AtomicReference<String> message = new AtomicReference<>(null);
        watcher.setUpdateCallback((msg) -> {
            message.set(msg);
        });
        watcher.update();

        Awaitility.await().atMost(TEN_SECONDS).until(() -> message.get() != null);
        System.out.println("test method : " + message.get());
    }
    @Test
    public void testUpdateForAddPolicy() throws InterruptedException {
        AtomicReference<String> message = new AtomicReference<>(null);
        watcher.setUpdateCallback((msg)-> {
            message.set(msg);
        });
        watcher.updateForAddPolicy("alice", "data1", "read");

        Awaitility.await().atMost(TEN_SECONDS).until(() -> message.get() != null);
        System.out.println("test method : " + message.get());

    }

    @Test
    public void testUpdateForRemovePolicy() throws InterruptedException {
        AtomicReference<String> message = new AtomicReference<>(null);
        watcher.setUpdateCallback((msg) -> {
            message.set(msg);
        });
        watcher.updateForRemovePolicy("alice", "data1", "read");

        Awaitility.await().atMost(TEN_SECONDS).until(() -> message.get() != null);
        System.out.println("test method : " + message.get());

    }

    @Test
    public void testUpdateForRemoveFilteredPolicy() throws InterruptedException {
        AtomicReference<String> message = new AtomicReference<>(null);
        watcher.setUpdateCallback((msg) -> {
            message.set(msg);
        });
        watcher.updateForRemoveFilteredPolicy("alice", "data1", 1,"read");

        Awaitility.await().atMost(TEN_SECONDS).until(() -> message.get() != null);
        System.out.println("test method : " + message.get());

    }

    @Test
    public void testUpdateForSavePolicy() throws InterruptedException {
        AtomicReference<String> message = new AtomicReference<>(null);
        watcher.setUpdateCallback((msg) -> {
            message.set(msg);
        });
        watcher.updateForSavePolicy(new Model());
        Awaitility.await().atMost(TEN_SECONDS).until(() -> message.get() != null);
        System.out.println("test method : " + message.get());

    }

    @Test
    public void testUpdateForAddPolicies() throws InterruptedException {
        AtomicReference<String> message = new AtomicReference<>(null);
        watcher.setUpdateCallback((msg) -> {
            message.set(msg);
        });
        List<List<String>> rules = Arrays.asList(
                Arrays.asList("jack", "data4", "read"),
                Arrays.asList("katy", "data4", "write"),
                Arrays.asList("leyo", "data4", "read"),
                Arrays.asList("ham", "data4", "write")
        );
        watcher.updateForAddPolicies("alice", "data1", rules);
        Awaitility.await().atMost(TEN_SECONDS).until(() -> message.get() != null);
        System.out.println("test method : " + message.get());
    }

    @Test
    public void testUpdateForRemovePolicies() throws InterruptedException {
        AtomicReference<String> message = new AtomicReference<>(null);
        watcher.setUpdateCallback((msg) -> {
            message.set(msg);
        });
        List<List<String>> rules = Arrays.asList(
                Arrays.asList("jack", "data4", "read"),
                Arrays.asList("katy", "data4", "write"),
                Arrays.asList("leyo", "data4", "read"),
                Arrays.asList("ham", "data4", "write")
        );
        watcher.updateForRemovePolicies("alice", "data1", rules);

        Awaitility.await().atMost(TEN_SECONDS).until(() -> message.get() != null);
        System.out.println("test method : " + message.get());
    }
}
