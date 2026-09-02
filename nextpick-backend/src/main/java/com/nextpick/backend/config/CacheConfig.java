package com.nextpick.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class CacheConfig {

    public static final String CATALOG_LISTS = "catalogLists";
    public static final String CATALOG_SEARCH = "catalogSearch";
    public static final String CATALOG_DETAILS = "catalogDetails";
    public static final String CATALOG_CONFIGURATION = "catalogConfiguration";
    public static final String RECOMMENDATIONS = "recommendations";

    @Bean
    CacheManager cacheManager(CacheProperties properties) {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                cache(CATALOG_LISTS, properties.listsTtl(), 500),
                cache(CATALOG_SEARCH, properties.searchTtl(), 500),
                cache(CATALOG_DETAILS, properties.detailsTtl(), 1_000),
                cache(CATALOG_CONFIGURATION, properties.configurationTtl(), 50),
                cache(RECOMMENDATIONS, properties.listsTtl(), 1_000)
        ));
        return manager;
    }

    private CaffeineCache cache(String name, Duration ttl, long maximumSize) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(maximumSize)
                .build());
    }

    @ConfigurationProperties(prefix = "nextpick.cache")
    public record CacheProperties(
            Duration listsTtl,
            Duration searchTtl,
            Duration detailsTtl,
            Duration configurationTtl
    ) {
    }
}
