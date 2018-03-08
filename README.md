# yggdrasil-log-hive
Topology to write logs in Hive

## Build

mvn clean install -DskipTests -P environment (see pom.xml for defined environments), eg:

mvn clean install -DskipTests -P development
mvn clean install -DskipTests -P integration

## Run (topology)

Run local cluster or deploy with "storm jar ...".
