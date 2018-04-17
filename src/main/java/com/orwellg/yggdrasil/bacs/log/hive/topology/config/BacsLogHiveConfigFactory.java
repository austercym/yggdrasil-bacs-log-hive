package com.orwellg.yggdrasil.bacs.log.hive.topology.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BacsLogHiveConfigFactory {
    private final static Logger LOG = LogManager.getLogger(BacsLogHiveConfigFactory.class);

    private static BacsLogHiveConfig bacsLogHiveConfig;

    public static synchronized void initBacsLogHiveConfig() {

        if (bacsLogHiveConfig == null) {
            bacsLogHiveConfig = new BacsLogHiveConfig();
            try {
                bacsLogHiveConfig.start();
            } catch (Exception e) {
                LOG.error("The Product DSL configuration params cannot be started. The system will work with the parameters for default. Message: {}",  e.getMessage(),  e);
            }
        }
    }

    public static synchronized BacsLogHiveConfig getBacsLogHiveConfig() {
        initBacsLogHiveConfig();
        return bacsLogHiveConfig;
    }
}

