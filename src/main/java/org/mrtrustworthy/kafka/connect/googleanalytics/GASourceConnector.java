package org.mrtrustworthy.kafka.connect.googleanalytics;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.mrtrustworthy.kafka.connect.googleanalytics.source.GAConnectorConfig;
import org.mrtrustworthy.kafka.connect.googleanalytics.source.GASourceTask;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GASourceConnector extends SourceConnector{

    private static final Logger logger = LoggerFactory.getLogger(GASourceConnector.class);

    private GAConnectorConfig config;


    @Override
    public String version() {
        return "1.0.0-rc1";
    }

    @Override
    public Class<? extends Task> taskClass() {
        return GASourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int i) {
        logger.info("Creating task configs");
        return this.config.createTaskConfigurations(i);
    }

    @Override
    public void start(Map<String, String> map) {
        logger.info("Starting GASourceConnector");
        this.config = GAConnectorConfig.fromConfigMap(map);
    }

    @Override
    public void stop() {

    }

    @Override
    public ConfigDef config() {
        return GAConnectorConfig.CONFIG_DEF;
    }
}
