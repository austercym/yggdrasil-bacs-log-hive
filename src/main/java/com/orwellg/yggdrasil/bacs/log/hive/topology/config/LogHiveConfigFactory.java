package com.orwellg.yggdrasil.bacs.log.hive.topology.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogHiveConfigFactory {
    private final static Logger LOG = LogManager.getLogger(LogHiveConfigFactory.class);

    private static LogHiveConfig logHiveConfig;

    public static synchronized void initLogHiveConfig() {

        if (logHiveConfig == null) {
            logHiveConfig = new LogHiveConfig();
            try {
                logHiveConfig.start();
            } catch (Exception e) {
                LOG.error("The Product DSL configuration params cannot be started. The system will work with the parameters for default. Message: {}",  e.getMessage(),  e);
            }
        }
    }

    public static synchronized LogHiveConfig getLogHiveConfig() {
        initLogHiveConfig();
        return logHiveConfig;
    }
}
