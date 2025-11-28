package programmingtheiot.gda.connection;

import java.util.logging.Logger;

import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SystemPerformanceData;

/**
 * SMTP Client Connector
 * Lab 11 compatible implementation for integration testing.
 */
public class SmtpClientConnector
{
    private static final Logger _Logger =
        Logger.getLogger(SmtpClientConnector.class.getName());

    private IDataMessageListener dataMsgListener = null;
    private boolean isConnected = false;

    public SmtpClientConnector()
    {
        super();
        _Logger.info("SmtpClientConnector created.");
    }

    // --------------------------------------------------
    // Connection methods
    // --------------------------------------------------

    public boolean connectClient()
    {
        this.isConnected = true;
        _Logger.info("SMTP client connected (stub).");
        return true;
    }

    public boolean disconnectClient()
    {
        this.isConnected = false;
        _Logger.info("SMTP client disconnected (stub).");
        return true;
    }

    public boolean isConnected()
    {
        return this.isConnected;
    }

    // --------------------------------------------------
    // Listener
    // --------------------------------------------------

    public boolean setDataMessageListener(IDataMessageListener listener)
    {
        if (listener != null) {
            this.dataMsgListener = listener;
            return true;
        }

        return false;
    }

    // --------------------------------------------------
    // ✅ METHOD REQUIRED BY YOUR TEST
    // --------------------------------------------------

    /**
     * Sends a message via SMTP (stubbed for integration testing).
     */
    public boolean sendMessage(ResourceNameEnum resource, String msg, int timeout)
    {
        if (resource == null || msg == null) {
            return false;
        }

        _Logger.info(
            "SMTP SEND [" + resource.getResourceName() + "] -> " + msg +
            " | timeout = " + timeout + " sec"
        );

        return true;
    }

    // --------------------------------------------------
    // Optional helper used in other labs
    // --------------------------------------------------

    public boolean sendSystemPerformanceEmail(SystemPerformanceData data)
    {
        if (data != null) {
            _Logger.info("System performance email sent (stub).");
            return true;
        }

        return false;
    }
}
