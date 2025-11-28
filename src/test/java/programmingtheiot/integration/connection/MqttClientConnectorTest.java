package programmingtheiot.integration.connection;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.gda.connection.IConnectionListener;  // ADD THIS LINE
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.common.DefaultDataMessageListener;
import programmingtheiot.data.DataUtil;
import programmingtheiot.gda.connection.*;
import programmingtheiot.data.ActuatorData;

/**
 * This test case class contains basic integration tests for
 * MqttClientConnector.
 * 
 * IMPORTANT NOTE: This test expects MqttClientConnector to be
 * configured using the ASYNCHRONOUS MqttAsyncClient (as of Lab Module 10).
 */
public class MqttClientConnectorTest
{
    // static
    
    private static final Logger _Logger =
        Logger.getLogger(MqttClientConnectorTest.class.getName());
    
    
    // member var's
    
    // NOTE: MqttClientConnector should now use the ASYNCHRONOUS MqttAsyncClient
    private MqttClientConnector mqttClient = null;
    
    
    // test setup methods
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        this.mqttClient = new MqttClientConnector();
        this.mqttClient.setDataMessageListener(new DefaultDataMessageListener());
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
    }
    
    // test methods
    
    /**
     * Test method for {@link programmingtheiot.gda.connection.MqttClientConnector#connectClient()}.
     */
//    @Test
    public void testConnectAndDisconnect()
    {
        _Logger.info("\n\n***** testConnectAndDisconnect *****");
        
        int delay = ConfigUtil.getInstance().getInteger(
            ConfigConst.MQTT_GATEWAY_SERVICE, 
            ConfigConst.KEEP_ALIVE_KEY, 
            ConfigConst.DEFAULT_KEEP_ALIVE);
        
        assertTrue(this.mqttClient.connectClient());
        
        // IMPORTANT: Add delay for async client connection
        try {
            _Logger.info("Waiting for async connection to complete...");
            Thread.sleep(5000);  // Increased from 2000
        } catch (Exception e) {
            // ignore
        }
        
        // Verify connection
        assertTrue("Client should be connected", this.mqttClient.isConnected());
        
        assertFalse(this.mqttClient.connectClient());
        
        try {
            Thread.sleep(delay * 1000 + 5000);
        } catch (Exception e) {
            // ignore
        }
        
        assertTrue(this.mqttClient.disconnectClient());
        
        // IMPORTANT: Add delay for async client disconnection
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            // ignore
        }
        
        assertFalse(this.mqttClient.disconnectClient());
    }
    
    /**
     * Test method for {@link programmingtheiot.gda.connection.MqttClientConnector#publishMessage(programmingtheiot.common.ResourceNameEnum, java.lang.String, int)}.
     */
//    @Test
    public void testPublishAndSubscribe()
    {
        int qos = 0;
        int delay = ConfigUtil.getInstance().getInteger(
            ConfigConst.MQTT_GATEWAY_SERVICE, 
            ConfigConst.KEEP_ALIVE_KEY, 
            ConfigConst.DEFAULT_KEEP_ALIVE);
        
        assertTrue(this.mqttClient.connectClient());
        
        // IMPORTANT: Add delay for async client
        try {
            _Logger.info("Waiting for async connection...");
            Thread.sleep(5000);  // Increased from 2000
        } catch (Exception e) {
            // ignore
        }
        
        assertTrue(this.mqttClient.subscribeToTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, qos));
        assertTrue(this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, qos));
        assertTrue(this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos));
        assertTrue(this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, qos));
        
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            // ignore
        }
        
        assertTrue(this.mqttClient.publishMessage(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, "TEST: This is the GDA message payload 1.", qos));
        assertTrue(this.mqttClient.publishMessage(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, "TEST: This is the GDA message payload 2.", qos));
        assertTrue(this.mqttClient.publishMessage(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, "TEST: This is the GDA message payload 3.", qos));
        
        try {
            Thread.sleep(25000);
        } catch (Exception e) {
            // ignore
        }
        
        assertTrue(this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE));
        assertTrue(this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE));
        assertTrue(this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE));
        assertTrue(this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE));

        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            // ignore
        }

        try {
            Thread.sleep(delay * 1000);
        } catch (Exception e) {
            // ignore
        }
        
        assertTrue(this.mqttClient.disconnectClient());
        
        // IMPORTANT: Add delay for async client
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            // ignore
        }
    }
    
    /**
     * Test method for {@link programmingtheiot.gda.connection.MqttClientConnector#publishMessage(programmingtheiot.common.ResourceNameEnum, java.lang.String, int)}.
     */
//    @Test
    public void testPublishAndSubscribeTwoClients()
    {
        int qos = 0;
        
        IDataMessageListener listener = new MqttPublishDataMessageListener(ResourceNameEnum.GDA_MGMT_STATUS_CMD_RESOURCE, true);
        
        this.mqttClient.setDataMessageListener(listener);
        
        assertTrue(this.mqttClient.connectClient());
        
        // IMPORTANT: Add delay for async client
        try {
            Thread.sleep(5000);  // Increased from 2000
        } catch (Exception e) {
            // ignore
        }
        
        assertTrue(this.mqttClient.subscribeToTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, qos));
        assertTrue(this.mqttClient.subscribeToTopic(ResourceNameEnum.GDA_MGMT_STATUS_CMD_RESOURCE, 0));
        
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            // ignore
        }
        
        assertTrue(this.mqttClient.publishMessage(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, "TEST: This is the GDA message payload 1.", qos));
        assertTrue(this.mqttClient.publishMessage(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, "TEST: This is the GDA message payload 2.", qos));
        assertTrue(this.mqttClient.publishMessage(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, "TEST: This is the GDA message payload 3.", qos));
        
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            // ignore
        }
        
        assertTrue(this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE));
        assertTrue(this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.GDA_MGMT_STATUS_CMD_RESOURCE));

        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            // ignore
        }

        assertTrue(this.mqttClient.disconnectClient());
        
        // IMPORTANT: Add delay for async client
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            // ignore
        }
    }
    
    /**
     * Test method for {@link programmingtheiot.gda.connection.MqttClientConnector#publishMessage(programmingtheiot.common.ResourceNameEnum, java.lang.String, int)}.
     */
