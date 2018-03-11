package org.mrtrustworthy.kafka.connect.googleanalytics.source;

import com.google.api.services.analyticsreporting.v4.model.Report;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.apache.kafka.connect.source.SourceTaskContext;
import org.mrtrustworthy.kafka.connect.googleanalytics.GASourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class GASourceTask extends SourceTask {
    private static final Logger log = LoggerFactory.getLogger(GASourceConnector.class);

    private GAReportFetcher fetcher;
    private GAConnectorConfig config;
    private ReportParser reportParser;

    // TODO use initialize() to resume https://kafka.apache.org/documentation/#connect_resuming
    private int offset;

    public void setFetcher(GAReportFetcher fetcher) {
        this.fetcher = fetcher;
    }

    public void setConfig(GAConnectorConfig config) {
        this.config = config;
    }

    public void setReportParser(ReportParser reportParser) {
        this.reportParser = reportParser;
    }

    /**
     * This should be the only place where the topic name is assembled
     * TODO: Turn the topic.prefix into a topic.template and format accordingly
     * @return the topic name
     */
    private String buildTopicName(){
        // replacing - with _ for avro subject names - do we actually need to do this or can subject/topic name differ?
        return (this.config.getTopicName() + this.config.getViewId()).replace("-", "_");
    }


    @Override
    public String version() {
        return "1.0.0-rc1";
    }

    @Override
    public void start(Map<String, String> props) {
        this.config = GAConnectorConfig.fromConfigMap(props, GAConnectorConfig.ConfigType.TASK_CONFIG);
        this.fetcher = new GAReportFetcher(this.config);
        this.reportParser = new ReportParser();
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        final ArrayList<SourceRecord> records = new ArrayList<>();
        this.fetcher.maybeInitializeAnalyticsReporting();
        Report report;
        try {
            report = this.fetcher.getReport();
        } catch (IOException e) {
            log.error("Got IOException when polling for new records! Ignoring it, trying to proceed");
            return records;
        }

        List<Struct> structs = this.reportParser.parseReport(report, this.buildTopicName());

        for (Struct struct : structs) {
            records.add(this.buildSourceRecord(struct));
        }

        Thread.sleep(this.config.getPollingFrequency());

        return records;
    }

    public SourceRecord buildSourceRecord(Struct struct){
        Map<String, String> sourcePartition = Collections.singletonMap("propertyId", this.config.getViewId());
        Map<String, String> sourceOffset = Collections.singletonMap("position", Integer.toString(this.offset++));
        return new SourceRecord(
            sourcePartition,
            sourceOffset,
            this.buildTopicName(),
            this.reportParser.getSchema(),
            struct
        );
    }


    @Override
    public synchronized void stop() {

    }
}
