package programmingtheiot.gda.connection;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocketFactory;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.gda.connection.IConnectionListener;

public class MqttClientConnector implements IPubSubClient, MqttCallbackExtended
{
    private static final Logger _Logger =
        Logger.getLogger(MqttClientConnector.class.getName());

    private String host = ConfigConst.DEFAULT_HOST;
    private int port = ConfigConst.DEFAULT_MQTT_PORT;
    private int brokerKeepAlive = ConfigConst.DEFAULT_KEEP_ALIVE;
    private String clientID = null;
    private String brokerAddr = null;

    private MqttAsyncClient mqttClient = null;
    private MqttConnectOptions connOpts = null;
    private MemoryPersistence persistence = null;

    private IDataMessageListener dataMsgListener = null;
    private IConnectionListener connListener = null;

    private boolean useCloudGatewayConfig = false;

    // -----------------------------------------------------------------
    // ---------------------- CONSTRUCTORS ------------------------------
    // -----------------------------------------------------------------

    public MqttClientConnector()
    {
        this(false);
    }

    public MqttClientConnector(boolean useCloudGatewayConfig)
    {
        this(useCloudGatewayConfig ?
            ConfigConst.CLOUD_GATEWAY_SERVICE : null);
    }

    public MqttClientConnector(String cloudGatewayConfigSectionName)
    {
        super();

        if (cloudGatewayConfigSectionName != null &&
            cloudGatewayConfigSectionName.trim().length() > 0) {

            this.useCloudGatewayConfig = true;
            initClientParameters(cloudGatewayConfigSectionName);

        } else {
            this.useCloudGatewayConfig = false;
            initClientParameters(ConfigConst.MQTT_GATEWAY_SERVICE);
        }

        _Logger.info("MQTT client created: " + this.brokerAddr);
    }

    // -----------------------------------------------------------------
    // ---------------------- CONNECTION MGMT ---------------------------
    // -----------------------------------------------------------------

    @Override
    public boolean connectClient()
    {
        try {
            if (this.mqttClient == null) {
                this.mqttClient = new MqttAsyncClient(
                    this.brokerAddr,
                    this.clientID,
                    this.persistence);

                this.mqttClient.setCallback(this);
            }

            if (!this.mqttClient.isConnected()) {
                this.mqttClient.connect(this.connOpts);
                return true;
            }
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "MQTT connect failed", e);
        }

