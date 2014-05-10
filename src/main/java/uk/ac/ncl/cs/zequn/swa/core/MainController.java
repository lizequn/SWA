package uk.ac.ncl.cs.zequn.swa.core;

import uk.ac.ncl.cs.zequn.swa.avg.AvgCalculateImpl;
import uk.ac.ncl.cs.zequn.swa.filesystem.LogAccess;
import uk.ac.ncl.cs.zequn.swa.model.Tuple;
import uk.ac.ncl.cs.zequn.swa.monitor.MemoryMonitor;
import uk.ac.ncl.cs.zequn.swa.monitor.MemoryMonitorImpl;
import uk.ac.ncl.cs.zequn.swa.monitor.MemoryMonitorListener;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author ZequnLi
 *         Date: 14-4-21
 */
public class MainController {
    private final InMemoryStore inMemoryStore;
    private final Strategy strategy;
    private final TupleFactory factory;
    private final Calculate calculate;
    private final long time;
    private final long period;
    private final TimerTask timerTask;
    private final long numOfTuples;
    private final AtomicReference<Double> resultList;
    private final ResultOutput resultOutput;
    private boolean calFlag = false;
    private final MemoryMonitor memoryMonitor = new MemoryMonitorImpl(1000,new LogAccess("memory"),new LogAccess("diskWrite"),new LogAccess("diskRead"),new LogAccess("latency"));
    //private final MemoryMonitorImpl memoryMonitor = new MemoryMonitorImpl(1000,new LogAccess("memory"),null,null,new LogAccess("latency"));

    public MainController(Strategy strategy,long time,long period,ResultOutput resultOutputListener) throws SQLException, IOException {
        this.resultOutput = resultOutputListener;
        this.time = time;
        this.period = period;
        this.numOfTuples = period/time;
        //define max tuple in memory
        inMemoryStore = new InMemoryStore(true ,10*60*1,memoryMonitor);
        this.strategy = strategy;
        switch (strategy){
            case AVG:
                calculate = new AvgCalculateImpl();break;
            default:
                throw new IllegalStateException();
        }
        factory = new TupleFactory(calculate);
        this.resultList = new AtomicReference<Double>();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                memoryMonitor.latencyBefore();
                Tuple newTuple = factory.getResult();
                if(newTuple == null) return;
                Tuple oldTuple = null;
                inMemoryStore.put(newTuple);
                if(inMemoryStore.getSize()>numOfTuples){
                    //System.out.println("remove !!!!!!!!!!!!!!"+inMemoryStore.getSize());
                    oldTuple = inMemoryStore.get();
                }
                if(resultList.get() != null){
                    resultList.set(calculate.updateResult(resultList.get(), inMemoryStore.getRealSize(), newTuple, oldTuple));
                } else {
                    resultList.set(calculate.updateResult(-1, inMemoryStore.getRealSize(), newTuple, oldTuple));
                }

                resultOutput.output(calculate.getResult(resultList.get(),inMemoryStore.getRealSize())+"");
                memoryMonitor.latencyAfter();
            }
        };
//        garbage collect
//        TimerTask gc = new TimerTask() {
//            @Override
//            public void run() {
//                System.gc();
//            }
//        };
//        new Timer().scheduleAtFixedRate(gc,0,1000*60*5);

        memoryMonitor.addListener(new MemoryMonitorListener() {
            @Override
            public void monitor() {
                System.out.println("total input:"+inMemoryStore.getRealSize());
            }
        });
        memoryMonitor.addListener(new MemoryMonitorListener() {
            @Override
            public void monitor() {
                System.out.println("total tuples:"+inMemoryStore.getSize());
            }
        });
        memoryMonitor.start();
    }
    public void offer(double input){
        if(!calFlag) {
            calFlag = true;
            new Timer().scheduleAtFixedRate(timerTask,0,time);
        }
        factory.offer(input);
        memoryMonitor.inputRateCount();
    }

    public void end(){
        memoryMonitor.flushLog();
    }
}
