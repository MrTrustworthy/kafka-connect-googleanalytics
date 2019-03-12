package org.mrtrustworthy.kafka.connect.googleanalytics.source;

import org.apache.http.auth.AUTH;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.mrtrustworthy.kafka.connect.googleanalytics.GASourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.apache.kafka.common.config.ConfigDef.*;


public class GAConnectorConfig {
    private static final Logger log = LoggerFactory.getLogger(GASourceConnector.class);

    public final static String APPLICATION_NAME = "connector.name";
    //public final static String CONNECTOR_CLASS = "connector.class";
    //public final static String TASK_MAX = "connector.task.max";
    public final static String TOPIC_PREFIX = "connector.topic.prefix";
    public final static String POLLING_FREQUENCY = "connector.polling.frequency";
    public final static String VIEW_ID = "source.ga.view.id";
    public final static String DIMENSIONS = "source.ga.view.fetch.dimensions";
    public final static String MEASURES = "source.ga.view.fetch.measures";
    public final static String AUTHZ_MODE = "source.ga.authorization.mode";


    public static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(APPLICATION_NAME, Type.STRING, Importance.LOW, "When fetching data from Google Analytics, this is the application name to be sent in the User-Agent header of each request or {@code null} for none.")
            .define(TOPIC_PREFIX, Type.STRING, Importance.HIGH, "The topic to publish data to.")
            .define(POLLING_FREQUENCY, Type.INT, Importance.HIGH, "How frequently to poll for new data, in milliseconds.")
            .define(VIEW_ID, Type.STRING, Importance.HIGH, "View Id of Google Analytics View, typically numeric value.")
            .define(DIMENSIONS, Type.LIST, Importance.HIGH, "The dimensions to fetch from Google Analytics.")
            .define(MEASURES, Type.LIST, Importance.HIGH, "The measures to fetch from Google Analytics.")
            .define(AUTHZ_MODE, Type.STRING, Importance.HIGH, "The authorization mode to be used when calling Google Analytics. Supported values are 'service_account' or 'installed_application.")
            ;

    private String applicationName;
    private String topicPrefix;
    private int pollingFrequency;
    private String viewId;
    private String[] dimensions;
    private String[] measures;
    private String authzMode;

    public static GAConnectorConfig fromConfigMap(Map<String, String> map) {

        GAConnectorConfig conf = new GAConnectorConfig();
        conf.setApplicationName(map.get(APPLICATION_NAME));
        conf.setTopicPrefix(map.get(TOPIC_PREFIX));
        conf.setViewId(map.get(VIEW_ID));
        conf.setDimensions(map.get(DIMENSIONS).split("\\s*,\\s*"));
        conf.setMeasures(map.get(MEASURES).split("\\s*,\\s*"));
        conf.setPollingFrequency(Integer.parseInt(map.get(POLLING_FREQUENCY)));
        conf.setAuthzMode(map.get(AUTHZ_MODE));

        return conf;
    }

    /**
     * @param maxTasks is ignored, we only spawn one
     * @return a list containing only one serialized task config
     */
    public List<Map<String, String>> createTaskConfigurations(int maxTasks) {

        // TODO spawn more tasks if multiple property id's are entered
        ArrayList<Map<String, String>> configs = new ArrayList<>();
        // Only one input partition makes sense.
        Map<String, String> config = new HashMap<>();

        config.put(APPLICATION_NAME, this.applicationName);
        config.put(VIEW_ID, this.viewId);
        config.put(TOPIC_PREFIX, this.topicPrefix);
        config.put(DIMENSIONS, String.join(",", this.dimensions));
        config.put(MEASURES, String.join(",", this.measures));
        config.put(POLLING_FREQUENCY, Integer.toString(this.pollingFrequency));
        config.put(AUTHZ_MODE, this.authzMode);

        configs.add(config);
        return configs;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getTopicPrefix() {
        return topicPrefix;
    }

    public void setTopicPrefix(String topicPrefix) {
        this.topicPrefix = topicPrefix;
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public int getPollingFrequency() {
        return pollingFrequency;
    }

    public void setPollingFrequency(int pollingFrequency) {
        this.pollingFrequency = pollingFrequency;
    }

    public String[] getDimensions() {
        return dimensions;
    }

    public void setDimensions(String[] dimensions) {
        this.dimensions = dimensions;
    }

    public String[] getMeasures() {
        return measures;
    }

    public void setMeasures(String[] measures) {
        this.measures = measures;
    }

    public String getAuthzMode() {
        return authzMode;
    }

    public void setAuthzMode(String authzMode) {
        this.authzMode = authzMode;
    }
}
