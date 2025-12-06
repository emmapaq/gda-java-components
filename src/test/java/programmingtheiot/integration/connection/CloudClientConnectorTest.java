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
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.gda.connection.CloudClientConnector;

/**
 * Test class for CloudClientConnector integration tests.
 * 
 * This class tests the MQTT cloud connectivity functionality with Ubidots.
 */
public class CloudClientConnectorTest
{
	private static final Logger _Logger =
		Logger.getLogger(CloudClientConnectorTest.class.getName());
	
	private CloudClientConnector cloudClient = null;
	
	// Setup and teardown
	
	/**
	 * Setup method - runs before each test
	 */
	@Before
	public void setUp() throws Exception
	{
		this.cloudClient = new CloudClientConnector();
	}
	
	/**
	 * Teardown method - runs after each test
	 */
	@After
	public void tearDown() throws Exception
	{
		if (this.cloudClient != null && this.cloudClient.isConnected()) {
			this.cloudClient.disconnectClient();
		}
	}
	
	// Test methods
	
	/**
	 * Test basic connection and disconnection to Ubidots
	 */
	@Test
	public void testConnectAndDisconnect()
	{
		_Logger.info("\n========================================");
		_Logger.info("TEST: Connect and Disconnect");
		_Logger.info("========================================");
		
		// Test connection
		boolean connectResult = this.cloudClient.connectClient();
		assertTrue("Connection should succeed", connectResult);
		assertTrue("Should be connected", this.cloudClient.isConnected());
		_Logger.info("✅ Connection successful");
		
		// Small delay to ensure connection is established
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Test disconnection
		boolean disconnectResult = this.cloudClient.disconnectClient();
		assertTrue("Disconnection should succeed", disconnectResult);
		assertFalse("Should not be connected", this.cloudClient.isConnected());
		_Logger.info("✅ Disconnection successful");
	}
	
	/**
	 * Test publishing temperature sensor data to Ubidots
	 */
	@Test
	public void testPublishTemperatureData()
	{
		_Logger.info("\n========================================");
		_Logger.info("TEST: Publish Temperature Data");
		_Logger.info("========================================");
		
		// Connect first
		assertTrue("Should connect", this.cloudClient.connectClient());
		_Logger.info("Connected to Ubidots");
		
		// Wait for connection to stabilize
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Create test temperature data
		SensorData tempData = new SensorData();
		tempData.setName(ConfigConst.TEMP_SENSOR_NAME);
		tempData.setValue(22.5f);
		
		_Logger.info("Publishing Temperature: " + tempData.getValue() + "°C");
		
		// Publish
		boolean result = this.cloudClient.sendEdgeDataToCloud(
			ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, tempData);
		
		assertTrue("Temperature publish should succeed", result);
		_Logger.info("✅ Temperature data published successfully");
		
		// Wait for message to be delivered
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Cleanup
		this.cloudClient.disconnectClient();
		_Logger.info("Check Ubidots dashboard for 'Temperature Sensor' variable");
	}
	
	/**
	 * Test publishing humidity sensor data to Ubidots
	 */
	@Test
	public void testPublishHumidityData()
	{
		_Logger.info("\n========================================");
		_Logger.info("TEST: Publish Humidity Data");
		_Logger.info("========================================");
		
		// Connect first
		assertTrue("Should connect", this.cloudClient.connectClient());
		_Logger.info("Connected to Ubidots");
		
		// Wait for connection to stabilize
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Create test humidity data
		SensorData humidData = new SensorData();
		humidData.setName(ConfigConst.HUMIDITY_SENSOR_NAME);
		humidData.setValue(65.0f);
		
		_Logger.info("Publishing Humidity: " + humidData.getValue() + "%");
		
		// Publish
		boolean result = this.cloudClient.sendEdgeDataToCloud(
			ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, humidData);
		
		assertTrue("Humidity publish should succeed", result);
		_Logger.info("✅ Humidity data published successfully");
		
		// Wait for message to be delivered
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Cleanup
		this.cloudClient.disconnectClient();
		_Logger.info("Check Ubidots dashboard for 'Humidity Sensor' variable");
	}
	
