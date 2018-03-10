package org.mrtrustworthy.kafka.connect.googleanalytics.source;

import org.apache.kafka.common.config.ConfigDef;
import org.mrtrustworthy.kafka.connect.googleanalytics.GASourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;


public class GAConnectorConfig {
    private static final Logger log = LoggerFactory.getLogger(GASourceConnector.class);

    public enum ConfigType {
        CONNECTOR_CONFIG, TASK_CONFIG;
    }

    // general config
    public final static String TOPIC_CONFIG = "topic.prefix";
    public final static String VIEW_CONFIG = "view.id";
    public final static String POLLING_FREQUENCY = "polling.frequency";
    public final static String DIMENSIONS = "fetch.dimensions";
    public final static String MEASURES = "fetch.measures";

    // Google key stuff
    public final static String TYPE = "google.type";
    public final static String PROJECT_ID = "google.project_id";
    public final static String PRIVATE_KEY_ID = "google.private_key_id";
    public final static String PRIVATE_KEY = "google.private_key";
    public final static String CLIENT_EMAIL = "google.client_email";
    public final static String CLIENT_ID = "google.client_id";
    public final static String AUTH_URI = "google.auth_uri";
    public final static String TOKEN_URI = "google.token_uri";
    public final static String AUTH_PROVIDER_X509_CERT_URL = "google.auth_provider_x509_cert_url";
    public final static String CLIENT_X509_CERT_URL = "google.client_x509_cert_url";


    public static final ConfigDef CONFIG_DEF = new ConfigDef()
        // basic stuff
        .define(VIEW_CONFIG, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "View Id of Google Analytics View, typically numeric value")
        .define(POLLING_FREQUENCY, ConfigDef.Type.INT, ConfigDef.Importance.HIGH, "How frequently to poll for new data, in milliseconds")
        .define(DIMENSIONS, ConfigDef.Type.LIST, ConfigDef.Importance.HIGH, "The dimensions to fetch")
        .define(MEASURES, ConfigDef.Type.LIST, ConfigDef.Importance.HIGH, "The measures to fetch")
        .define(TOPIC_CONFIG, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "The topic to publish data to")
        // Google analytics key
        .define(TYPE, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "The google analytics type")
        .define(PROJECT_ID, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "The google analytics project_id")
        .define(PRIVATE_KEY_ID, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "The google analytics private_key_id")
        .define(PRIVATE_KEY, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "The google analytics private_key")
        .define(CLIENT_EMAIL, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "The google analytics client_email")
        .define(CLIENT_ID, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "The google analytics client_id")
        .define(AUTH_URI, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "The google analytics auth_uri")
        .define(TOKEN_URI, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "The google analytics token_uri")
        .define(AUTH_PROVIDER_X509_CERT_URL, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "The google analytics auth_provider_x509_cert_url")
        .define(CLIENT_X509_CERT_URL, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "The google analytics client_x509_cert_url");


    // This is one of {ConnectorConfig, TaskConfig} and allows us to use this class for both use cases
    private ConfigType configType;

    // basic stuff
    private String topicName;
    private String viewId;
    private int pollingFrequency;
    private List<String> dimensions;
    private List<String> measures;

    // google analytics key stuff
    private String type;
    private String project_id;
    private String private_key_id;
    private String private_key;
    private String client_email;
    private String client_id;
    private String auth_uri;
    private String token_uri;
    private String auth_provider_x509_cert_url;
    private String client_x509_cert_url;

    public static GAConnectorConfig fromConfigMap(Map<String, String> map, ConfigType configType) {


        GAConnectorConfig conf = new GAConnectorConfig();
        // basic config
        conf.setConfigType(configType);
        conf.setTopicName(map.get(TOPIC_CONFIG));
        conf.setViewId(map.get(VIEW_CONFIG));
        conf.setDimensions(Arrays.asList(map.get(DIMENSIONS).split("\\s*,\\s*")));
        conf.setMeasures(Arrays.asList(map.get(MEASURES).split("\\s*,\\s*")));
        conf.setPollingFrequency(Integer.parseInt(map.get(POLLING_FREQUENCY)));

        // GA key config
        conf.setType(map.get(TYPE));
        conf.setProject_id(map.get(PROJECT_ID));
        conf.setPrivate_key_id(map.get(PRIVATE_KEY_ID));
        conf.setPrivate_key(map.get(PRIVATE_KEY));
        conf.setClient_email(map.get(CLIENT_EMAIL));
        conf.setClient_id(map.get(CLIENT_ID));
        conf.setAuth_uri(map.get(AUTH_URI));
        conf.setToken_uri(map.get(TOKEN_URI));
        conf.setAuth_provider_x509_cert_url(map.get(AUTH_PROVIDER_X509_CERT_URL));
        conf.setClient_x509_cert_url(map.get(CLIENT_X509_CERT_URL));

        return conf;
    }

