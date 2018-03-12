package com.orwellg.yggdrasil.bacs.log.hive.topology;

import com.orwellg.umbrella.commons.storm.config.topology.TopologyConfig;
import com.orwellg.umbrella.commons.storm.config.topology.TopologyConfigFactory;
import com.orwellg.umbrella.commons.storm.topology.component.bolt.EventErrorBolt;
import com.orwellg.umbrella.commons.storm.topology.component.spout.KafkaSpout;
import com.orwellg.umbrella.commons.storm.wrapper.kafka.KafkaBoltWrapper;
import com.orwellg.umbrella.commons.storm.wrapper.kafka.KafkaSpoutWrapper;
import com.orwellg.umbrella.commons.utils.config.ZookeeperUtils;
import com.orwellg.yggdrasil.bacs.log.hive.topology.bolts.BacsLogHiveKafkaEventProcessBolt;
import com.orwellg.yggdrasil.bacs.log.hive.topology.config.LogHiveOptions;
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
    private static final String TOPOLOGY_NAME = "yggdrasil-bacs-log-hive";
    private static final String KAFKA_EVENT_READER_COMPONENT = "logReader";
    private static final String PROCESS_COMPONENT = "logProcessEvent";
    private static final String SAVE_TO_HIVE_COMPONENT = "logSaveToHive";
    private static final String ERROR_HANDLING = "logErrorHandling";
    private static final String ERROR_PRODUCER_COMPONENT = "logErrorProducer";

    /**
     * Set up log to hive topology and load it into storm.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        boolean local = false;
        String zookeeperHost = "";

        if (args.length >= 1 && args[0].equals("local")) {
            LOG.info("*********** Local parameter received, will work with LocalCluster ************");
            local = true;
        }

        if (local) {
            LocalCluster cluster = new LocalCluster();
            loadTopologyInStorm(cluster);
            Thread.sleep(6000000);
            cluster.shutdown();
            ZookeeperUtils.close();
        } else {
            if(args.length >= 1) {
                zookeeperHost = args[0];
                LOG.info("*********** Set Zookeeper host by parameter {} ************", zookeeperHost);
                loadTopologyInStorm(zookeeperHost);
            }
            else {
                LOG.info("*********** Set topology with default properties ************");
                loadTopologyInStorm();
            }
        }
    }
    public static void loadTopologyInStorm(LocalCluster cluster) throws Exception {
        loadTopologyInStorm(cluster, null, null);
    }
    public static void loadTopologyInStorm(String zookeeperhost) throws Exception {
        loadTopologyInStorm(null, zookeeperhost, null);
    }
    public static void loadTopologyInStorm() throws Exception {
        loadTopologyInStorm(null, null, null);
    }
    /**
     * Set up log to hive topology and load into storm.<br/>
     * It may take some 2min to execute synchronously, then another some 2min to
     * completely initialize storm asynchronously.<br/>
     * Pre: kafka+zookeeper servers up in addresses as defined in TopologyConfig.
     *
     * @param localCluster
     *            null to submit to remote cluster.
     */
    public static void loadTopologyInStorm(LocalCluster localCluster, String zookeeperHost, String propertyFile) throws Exception {
        LOG.info("Creating {} topology...", TOPOLOGY_NAME);

        // ------------ Configuration ------------
        // Read configuration params from topology.properties and zookeeper
        TopologyConfig config = TopologyConfigFactory.getTopologyConfig(propertyFile, zookeeperHost);
        TopologyBuilder builder = new TopologyBuilder();
        HiveBolt hiveBolt = getHiveBolt();

        // Create the spout that read the events from Kafka
        builder.setSpout(KAFKA_EVENT_READER_COMPONENT, new KafkaSpoutWrapper(config.getKafkaSubscriberSpoutConfig(),
                String.class, String.class).getKafkaSpout(),config.getKafkaSpoutHints());

        // Parse the events and we send it to the rest of the topology
        builder.setBolt(PROCESS_COMPONENT,
                new BacsLogHiveKafkaEventProcessBolt(), config.getEventProcessHints())
                .shuffleGrouping(KAFKA_EVENT_READER_COMPONENT, KafkaSpout.EVENT_SUCCESS_STREAM);

        builder.setBolt(SAVE_TO_HIVE_COMPONENT, hiveBolt, config.getEventErrorHints()
        ).shuffleGrouping(PROCESS_COMPONENT);

        // ------------ Manage Errors ------------
        builder.setBolt(ERROR_HANDLING,
                new EventErrorBolt(),
                config.getEventErrorHints()
        ).shuffleGrouping(KAFKA_EVENT_READER_COMPONENT,  KafkaSpout.EVENT_ERROR_STREAM);

        builder.setBolt(ERROR_PRODUCER_COMPONENT,
                new KafkaBoltWrapper(config.getKafkaPublisherErrorBoltConfig(), String.class, String.class).getKafkaBolt(),
                config.getEventErrorHints()
        ).shuffleGrouping(ERROR_HANDLING);

        // ------------ Build the topology ------------
        StormTopology topology = builder.createTopology();

        LOG.info("{} Topology created, submitting it to storm...", TOPOLOGY_NAME);

        // Create the basic config and upload the topology
        Config conf = new Config();
        conf.setDebug(false);
        conf.setMaxTaskParallelism(config.getTopologyMaxTaskParallelism());
        conf.setNumWorkers(config.getTopologyNumWorkers());

        if (localCluster != null) {
            localCluster.submitTopology(TOPOLOGY_NAME, conf, topology);
            LOG.info("{} Topology submitted to storm (LocalCluster).", TOPOLOGY_NAME);
        } else {
            StormSubmitter.submitTopology(TOPOLOGY_NAME, conf, topology);
            LOG.info("{} Party Topology submitted to storm (StormSubmitter).", TOPOLOGY_NAME);
        }
    }
    public static HiveBolt getHiveBolt() {
        HiveOptions hiveOptions = LogHiveOptions.hiveOptions();
        return new HiveBolt(hiveOptions);
    }
}
