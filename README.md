# yggdrasil--bacs-log-hive
Topology to write bacs logs in Hive

Typical scenario:

Receive log from "subscriber.topic" kafka topic defined in topology.properties.
Insert element in hive.

## Build

mvn clean install -DskipTests -P environment (see pom.xml for defined environments), eg:

mvn clean install -DskipTests -P dev
mvn clean install -DskipTests -P production

## Configuration:
topology.properties:
- "zookeeper.host" (set by maven profiles, see "Build").
- Topic names (for subscriber and error).

## Upload to storm cluster in SID environment:

Upload generated jar-with-dependencies.

## Deploy in OVH test storm cluster:

```sh
storm jar <name>-jar-with-dependencies.jar com.orwellg.yggdrasil.bacs.log.hive.topology.BacsLogHiveTopology
```