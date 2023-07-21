// Copyright 2023 The casbin Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.casbin.watcherEx;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import org.casbin.jcasbin.model.Model;
import org.casbin.jcasbin.persist.WatcherEx;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class RedisWatcherEx implements WatcherEx {
    private final Lock lock;

    private final String localId;
    private final RedisCommands<String, String> subCommands;
    private final RedisCommands<String, String> pubCommands;
    private final String channel;
    private final WatcherOptions options;
    private Object updateCallback;

    public RedisWatcherEx(WatcherOptions options) {
        this.options = options;
        this.localId = UUID.randomUUID().toString();
        this.channel = options.getChannel();
        this.lock = new ReentrantLock();

        subCommands = RedisClient.create(options.getOptions()).connect().sync();
        pubCommands = RedisClient.create(options.getOptions()).connect().sync();

        if (!subCommands.ping().equals("PONG")) {
            throw new RuntimeException("Failed to connect to Redis subscriber");
        }

        if (!pubCommands.ping().equals("PONG")) {
            throw new RuntimeException("Failed to connect to Redis publisher");
        }
        subscribe();
    }


    @Override
    public void setUpdateCallback(Runnable runnable) {
        this.updateCallback = runnable;
    }

    @Override
    public void setUpdateCallback(Consumer<String> consumer) {
        this.updateCallback = consumer;
    }

    /**
     * Update publishes a message to all other casbin instances telling them to
     * invoke their update callback
     */
    @Override
    public void update() {
        logRecord(() -> {
            lock.lock();
            try {
                Msg msg = new Msg();
                msg.setMethod(UpdateType.Update);
                msg.setId(localId);
                publishMessage(msg);
            } finally {
                lock.unlock();
            }
        });
    }

    /**
     * UpdateForAddPolicy calls the update callback of other instances to synchronize their policy.
     * It is called after Enforcer.AddPolicy()
     */
    @Override
    public void updateForAddPolicy(String sec, String ptype, String... params) {
        logRecord(() -> {
            lock.lock();
            try {
                Msg msg = new Msg();
                msg.setMethod(UpdateType.UpdateForAddPolicy);
                msg.setId(localId);
                msg.setSec(sec);
                msg.setPtype(ptype);
                msg.setNewRule(Arrays.asList(params));
                publishMessage(msg);
            } finally {
                lock.unlock();
            }
        });
    }

    /**
     * UpdateForRemovePolicy calls the update callback of other instances to synchronize their policy.
     * It is called after Enforcer.RemovePolicy()
     */
    @Override
    public void updateForRemovePolicy(String sec, String ptype, String... params) {
        logRecord(() -> {
            lock.lock();
            try {
                Msg msg = new Msg();
                msg.setMethod(UpdateType.UpdateForRemovePolicy);
                msg.setId(localId);
                msg.setSec(sec);
                msg.setPtype(ptype);
                msg.setNewRule(Arrays.asList(params));
                publishMessage(msg);
            } finally {
                lock.unlock();
            }
        });
    }

    /**
     * UpdateForRemoveFilteredPolicy calls the update callback of other instances to synchronize their policy.
     * It is called after Enforcer.RemoveFilteredNamedGroupingPolicy()
     */
    @Override
    public void updateForRemoveFilteredPolicy(String sec, String ptype, int fieldIndex, String... fieldValues) {
        logRecord(() -> {
            lock.lock();
            try {
                Msg msg = new Msg();
                msg.setMethod(UpdateType.UpdateForRemoveFilteredPolicy);
                msg.setId(localId);
                msg.setSec(sec);
                msg.setPtype(ptype);
                msg.setFieldIndex(fieldIndex);
                msg.setFieldValues(Arrays.asList(fieldValues));
                publishMessage(msg);
            } finally {
                lock.unlock();
            }
        });
    }

    /**
     * UpdateForSavePolicy calls the update callback of other instances to synchronize their policy.
     * It is called after Enforcer.RemoveFilteredNamedGroupingPolicy()
     */
    @Override
    public void updateForSavePolicy(Model model) {
        logRecord(() -> {
            lock.lock();
            try {
                Msg msg = new Msg();
                msg.setMethod(UpdateType.UpdateForSavePolicy);
                msg.setId(localId);
                publishMessage(msg);
            } finally {
                lock.unlock();
            }
        });
    }

    /**
     * UpdateForAddPolicies calls the update callback of other instances to synchronize their policies in batch.
     * It is called after Enforcer.AddPolicies()
     */
    @Override
    public void updateForAddPolicies(String sec, String ptype, List<List<String>> rules) {
        logRecord(() -> {
            lock.lock();
            try {
                Msg msg = new Msg();
                msg.setMethod(UpdateType.UpdateForAddPolicies);
                msg.setId(localId);
                msg.setSec(sec);
                msg.setPtype(ptype);
                msg.setNewRules(rules);
                publishMessage(msg);
            } finally {
                lock.unlock();
            }
        });
    }

    /**
     * UpdateForRemovePolicies calls the update callback of other instances to synchronize their policies in batch.
     * It is called after Enforcer.RemovePolicies()
     */
    @Override
    public void updateForRemovePolicies(String sec, String ptype, List<List<String>> rules) {
        logRecord(() -> {
            lock.lock();
            try {
                Msg msg = new Msg();
                msg.setMethod(UpdateType.UpdateForRemovePolicies);
                msg.setId(localId);
                msg.setSec(sec);
                msg.setPtype(ptype);
                msg.setNewRules(rules);
                publishMessage(msg);
            } finally {
                lock.unlock();
            }
        });
    }
    private void publishMessage(Msg msg) {
        try {
            String data = msg.toJson();
            pubCommands.publish(channel, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void subscribe() {
        lock.lock();
        try {
            StatefulRedisPubSubConnection<String, String> subConnection = RedisClient.create(options.getOptions()).connectPubSub();
            RedisPubSubAsyncCommands<String, String> subAsyncCommands = subConnection.async();

            subAsyncCommands.subscribe(channel);

            subConnection.addListener(new RedisPubSubAdapter<String, String>() {
                @Override
                public void message(String channel, String message) {
                    Msg msg = Msg.fromJson(message);
                    if (msg == null) {
                        System.out.printf("Failed to parse message: %s\n", message);
                    } else {
                        boolean isSelf = msg.getId().equals(options.getLocalID());
                        if (!options.isIgnoreSelf() || !isSelf) {
                            if (updateCallback instanceof Consumer) {
                                ((Consumer<String>) updateCallback).accept(message);
                            } else if (updateCallback instanceof Runnable) {
                                ((Runnable) updateCallback).run();
                            }
                        }
                    }
                }
            });

        } finally {
            lock.unlock();
        }
    }

    /**
     * Logs the execution of a runnable by running it and catching any exceptions.
     * If an exception occurs, it is printed to the standard error output.
     */
    private void logRecord(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