	/**
	 * Test publishing pressure sensor data to Ubidots
	 */
	@Test
	public void testPublishPressureData()
	{
		_Logger.info("\n========================================");
		_Logger.info("TEST: Publish Pressure Data");
		_Logger.info("========================================");
		
		// Connect first
		assertTrue("Should connect", this.cloudClient.connectClient());
		_Logger.info("Connected to Ubidots");
		
		// Wait for connection to stabilize
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Create test pressure data
		SensorData pressureData = new SensorData();
		pressureData.setName(ConfigConst.PRESSURE_SENSOR_NAME);
		pressureData.setValue(1013.25f);
		
		_Logger.info("Publishing Pressure: " + pressureData.getValue() + " hPa");
		
		// Publish
		boolean result = this.cloudClient.sendEdgeDataToCloud(
			ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, pressureData);
		
		assertTrue("Pressure publish should succeed", result);
		_Logger.info("✅ Pressure data published successfully");
		
		// Wait for message to be delivered
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Cleanup
		this.cloudClient.disconnectClient();
		_Logger.info("Check Ubidots dashboard for 'Pressure Sensor' variable");
	}
	
	/**
	 * Test publishing multiple sensor readings to Ubidots
	 */
	@Test
	public void testPublishMultipleSensorData()
	{
		_Logger.info("\n========================================");
		_Logger.info("TEST: Publish Multiple Sensor Data");
		_Logger.info("========================================");
		
		// Connect first
		assertTrue("Should connect", this.cloudClient.connectClient());
		_Logger.info("Connected to Ubidots");
		
		// Wait for connection to stabilize
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Publish Temperature
		SensorData tempData = new SensorData();
		tempData.setName(ConfigConst.TEMP_SENSOR_NAME);
		tempData.setValue(23.8f);
		_Logger.info("Publishing Temperature: " + tempData.getValue() + "°C");
		boolean tempResult = this.cloudClient.sendEdgeDataToCloud(
			ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, tempData);
		assertTrue("Temperature publish should succeed", tempResult);
		
		try { Thread.sleep(500); } catch (InterruptedException e) {}
		
		// Publish Humidity
		SensorData humidData = new SensorData();
		humidData.setName(ConfigConst.HUMIDITY_SENSOR_NAME);
		humidData.setValue(68.5f);
		_Logger.info("Publishing Humidity: " + humidData.getValue() + "%");
		boolean humidResult = this.cloudClient.sendEdgeDataToCloud(
			ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, humidData);
		assertTrue("Humidity publish should succeed", humidResult);
		
		try { Thread.sleep(500); } catch (InterruptedException e) {}
		
		// Publish Pressure
		SensorData pressureData = new SensorData();
		pressureData.setName(ConfigConst.PRESSURE_SENSOR_NAME);
		pressureData.setValue(1015.5f);
		_Logger.info("Publishing Pressure: " + pressureData.getValue() + " hPa");
		boolean pressureResult = this.cloudClient.sendEdgeDataToCloud(
			ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, pressureData);
		assertTrue("Pressure publish should succeed", pressureResult);
		
		_Logger.info("✅ All sensor data published successfully");
		
		// Wait for all messages to be delivered
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Cleanup
		this.cloudClient.disconnectClient();
		_Logger.info("Check Ubidots dashboard for all sensor variables");
	}
	
	/**
	 * Test publishing system performance data to Ubidots
	 */
	@Test
	public void testPublishSystemPerformanceData()
	{
		_Logger.info("\n========================================");
		_Logger.info("TEST: Publish System Performance Data");
		_Logger.info("========================================");
		
		// Connect first
		assertTrue("Should connect", this.cloudClient.connectClient());
		_Logger.info("Connected to Ubidots");
		
		// Wait for connection to stabilize
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Create test system performance data
		SystemPerformanceData sysPerfData = new SystemPerformanceData();
		sysPerfData.setCpuUtilization(45.2f);
		sysPerfData.setMemoryUtilization(62.8f);
		
		_Logger.info("Publishing CPU Utilization: " + sysPerfData.getCpuUtilization() + "%");
		_Logger.info("Publishing Memory Utilization: " + sysPerfData.getMemoryUtilization() + "%");
		
		// Publish
		boolean result = this.cloudClient.sendEdgeDataToCloud(
			ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, sysPerfData);
		
		assertTrue("System performance publish should succeed", result);
		_Logger.info("✅ System performance data published successfully");
		
		// Wait for messages to be delivered
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Cleanup
		this.cloudClient.disconnectClient();
		_Logger.info("Check Ubidots dashboard for 'cpu-utilization' and 'memory-utilization' variables");
	}
	
