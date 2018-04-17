package com.orwellg.yggdrasil.bacs.log.hive.topology.config;

import com.orwellg.umbrella.commons.storm.config.topology.TopologyConfig;
import com.orwellg.umbrella.commons.storm.config.topology.TopologyConfigFactory;
import org.apache.storm.hive.bolt.mapper.DelimitedRecordHiveMapper;
import org.apache.storm.hive.common.HiveOptions;
import org.apache.storm.tuple.Fields;

public class BacsLogHiveOptions {
    //private static String bacsDatabase = "bacs_qa";
    //private static String bacsTransactionTable = "credittransactioninformation";

    public static HiveOptions hiveOptions(String bacsDatabase, String bacsLogTable) {
        TopologyConfig config = TopologyConfigFactory.getTopologyConfig();

        // Hive connection configuration
        String metaStoreURI = config.getHiveConfig().getHiveParams().getHost()+":"+config.getHiveConfig().getHiveParams().getPort();

        String dbName = bacsDatabase;
        String tblName = bacsLogTable;
        // Fields for possible column data
        String[] colNames = {"id","instance","loglevel","component","message","errormessage","trace","logtimestamp"};

        // Record Writer configuration
        DelimitedRecordHiveMapper mapper = new DelimitedRecordHiveMapper()
                .withColumnFields(new Fields(colNames));

        HiveOptions hiveOptions = new HiveOptions(metaStoreURI, dbName, tblName, mapper)
                .withTxnsPerBatch(config.getHiveConfig().getHiveParams().getTxnsPerBatch())
                .withBatchSize(config.getHiveConfig().getHiveParams().getBatchSize())
                .withIdleTimeout(config.getHiveConfig().getHiveParams().getIdleTimeout())
                .withMaxOpenConnections(config.getHiveConfig().getHiveParams().getMaxOpenConnections())
                .withCallTimeout(config.getHiveConfig().getHiveParams().getCallTimeout())
                .withKerberosPrincipal("storm-frigg_cluster@ORWELLG.LOCAL")
                .withKerberosKeytab("/etc/security/keytabs/storm.headless.keytab");
        return hiveOptions;
    }
}