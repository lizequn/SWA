package uk.ac.ncl.cs.zequn.swa.core;

import uk.ac.ncl.cs.zequn.swa.avg.AvgCalculateImpl;
import uk.ac.ncl.cs.zequn.swa.model.Result;
import uk.ac.ncl.cs.zequn.swa.model.SingleInput;
import uk.ac.ncl.cs.zequn.swa.model.Tuple;
import uk.ac.ncl.cs.zequn.swa.monitor.MemoryMonitor;
import uk.ac.ncl.cs.zequn.swa.monitor.MemoryMonitorListener;

import java.util.ArrayList;
import java.util.List;
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
    private final MemoryMonitor memoryMonitor = new MemoryMonitor(1000);

    public MainController(Strategy strategy,long time,long period,ResultOutput resultOutputListener){
        this.resultOutput = resultOutputListener;
        this.time = time;
        this.period = period;
        this.numOfTuples = period/time;
        System.out.println("debug!!!!!!!!!!!!!!"+numOfTuples);
        inMemoryStore = new InMemoryStore();
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
                    System.out.println("debug!!!!!!!!!!!!!!"+inMemoryStore.getSize());
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
                System.out.println(inMemoryStore.getRealSize());
            }
        });
        memoryMonitor.addListener(new MemoryMonitorListener() {
            @Override
            public void monitor() {
                System.out.println(inMemoryStore.getSize());
            }
        });
        memoryMonitor.start();
    }
    public void offer(SingleInput input){
        if(!calFlag) {
            calFlag = true;
            new Timer().scheduleAtFixedRate(timerTask,0,time);
        }
        factory.offer(input);
        memoryMonitor.inputRateCheck();
    }





}
