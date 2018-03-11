
CONFLUENT_PATH := ../confluent-4.0.0

run-kafka:
	${CONFLUENT_PATH}/bin/confluent start

TOPIC = "ga_test.*"

run-consumer:
	${CONFLUENT_PATH}/bin/kafka-console-consumer --bootstrap-server localhost:9092 --whitelist ${TOPIC} --from-beginning

run-avro-consumer:
	${CONFLUENT_PATH}/bin/kafka-avro-console-consumer --bootstrap-server localhost:9092 --whitelist ${TOPIC} --from-beginning

run-producer:
	${CONFLUENT_PATH}/bin/kafka-console-producer --broker-list localhost:9092 --topic blub

list-topics:
	${CONFLUENT_PATH}/bin/kafka-topics --list --zookeeper localhost:2181

basejar:
	mvn clean package -DskipTests

testjar:
	mkdir testjar

testjar/kafka-connect-ga-1.0.0-rc1.jar: basejar testjar
	cp target/kafka-connect-ga-1.0.0-rc1.jar testjar/kafka-connect-ga-1.0.0-rc1.jar

test-run: testjar/kafka-connect-ga-1.0.0-rc1.jar
	${CONFLUENT_PATH}/bin/connect-standalone src/main/resources/connect-standalone.properties src/main/resources/test-conf.properties
