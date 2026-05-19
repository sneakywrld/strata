package com.protectcord.strata.paper.api;

import com.protectcord.strata.api.core.StrataAPI;
import com.protectcord.strata.api.core.StrataProvider;

public final class StrataProviderBootstrap {

    private StrataProviderBootstrap() {}

    public static void initialize(StrataAPI api) {
        StrataProvider.set(api);
    }

    public static void shutdown() {
        StrataProvider.set(null);
    }
}
