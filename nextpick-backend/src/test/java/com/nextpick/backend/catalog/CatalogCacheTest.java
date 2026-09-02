package com.nextpick.backend.catalog;

import com.nextpick.backend.catalog.mapper.MovieCatalogMapper;
import com.nextpick.backend.catalog.mapper.TvCatalogMapper;
import com.nextpick.backend.catalog.tmdb.TmdbClient;
import com.nextpick.backend.catalog.tmdb.TmdbDtos;
import com.nextpick.backend.config.CacheConfig;
import com.nextpick.backend.config.TmdbProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(CatalogCacheTest.Config.class)
class CatalogCacheTest {
    @Autowired CatalogService service;
    @Autowired TmdbClient client;
    @Autowired CacheManager cacheManager;

    @BeforeEach
    void clean() {
        reset(client);
        cacheManager.getCache(CacheConfig.CATALOG_LISTS).clear();
    }

    @Test
    void equivalentRequestsProduceOneExternalCall() {
        when(client.getPage(eq("/trending/all/week"), anyMap())).thenReturn(
                new TmdbDtos.Page(1, List.of(new TmdbDtos.Media(1, "Film", null, null, null, null,
                        7.0, "2020-01-01", null, List.of(), "movie")), 1, 1));

        service.trending(0);
        service.trending(0);

        verify(client, times(1)).getPage(eq("/trending/all/week"), anyMap());
    }

    @EnableCaching
    @Configuration
    static class Config {
        @Bean TmdbClient client() { return mock(TmdbClient.class); }
        @Bean TmdbProperties properties() { return new TmdbProperties("http://test", "token", "es-ES", "ES",
                "http://images", Duration.ofSeconds(1), Duration.ofSeconds(1), 0); }
        @Bean MovieCatalogMapper movieMapper(TmdbProperties p) { return new MovieCatalogMapper(p); }
        @Bean TvCatalogMapper tvMapper(TmdbProperties p) { return new TvCatalogMapper(p); }
        @Bean CatalogService service(TmdbClient c, MovieCatalogMapper m, TvCatalogMapper t, TmdbProperties p) {
            return new CatalogService(c, m, t, p);
        }
        @Bean CacheManager cacheManager() { return new ConcurrentMapCacheManager(CacheConfig.CATALOG_LISTS); }
    }
}
