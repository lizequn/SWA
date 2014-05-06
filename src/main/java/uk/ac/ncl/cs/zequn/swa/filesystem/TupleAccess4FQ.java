package uk.ac.ncl.cs.zequn.swa.filesystem;

import uk.ac.ncl.cs.zequn.swa.filequeue.FileQueue;
import uk.ac.ncl.cs.zequn.swa.filequeue.FileQueueImpl;
import uk.ac.ncl.cs.zequn.swa.filequeue.ObjectConverterImpl;
import uk.ac.ncl.cs.zequn.swa.model.Tuple;
import uk.ac.ncl.cs.zequn.swa.monitor.MemoryMonitor;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * @author ZequnLi
 *         Date: 14-5-4
 */
public class TupleAccess4FQ implements TupleAccess {
    private final FileQueue<Tuple> fileQueue;
    public TupleAccess4FQ(String path,String name) throws IOException {
        File db = new File(path,name);
        fileQueue = new FileQueueImpl<Tuple>(db,new ObjectConverterImpl<Tuple>());
    }

    @Override
    public void insertTuple(Tuple tuple) {
        fileQueue.add(tuple);
    }

    @Override
    public Tuple getTuple() {
        return fileQueue.peek();
    }

    @Override
    public void init() {

    }


    public static void main(String [] args) throws SQLException, IOException {
        MemoryMonitor memoryMonitor = new MemoryMonitor(1000,new LogAccess("fqinsert"),null,null,null);
        Tuple tuple = new Tuple(12,3222);
        long counter = 0;
        TupleAccess access = new TupleAccess4FQ("D://","db");
        memoryMonitor.start();
        while(true){
            memoryMonitor.inputRateCheck();
            counter++;
            access.insertTuple(tuple);
            if(counter %50000 ==0){
                memoryMonitor.flushLog();
                break;
            }
        }
    }
}