    /**
     * @param maxTasks is ignored, we only spawn one
     * @return a list containing only one serialized task config
     */
    public List<Map<String, String>> createTaskConfigurations(int maxTasks) {
        // TODO validate that task_configs only have one property id
        if (this.configType == ConfigType.TASK_CONFIG) {
            throw new IllegalArgumentException("Can't create task configurations from a task configuration");
        }

        // TODO spawn more tasks if multiple property id's are entered
        ArrayList<Map<String, String>> configs = new ArrayList<>();
        // Only one input partition makes sense.
        Map<String, String> config = new HashMap<>();

        // basic stuff
        config.put(VIEW_CONFIG, this.viewId);
        config.put(TOPIC_CONFIG, this.topicName);
        config.put(DIMENSIONS, String.join(",", this.dimensions));
        config.put(MEASURES, String.join(",", this.measures));
        config.put(POLLING_FREQUENCY, Integer.toString(this.pollingFrequency));


        // GA key stuff
        config.put(TYPE, this.type);
        config.put(PROJECT_ID, this.project_id);
        config.put(PRIVATE_KEY_ID, this.private_key_id);
        config.put(PRIVATE_KEY, this.private_key);
        config.put(CLIENT_EMAIL, this.client_email);
        config.put(CLIENT_ID, this.client_id);
        config.put(AUTH_URI, this.auth_uri);
        config.put(TOKEN_URI, this.token_uri);
        config.put(AUTH_PROVIDER_X509_CERT_URL, this.auth_provider_x509_cert_url);
        config.put(CLIENT_X509_CERT_URL, this.client_x509_cert_url);

        configs.add(config);
        return configs;
    }

    /**
     * Google apparently decided that the only acceptable way to parse credentials is to use an InputStream of JSON
     * so, we'll assemble one here...
     *
     * @return an input stream consisting of an UTF-8 encoded json string
     */
    public InputStream getGoogleConfigurationAsInputStream() {
        String template = "\"%s\":\"%s\",\n";
        String jsonString = "{" +
            String.format(template, TYPE.replace("google.", ""), this.type) +
            String.format(template, PROJECT_ID.replace("google.", ""), this.project_id) +
            String.format(template, PRIVATE_KEY_ID.replace("google.", ""), this.private_key_id) +
            String.format(template, PRIVATE_KEY.replace("google.", ""), this.private_key.replace("\n", "\\n")) +
            String.format(template, CLIENT_EMAIL.replace("google.", ""), this.client_email) +
            String.format(template, CLIENT_ID.replace("google.", ""), this.client_id) +
            String.format(template, AUTH_URI.replace("google.", ""), this.auth_uri) +
            String.format(template, TOKEN_URI.replace("google.", ""), this.token_uri) +
            String.format(template, AUTH_PROVIDER_X509_CERT_URL.replace("google.", ""), this.auth_provider_x509_cert_url) +
            String.format("\"%s\":\"%s\"", CLIENT_X509_CERT_URL.replace("google.", ""), this.client_x509_cert_url) +
            "}";

        try {
            return new ByteArrayInputStream(jsonString.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.warn("Could not parse the google analytics configuration as UTF-8");
            return new ByteArrayInputStream(jsonString.getBytes());
        }
    }

    public ConfigType getConfigType() {
        return configType;
    }

    public void setConfigType(ConfigType configType) {
        this.configType = configType;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getViewId() {
        return viewId;
    }

    public String getType() {
        return type;
    }

    public String getProject_id() {
        return project_id;
    }

    public String getPrivate_key_id() {
        return private_key_id;
    }

    public String getPrivate_key() {
        return private_key;
    }

    public String getClient_email() {
        return client_email;
    }

    public String getClient_id() {
        return client_id;
    }

    public String getAuth_uri() {
        return auth_uri;
    }

    public String getToken_uri() {
        return token_uri;
    }

    public String getAuth_provider_x509_cert_url() {
        return auth_provider_x509_cert_url;
    }

    public String getClient_x509_cert_url() {
        return client_x509_cert_url;
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

    public List<String> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<String> dimensions) {
        this.dimensions = dimensions;
    }

    public List<String> getMeasures() {
        return measures;
    }

    public void setMeasures(List<String> measures) {
        this.measures = measures;
    }


    public void setType(String type) {
        this.type = type;
    }


    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }


    public void setPrivate_key_id(String private_key_id) {
        this.private_key_id = private_key_id;
    }


    public void setPrivate_key(String private_key) {
        this.private_key = private_key;
    }


    public void setClient_email(String client_email) {
        this.client_email = client_email;
    }


    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }


    public void setAuth_uri(String auth_uri) {
        this.auth_uri = auth_uri;
    }


    public void setToken_uri(String token_uri) {
        this.token_uri = token_uri;
    }


    public void setAuth_provider_x509_cert_url(String auth_provider_x509_cert_url) {
        this.auth_provider_x509_cert_url = auth_provider_x509_cert_url;
    }


    public void setClient_x509_cert_url(String client_x509_cert_url) {
        this.client_x509_cert_url = client_x509_cert_url;
    }
}
