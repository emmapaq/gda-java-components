package programmingtheiot.gda.system;

import java.util.logging.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.SystemPerformanceData;

/**
 * Manager for collecting and forwarding system performance metrics.
 */
public class SystemPerformanceManager
{
    // private variables
    private ScheduledExecutorService schedExecSvc = null;
    private SystemCpuUtilTask sysCpuUtilTask = null;
    private SystemMemUtilTask sysMemUtilTask = null;
    private SystemDiskUtilTask sysDiskUtilTask = null;
    private Runnable taskRunner = null;
    private boolean isStarted = false;
    private String locationID = ConfigConst.NOT_SET;
    private IDataMessageListener dataMsgListener = null;

    private static final Logger _Logger = Logger.getLogger(SystemPerformanceManager.class.getName());
    private int pollRate = ConfigConst.DEFAULT_POLL_CYCLES;

    // constructor
    public SystemPerformanceManager()
    {
        this.locationID = ConfigUtil.getInstance().getProperty(
            ConfigConst.GATEWAY_DEVICE, ConfigConst.LOCATION_ID_PROP, ConfigConst.NOT_SET);

        this.sysCpuUtilTask = new SystemCpuUtilTask();
        this.sysMemUtilTask = new SystemMemUtilTask();
        this.sysDiskUtilTask = new SystemDiskUtilTask();

        this.schedExecSvc = Executors.newSingleThreadScheduledExecutor();

        this.taskRunner = new Runnable() {
            @Override
            public void run() {
                handleTelemetry();
            }
        };
    }

    // telemetry handler
    public void handleTelemetry()
    {
        float cpuUtil = this.sysCpuUtilTask.getTelemetryValue();
        float memUtil = this.sysMemUtilTask.getTelemetryValue();
        float diskUtil = this.sysDiskUtilTask.getTelemetryValue();

        _Logger.info("CPU: " + cpuUtil + "%, Mem: " + memUtil + "%, Disk: " + diskUtil + "%");

        SystemPerformanceData spd = new SystemPerformanceData();
        spd.setLocationID(this.locationID);
        spd.setCpuUtilization(cpuUtil);
        spd.setMemoryUtilization(memUtil);
        spd.setDiskUtilization(diskUtil);

        if (this.dataMsgListener != null) {
            this.dataMsgListener.handleSystemPerformanceMessage(
                ResourceNameEnum.GDA_SYSTEM_PERF_MSG_RESOURCE, spd);
        }
    }

    // listener setter
    public void setDataMessageListener(IDataMessageListener listener)
    {
        if (listener != null) {
            this.dataMsgListener = listener;
        }
    }

    // start telemetry collection
    public boolean startManager()
    {
        if (!this.isStarted) {
            _Logger.info("SystemPerformanceManager is starting...");

            this.schedExecSvc.scheduleAtFixedRate(this.taskRunner, 1L, this.pollRate, TimeUnit.SECONDS);
            this.isStarted = true;
        } else {
            _Logger.info("SystemPerformanceManager is already started.");
        }

        return this.isStarted;
    }

    // stop telemetry collection
    public boolean stopManager()
    {
        if (this.schedExecSvc != null && !this.schedExecSvc.isShutdown()) {
            this.schedExecSvc.shutdown();
            this.isStarted = false;
            _Logger.info("SystemPerformanceManager is stopped.");
        }

        return true;
    }
}
