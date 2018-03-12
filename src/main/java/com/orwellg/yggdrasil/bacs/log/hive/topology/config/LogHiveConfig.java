package com.orwellg.yggdrasil.bacs.log.hive.topology.config;

import com.netflix.config.DynamicPropertyFactory;
import com.orwellg.umbrella.commons.beans.config.zookeeper.ZkConfigurationParams;
import com.orwellg.umbrella.commons.config.HiveConfig;
import com.orwellg.umbrella.commons.utils.config.ZookeeperUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogHiveConfig extends ZkConfigurationParams {
    private final static Logger LOG = LogManager.getLogger(LogHiveConfig.class);


    public static final String DEFAULT_PROPERTIES_FILE = "topology.properties";

//    public static final String DEFAULT_SUB_BRANCH      = "/yggdrasil/transaction/logs/hive";
//    public static final String ZK_SUB_BRANCH_KEY       = "zookeeper.transaction.logs.config.sub_branch";

    private HiveConfig hiveConfig;


    public HiveConfig getHiveConfig() {
        return hiveConfig;
    }

    public void setHiveConfig(HiveConfig hiveConfig) {
        this.hiveConfig = hiveConfig;
    }

    public LogHiveConfig() {
        hiveConfig = new HiveConfig(DEFAULT_PROPERTIES_FILE);
        super.setPropertiesFile(DEFAULT_PROPERTIES_FILE);

//        super.setApplicationRootConfig(ZK_SUB_BRANCH_KEY, DEFAULT_SUB_BRANCH);
    }

    @Override
    public void start() throws Exception {
        hiveConfig.start();
        super.start();
    }

    @Override
    protected void loadParameters() {

        DynamicPropertyFactory dynamicPropertyFactory = null;
        try {
            dynamicPropertyFactory = ZookeeperUtils.getDynamicPropertyFactory();
        } catch (Exception e) {
            LOG.error("Error when try get the dynamic property factory from Zookeeper. Message: {}",  e.getMessage(), e);
        }

        if (dynamicPropertyFactory != null) {

//            kafkaSpoutHints = ZookeeperUtils.getDynamicPropertyFactory().getIntProperty("yggdrasil.transaction.log.hive.hints.spout", DEFAULT_HINTS_KAFKA_SPOUT);
        }

    }

    @Override
    public void close() throws Exception{
        hiveConfig.close();
        super.close();
    }
}
