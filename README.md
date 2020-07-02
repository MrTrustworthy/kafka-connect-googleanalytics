# kafka-connect-googleanalytics

# This repository is not maintained anymore and probably already outdated. Feel free to fork it and update it to new Kafka/GA/Java versions. Issues and PRs will not be worked on.

## Current TODO's

* Crawl multiple views in one connector
* Allow for setting one field as a key
* Add an option to define all dimension values as key of the message
* Allow for partitioning based on one column
* How should we handle offsets? Maybe just use the current timestamp?
* Integrate the real time API as well (currently only using the reporting API)
* Figure out how avro schemas can use "_" while letting kafka topics use "-" in subject/topic names

## Testing

Well, automatically testing a library that requires two hard-to-mock dependencies (Kafka and Google Analytics) is a bit... difficult. If you want to test, follow those steps:

0. If you don't already have, create a Google Analytics service account, give it at least READ access to the Google Analytics View you want to extract data from, and download the JSON file containing the keys/secrets. 
1. in `src/main/resources`, copy the `test-conf.properties.template` file and remove the `.template` extensions, then fill out the required key/values (look at the downloaded JSON for that). 
2. Download the confluent kafka distribution and extract it into a sibling folder of this one (or adjust the path to confluent in the makefile).
3. Run `mvn test` to run some (pretty basic) unit tests. Those tests will make no assumptions about the data in your property - it's more of a smoke test that looks for obvious crashes and prints out some results so you can manually check if that's what you expected.
4. Start kafka and schema registry by running `make run-kafka`.
5. Run `make test-run` to start to send some data to kafka. In another terminal, run `make run-[avro-]consumer` to listen to the topic