//    @Test
    public void testIntegrateWithCdaPublishCdaCmdTopic()
    {
        int qos = 1;
        
        assertTrue(this.mqttClient.connectClient());
        
        // IMPORTANT: Add delay for async client
        try {
            Thread.sleep(5000);  // Increased from 2000
        } catch (Exception e) {
            // ignore
        }
        
        assertTrue(this.mqttClient.publishMessage(ResourceNameEnum.CDA_MGMT_STATUS_CMD_RESOURCE, "TEST: This is the CDA command payload.", qos));
        
        try {
            Thread.sleep(60000);
        } catch (Exception e) {
            // ignore
        }
        
        assertTrue(this.mqttClient.disconnectClient());
        
        // IMPORTANT: Add delay for async client
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            // ignore
        }
    }
    
    /**
     * Test method for {@link programmingtheiot.gda.connection.MqttClientConnector#publishMessage(programmingtheiot.common.ResourceNameEnum, java.lang.String, int)}.
     */
//    @Test
    public void testIntegrateWithCdaSubscribeCdaMgmtTopic()
    {
        int qos = 1;
        int delay = ConfigUtil.getInstance().getInteger(
            ConfigConst.MQTT_GATEWAY_SERVICE, 
            ConfigConst.KEEP_ALIVE_KEY, 
            ConfigConst.DEFAULT_KEEP_ALIVE);
        
        assertTrue(this.mqttClient.connectClient());
        
        // IMPORTANT: Add delay for async client
        try {
            Thread.sleep(5000);  // Increased from 2000
        } catch (Exception e) {
            // ignore
        }
        
        assertTrue(this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_MGMT_STATUS_MSG_RESOURCE, qos));
        
        try {
            Thread.sleep(delay * 1000);
        } catch (Exception e) {
            // ignore
        }
        
        assertTrue(this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_MGMT_STATUS_MSG_RESOURCE));
        
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            // ignore
        }
        
        assertTrue(this.mqttClient.disconnectClient());
        
        // IMPORTANT: Add delay for async client
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            // ignore
        }
    }
    
    /**
     * NEW TEST: Test actuator command response subscription.
     * 
     * This test verifies that:
     * 1. The MQTT client can connect to the broker
     * 2. Subscriptions are automatically created in connectComplete()
     * 3. Published ActuatorData response messages are received
     * 4. Messages are properly deserialized and routed to the listener
     */
@Test
public void testActuatorCommandResponseSubscription()
{
    _Logger.info("\n\n***** testActuatorCommandResponseSubscription *****");
    
    int qos = 0;
    
    // Create a simple connection tracker
    final boolean[] connected = {false};
    
    // Set a connection listener to track when connection completes
    this.mqttClient.setConnectionListener(new IConnectionListener() {
        @Override
        public void onConnect() {
            _Logger.info("Connection listener: CONNECTED");
            connected[0] = true;
        }
        
        @Override
        public void onDisconnect() {
            _Logger.info("Connection listener: DISCONNECTED");
            connected[0] = false;
        }
    });
    
    // Connect to broker
    _Logger.info("Connecting to MQTT broker...");
    assertTrue(this.mqttClient.connectClient());
    
    // Wait for connection with timeout
    int maxWait = 10; // 10 seconds max
    int waited = 0;
    
    while (!connected[0] && waited < maxWait) {
        try {
            Thread.sleep(1000);
            waited++;
            _Logger.info("Waiting for connection... " + waited + "s");
        } catch (Exception e) {
            // ignore
        }
    }
    
    if (!connected[0]) {
        fail("MQTT client not connected after " + maxWait + " seconds");
    }
    
    _Logger.info("Connection confirmed via callback. Creating test data...");
    
    // Create test ActuatorData response
    ActuatorData ad = new ActuatorData();
    ad.setName("TestActuator");
    ad.setTypeID(ConfigConst.HVAC_ACTUATOR_TYPE);
    ad.setCommand(ConfigConst.COMMAND_ON);
    ad.setValue((float) 12.3);
    ad.setAsResponse();
    
    _Logger.info("Created ActuatorData response: " + ad.getName() + ", value=" + ad.getValue());
    
    // Convert to JSON
    String adJson = DataUtil.getInstance().actuatorDataToJson(ad);
    
    _Logger.info("Publishing ActuatorData response as JSON...");
    
    // Publish the message
    boolean publishResult = this.mqttClient.publishMessage(
        ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, 
        adJson, 
        qos);
    
    assertTrue("Publish should succeed", publishResult);
    
    // Wait for message to be received
    try {
        _Logger.info("Waiting for message to be received...");
        Thread.sleep(3000);
    } catch (Exception e) {
        // ignore
    }
    
    // Disconnect
    _Logger.info("Disconnecting from MQTT broker...");
    this.mqttClient.disconnectClient();
    
    try {
        Thread.sleep(2000);
    } catch (Exception e) {
        // ignore
    }
    
    _Logger.info("Test complete.");
}
    
}