package com.dongfeng.redis;

import com.dongfeng.constant.EscapeChar;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

/**
 * @author xuchengdong@qbao.com on 2016/10/20.
 */
public class RedisClient {
    @NotNull
    private final String nameSpace;

    @NotNull
    private final RedisTemplate client;

    public RedisClient(final String nameSpace, final RedisTemplate client) {
        this.nameSpace = nameSpace;
        this.client = client;
    }

    public void set(String key, Object obj, int second, String... businessSpace) {
        client.boundValueOps(getKey(key, businessSpace)).set(obj, second, TimeUnit.SECONDS);
    }

    public Object get(String key, String... businessSpace) {
        return client.boundValueOps(getKey(key, businessSpace)).get();
    }

    public void del(String key, String... businessSpace) {
        client.delete(getKey(key, businessSpace));
    }

    public void setKeySerializer(RedisSerializer<?> serializer) {
        client.setKeySerializer(serializer);
    }

    public void setValueSerializer(RedisSerializer<?> serializer) {
        client.setValueSerializer(serializer);
    }

    private String getKey(String key, String... businessSpace) {
        StringBuilder bisSpace = null;
        if (businessSpace != null && businessSpace.length > 0) {
            bisSpace = new StringBuilder(businessSpace.length * 10);
            for (String space : businessSpace) {
                bisSpace.append(space).append(EscapeChar.COLON);
            }
        }
        return nameSpace + EscapeChar.COLON + (bisSpace != null ? bisSpace.toString() : "") + key;
    }
}
