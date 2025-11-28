package programmingtheiot.gda.connection;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;

public class CloudClientConnector implements ICloudClient, MqttCallback
{
    private static final Logger _Logger =
        Logger.getLogger(CloudClientConnector.class.getName());

    private MqttClient mqttClient = null;
    private MqttConnectOptions connOptions = null;

    private String host = null;
    private int port = ConfigConst.DEFAULT_MQTT_PORT;
    private String brokerURI = null;
    private String clientID = null;
    private int qos = ConfigConst.DEFAULT_QOS;

    private boolean isConnected = false;
    private IDataMessageListener dataMsgListener = null;

    // -------------------------------------------------
    // Constructors
    // -------------------------------------------------

    public CloudClientConnector()
    {
        this(ConfigConst.CLOUD_GATEWAY_SERVICE);
    }

    public CloudClientConnector(String configSection)
    {
        super();

        ConfigUtil configUtil = ConfigUtil.getInstance();

        this.host = configUtil.getProperty(
            configSection,
            ConfigConst.HOST_KEY,
            ConfigConst.DEFAULT_HOST);

        this.port = configUtil.getInteger(
            configSection,
            ConfigConst.PORT_KEY,
            ConfigConst.DEFAULT_MQTT_PORT);

        this.clientID = MqttClient.generateClientId();

        this.qos = configUtil.getInteger(
            configSection,
            ConfigConst.DEFAULT_QOS_KEY,
            ConfigConst.DEFAULT_QOS);

        // Build broker URI with protocol
        this.brokerURI = "tcp://" + this.host + ":" + this.port;

        try {
            this.mqttClient = new MqttClient(this.brokerURI, this.clientID);
            this.mqttClient.setCallback(this);

            this.connOptions = new MqttConnectOptions();
            this.connOptions.setCleanSession(true);

            _Logger.info("CloudClientConnector initialized with broker: " + this.brokerURI);

        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Failed to initialize Cloud MQTT client", e);
        }
    }

    // -------------------------------------------------
    // ICloudClient implementation
    // -------------------------------------------------

    @Override
    public boolean connectClient()
    {
        try {
            if (!this.mqttClient.isConnected()) {
                _Logger.info("Connecting to cloud broker: " + this.brokerURI);
                this.mqttClient.connect(this.connOptions);
                this.isConnected = true;
                _Logger.info("Connected to Cloud MQTT Broker.");
            }
            return true;

        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Cloud MQTT connection failed", e);
            return false;
        }
    }

    @Override
    public boolean disconnectClient()
    {
        try {
            if (this.mqttClient.isConnected()) {
                this.mqttClient.disconnect();
                this.isConnected = false;
                _Logger.info("Disconnected from Cloud MQTT Broker.");
            }
            return true;

        } catch (MqttException e) {
            _Logger.log(Level.WARNING, "Cloud MQTT disconnect failed", e);
            return false;
        }
    }

    public boolean isConnected()
    {
        return (this.mqttClient != null && this.mqttClient.isConnected());
    }

    @Override
    public boolean setDataMessageListener(IDataMessageListener listener)
    {
        if (listener != null) {
            this.dataMsgListener = listener;
            return true;
        }
        return false;
    }

    @Override
    public boolean sendEdgeDataToCloud(ResourceNameEnum resource, SensorData data)
    {
        if (data != null) {
            String payload = DataUtil.getInstance().sensorDataToJson(data);
            return publish(resource.getResourceName(), payload);
        }
        return false;
    }

    @Override
    public boolean sendEdgeDataToCloud(ResourceNameEnum resource, SystemPerformanceData data)
    {
        if (data != null) {
            // Convert to CPU metric
            SensorData cpuData = new SensorData();
            cpuData.updateData(data);
            cpuData.setName(ConfigConst.CPU_UTIL_NAME);
            cpuData.setValue(data.getCpuUtilization());
            
            boolean cpuSuccess = sendEdgeDataToCloud(resource, cpuData);
            
            // Convert to Memory metric
            SensorData memData = new SensorData();
            memData.updateData(data);
            memData.setName(ConfigConst.MEM_UTIL_NAME);
            memData.setValue(data.getMemoryUtilization());
            
            boolean memSuccess = sendEdgeDataToCloud(resource, memData);
            
            return (cpuSuccess && memSuccess);
        }
        return false;
    }

    private boolean publish(String topic, String payload)
    {
        if (!this.isConnected || payload == null) {
            _Logger.warning("Cannot publish - not connected or payload is null");
            return false;
        }

        try {
            MqttMessage msg = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            msg.setQos(this.qos);
            this.mqttClient.publish(topic, msg);
            
            _Logger.fine("Published to cloud topic: " + topic);

            return true;

        } catch (Exception e) {
            _Logger.log(Level.WARNING, "Publish to cloud failed for topic: " + topic, e);
            return false;
        }
    }

    @Override
    public boolean subscribeToCloudEvents(ResourceNameEnum resource)
    {
        try {
            if (this.mqttClient.isConnected()) {
                this.mqttClient.subscribe(resource.getResourceName(), this.qos);
                _Logger.info("Subscribed to cloud topic: " + resource.getResourceName());
                return true;
            }

        } catch (Exception e) {
            _Logger.log(Level.WARNING, "Cloud subscription failed", e);
        }

        return false;
    }

    @Override
    public boolean unsubscribeFromCloudEvents(ResourceNameEnum resource)
    {
        try {
            if (this.mqttClient.isConnected()) {
                this.mqttClient.unsubscribe(resource.getResourceName());
                _Logger.info("Unsubscribed from cloud topic: " + resource.getResourceName());
                return true;
            }
        } catch (Exception e) {
            _Logger.log(Level.WARNING, "Cloud unsubscribe failed", e);
        }

        return false;
    }

    // -------------------------------------------------
    // MqttCallback implementation
    // -------------------------------------------------

    @Override
    public void connectionLost(Throwable cause)
    {
        this.isConnected = false;
        _Logger.warning("Cloud MQTT connection lost: " + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception
    {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);

        _Logger.info("Cloud message received on topic: " + topic);

        if (this.dataMsgListener == null) {
            _Logger.warning("No DataMessageListener registered.");
            return;
        }

        // Try to parse as ActuatorData (for cloud commands)
        try {
            ActuatorData actuatorData = DataUtil.getInstance().jsonToActuatorData(payload);
            if (actuatorData != null) {
                this.dataMsgListener.handleActuatorCommandRequest(
                    ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, actuatorData);
            }
        } catch (Exception e) {
            _Logger.fine("Message not ActuatorData, ignoring: " + e.getMessage());
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token)
    {
        _Logger.fine("Cloud delivery complete.");
    }
}