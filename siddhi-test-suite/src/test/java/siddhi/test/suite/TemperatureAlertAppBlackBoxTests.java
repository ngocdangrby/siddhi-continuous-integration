/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package siddhi.test.suite;

import com.google.common.io.Resources;
import io.siddhi.distribution.test.framework.SiddhiRunnerContainer;
import io.siddhi.distribution.test.framework.util.NatsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Black-box Testsuite for Temp-Alert-App.
 * Description: Used for temperature monitoring and anomaly detection. Consumes events from a Nats topic,
 * filters the event under types 'monitored' and 'internal'.
 * Monitored events are then sent through a pattern and the matched events will be alerted to a Nats topic.
 * The internal events are persisted to a table.
 * Siddhi App: test/resources/TemperatureAlertApp/Temp-Alert-App.siddhi
 * Third-party Jars used:
 * java_nats_streaming_2.1.2 (imported from test/resources/TemperatureAlertApp/jars)
 * jnats_2.3.0 (imported from test/resources/TemperatureAlertApp/jars)
 * mysql-connector-java_5.1.38 (imported from maven-dependency-plugin to /target/TemperatureAlertApp/jars)
 * com.google.protobuf_3.6.1 (imported from maven-dependency-plugin to /target/TemperatureAlertApp/jars)
 */
public class TemperatureAlertAppBlackBoxTests extends AbstractTemperatureAlertAppTests {
    private static final Logger logger = LoggerFactory.getLogger(TemperatureAlertAppBlackBoxTests.class);

    private static final String DATABSE_JDBC_URL = "jdbc:mysql://mysql:3306/";
    private static final String DATABSE_USERNAME = "user";
    private static final String DATABSE_PASSWORD = "siddhi";
    private static final String DATABSE_DRIVER_NAME = "com.mysql.jdbc.Driver";
    private static final String NATS_CLUSTER_ID = "TemperatureCluster";
    private static final String NATS_BOOTSTRAP_URL = "nats://nats-streaming:443";
    private static final String NATS_INPUT_DESTINATION = "Temp-Alert-App_DeviceTempStream";
    private static final String NATS_OUTPUT_DESTINATION = "Temp-Alert-App_AlertStream";

    @BeforeClass
    public void setUpCluster() throws IOException, InterruptedException {
        //points to the directory maven-dependency-plugin imported the jars
        Path jarsFromMaven = Paths.get("target", "TemperatureAlertApp/jars");
        URL appUrl = Resources.getResource("TemperatureAlertApp/apps");
        URL extraJarsUrl = Resources.getResource("TemperatureAlertApp/jars");
        URL configUrl = Resources.getResource("TemperatureAlertApp/config/TemperatureDB_Datasource.yaml");

        natsClient = new NatsClient(NATS_CLUSTER_ID, NATS_BOOTSTRAP_URL);
        natsClient.connect();

        Map<String, String> envMap = new HashMap<>();
        envMap.put("CLUSTER_ID", NATS_CLUSTER_ID);
        envMap.put("INPUT_DESTINATION", NATS_INPUT_DESTINATION);
        envMap.put("OUTPUT_DESTINATION", NATS_OUTPUT_DESTINATION);
        envMap.put("NATS_URL", NATS_BOOTSTRAP_URL);
        envMap.put("DATABASE_URL", DATABSE_JDBC_URL);
        envMap.put("USERNAME", DATABSE_USERNAME);
        envMap.put("PASSWORD", DATABSE_PASSWORD);
        envMap.put("JDBC_DRIVER_NAME", DATABSE_DRIVER_NAME);
        siddhiRunnerContainer = new SiddhiRunnerContainer("siddhiio/siddhi-runner-ubuntu:5.1.0-m2")
                .withSiddhiApps(appUrl.getPath())
                .withJars(extraJarsUrl.getPath())
                .withJars(jarsFromMaven.toString())
                .withConfig(configUrl.getPath())
                .withEnv(envMap)
                .withLogConsumer(new Slf4jLogConsumer(logger));
        siddhiRunnerContainer.start();
        siddhiRunnerContainer.followOutput(siddhiLogConsumer, OutputFrame.OutputType.STDOUT);
        setClusterConfigs(NATS_CLUSTER_ID, NATS_BOOTSTRAP_URL, NATS_INPUT_DESTINATION, NATS_OUTPUT_DESTINATION);
    }

    @AfterClass
    public void shutdownCluster() {
        if (siddhiRunnerContainer != null) {
            siddhiRunnerContainer.stop();
        }
    }
}
