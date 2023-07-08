Redis WatcherEx
---


[![Go](https://github.com/casbin/redis-watcher/actions/workflows/ci.yml/badge.svg)](https://github.com/casbin/redis-watcher/actions/workflows/ci.yml)
[![report](https://goreportcard.com/badge/github.com/casbin/redis-watcher)](https://goreportcard.com/report/github.com/casbin/redis-watcher)
[![Coverage Status](https://coveralls.io/repos/github/casbin/redis-watcher/badge.svg?branch=master)](https://coveralls.io/github/casbin/redis-watcher?branch=master)
[![Go Reference](https://pkg.go.dev/badge/github.com/casbin/redis-watcher/v2.svg)](https://pkg.go.dev/github.com/casbin/redis-watcher/v2)
[![Release](https://img.shields.io/github/v/release/casbin/redis-watcher)](https://github.com/casbin/redis-watcher/releases/latest)

Redis Watcher is a [Redis](http://redis.io) watcher for [Casbin](https://github.com/casbin/casbin).

## Simple Example

```java
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.watcher.RedisWatcher;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // Initialize the Redis connection pool.
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        JedisPool jedisPool = new JedisPool(poolConfig, "localhost", 6379);

        // Initialize the watcher.
        RedisWatcher watcher = new RedisWatcher(jedisPool, "/casbin");

        // Initialize the enforcer.
        Enforcer enforcer = new Enforcer("examples/rbac_model.conf", "examples/rbac_policy.csv");

        // Set the watcher for the enforcer.
        enforcer.setWatcher(watcher);

        // Set callback to local example
        watcher.setUpdateCallback(Main::updateCallback);

        // Update the policy to test the effect.
        // You should see "[casbin rules updated]" in the log.
        enforcer.savePolicy();

        // Only exists in test
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }

    private static void updateCallback(String msg) {
        LOGGER.info(msg);
    }
}

```

## Getting Help

- [jCasbin](https://github.com/casbin/jcasbin)

## License

This project is under Apache 2.0 License. See the [LICENSE](LICENSE) file for the full license text.
