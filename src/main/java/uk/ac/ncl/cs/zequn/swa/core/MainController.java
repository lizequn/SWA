package uk.ac.ncl.cs.zequn.swa.core;

import uk.ac.ncl.cs.zequn.swa.avg.AvgCalculateImpl;
import uk.ac.ncl.cs.zequn.swa.filesystem.LogAccess;
import uk.ac.ncl.cs.zequn.swa.model.Result;
import uk.ac.ncl.cs.zequn.swa.model.Tuple;
import uk.ac.ncl.cs.zequn.swa.monitor.MemoryMonitor;
import uk.ac.ncl.cs.zequn.swa.monitor.MemoryMonitorListener;

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
    private final AtomicReference<Result> resultList;
    private final ResultOutput resultOutput;
    private boolean calFlag = false;
    //private final MemoryMonitor memoryMonitor = new MemoryMonitor(1000,new LogAccess("memory"),new LogAccess("diskWrite"),new LogAccess("diskRead"));
    private final MemoryMonitor memoryMonitor = new MemoryMonitor(1000,new LogAccess("memory"),null,null);

    public MainController(Strategy strategy,long time,long period,ResultOutput resultOutputListener) throws SQLException {
        this.resultOutput = resultOutputListener;
        this.time = time;
        this.period = period;
        this.numOfTuples = period/time;
        //define max tuple in memory
        inMemoryStore = new InMemoryStore(false,100*60*11,memoryMonitor);
        this.strategy = strategy;
        switch (strategy){
            case AVG:
                calculate = new AvgCalculateImpl();break;
            default:
                throw new IllegalStateException();
        }
        factory = new TupleFactory(strategy,calculate);
        this.resultList = new AtomicReference<Result>();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Tuple newTuple = factory.getResult();
                if(newTuple == null) return;
                Tuple oldTuple = null;
                inMemoryStore.put(newTuple);
                if(inMemoryStore.getSize()>numOfTuples){
                    //System.out.println("remove !!!!!!!!!!!!!!"+inMemoryStore.getSize());
                    oldTuple = inMemoryStore.get();
                }
                if(resultList.get() != null){
                    resultList.set(calculate.calResult(resultList.get(),inMemoryStore.getRealSize(),newTuple,oldTuple));
                } else {
                    resultList.set(calculate.calResult(null,inMemoryStore.getRealSize(),newTuple,oldTuple));
                }

                resultOutput.output(calculate.getResult(resultList.get(),inMemoryStore.getRealSize())+"");
            }
        };


        memoryMonitor.addListener(new MemoryMonitorListener() {
            @Override
            public void monitor() {
                System.out.println("total input:"+inMemoryStore.getRealSize());
            }
        });
        memoryMonitor.addListener(new MemoryMonitorListener() {
            @Override
            public void monitor() {
                System.out.println("total tuples"+inMemoryStore.getSize());
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
        memoryMonitor.inputRateCheck();
    }

    public void end(){
        memoryMonitor.flushLog();
    }





}
