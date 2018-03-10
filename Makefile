
run-kafka:
	../confluent-4.0.0/bin/confluent start

TOPIC = ".*"

run-consumer:
	../confluent-4.0.0/bin/kafka-console-consumer --bootstrap-server localhost:9092 --whitelist ${TOPIC} --from-beginning


run-producer:
	../confluent-4.0.0/bin/kafka-console-producer --broker-list localhost:9092 --topic blub

list-topics:
	../confluent-4.0.0/bin/kafka-topics --list --zookeeper localhost:2181

basejar:
	mvn clean package -DskipTests

testjar:
	mkdir testjar

testjar/kafka-connect-ga-1.0.0-rc1.jar: basejar testjar
	cp target/kafka-connect-ga-1.0.0-rc1.jar testjar/kafka-connect-ga-1.0.0-rc1.jar

test-run: testjar/kafka-connect-ga-1.0.0-rc1.jar
	../confluent-4.0.0/bin/connect-standalone src/main/resources/connect-standalone.properties src/main/resources/test-conf.properties
