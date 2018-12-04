package com.orwellg.yggdrasil.bacs.log.hive.topology;

import com.orwellg.umbrella.commons.storm.config.topology.TopologyConfig;
import com.orwellg.umbrella.commons.storm.topology.component.base.AbstractTopologyMain;
import com.orwellg.yggdrasil.bacs.log.hive.topology.config.BacsLogHiveConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;

public class BacsLogSaveToHive extends AbstractTopologyMain {

    private static final Logger LOG = LogManager.getLogger(BacsLogSaveToHive.class);

    public static void main(String[] args) throws Exception {
        boolean local = false;

        if (args.length >= 1 && args[0].equals("local")) {
            LOG.info("*********** Local parameter received, will work with LocalCluster ************");
            local = true;
        }

        BacsLogHiveTopology topology = new BacsLogHiveTopology();
        if (local) {
            LocalCluster localCluster = new LocalCluster();
            localCluster.submitTopology(topology.name(), config(""), topology.load());
            Thread.sleep(6000000L);
            localCluster.shutdown();
        } else {
            if (args.length >= 1) {
                String zookeeperHost = args[0];
                StormSubmitter.submitTopology(topology.name(), config(zookeeperHost), topology.load());
            } else {
                LOG.error("Not received parameter with zookeeper connection string");
                System.exit(-1);
            }
        }
    }

    private static Config config(String zookeeper) {
        TopologyConfig config = BacsLogHiveConfigFactory.getDSLTopologyConfig(zookeeper);
        return config(config);
    }
}
