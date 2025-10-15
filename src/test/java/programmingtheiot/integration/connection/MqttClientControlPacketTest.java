package programmingtheiot.integration.connection;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.*;
import programmingtheiot.gda.connection.*;

/**
 * This test case class contains very basic integration tests for
 * MqttClientControlPacketTest. It should not be considered complete,
 * but serve as a starting point for the student implementing
 * additional functionality within their Programming the IoT
 * environment.
 *
 */
public class MqttClientControlPacketTest
{
    // static
    
    private static final Logger _Logger =
        Logger.getLogger(MqttClientControlPacketTest.class.getName());
    
    
    // member var's
    
    private MqttClientConnector mqttClient = null;
    
    
    // test setup methods
    
    @Before
    public void setUp() throws Exception
    {
        this.mqttClient = new MqttClientConnector();
    }
    
    @After
    public void tearDown() throws Exception
    {
        if (this.mqttClient != null) {
            this.mqttClient.disconnectClient();
        }
    }
    
    // test methods
    
    @Test
    public void testConnectAndDisconnect()
    {
        // CONNECT and CONNACK packets
        assertTrue("MQTT client should connect successfully", 
                   this.mqttClient.connectClient());
        
        // DISCONNECT packet
        assertTrue("MQTT client should disconnect successfully", 
                   this.mqttClient.disconnectClient());
    }
    
    @Test
    public void testServerPing()
    {
        // Connect first
        assertTrue("MQTT client should connect successfully", 
                   this.mqttClient.connectClient());
        
        // Wait for keep-alive period to see PINGREQ and PINGRESP
        // Note: You may need to adjust timing based on your keep-alive settings
        try {
            Thread.sleep(70000); // Wait 70 seconds (longer than typical keep-alive)
        } catch (InterruptedException e) {
            // Ignore
        }
        
        // Disconnect
        assertTrue("MQTT client should disconnect successfully", 
                   this.mqttClient.disconnectClient());
    }
    
    @Test
    public void testPubSub()
    {
        // Connect first
        assertTrue("MQTT client should connect successfully", 
                   this.mqttClient.connectClient());
        
        // SUBSCRIBE and SUBACK packets
        assertTrue("Should subscribe to topic with QoS 1", 
                   this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, 1));
        
        assertTrue("Should subscribe to topic with QoS 2", 
                   this.mqttClient.subscribeToTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, 2));
        
        // PUBLISH and PUBACK packets (QoS 1)
        String testMessageQos1 = "Test message with QoS 1";
        assertTrue("Should publish message with QoS 1", 
                   this.mqttClient.publishMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, testMessageQos1, 1));
        
        // PUBLISH, PUBREC, PUBREL, and PUBCOMP packets (QoS 2)
        String testMessageQos2 = "Test message with QoS 2";
        assertTrue("Should publish message with QoS 2", 
                   this.mqttClient.publishMessage(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, testMessageQos2, 2));
        
        // UNSUBSCRIBE and UNSUBACK packets
        assertTrue("Should unsubscribe from topic", 
                   this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE));
        
        assertTrue("Should unsubscribe from topic", 
                   this.mqttClient.unsubscribeFromTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE));
        
        // Disconnect
        assertTrue("MQTT client should disconnect successfully", 
                   this.mqttClient.disconnectClient());
    }
}