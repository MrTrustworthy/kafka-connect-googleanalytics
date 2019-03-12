package org.mrtrustworthy.kafka.connect.googleanalytics.source;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

public class ServiceAccountAnalyticsReportingService extends AnalyticsReportingService {

    // check this https://stackoverflow.com/questions/12837748/analytics-google-api-error-403-user-does-not-have-any-google-analytics-account

    @Override
    protected Credential credential() throws Exception {
        return GoogleCredential
                .fromStream(loadFromClasspath("/service_account_secrets.json"))
                .createScoped(SCOPES);
    }
}
