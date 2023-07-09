package org.casbin.watcherEx;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.casbin.jcasbin.model.Model;
import org.casbin.jcasbin.persist.WatcherEx;
import org.casbin.jcasbin.persist.WatcherUpdatable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class RedisWatcherEx implements WatcherEx , WatcherUpdatable {
    private Runnable updateCallback;
    private final JedisPool jedisPool;
    private final String localId;
    private final String redisChannelName;

    public RedisWatcherEx(String redisIp, int redisPort, String redisChannelName, int timeout, String password) {
        this.jedisPool = new JedisPool(new JedisPoolConfig(), redisIp, redisPort, timeout, password);
        this.localId = UUID.randomUUID().toString();
        this.redisChannelName = redisChannelName;
        startSubscription();
    }

    public RedisWatcherEx(JedisPoolConfig config, String redisIp, int redisPort, String redisChannelName, int timeout, String password) {
        this.jedisPool = new JedisPool(config, redisIp, redisPort, timeout, password);
        this.localId = UUID.randomUUID().toString();
        this.redisChannelName = redisChannelName;
        startSubscription();
    }

    public RedisWatcherEx(String redisIp, int redisPort, String redisChannelName) {
        this(redisIp, redisPort, redisChannelName, 2000, (String) null);
    }

    @Override
    public void setUpdateCallback(Runnable runnable) {
        this.updateCallback = runnable;
    }

    @Override
    public void setUpdateCallback(Consumer<String> consumer) {

    }

    @Override
    public void update() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(redisChannelName, "Casbin policy has a new version from redis watcher: " + localId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the subscription to the Redis channel for receiving updates.
     * The subscription runs in a separate thread from a thread pool.
     * When a new message is received on the channel, the updateCallback is executed.
     */
    private void startSubscription() {
        Thread subscriptionThread = new Thread(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                JedisPubSub subscriber = new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        if (updateCallback != null) {
                            updateCallback.run();
                        }
                    }
                };
                while (true) {
                    jedis.subscribe(subscriber, redisChannelName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        subscriptionThread.start();
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

    /**
     * updateForAddPolicy calls the update callback of other instances to synchronize their policy.
     * It is called after a policy is added via Enforcer.addPolicy(), Enforcer.addNamedPolicy(),
     * Enforcer.addGroupingPolicy() and Enforcer.addNamedGroupingPolicy().
     */

    @Override
    public void updateForAddPolicy(String sec, String ptype, String... params) {
        logRecord(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                Msg msg = getMsg(sec, ptype, params);
                msg.setMethod(UpdateType.UpdateForAddPolicy);
                byte[] data = msg.marshalBinary();
                jedis.publish(redisChannelName.getBytes(), data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * updateForRemovePolicy calls the update callback of other instances to synchronize their policy.
     * It is called after a policy is removed by Enforcer.removePolicy(), Enforcer.removeNamedPolicy(),
     * Enforcer.removeGroupingPolicy() and Enforcer.removeNamedGroupingPolicy().
     */
    @Override
    public void updateForRemovePolicy(String sec, String ptype, String... params) {
        logRecord(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                Msg msg = getMsg(sec, ptype, params);
                msg.setMethod(UpdateType.UpdateForRemovePolicy);

                byte[] data = msg.marshalBinary();
                jedis.publish(redisChannelName.getBytes(), data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    private Msg getMsg(String sec, String ptype, String[] params) {
        Msg msg = new Msg();
        msg.setId(localId);
        msg.setSec(sec);
        msg.setPtype(ptype);
        msg.setNewRule(Arrays.asList(params).toArray(new String[params.length]));
        return msg;
    }

    /**
     * updateForRemoveFilteredPolicy calls the update callback of other instances to synchronize their policy.
     * It is called after Enforcer.RemoveFilteredPolicy(), Enforcer.RemoveFilteredNamedPolicy(),
     * Enforcer.RemoveFilteredGroupingPolicy() and Enforcer.RemoveFilteredNamedGroupingPolicy().
     */
    @Override
    public void updateForRemoveFilteredPolicy(String sec, String ptype, int fieldIndex, String... fieldValues) {
        logRecord(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                Msg msg = getMsgFilter(sec, ptype, fieldIndex, fieldValues);
                msg.setMethod(UpdateType.UpdateForRemoveFilteredPolicy);
                String dataStr = getBinaryMsg(msg);
                jedis.publish(redisChannelName, dataStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    private Msg getMsgFilter(String sec, String ptype, int fieldIndex, String[] fieldValues) {
        Msg msg = new Msg();
        msg.setId(localId);
        msg.setSec(sec);
        msg.setPtype(ptype);
        msg.setFieldIndex(fieldIndex);
        msg.setFieldValues(Arrays.asList(fieldValues).toArray(new String[fieldValues.length]));
        return msg;
    }

    /**
     * updateForSavePolicy calls the update callback of other instances to synchronize their policy.
     * It is called after Enforcer.savePolicy()
     */
    @Override
    public void updateForSavePolicy(Model model) {
        logRecord(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                Msg msg = new Msg();
                msg.setMethod(UpdateType.UpdateForSavePolicy);
                msg.setId(localId);

                String dataStr = getBinaryMsg(msg);
                jedis.publish(redisChannelName, dataStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * updateForAddPolicies calls the update callback of other instances to synchronize their policy.
     * It is called after Enforcer.addPolicies(), Enforcer.addNamedPolicies(),
     * Enforcer.addGroupingPolicies() and Enforcer.addNamedGroupingPolicies().
     */
    @Override
    public void updateForAddPolicies(String sec, String ptype, List<List<String>> rules) {
        logRecord(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                Msg msg = getMsgs(sec, ptype, rules);
                msg.setMethod(UpdateType.UpdateForAddPolicies);
                String dataStr = getBinaryMsg(msg);
                jedis.publish(redisChannelName, dataStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * updateForRemovePolicies calls the update callback of other instances to synchronize their policy.
     * It is called after Enforcer.removePolicies(), Enforcer.removeNamedPolicies(),
     * Enforcer.removeGroupingPolicies() and Enforcer.removeNamedGroupingPolicies().
     */
    @Override
    public void updateForRemovePolicies(String sec, String ptype, List<List<String>> rules) {
        logRecord(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                Msg msg = getMsgs(sec, ptype, rules);
                msg.setMethod(UpdateType.UpdateForRemovePolicies);

                String dataStr = getBinaryMsg(msg);
                jedis.publish(redisChannelName, dataStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    private Msg getMsgs(String sec, String ptype, List<List<String>> rules) {
        Msg msg = new Msg();
        msg.setId(localId);
        msg.setSec(sec);
        msg.setPtype(ptype);
        msg.setNewRules(rules);
        return msg;
    }

    /**
     * updateForUpdatePolicy calls the update callback of other instances to synchronize their policy.
     * It is called after Enforcer.UpdatePolicy()
     */
    @Override
    public void updateForUpdatePolicy(List<String> oldRules, List<String> newRules) {
        logRecord(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                Msg msg = new Msg();
                msg.setMethod(UpdateType.UpdateForUpdatePolicy);
                msg.setId(localId);
                msg.setOldRule(oldRules.toArray(new String[oldRules.size()]));
                msg.setNewRule(newRules.toArray(new String[newRules.size()]));

                String dataStr = getBinaryMsg(msg);
                jedis.publish(redisChannelName, dataStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * get serialized msg
     */
    private static String getBinaryMsg(Msg msg) throws JsonProcessingException {
        byte[] data = msg.marshalBinary();
        String dataStr = new String(data, StandardCharsets.UTF_8);
        return dataStr;
    }
}