        return false;
    }

    @Override
    public boolean disconnectClient()
    {
        try {
            if (this.mqttClient != null &&
                this.mqttClient.isConnected()) {

                this.mqttClient.disconnect();

                if (this.connListener != null) {
                    this.connListener.onDisconnect();
                }

                return true;
            }
        } catch (MqttException e) {
            _Logger.log(Level.SEVERE, "Disconnect failed", e);
        }

        return false;
    }

    public boolean isConnected()
    {
        return (this.mqttClient != null &&
                this.mqttClient.isConnected());
    }

    // -----------------------------------------------------------------
    // ------------------- REQUIRED PROTECTED API -----------------------
    // -----------------------------------------------------------------

    protected boolean publishMessage(String topicName, byte[] payload, int qos)
    {
        if (topicName == null || payload == null || payload.length == 0) {
            return false;
        }

        if (qos < 0 || qos > 2) {
            qos = ConfigConst.DEFAULT_QOS;
        }

        try {
            MqttMessage msg = new MqttMessage(payload);
            msg.setQos(qos);
            this.mqttClient.publish(topicName, msg);
            return true;
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Publish failed", e);
        }

        return false;
    }

    protected boolean subscribeToTopic(String topicName, int qos)
    {
        return subscribeToTopic(topicName, qos, null);
    }

    protected boolean subscribeToTopic(
        String topicName, int qos, IMqttMessageListener listener)
    {
        if (topicName == null) return false;

        if (qos < 0 || qos > 2) {
            qos = ConfigConst.DEFAULT_QOS;
        }

        try {
            if (listener != null) {
                this.mqttClient.subscribe(topicName, qos, listener);
            } else {
                this.mqttClient.subscribe(topicName, qos);
            }
            return true;
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Subscribe failed", e);
        }

        return false;
    }

    protected boolean unsubscribeFromTopic(String topicName)
    {
        if (topicName == null) return false;

        try {
            this.mqttClient.unsubscribe(topicName);
            return true;
        } catch (Exception e) {
            _Logger.log(Level.SEVERE, "Unsubscribe failed", e);
        }

        return false;
    }

    // -----------------------------------------------------------------
    // ------------------- PUBLIC DELEGATES -----------------------------
    // -----------------------------------------------------------------

    @Override
    public boolean publishMessage(ResourceNameEnum topicName, String msg, int qos)
    {
        return publishMessage(
            topicName.getResourceName(),
            msg.getBytes(),
            qos
        );
    }

    @Override
    public boolean subscribeToTopic(ResourceNameEnum topicName, int qos)
    {
        return subscribeToTopic(
            topicName.getResourceName(),
            qos
        );
    }

    @Override
    public boolean unsubscribeFromTopic(ResourceNameEnum topicName)
    {
        return unsubscribeFromTopic(
            topicName.getResourceName()
        );
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

    // -----------------------------------------------------------------
    // ---------------- CONNECTION LISTENER -----------------------------
    // -----------------------------------------------------------------

    public boolean setConnectionListener(IConnectionListener listener)
    {
    	if (listener != null) {
    		_Logger.info("Setting connection listener.");
    		this.connListener = listener;
    		return true;
    	}
    	_Logger.warning("No connection listener specified.");
    	return false;
    }

    // -----------------------------------------------------------------
    // ---------------- MQTT CALLBACK EVENTS ----------------------------
    // -----------------------------------------------------------------

    @Override
    public void connectComplete(boolean reconnect, String serverURI)
    {
    	_Logger.info("MQTT connection successful (reconnect = " + reconnect + ")");

    	if (!this.useCloudGatewayConfig) {
    		try {
    			this.mqttClient.subscribe(
    				ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE.getResourceName(),
    				1
    				
    			);
    		} catch (Exception e) {
    			_Logger.warning("Subscription failed.");
    		}
    	}

    	// REQUIRED CALLBACK
    	if (this.connListener != null) {
    		this.connListener.onConnect();
    	}
    }


    @Override
    public void connectionLost(Throwable cause)
    {
        _Logger.log(Level.WARNING, "MQTT connection lost", cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message)
        throws Exception
    {
        _Logger.info("Message received: " + topic);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token)
    {
        _Logger.fine("Delivery complete");
    }

    // -----------------------------------------------------------------
    // ---------------- CONFIG INITIALIZATION ---------------------------
    // -----------------------------------------------------------------

    private void initClientParameters(String configSection)
    {
        ConfigUtil configUtil = ConfigUtil.getInstance();

        // Read host from config
        this.host = configUtil.getProperty(configSection, ConfigConst.HOST_KEY);
        if (this.host == null || this.host.isEmpty()) {
            _Logger.warning("Host not found in config, using default: " + ConfigConst.DEFAULT_HOST);
            this.host = ConfigConst.DEFAULT_HOST;
        }

        // Read port from config
        this.port = configUtil.getInteger(
            configSection, ConfigConst.PORT_KEY, ConfigConst.DEFAULT_MQTT_PORT);

        this.brokerKeepAlive = configUtil.getInteger(
            configSection, ConfigConst.KEEP_ALIVE_KEY, ConfigConst.DEFAULT_KEEP_ALIVE);

        this.clientID = MqttClient.generateClientId();
        this.persistence = new MemoryPersistence();

        this.connOpts = new MqttConnectOptions();
        this.connOpts.setKeepAliveInterval(this.brokerKeepAlive);
        this.connOpts.setCleanSession(true);
        this.connOpts.setAutomaticReconnect(true);

        boolean enableEncryption =
            configUtil.getBoolean(configSection, ConfigConst.ENABLE_CRYPT_KEY);

        if (enableEncryption) {
            initSecureConnectionParameters(configUtil, configSection);
        } else {
            // Always ensure brokerAddr is non-null
            this.brokerAddr = "tcp://" + this.host + ":" + this.port;
        }

        boolean enableAuth =
            configUtil.getBoolean(configSection, ConfigConst.ENABLE_AUTH_KEY);

        if (enableAuth) {
            initCredentialConnectionParameters(configUtil, configSection);
        }

        // Fallback check
        if (this.brokerAddr == null || this.brokerAddr.isEmpty()) {
            this.brokerAddr = "tcp://" + ConfigConst.DEFAULT_HOST + ":" + ConfigConst.DEFAULT_MQTT_PORT;
            _Logger.warning("Broker address not set, using fallback: " + this.brokerAddr);
        }
    }

    private void initSecureConnectionParameters(
        ConfigUtil configUtil, String configSection)
    {
        try {
            String pemFileName =
                configUtil.getProperty(configSection, ConfigConst.CERT_FILE_KEY);

            if (pemFileName != null) {
                File pemFile = new File(pemFileName);

                if (pemFile.exists()) {
                    Class<?> certUtilClass =
                        Class.forName("programmingtheiot.common.SimpleCertManagementUtil");

                    Object certUtil =
                        certUtilClass.getMethod("getInstance").invoke(null);

                    boolean loaded =
                        (boolean) certUtilClass
                            .getMethod("loadCertificate", String.class)
                            .invoke(certUtil, pemFileName);

                    if (loaded) {
                        SSLSocketFactory socketFactory =
                            (SSLSocketFactory) certUtilClass
                                .getMethod("getSocketFactory")
                                .invoke(certUtil);

                        this.connOpts.setSocketFactory(socketFactory);

                        int securePort = configUtil.getInteger(
                            configSection,
                            ConfigConst.SECURE_PORT_KEY,
                            ConfigConst.DEFAULT_MQTT_SECURE_PORT);

                        this.brokerAddr =
                            "ssl://" + this.host + ":" + securePort;
                    }
                }
            }
        } catch (Exception e) {
            _Logger.log(Level.WARNING,
                "TLS configuration failed. Falling back to TCP.", e);

            this.brokerAddr = "tcp://" + this.host + ":" + this.port;
        }
    }

    private void initCredentialConnectionParameters(
        ConfigUtil configUtil, String configSection)
    {
        try {
            String credFileName =
                configUtil.getProperty(configSection, ConfigConst.CRED_FILE_KEY);

            if (credFileName != null) {
                File credFile = new File(credFileName);

                if (credFile.exists()) {
                    Properties credProps = new Properties();
                    credProps.load(new java.io.FileInputStream(credFile));

                    String userName =
                        credProps.getProperty(ConfigConst.USER_NAME_KEY);

                    String password =
                        credProps.getProperty("userPassword");

                    if (userName != null && password != null) {
                        this.connOpts.setUserName(userName);
                        this.connOpts.setPassword(password.toCharArray());
                    }
                }
            }
        } catch (Exception e) {
            _Logger.log(Level.WARNING, "Credential load failed", e);
        }
    }
}
