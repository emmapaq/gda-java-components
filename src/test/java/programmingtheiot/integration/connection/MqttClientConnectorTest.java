

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
import programmingtheiot.gda.connection.*;

/**
 * This test case class contains very basic integration tests for
 * MqttClientConnector. It should not be considered complete,
 * but serve as a starting point for the student implementing
 * additional functionality within their Programming the IoT
 * environment.
 * 
 * IMPORTANT NOTE: This test expects MqttClientConnector to be
 * configured using the synchronous MqttClient.
 * 
 */
public class MqttClientConnectorTest
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(MqttClientConnectorTest.class.getName());
	
	
	// member var's
	
	// TODO: make sure MqttClientConnector is configured to
	// use the synchronous MqttClient
	private MqttClientConnector mqttClient = null;
	
	
	// test setup methods
	
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
	}
	
	// test methods
	
	/**
	 * Test method for {@link programmingtheiot.gda.connection.MqttClientConnector#connectClient()}.
	 */
	@Test
	public void testConnectAndDisconnect()
	{
	    int delaySec = ConfigUtil.getInstance().getInteger(
	            ConfigConst.MQTT_GATEWAY_SERVICE,
	            ConfigConst.KEEP_ALIVE_KEY,
	            ConfigConst.DEFAULT_KEEP_ALIVE
	    );

	    // FIRST connect should be true
	    assertTrue(this.mqttClient.connectClient());

	    // SECOND connect should be false
	    assertFalse(this.mqttClient.connectClient());

	    // Wait for keep-alive timeout
	    try {
	        Thread.sleep((delaySec * 1000) + 3000);
	    } catch (InterruptedException e) {
	        // ignore
	    }

	    // FIRST disconnect should be true
	    assertTrue(this.mqttClient.disconnectClient());

	    // SECOND disconnect should be false
	    assertFalse(this.mqttClient.disconnectClient());
	}


	
	/**
	 * Test method for {@link programmingtheiot.gda.connection.MqttClientConnector#publishMessage(programmingtheiot.common.ResourceNameEnum, java.lang.String, int)}.
	 */
	@Test
	public void testPublishAndSubscribe()
	{
		int qos = 0;
		int delay = ConfigUtil.getInstance().getInteger(ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.KEEP_ALIVE_KEY, ConfigConst.DEFAULT_KEEP_ALIVE);
		
		assertTrue(this.mqttClient.connectClient());
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
	}
	
	/**
	 * Test method for {@link programmingtheiot.gda.connection.MqttClientConnector#publishMessage(programmingtheiot.common.ResourceNameEnum, java.lang.String, int)}.
	 */
 @Test
	public void testPublishAndSubscribeTwoClients()
	{
		int qos = 0;
		
		IDataMessageListener listener = new MqttPublishDataMessageListener(ResourceNameEnum.GDA_MGMT_STATUS_CMD_RESOURCE, true);
		
		this.mqttClient.setDataMessageListener(listener);
		
		assertTrue(this.mqttClient.connectClient());
		
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
	}
	
	/**
	 * Test method for {@link programmingtheiot.gda.connection.MqttClientConnector#publishMessage(programmingtheiot.common.ResourceNameEnum, java.lang.String, int)}.
	 */
	@Test
	public void testIntegrateWithCdaPublishCdaCmdTopic()
	{
		int qos = 1;
		
		assertTrue(this.mqttClient.connectClient());
		assertTrue(this.mqttClient.publishMessage(ResourceNameEnum.CDA_MGMT_STATUS_CMD_RESOURCE, "TEST: This is the CDA command payload.", qos));
		
		try {
			Thread.sleep(60000);
		} catch (Exception e) {
			// ignore
		}
		
		assertTrue(this.mqttClient.disconnectClient());
	}
	
	/**
	 * Test method for {@link programmingtheiot.gda.connection.MqttClientConnector#publishMessage(programmingtheiot.common.ResourceNameEnum, java.lang.String, int)}.
	 */
	@Test
	public void testIntegrateWithCdaSubscribeCdaMgmtTopic()
	{
		int qos = 1;
		int delay = ConfigUtil.getInstance().getInteger(ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.KEEP_ALIVE_KEY, ConfigConst.DEFAULT_KEEP_ALIVE);
		
		assertTrue(this.mqttClient.connectClient());
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
	}
	
}
