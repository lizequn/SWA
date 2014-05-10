package uk.ac.ncl.cs.zequn.swa.avg;

import uk.ac.ncl.cs.zequn.swa.core.Calculate;
import uk.ac.ncl.cs.zequn.swa.model.Tuple;

/**
 * @author ZequnLi
 *         Date: 14-4-21
 */
public class AvgCalculateImpl implements Calculate{
    @Override
    public Tuple updateCurrentTuple(Tuple tuple, double input) {
        tuple.setResult(tuple.getResult()+input);
        tuple.setSize(tuple.getSize() + 1);
        return tuple;
    }

    @Override
    public double updateResult(double result, long realSize, Tuple newTuple, Tuple oldTuple) {
        if(result <= 0){
            return newTuple.getResult();
        }
        if(oldTuple == null){
            double sum = result + newTuple.getResult();
            return sum;
        }
        double oldResult = result;
        double newResult = oldResult - oldTuple.getResult()+newTuple.getResult();
        return newResult;
    }

    @Override
    public String getResult(double result, long size) {
        return result/size +"";
    }
}
