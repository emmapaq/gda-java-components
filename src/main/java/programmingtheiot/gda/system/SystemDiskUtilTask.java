package programmingtheiot.gda.system;

import java.io.File;
import programmingtheiot.common.ConfigConst;

/**
 * Task to retrieve disk utilization percentage.
 */
public class SystemDiskUtilTask extends BaseSystemUtilTask
{
    private static final String DEFAULT_PATH = "/"; // or customize for your deployment

    public SystemDiskUtilTask()
    {
        super(ConfigConst.NOT_SET, ConfigConst.DEFAULT_TYPE_ID);
    }

    @Override
    public float getTelemetryValue()
    {
        File root = new File(DEFAULT_PATH);
        long total = root.getTotalSpace();
        long free = root.getFreeSpace();
        long used = total - free;

        if (total > 0) {
            return ((float) used / (float) total) * 100.0f;
        } else {
            return 0.0f;
        }
    }
}
