package org.mrtrustworthy.kafka.connect.googleanalytics.source;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GAReportFetcher {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final String APPLICATION_NAME;

    private final AnalyticsReporting service;

    public GAReportFetcher(String applicationName, String authzMode) throws Exception {

        APPLICATION_NAME = applicationName;
        switch (authzMode) {
            case "service_account":
                service = new ServiceAccountAnalyticsReportingService().analyticsReporting();
                break;
            case "installed_application":
                service = new InstalledApplicationAnalyticsReportingService().analyticsReporting();
                break;
            default:
                service = null;
        }
        if (service == null) {
            throw new IllegalArgumentException(authzMode + " is not a supported OAuth Mode.");
        }
    }

    /**
     * Queries the Analytics Reporting API V4.
     *
     * @return GetReportResponse The Analytics Reporting API V4 response.
     * @throws IOException might fail
     */
    protected Report getReport(String viewId, String[] metrics, String[] dimensions) throws IOException {
        // Create the DateRange object.
        DateRange dateRange = new DateRange();
        dateRange.setStartDate("17DaysAgo");
        dateRange.setEndDate("today");

        // Create the ReportRequest object.
        ReportRequest request = new ReportRequest()
            .setViewId(viewId)
            .setDateRanges(Collections.singletonList(dateRange))
            .setMetrics(metrics(metrics))
            .setDimensions(dimensions(dimensions));

        ArrayList<ReportRequest> requests = new ArrayList<ReportRequest>();
        requests.add(request);

        // Create the GetReportsRequest object.
        GetReportsRequest getReport = new GetReportsRequest().setReportRequests(requests);

        // Call the batchGet method.
        GetReportsResponse response = service.reports().batchGet(getReport).execute();

        // Return the response.
        return response.getReports().get(0);
    }

    /**
     * Returns the top 25 organic search keywords and traffic sources by visits. The Core Reporting
     * API is used to retrieve this data.
     *
     * @param analytics the Analytics service object used to access the API.
     * @param viewId   the table ID from which to retrieve data.
     * @return the response from the API.
     * @throws IOException if an API error occured.
     */
    private static GetReportsResponse executeDataQuery(AnalyticsReporting analytics, String viewId) throws IOException {
        return analytics.reports().batchGet(
                new GetReportsRequest().setReportRequests(Arrays.asList(
                        new ReportRequest()
                                .setViewId(viewId)
                                .setDateRanges(Collections.singletonList(
                                        new DateRange().setStartDate("1daysAgo").setEndDate("today")
                                ))
                                .setMetrics(metrics("users"))
                                .setDimensions(dimensions("userType", "sessionCount"))
                ))
        ).execute();
    }

    private static List<Metric> metrics(String... measures) {
        return Arrays.stream(measures)
                .map((m) -> new Metric().setExpression("ga:" + m).setAlias(m))
                .collect(Collectors.toList());
    }

    private static List<Dimension> dimensions(String... dimensions) {
        return Arrays.stream(dimensions)
                .map((m) -> new Dimension().setName("ga:" + m))
                .collect(Collectors.toList());
    }

    /**
     * Parses and prints the Analytics Reporting API V4 response.
     *
     * @param response An Analytics Reporting API V4 response.
     */
    private static void printResponse(GetReportsResponse response) {

        for (Report report : response.getReports()) {
            ColumnHeader header = report.getColumnHeader();
            List<String> dimensionHeaders = header.getDimensions();
            List<MetricHeaderEntry> metricHeaders = header.getMetricHeader().getMetricHeaderEntries();
            List<ReportRow> rows = report.getData().getRows();

            for (ReportRow row : rows) {
                List<String> dimensions = row.getDimensions();
                List<DateRangeValues> metrics = row.getMetrics();

                for (int i = 0; i < dimensionHeaders.size() && i < dimensions.size(); i++) {
                    System.out.println(dimensionHeaders.get(i) + ": " + dimensions.get(i));
                }

                for (int j = 0; j < metrics.size(); j++) {
                    System.out.print("Date Range (" + j + "): ");
                    DateRangeValues values = metrics.get(j);
                    for (int k = 0; k < values.getValues().size() && k < metricHeaders.size(); k++) {
                        System.out.println(metricHeaders.get(k).getName() + ": " + values.getValues().get(k));
                    }
                }
            }
        }
    }

}