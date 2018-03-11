# kafka-connect-googleanalytics

## Current TODO's

* Crawl multiple views in one connector
* Allow for setting one field as a key
* Allow for partitioning based on one column
* How should we handle offsets?
* Integrate the real time API as well (currently only using the reporting API)

## Testing

Well, automatically testing a library that requires two hard-to-mock dependencies (Kafka and Google Analytics) is a bit... difficult. If you want to test, follow those steps:

0. If you don't already have, create a Google Analytics service account, give it at least READ access to the Google Analytics View you want to extract data from, and download the JSON file containing the keys/secrets. 
1. in `src/main/resources`, copy the `test-conf.properties.template` file and remove the `.template` extensions, then fill out the required key/values (look at the downloaded JSON for that). 
2. Download the confluent kafka distribution and extract it into a sibling folder of this one (or adjust the path to confluent in the makefile).
3. Run `mvn test` to run some (pretty basic) unit tests. Those tests will make no assumptions about the data in your property - it's more of a smoke test that looks for obvious crashes.
4. Run `make test-run` to start to send some data to kafka. In another terminal, run `make run-consumer` to listen to the topic