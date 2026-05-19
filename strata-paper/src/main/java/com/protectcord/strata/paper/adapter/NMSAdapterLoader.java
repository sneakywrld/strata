package com.protectcord.strata.paper.adapter;

import com.protectcord.strata.nms.NMSAdapter;
import com.protectcord.strata.nms.NMSVersion;
import com.protectcord.strata.nms.VersionDetector;

import java.util.Optional;
import java.util.ServiceLoader;
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

        NMSVersion detectedVersion = VersionDetector.detect();
        logger.info("Detected Minecraft version group: " + detectedVersion);

        ServiceLoader<NMSAdapter> loader = ServiceLoader.load(NMSAdapter.class);
        for (NMSAdapter candidate : loader) {
            if (candidate.version() == detectedVersion) {
                adapter = candidate;
                logger.info("Loaded NMS adapter: " + candidate.getClass().getSimpleName() +
                        " for " + detectedVersion);
                return adapter;
            }
        }

        throw new IllegalStateException(
                "No NMS adapter found for version " + detectedVersion +
                ". Ensure the correct strata-nms module is included in the plugin jar.");
    }

    public Optional<NMSAdapter> getLoaded() {
        return Optional.ofNullable(adapter);
    }
}
