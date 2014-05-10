package uk.ac.ncl.cs.zequn.swa.core;

import uk.ac.ncl.cs.zequn.swa.model.Tuple;

/**
 * @author ZequnLi
 *         Date: 14-4-21
 */
public class TupleFactory {
    private Tuple  tuple;
    private Calculate calculate;
    private Object lock = new Object();
    public TupleFactory(Calculate calculate){
        this.calculate = calculate;
    }

    public void offer(double input){
        synchronized(lock){
            if(null == tuple){
                tuple = new Tuple(0,0);
            }
            calculate.updateCurrentTuple(tuple, input);
        }
    }
    public Tuple getResult(){
        synchronized(lock){
            if(null == tuple){
                return null;
            }
            Tuple result = new Tuple(tuple.getSize(),tuple.getResult());
            tuple= null;
           // System.out.println(result.getSize()+"  "+result.getResult());
            return result;
        }
    }
}
