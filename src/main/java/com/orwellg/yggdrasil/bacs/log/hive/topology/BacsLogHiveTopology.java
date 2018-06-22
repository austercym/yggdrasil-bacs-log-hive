package com.orwellg.yggdrasil.bacs.log.hive.topology;

import com.orwellg.umbrella.commons.storm.config.topology.TopologyConfig;
import com.orwellg.umbrella.commons.storm.config.topology.TopologyConfigFactory;
import com.orwellg.umbrella.commons.storm.topology.component.bolt.EventErrorBolt;
import com.orwellg.umbrella.commons.storm.topology.component.spout.KafkaSpout;
import com.orwellg.umbrella.commons.storm.wrapper.kafka.KafkaBoltWrapper;
import com.orwellg.umbrella.commons.storm.wrapper.kafka.KafkaSpoutWrapper;
import com.orwellg.yggdrasil.bacs.log.hive.topology.bolts.BacsLogHiveKafkaEventProcessBolt;
import com.orwellg.yggdrasil.bacs.log.hive.topology.config.BacsLogHiveConfig;
import com.orwellg.yggdrasil.bacs.log.hive.topology.config.BacsLogHiveConfigFactory;
import com.orwellg.yggdrasil.bacs.log.hive.topology.config.BacsLogHiveOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.hive.bolt.HiveBolt;
import org.apache.storm.hive.common.HiveOptions;
import org.apache.storm.topology.TopologyBuilder;

/**
 * Storm topology to save bacs logs to Hive from kafka topic. Topology
 * summary:
 * <li>BacsLogHiveKafkaEventProcessBolt
 * <li>HiveBolt
 * @author m.kabza
 *
 */
public class BacsLogHiveTopology {
    private final static Logger LOG = LogManager.getLogger(BacsLogHiveTopology.class);
    private static final String KAFKA_EVENT_READER_COMPONENT = "logReader";
    private static final String PROCESS_COMPONENT = "logProcessEvent";
    private static final String SAVE_TO_HIVE_COMPONENT = "logSaveToHive";
    private static final String ERROR_HANDLING = "logErrorHandling";
    private static final String ERROR_PRODUCER_COMPONENT = "logErrorProducer";

    public static void main(String[] args) throws Exception {
        boolean local = false;

        if (args.length >= 1 && args[0].equals("local")) {
            LOG.info("*********** Local parameter received, will work with LocalCluster ************");
            local = true;
        }

        if (local) {
            BacsLogHiveConfigFactory.initBacsInboundHiveConfig(null);
            LocalCluster cluster = new LocalCluster();
            loadTopologyInStorm(cluster);
            Thread.sleep(6000000);
            cluster.shutdown();
            BacsLogHiveConfigFactory.getBacsLogsConfig().close();
        } else {
            loadTopologyInStorm();
        }
    }

    public static void loadTopologyInStorm() throws Exception {
        loadTopologyInStorm(null);
    }

    public static void loadTopologyInStorm(LocalCluster localCluster) throws Exception {
        LOG.info("Creating BACS inbound hive Topology");

        // Read configuration params from topology.properties and zookeeper
        BacsLogHiveConfig bacsConfig = BacsLogHiveConfigFactory.getBacsLogsConfig();
        String topologyName = bacsConfig.getTopologyName();
        LOG.info("*********** topology name: {}  ************", topologyName);
        LOG.info("*********** database name: {}  ************", bacsConfig.getDatabaseName());

        TopologyBuilder builder = new TopologyBuilder();
        HiveBolt hiveBolt = getHiveBolt(bacsConfig.getDatabaseName(),bacsConfig.getTableName());

        // Create the spout that read the events from Kafka
        builder.setSpout(KAFKA_EVENT_READER_COMPONENT, new KafkaSpoutWrapper(bacsConfig.getKafkaSubscriberSpoutConfig(),
                String.class, String.class).getKafkaSpout(),bacsConfig.getKafkaSpoutHints());

        // Parse the events and we send it to the rest of the topology
        builder.setBolt(PROCESS_COMPONENT,
                new BacsLogHiveKafkaEventProcessBolt(), bacsConfig.getEventProcessHints())
                .shuffleGrouping(KAFKA_EVENT_READER_COMPONENT, KafkaSpout.EVENT_SUCCESS_STREAM);

        builder.setBolt(SAVE_TO_HIVE_COMPONENT, hiveBolt, bacsConfig.getEventErrorHints()
        ).shuffleGrouping(PROCESS_COMPONENT);

        // ------------ Manage Errors ------------
        builder.setBolt(ERROR_HANDLING,
                new EventErrorBolt(),
                bacsConfig.getEventErrorHints()
        ).shuffleGrouping(KAFKA_EVENT_READER_COMPONENT,  KafkaSpout.EVENT_ERROR_STREAM);

        builder.setBolt(ERROR_PRODUCER_COMPONENT,
                new KafkaBoltWrapper(bacsConfig.getKafkaPublisherErrorBoltConfig(), String.class, String.class).getKafkaBolt(),
                bacsConfig.getEventErrorHints()
        ).shuffleGrouping(ERROR_HANDLING);

        // ------------ Build the topology ------------
        StormTopology topology = builder.createTopology();
        LOG.info("Bacs Hive Log Topology created");

        // Create the basic config and upload the topology
        Config conf = new Config();
        conf.setDebug(false);
        conf.setNumWorkers(bacsConfig.getTopologyNumWorkers());
        conf.setMaxTaskParallelism(bacsConfig.getTopologyMaxTaskParallelism());

        if (localCluster != null) {
            localCluster.submitTopology(topologyName, conf, topology);
            LOG.info("Bacs Log Hive Topology submitted to storm (LocalCluster).");
        } else {
            StormSubmitter.submitTopology(topologyName, conf, topology);
            LOG.info("Bacs Log Hive submitted to storm (StormSubmitter).");
        }

    }

    public static HiveBolt getHiveBolt(String dbName, String table) {
        HiveOptions hiveOptions = BacsLogHiveOptions.hiveOptions(dbName, table);
        return new HiveBolt(hiveOptions);
    }
}
