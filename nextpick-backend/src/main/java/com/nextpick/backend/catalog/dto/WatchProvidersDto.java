package com.nextpick.backend.catalog.dto;

import java.util.List;

public record WatchProvidersDto(
        String region,
        String link,
        List<ProviderDto> streaming,
        List<ProviderDto> free,
        List<ProviderDto> rent,
        List<ProviderDto> buy
) {
    public static WatchProvidersDto empty(String region) {
        return new WatchProvidersDto(region, null, List.of(), List.of(), List.of(), List.of());
    }
}
