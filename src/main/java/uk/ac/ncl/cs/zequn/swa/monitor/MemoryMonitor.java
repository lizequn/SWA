package uk.ac.ncl.cs.zequn.swa.monitor;

/**
 * Created by Zequn on 2014/5/10.
 */
public interface MemoryMonitor {
    void inputRateCount();
    void diskReadCount();
    void diskWriteCount();
    void latencyBefore();
    void latencyAfter();
    void start();
    void flushLog();
    void addListener(MemoryMonitorListener listener);
}
