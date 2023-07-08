package org.casbin.watcherEx;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.casbin.jcasbin.model.Model;
import org.casbin.jcasbin.persist.WatcherEx;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class RedisWatcherEx implements WatcherEx {
    private Runnable updateCallback;
    private final JedisPool jedisPool;
    private final String localId;
    private final String redisChannelName;
    private SubThread subThread;

    public RedisWatcherEx(String redisIp, int redisPort, String redisChannelName, int timeout, String password) {
        this.jedisPool = new JedisPool(new JedisPoolConfig(), redisIp, redisPort, timeout, password);
        this.localId = UUID.randomUUID().toString();
        this.redisChannelName = redisChannelName;
        startSub();
    }

    public RedisWatcherEx(JedisPoolConfig config, String redisIp, int redisPort, String redisChannelName, int timeout, String password) {
        this.jedisPool = new JedisPool(config, redisIp, redisPort, timeout, password);
        this.localId = UUID.randomUUID().toString();
        this.redisChannelName = redisChannelName;
        startSub();
    }

    public RedisWatcherEx(String redisIp, int redisPort, String redisChannelName) {
        this(redisIp, redisPort, redisChannelName, 2000, (String) null);
    }

    @Override
    public void setUpdateCallback(Runnable runnable) {
        this.updateCallback = runnable;
        subThread.setUpdateCallback(runnable);
    }

    @Override
    public void setUpdateCallback(Consumer<String> consumer) {
        subThread.setUpdateCallback(consumer);
    }

    @Override
    public void update() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(redisChannelName, "Casbin policy has a new version from redis watcher: " + localId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startSub() {
        subThread = new SubThread(jedisPool, redisChannelName, updateCallback);
        subThread.start();
    }

    private void logRecord(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * UpdateForAddPolicy calls the update callback of other instances to synchronize their policy.
     * It is called after Enforcer.AddPolicy()
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
     * pdateForRemovePolicy calls the update callback of other instances to synchronize their policy.
     * It is called after Enforcer.RemovePolicy()
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
     * UpdateForRemoveFilteredPolicy calls the update callback of other instances to synchronize their policy.
     * It is called after Enforcer.RemoveFilteredNamedGroupingPolicy()
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
     * UpdateForSavePolicy calls the update callback of other instances to synchronize their policy.
     * It is called after Enforcer.RemoveFilteredNamedGroupingPolicy()
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
     * UpdateForAddPolicies calls the update callback of other instances to synchronize their policies in batch.
     * It is called after Enforcer.AddPolicies()
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
     * UpdateForRemovePolicies calls the update callback of other instances to synchronize their policies in batch.
     * It is called after Enforcer.RemovePolicies()
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
     * UpdateForUpdatePolicy calls the update callback of other instances to synchronize their policy.
     * It is called after Enforcer.UpdatePolicy()
     */
    public void updateForUpdatePolicy(String sec, String ptype, String[] oldRule, String[] newRule) {
        logRecord(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                Msg msg = new Msg();
                msg.setMethod(UpdateType.UpdateForUpdatePolicy);
                msg.setId(localId);
                msg.setSec(sec);
                msg.setPtype(ptype);
                msg.setOldRule(oldRule);
                msg.setNewRule(newRule);

                String dataStr = getBinaryMsg(msg);
                jedis.publish(redisChannelName, dataStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * UpdateForUpdatePolicies calls the update callback of other instances to synchronize their policy.
     * It is called after Enforcer.UpdatePolicies()
     */
    public void updateForUpdatePolicies(String sec, String ptype, List<List<String>> oldRules, List<List<String>> newRules) {
        logRecord(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                Msg msg = new Msg();
                msg.setMethod(UpdateType.UpdateForUpdatePolicies);
                msg.setId(localId);
                msg.setSec(sec);
                msg.setPtype(ptype);
                msg.setOldRules(oldRules);
                msg.setNewRules(newRules);

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
