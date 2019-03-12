package org.mrtrustworthy.kafka.connect.googleanalytics.source;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.model.Report;
import org.apache.kafka.connect.data.Struct;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class GAReportFetcherTest {

    GAConnectorConfig getSampleConfig() {
        Properties prop = new Properties();
        ClassLoader loader = GAConnectorConfig.class.getClassLoader();
        InputStream stream = loader.getResourceAsStream("test-conf.properties");
        try {
            prop.load(stream);
        } catch (IOException e) {
            assertTrue(false, "This should not throw - is the file there?");
        }
        Map<String, String> map = prop.entrySet().stream().collect(
                Collectors.toMap(
                        es -> es.getKey().toString(),
                        es -> es.getValue().toString()
                )
        );

        GAConnectorConfig conf = GAConnectorConfig.fromConfigMap(map);
        return conf;
    }

    @Test
    void testGAReportFetcherInitialization() throws Exception {
        GAConnectorConfig conf = getSampleConfig();
        GAReportFetcher gafetcher = new GAReportFetcher(conf.getApplicationName(), conf.getAuthzMode());
        assertNotNull(gafetcher);
    }


    @Test
    void testStructAssembly() throws Exception {
        GAConnectorConfig conf = getSampleConfig();
        GAReportFetcher gafetcher = new GAReportFetcher(conf.getApplicationName(), conf.getAuthzMode());
        GASourceTask task = new GASourceTask();
        ReportParser repParser = new ReportParser();
        task.setConfig(conf);
        task.setFetcher(gafetcher);
        task.setReportParser(repParser);

        Report report = gafetcher.getReport(conf.getViewId(), conf.getMeasures(), conf.getDimensions());
        assertNotNull(report);
        System.out.println("Report: " + report);

        repParser.maybeUpdateSchema(report, conf.getTopicPrefix());
        System.out.println("Schema: " + repParser.getSchema());

        List<Struct> structs = repParser.createStructsOffReport(report);
        System.out.println("Structs: " + structs);

    }
}
