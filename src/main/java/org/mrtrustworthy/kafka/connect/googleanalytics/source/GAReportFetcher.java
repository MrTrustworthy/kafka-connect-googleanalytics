package org.mrtrustworthy.kafka.connect.googleanalytics.source;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.api.services.analyticsreporting.v4.AnalyticsReportingScopes;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;

import com.google.api.services.analyticsreporting.v4.model.DateRange;
import com.google.api.services.analyticsreporting.v4.model.GetReportsRequest;
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;
import com.google.api.services.analyticsreporting.v4.model.Metric;
import com.google.api.services.analyticsreporting.v4.model.Dimension;
import com.google.api.services.analyticsreporting.v4.model.Report;
import com.google.api.services.analyticsreporting.v4.model.ReportRequest;
import kafka.common.KafkaException;

public class GAReportFetcher {
    private static final String APPLICATION_NAME = "org.mrtrustworthy.kafka.connect.googleanalytics.GAReportFetcher";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private GAConnectorConfig conf;

    private AnalyticsReporting service;


    public GAReportFetcher(GAConnectorConfig conf) {
        // TODO how can we update this on-demand?
        this.conf = conf;
    }

    /**
     * Initializes an Analytics Reporting API V4 service object.
     *
     * @throws InterruptedException might fail
     */
    public void maybeInitializeAnalyticsReporting() {
        if (this.service != null) return;
        try {
            this.service = this.getAnalyticsService();
        } catch (IOException | GeneralSecurityException e) {
            throw new KafkaException("Error starting task, could not initialize AnalyticsReporting: " + e.toString());
        }
    }


    /**
     * Initializes an Analytics Reporting API V4 service object.
     *
     * @throws IOException              might fail
     * @throws GeneralSecurityException might fail
     */
    protected AnalyticsReporting getAnalyticsService() throws GeneralSecurityException, IOException {

        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential credential = GoogleCredential
            .fromStream(this.conf.getGoogleConfigurationAsInputStream())
            .createScoped(AnalyticsReportingScopes.all());

        // Construct the Analytics Reporting service object.
        AnalyticsReporting service = new AnalyticsReporting
            .Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build();

        return service;
    }

    /**
     * Queries the Analytics Reporting API V4.
     *
     * @return GetReportResponse The Analytics Reporting API V4 response.
     * @throws IOException might fail
     */
    protected Report getReport() throws IOException {
        // Create the DateRange object.
        DateRange dateRange = new DateRange();
        dateRange.setStartDate("17DaysAgo");
        dateRange.setEndDate("today");

        // Create the ReportRequest object.
        ReportRequest request = new ReportRequest()
            .setViewId(this.conf.getViewId())
            .setDateRanges(Collections.singletonList(dateRange))
            .setMetrics(this.getMetricsFromConfig())
            .setDimensions(this.getDimensionsFromConfig());

        ArrayList<ReportRequest> requests = new ArrayList<ReportRequest>();
        requests.add(request);

        // Create the GetReportsRequest object.
        GetReportsRequest getReport = new GetReportsRequest().setReportRequests(requests);

        // Call the batchGet method.
        GetReportsResponse response = service.reports().batchGet(getReport).execute();

        // Return the response.
        return response.getReports().get(0);
    }

    private List<Metric> getMetricsFromConfig() {
        return this.conf.getMeasures().stream()
            .map((m) -> new Metric().setExpression("ga:" + m).setAlias(m))
            .collect(Collectors.toList());
    }

    private List<Dimension> getDimensionsFromConfig() {
        return this.conf.getDimensions().stream()
            .map((m) -> new Dimension().setName("ga:" + m))
            .collect(Collectors.toList());
    }


}