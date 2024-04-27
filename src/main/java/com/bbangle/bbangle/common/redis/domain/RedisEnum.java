package com.bbangle.bbangle.common.redis.domain;

import java.util.Arrays;

public enum RedisEnum {
    MIGRATION,
    STORE,
    BOARD,
    BEST_KEYWORD;

    public static boolean checkRedisNamespace(String namespace) {
        return Arrays.stream(RedisEnum.values())
            .anyMatch(e -> e.name()
                .equals(namespace));
    }
}
