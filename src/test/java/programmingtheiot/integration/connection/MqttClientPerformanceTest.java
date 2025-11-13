package programmingtheiot.integration.connection;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.gda.connection.MqttClientConnector;

/**
 * MQTT Client Performance Test
 * 
 * Tests MQTT publish performance across different QoS levels (0, 1, 2).
 * 
 * IMPORTANT: Run these tests LOCALLY ONLY! 
 * DO NOT run against public internet servers!
 * 
 * Before running:
 * 1. Start your local MQTT broker (e.g., Mosquitto on localhost:1883)
 * 2. Temporarily disable logging in MqttClientConnector.publishMessage()
 * 3. Temporarily disable logging in MqttClientConnector.deliveryComplete()
 */
public class MqttClientPerformanceTest
{
    // Static
    
    private static final Logger _Logger =
        Logger.getLogger(MqttClientPerformanceTest.class.getName());
    
    // NOTE: We'll use only 10,000 requests for MQTT
    public static final int MAX_TEST_RUNS = 10000;
    
    // Member variables
    
    private MqttClientConnector mqttClient = null;
    
    
    // Test setup methods
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        this.mqttClient = new MqttClientConnector();
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        // Ensure client is disconnected
        if (this.mqttClient != null) {
            this.mqttClient.disconnectClient();
        }
    }
    
    
    // Test methods
    
    /**
     * Test connect and disconnect performance
     * Measures the time to establish and tear down MQTT connection
     */
    @Test
    public void testConnectAndDisconnect()
    {
        _Logger.info("\n\n===== Testing MQTT Connect/Disconnect Performance =====");
        
        long startMillis = System.currentTimeMillis();
        
        assertTrue("Connect should succeed", this.mqttClient.connectClient());
        assertTrue("Disconnect should succeed", this.mqttClient.disconnectClient());
        
        long endMillis = System.currentTimeMillis();
        long elapsedMillis = endMillis - startMillis;
        
        _Logger.info("Connect and Disconnect: " + elapsedMillis + " ms");
        _Logger.info("========================================================\n");
    }
    
    /**
     * Test publish performance with QoS 0 (At most once delivery)
     * - No acknowledgment
     * - Fastest but least reliable
     */
    @Test
    public void testPublishQoS0()
    {
        _Logger.info("\n\n===== Testing MQTT Publish QoS 0 Performance =====");
        _Logger.info("QoS 0: At most once delivery (Fire and forget)");
        
        execTestPublish(MAX_TEST_RUNS, 0);
        
        _Logger.info("===================================================\n");
    }
    
    /**
     * Test publish performance with QoS 1 (At least once delivery)
     * - Acknowledged delivery
     * - Possible duplicates
     * - Moderate speed and reliability
     */
    @Test
    public void testPublishQoS1()
    {
        _Logger.info("\n\n===== Testing MQTT Publish QoS 1 Performance =====");
        _Logger.info("QoS 1: At least once delivery (Acknowledged)");
        
        execTestPublish(MAX_TEST_RUNS, 1);
        
        _Logger.info("===================================================\n");
    }
    
    /**
     * Test publish performance with QoS 2 (Exactly once delivery)
     * - Guaranteed delivery, no duplicates
     * - Slowest but most reliable
     */
    @Test
    public void testPublishQoS2()
    {
        _Logger.info("\n\n===== Testing MQTT Publish QoS 2 Performance =====");
        _Logger.info("QoS 2: Exactly once delivery (Guaranteed)");
        
        execTestPublish(MAX_TEST_RUNS, 2);
        
        _Logger.info("===================================================\n");
    }
    
    
    // Private methods
    
    /**
     * Execute the publish performance test
     * 
     * @param maxTestRuns Number of messages to publish
     * @param qos Quality of Service level (0, 1, or 2)
     */
    private void execTestPublish(int maxTestRuns, int qos)
    {
        assertTrue("Connect should succeed", this.mqttClient.connectClient());
        
        // Create a sample SensorData payload
        SensorData sensorData = new SensorData();
        sensorData.setName("TempSensor");
        sensorData.setValue(22.5f);
        
        String payload = DataUtil.getInstance().sensorDataToJson(sensorData);
        
        _Logger.info("Starting test: Publishing " + maxTestRuns + " messages at QoS " + qos);
        _Logger.info("Payload size: " + payload.length() + " bytes");
        
        long startMillis = System.currentTimeMillis();
        
        // Publish messages in a loop
        for (int sequenceNo = 0; sequenceNo < maxTestRuns; sequenceNo++) {
            this.mqttClient.publishMessage(
                ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, 
                payload, 
                qos);
            
            // Optional: Log progress every 1000 messages
            if ((sequenceNo + 1) % 1000 == 0) {
                _Logger.fine("Published " + (sequenceNo + 1) + " messages...");
            }
        }
        
        long endMillis = System.currentTimeMillis();
        long elapsedMillis = endMillis - startMillis;
        
        assertTrue("Disconnect should succeed", this.mqttClient.disconnectClient());
        
        // Calculate and display performance metrics
        double elapsedSeconds = elapsedMillis / 1000.0;
        double messagesPerSecond = maxTestRuns / elapsedSeconds;
        double avgLatencyMs = (double) elapsedMillis / maxTestRuns;
        
        _Logger.info("========== Performance Results ==========");
        _Logger.info("Publish message - QoS " + qos + " [" + maxTestRuns + "]: " + elapsedMillis + " ms");
        _Logger.info("Total time: " + String.format("%.2f", elapsedSeconds) + " seconds");
        _Logger.info("Throughput: " + String.format("%.2f", messagesPerSecond) + " messages/second");
        _Logger.info("Average latency: " + String.format("%.3f", avgLatencyMs) + " ms per message");
        _Logger.info("=========================================");
    }
}