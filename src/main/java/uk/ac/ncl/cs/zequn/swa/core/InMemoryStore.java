package uk.ac.ncl.cs.zequn.swa.core;

import uk.ac.ncl.cs.zequn.swa.filesystem.TupleAccess;
import uk.ac.ncl.cs.zequn.swa.filesystem.TupleAccess4FQ;
import uk.ac.ncl.cs.zequn.swa.model.Tuple;
import uk.ac.ncl.cs.zequn.swa.monitor.MemoryMonitor;
import uk.ac.ncl.cs.zequn.swa.monitor.MemoryMonitorImpl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;

/**
 * @author ZequnLi
 *         Date: 14-4-20
 */
public class InMemoryStore {
    private long realSize = 0;
    private boolean enableDiskStore;
    private long tupleSize = 0;
    private final TupleAccess tupleAccess;
    private final long maxSize;
    private final MemoryMonitor memoryMonitor;
    //private final LinkedBlockingDeque<Tuple> queue= new LinkedBlockingDeque<Tuple>();
    private final LinkedList<Tuple> queue = new LinkedList<Tuple>();
    public InMemoryStore(boolean enableDiskStore,long maxSize,MemoryMonitor memoryMonitor) throws SQLException, IOException {
        this.enableDiskStore = enableDiskStore;
        this.maxSize = maxSize;
        if(enableDiskStore){
//        this.tupleAccess = new TupleAccess4MySql();
        this.tupleAccess = new TupleAccess4FQ("D://","db");
        this.tupleAccess.init();
        }else {
            this.tupleAccess = null;
        }
        this.memoryMonitor = memoryMonitor;
    }

    public LinkedList<Tuple> getQueue(){
        return queue;
    }
    public boolean put(Tuple tuple)  {
        if(enableDiskStore){
            if(queue.size()<maxSize){
                realSize+= tuple.getSize();
                tupleSize++;
                return queue.offer(tuple);
            }else {
                realSize+=tuple.getSize();
                memoryMonitor.diskWriteCount();
                tupleAccess.insertTuple(tuple);
                tupleSize++;
                return true;
            }
        }else {
            realSize+= tuple.getSize();
            tupleSize++;
            return queue.offer(tuple);
        }

    }

    public Tuple get() {
        if(enableDiskStore){
            if(queue.size()<1){
                return null;
            }
            Tuple tuple = queue.poll();
            realSize-= tuple.getSize();
            if(queue.size()+1>=maxSize){
                memoryMonitor.diskReadCount();
                Tuple newTuple = tupleAccess.getTuple();
                if(null!=newTuple){
                    queue.offer(newTuple);
                }
            }
            tupleSize--;
            return tuple;
        } else {
            if(queue.size()<1){
                return null;
            }
            Tuple tuple = queue.poll();
            realSize-= tuple.getSize();
            tupleSize--;
            return tuple;
        }
    }

    public long getSize(){
       return this.tupleSize;
    }
    public long getRealSize(){
        return realSize;
    }

    public boolean isFull(){
        return queue.size()>= Config.MAX_MEMORY_SIZE;
    }

//    public static void main(String []args) throws SQLException, InterruptedException {
////        final long stopTime = 2*60*10;
////        final MemoryMonitorImpl memoryMonitor1 = new MemoryMonitorImpl(1000,new LogAccess("abc"),null,null);
////        final InMemoryStore inMemoryStore = new InMemoryStore(true,1000000000,memoryMonitor1);
////        TimerTask timerTask = new TimerTask() {
////            @Override
////            public void run() {
////                    if(inMemoryStore.getSize() >= 1000*60){
////                        inMemoryStore.getQueue().peek().setResult(123123);
////                    }else {
////                        inMemoryStore.put(new Tuple(222,222));
////                    }
////                    memoryMonitor1.inputRateCheck();
////                }
////        };
////        Timer timer = new Timer();
////        timer.scheduleAtFixedRate(timerTask, 0, 1);
////        memoryMonitor1.start();
////        Thread thread = new Thread(new Runnable() {
////            boolean stop = false;
////            long timeCounter = 0;
////
////            @Override
////            public void run() {
////                while (!stop){
////                    timeCounter++;
////                    if(timeCounter>=stopTime){
////                        stop = true;
////                    }
////                    try {
////                        Thread.sleep(1000);
////                    } catch (InterruptedException e) {
////                        e.printStackTrace();
////                    }
////                }
////            }
////        });
////        thread.start();
////        System.out.println("wait");
////
////        thread.join();
////        timer.cancel();
////        memoryMonitor1.flushLog();
////        System.out.println("end");
//
//    }

}
