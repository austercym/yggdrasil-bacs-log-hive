package com.orwellg.yggdrasil.bacs.log.hive.topology.config;

import com.netflix.config.DynamicPropertyFactory;
import com.orwellg.umbrella.commons.beans.config.zookeeper.ZkConfigurationParams;
import com.orwellg.umbrella.commons.config.HiveConfig;
import com.orwellg.umbrella.commons.utils.config.ZookeeperUtils;
import com.orwellg.yggdrasil.commons.storm.topology.config.DSLTopologyConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BacsLogHiveConfig extends DSLTopologyConfig {

    public static final String TOPOLOGY_NAME = "topology.name";
    public static final String DB_NAME = "database.name";
    public static final String TABLE_NAME = "table.name";

    public BacsLogHiveConfig() {
        this(DEFAULT_PROPERTIES_FILE);
    }

    public BacsLogHiveConfig(String propertiesFile) {
        super(propertiesFile);
    }

    public final static String DEFAULT_PROPERTIES_FILE = "topology.properties";

    public final static Logger LOG = LogManager.getLogger(BacsLogHiveConfig.class);

    public String getTopologyName(){
        return this.propertiesUtils.getStringProperty(TOPOLOGY_NAME);
    }
    public String getDatabaseName(){
        return this.propertiesUtils.getStringProperty(DB_NAME);
    }
    public String getTableName(){
        return this.propertiesUtils.getStringProperty(TABLE_NAME);
    }
}
