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

import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.RedisClusterClient;

public class WatcherOptions {
    private RedisURI options;
    private RedisClusterClient clusterOptions;
    private StatefulRedisConnection<String, String> subClient;
    private StatefulRedisConnection<String, String> pubClient;
    private String channel;
    private boolean ignoreSelf;
    private String localID;
    private Runnable optionalUpdateCallback;

    public RedisURI getOptions() {
        return options;
    }

    public void setOptions(RedisURI options) {
        this.options = options;
    }

    public RedisClusterClient getClusterOptions() {
        return clusterOptions;
    }

    public void setClusterOptions(RedisClusterClient clusterOptions) {
        this.clusterOptions = clusterOptions;
    }

    public StatefulRedisConnection<String, String> getSubClient() {
        return subClient;
    }

    public void setSubClient(StatefulRedisConnection<String, String> subClient) {
        this.subClient = subClient;
    }

    public StatefulRedisConnection<String, String> getPubClient() {
        return pubClient;
    }

    public void setPubClient(StatefulRedisConnection<String, String> pubClient) {
        this.pubClient = pubClient;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public boolean isIgnoreSelf() {
        return ignoreSelf;
    }

    public void setIgnoreSelf(boolean ignoreSelf) {
        this.ignoreSelf = ignoreSelf;
    }

    public String getLocalID() {
        return localID;
    }

    public void setLocalID(String localID) {
        this.localID = localID;
    }

    public Runnable getOptionalUpdateCallback() {
        return optionalUpdateCallback;
    }

    public void setOptionalUpdateCallback(Runnable optionalUpdateCallback) {
        this.optionalUpdateCallback = optionalUpdateCallback;
    }
}
