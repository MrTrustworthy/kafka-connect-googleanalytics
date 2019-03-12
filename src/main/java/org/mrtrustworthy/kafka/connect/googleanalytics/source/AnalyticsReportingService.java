package org.mrtrustworthy.kafka.connect.googleanalytics.source;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.AnalyticsReportingScopes;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

import static org.glassfish.jersey.server.ServerProperties.APPLICATION_NAME;

public abstract class AnalyticsReportingService {

    protected HttpTransport httpTransport;

    public AnalyticsReporting analyticsReporting() throws Exception {

        httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = credential();

        AnalyticsReporting service = new AnalyticsReporting
                .Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        return service;

    }

    protected InputStream loadFromClasspath(String name) {
        return AnalyticsReportingService.class.getResourceAsStream(name);
    }

    protected abstract Credential credential() throws Exception;

    protected static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    protected static final Collection<String> SCOPES = Collections.singleton(AnalyticsReportingScopes.ANALYTICS_READONLY);

}
