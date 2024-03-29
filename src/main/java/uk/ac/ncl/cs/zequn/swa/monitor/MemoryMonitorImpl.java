package uk.ac.ncl.cs.zequn.swa.monitor;

import uk.ac.ncl.cs.zequn.swa.filesystem.LogAccess;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author ZequnLi
 *         Date: 14-4-3
 */
public class MemoryMonitorImpl implements MemoryMonitor {
    private final long max = Runtime.getRuntime().maxMemory();
    private final long interval;
    private long counter= 0;
    private AtomicReference<Boolean> flag = new AtomicReference<Boolean>();
    private long inputNum;
    private long diskRead;
    private long diskWrite;
    private long latencyBefore;
    private long totalLatency;
    private long latencyNum;

    private final Object lock = new Object();
    private final Object diskLock1 = new Object();
    private final Object diskLock2 = new Object();
    private final Object latencyLock = new Object();
    private final List<MemoryMonitorListener> listenerList = new ArrayList<MemoryMonitorListener>();
    private final LogAccess logAccess;
    private final LogAccess diskWriteLog;
    private final LogAccess diskReadLog;
    private final LogAccess latencyLog;
    public MemoryMonitorImpl(long interval, LogAccess logAccess, LogAccess logAccess1, LogAccess logAccess2, LogAccess logAccess3) throws SQLException {
        this.interval = interval;
        this.logAccess = logAccess;
        diskWriteLog = logAccess1;
        diskReadLog = logAccess2;
        latencyLog= logAccess3;
        if(null!=logAccess){
            this.logAccess.init();
        }
        if(null!=diskWriteLog){
            this.diskWriteLog.init();
        }
        if(null!=diskReadLog){
            this.diskReadLog.init();
        }
        if(null!=latencyLog){
            this.latencyLog.init();
        }

        flag.set(true);
    }
    public void addListener(MemoryMonitorListener listener){
        listenerList.add(listener);
    }

    public void inputRateCount(){
        synchronized(lock){
            inputNum++;
        }
    }
    public void diskReadCount(){
        synchronized (diskLock1){
            diskRead++;
        }
    }
    public void diskWriteCount(){
        synchronized (diskLock2){
            diskWrite++;
        }
    }
    public void latencyBefore(){
        synchronized (latencyLock){
            latencyBefore = System.nanoTime();
        }
    }
    public void latencyAfter(){
        synchronized (latencyLock){
            totalLatency += System.nanoTime()-latencyBefore;
            latencyNum++;
        }
    }

    public void start(){
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private Runnable runnable = new Runnable() {
       @Override
       public void run() {
           while (flag.get()){
               long free = Runtime.getRuntime().freeMemory();
               System.out.println("Max Heap Size: " + B2M.byte2M(max));
               System.out.println("Current Free size: "+B2M.byte2M(free));
               counter++;

               synchronized (lock){
                   if(null!= logAccess){
                       logAccess.insertTuple(counter+"",B2M.byte2M(free)+"",(double)inputNum/(interval/1000) +"");
                   }
                   System.out.println("input rate: "+ (double)inputNum/(interval/1000));
                   inputNum =0;
               }
               synchronized (diskLock2){
                   if(null != diskWriteLog){
                       diskWriteLog.insertTuple(counter+"",diskWrite+"");
                       System.out.println("disk Write rate: "+ (double)diskWrite/(interval/1000));
                       diskWrite =0;
                   }

               }
               synchronized (diskLock1){
                   if(null != diskReadLog){
                       diskReadLog.insertTuple(counter+"",diskRead+"");
                       System.out.println("disk Read rate: "+ (double)diskRead/(interval/1000));
                       diskRead =0;
                   }

               }
               synchronized (latencyLock){
                   if(null!= latencyLog){
                       long average = TimeUnit.MICROSECONDS.convert(totalLatency / latencyNum,TimeUnit.NANOSECONDS);
                       latencyLog.insertTuple(counter+"",average+"");
                       System.out.println("average Latency: "+average);
                       totalLatency = 0;
                       latencyNum = 0;
                   }

               }
               if(listenerList.size()>0){
                   for(MemoryMonitorListener listener : listenerList){
                       listener.monitor();
                   }
               }
               try {
                   Thread.sleep(interval);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
       }
   };

    public void flushLog(){
        flag.set(false);
        if(null!= logAccess){
            logAccess.output2CSV("D://",logAccess.getTable());


        }
        if(null!= diskReadLog){
            diskReadLog.output2CSV("D://",diskReadLog.getTable());
            diskWriteLog.output2CSV("D://",diskWriteLog.getTable());
        }
        if(null!=latencyLog){
            latencyLog.output2CSV("D://",latencyLog.getTable());
        }

    }
}
