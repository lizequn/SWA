package uk.ac.ncl.cs.zequn.swa.monitor;

import uk.ac.ncl.cs.zequn.swa.util.B2M;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZequnLi
 *         Date: 14-4-3
 */
public class MemoryMonitor {
   private final long max = Runtime.getRuntime().maxMemory();
   private  final long interval;
   private long inputNum;
   private final Object lock = new Object();
   private final List<MemoryMonitorListener> listenerList = new ArrayList<MemoryMonitorListener>();

    public MemoryMonitor(long interval) {
        this.interval = interval;
    }
    public void addListener(MemoryMonitorListener listener){
        listenerList.add(listener);
    }

    public void inputRateCheck(){
        synchronized(lock){
            inputNum++;
        }
    }

    public void start(){
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private Runnable runnable = new Runnable() {
       @Override
       public void run() {
           while (true){
               long free = Runtime.getRuntime().freeMemory();
               System.out.println("Max Heap Size: " + B2M.byte2M(max));
               System.out.println("Current Free size: "+B2M.byte2M(free));
               synchronized (lock){
                   System.out.println("input rate: "+ (double)inputNum/(interval/1000));
                   inputNum =0;
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
}
