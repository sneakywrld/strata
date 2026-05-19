package com.protectcord.strata.paper.adapter;

import com.protectcord.strata.nms.NMSAdapter;
import com.protectcord.strata.nms.VersionDetector;

import java.util.Optional;
import java.util.logging.Logger;

public final class NMSAdapterLoader {

    private final Logger logger;
    private NMSAdapter adapter;

    public NMSAdapterLoader(Logger logger) {
        this.logger = logger;
    }

    public NMSAdapter load() {
        if (adapter != null) {
            return adapter;
        }

        adapter = VersionDetector.detect();
        logger.info("Loaded NMS adapter: " + adapter.getClass().getSimpleName()
                + " for " + adapter.version());
        return adapter;
    }

    public Optional<NMSAdapter> getLoaded() {
        return Optional.ofNullable(adapter);
    }
}