	/**
	 * Test publishing continuous stream of data to Ubidots
	 */
	@Test
	public void testPublishContinuousData()
	{
		_Logger.info("\n========================================");
		_Logger.info("TEST: Publish Continuous Data Stream");
		_Logger.info("========================================");
		
		// Connect first
		assertTrue("Should connect", this.cloudClient.connectClient());
		_Logger.info("Connected to Ubidots");
		
		// Wait for connection to stabilize
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Publish 5 temperature readings
		_Logger.info("Publishing 5 temperature readings...");
		for (int i = 1; i <= 5; i++) {
			SensorData tempData = new SensorData();
			tempData.setName(ConfigConst.TEMP_SENSOR_NAME);
			tempData.setValue(20.0f + i);
			
			_Logger.info("Reading " + i + ": " + tempData.getValue() + "°C");
			boolean result = this.cloudClient.sendEdgeDataToCloud(
				ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, tempData);
			assertTrue("Publish " + i + " should succeed", result);
			
			// Wait between readings
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		_Logger.info("✅ All 5 readings published successfully");
		
		// Wait for all messages to be delivered
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Cleanup
		this.cloudClient.disconnectClient();
		_Logger.info("Check Ubidots dashboard for temperature trend graph");
	}
	
	/**
	 * Test subscribe and unsubscribe from Ubidots topics
	 */
	@Test
	public void testSubscribeAndUnsubscribe()
	{
		_Logger.info("\n========================================");
		_Logger.info("TEST: Subscribe and Unsubscribe");
		_Logger.info("========================================");
		
		// Connect first
		assertTrue("Should connect", this.cloudClient.connectClient());
		_Logger.info("Connected to Ubidots");
		
		// Wait for connection to stabilize
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Subscribe
		_Logger.info("Subscribing to cloud events...");
		boolean subResult = this.cloudClient.subscribeToCloudEvents(
			ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE);
		assertTrue("Subscribe should succeed", subResult);
		_Logger.info("✅ Subscribed successfully");
		
		// Wait a moment
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Unsubscribe
		_Logger.info("Unsubscribing from cloud events...");
		boolean unsubResult = this.cloudClient.unsubscribeFromCloudEvents(
			ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE);
		assertTrue("Unsubscribe should succeed", unsubResult);
		_Logger.info("✅ Unsubscribed successfully");
		
		// Cleanup
		this.cloudClient.disconnectClient();
	}
	
	/**
	 * Test setting a data message listener
	 */
	@Test
	public void testSetDataMessageListener()
	{
		_Logger.info("\n========================================");
		_Logger.info("TEST: Set Data Message Listener");
		_Logger.info("========================================");
		
		// Create a simple listener
		IDataMessageListener listener = new IDataMessageListener() {
			@Override
			public boolean handleActuatorCommandRequest(ResourceNameEnum resourceName, ActuatorData data) {
				_Logger.info("Actuator command received: " + data.getName());
				return true;
			}
			
			@Override
			public boolean handleActuatorCommandResponse(ResourceNameEnum resourceName, ActuatorData data) {
				return true;
			}
			
			@Override
			public boolean handleIncomingMessage(ResourceNameEnum resourceName, String msg) {
				_Logger.info("Incoming message: " + msg);
				return true;
			}
			
			@Override
			public boolean handleSensorMessage(ResourceNameEnum resourceName, SensorData data) {
				_Logger.info("Sensor message: " + data.getName() + " = " + data.getValue());
				return true;
			}
			
			@Override
			public boolean handleSystemPerformanceMessage(ResourceNameEnum resourceName, SystemPerformanceData data) {
				_Logger.info("System performance: CPU=" + data.getCpuUtilization() + "%, Memory=" + data.getMemoryUtilization() + "%");
				return true;
			}
			
			@Override
			public boolean setActuatorDataListener(String name, programmingtheiot.common.IActuatorDataListener listener) {
				_Logger.info("Setting actuator data listener for: " + name);
				return true;
			}
		};
		
		// Set listener
		boolean result = this.cloudClient.setDataMessageListener(listener);
		assertTrue("Setting listener should succeed", result);
		_Logger.info("✅ Data message listener set successfully");
	}
}