package programmingtheiot.gda.system;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;

/**
 * Retrieves JVM memory utilization as a telemetry value.
 */
public class SystemMemUtilTask extends BaseSystemUtilTask
{
    private static final Logger _Logger = Logger.getLogger(SystemMemUtilTask.class.getName());

    /**
     * Default constructor.
     */
    public SystemMemUtilTask()
    {
        super(ConfigConst.NOT_SET, ConfigConst.DEFAULT_TYPE_ID);
    }

    /**
     * Returns the current heap memory usage as a percentage.
     */
    @Override
    public float getTelemetryValue()
    {
        MemoryUsage memUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        double memUsed = (double) memUsage.getUsed();
        double memMax  = (double) memUsage.getMax();

        _Logger.fine("Mem used: " + memUsed + "; Mem Max: " + memMax);

        double memUtil = (memUsed / memMax) * 100.0d;

        return (float) memUtil;
    }
}
