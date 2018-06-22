package com.orwellg.yggdrasil.bacs.log.hive.topology.config;

import com.orwellg.yggdrasil.commons.storm.topology.config.DSLTopologyConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BacsLogHiveConfigFactory extends DSLTopologyConfigFactory {
    private final static Logger LOG = LogManager.getLogger(BacsLogHiveConfigFactory.class);

    protected static BacsLogHiveConfig bacsLogsConfig;

    public static synchronized void initBacsInboundHiveConfig(String propertiesFile) {

        if (bacsLogsConfig == null) {
            if (propertiesFile != null) {
                LOG.info("Initializing topology with propertiesFile {}", propertiesFile);
                bacsLogsConfig = new BacsLogHiveConfig(propertiesFile);
            } else {
                LOG.info("Initializing topology with propertiesFile DEFAULT_PROPERTIES_FILE");
                bacsLogsConfig = new BacsLogHiveConfig();
            }
            try {
                bacsLogsConfig.start();
            } catch (Exception e) {
                LOG.error("Topology configuration params cannot be started. The system will work with default parameters. Message: {}",  e.getMessage(),  e);
            }
        }
    }

    public static synchronized BacsLogHiveConfig getBacsLogsConfig() {
        initBacsInboundHiveConfig(null);
        return bacsLogsConfig;
    }
}

