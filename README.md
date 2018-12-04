# yggdrasil--bacs-log-hive
Topology to write bacs logs in Hive

Typical scenario:

Receive log from "subscriber.topic" kafka topic defined in topology.properties.
Insert element in hive.

## Build

mvn package -P deploy

## Configuration:
topology.properties:
- Topic names (for subscriber and error).

## Upload to storm cluster:

Upload generated jar-with-dependencies.

## Deploy to storm cluster:

```sh
storm jar <path-to-jar-with-dependencies> com.orwellg.yggdrasil.bacs.log.hive.topology.BacsLogHiveTopology <zookeeper-hosts> -c nimbus.host=<nimbus-host>;
```

## Kill topology in storm cluster:
```sh
storm kill yggdrasil-bacs-log-hive
```