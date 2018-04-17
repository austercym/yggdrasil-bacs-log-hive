package com.orwellg.yggdrasil.bacs.log.hive.topology.bolts;

import com.orwellg.umbrella.avro.types.event.Event;
import com.orwellg.umbrella.avro.types.event.EventType;
import com.orwellg.umbrella.commons.storm.topology.component.bolt.KafkaEventProcessBolt;
import com.orwellg.yggdrasil.bacs.log.hive.topology.models.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.storm.tuple.Tuple;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Bolt that starts storm processing of log actions received by topic.
 *
 * @author m.kabza
 *
 */
public class BacsLogHiveKafkaEventProcessBolt extends KafkaEventProcessBolt {

    private static final long serialVersionUID = 1L;

    private final static Logger LOG = LogManager.getLogger(BacsLogHiveKafkaEventProcessBolt.class);

    @Override
    public void declareFieldsDefinition() {
        addFielsDefinition(Arrays.asList("id","instance","loglevel","component","message","errormessage","trace","logtimestamp"));
    }

    @Override
    public void sendNextStep(Tuple tuple, Event event) {

        LOG.info("New log message received");
        String logId = null;
        try {
            String eventKey = event.getEvent().getKey();
            logId = event.getProcessIdentifier().getUuid();

            LOG.info("[logId: {}] Received log message with event key {}", logId, eventKey);

            // Get the JSON message with the data
            EventType eventType = event.getEvent();
            String data = eventType.getData();
            LOG.info("[logId: {}] log message data: {} ", logId, data);

            Log log = gson.fromJson(data, Log.class);

            Map<String, Object> values = new HashMap<>();
            values.put("id", UUID.randomUUID().toString());
            values.put("instance", log.getInstance());
            values.put("loglevel", log.getLogLevel());
            values.put("component", log.getComponent());
            values.put("message", log.getMessage());
            values.put("errormessage", log.getErrorMessage());
            values.put("trace", log.getTrace());
            values.put("logtimestamp", GetDateTime(log.getTimestamp()));

            send(tuple, values);

            LOG.info("[logId: {}] log message sent", logId);
        } catch (Exception e) {
            LOG.error("[logId: {}] Error processing log message. Message: {}", logId, e.getMessage(), e);
        }
    }

    private String GetDateTime(Long time) {
        if (time == null) {
            return null;
        }
        LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.of("UTC"));

        return GetDateTimeString(dt);
    }

    private String GetDateTimeString(LocalDateTime dt) {
        return dt.toLocalDate() + " " + dt.withNano(0).toLocalTime();
    }
}