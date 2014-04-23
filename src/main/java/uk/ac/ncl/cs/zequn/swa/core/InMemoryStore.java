package uk.ac.ncl.cs.zequn.swa.core;

import uk.ac.ncl.cs.zequn.swa.model.Tuple;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author ZequnLi
 *         Date: 14-4-20
 */
public class InMemoryStore {
    private long realSize = 0;
    private final LinkedBlockingDeque<Tuple> queue= new LinkedBlockingDeque<Tuple>();


    public boolean put(Tuple tuple){
        realSize+= tuple.getSize();
        return queue.offer(tuple);
    }

    public Tuple get(){
        if(queue.size()<1){
            return null;
        }
        Tuple tuple = queue.poll();
        realSize-= tuple.getSize();
        return tuple;
    }

    public int getSize(){
       return queue.size();
    }
    public long getRealSize(){
        return realSize;
    }

    public boolean isFull(){
        return queue.size()>= Config.MAX_MEMORY_SIZE;
    }

}
