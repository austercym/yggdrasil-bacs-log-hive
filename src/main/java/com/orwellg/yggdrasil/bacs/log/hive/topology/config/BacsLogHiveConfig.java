package com.orwellg.yggdrasil.bacs.log.hive.topology.config;

import com.netflix.config.DynamicPropertyFactory;
import com.orwellg.umbrella.commons.beans.config.zookeeper.ZkConfigurationParams;
import com.orwellg.umbrella.commons.config.HiveConfig;
import com.orwellg.umbrella.commons.utils.config.ZookeeperUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BacsLogHiveConfig extends ZkConfigurationParams {
    private final static Logger LOG = LogManager.getLogger(BacsLogHiveConfig.class);

    public static final String TOPOLOGY_NAME = "topology.name";
    public static final String DD_NAME = "database.name";//todo: this should go to zookeeper
    public static final String TABLE_NAME = "table.name";
    private HiveConfig hiveConfig;


    public static final String DEFAULT_PROPERTIES_FILE = "bacs-log-hive-topology.properties";
    //public static final String DEFAULT_PROPERTIES_FILE = "topology.properties";

    public static final String DEFAULT_SUB_BRANCH      = "/com/orwellg/yggdrasil/hive";
    public static final String ZK_SUB_BRANCH_KEY       = "zookeeper.bacs.config.sub_branch";




    public HiveConfig getHiveConfig() {
        return hiveConfig;
    }
    public String getDatabaseName(){
        return this.propertiesUtils.getStringProperty(DD_NAME);
    }
    public String getTableName(){
        return this.propertiesUtils.getStringProperty(TABLE_NAME);
    }
    public String getTopologyName(){
        return this.propertiesUtils.getStringProperty(TOPOLOGY_NAME);
    }

    public BacsLogHiveConfig() {
        hiveConfig = new HiveConfig(DEFAULT_PROPERTIES_FILE);
        super.setPropertiesFile(DEFAULT_PROPERTIES_FILE);

        super.setApplicationRootConfig(ZK_SUB_BRANCH_KEY, DEFAULT_SUB_BRANCH);
    }

    @Override
    public void start() throws Exception {
        hiveConfig.start();
        super.start();
    }

    @Override
    public void loadParameters() {

        try {
            ZookeeperUtils.getDynamicPropertyFactory();
        } catch (Exception e) {
            LOG.error("Error when try get the dynamic property factory from Zookeeper. Message: {}",  e.getMessage(), e);
        }

    }

    @Override
    public void close() throws Exception{
        hiveConfig.close();
        super.close();
    };
}
