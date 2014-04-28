package uk.ac.ncl.cs.zequn.swa.avg;

import uk.ac.ncl.cs.zequn.swa.core.Calculate;
import uk.ac.ncl.cs.zequn.swa.core.Strategy;
import uk.ac.ncl.cs.zequn.swa.model.Result;
import uk.ac.ncl.cs.zequn.swa.model.Tuple;

/**
 * @author ZequnLi
 *         Date: 14-4-21
 */
public class AvgCalculateImpl implements Calculate{
    @Override
    public Tuple calInfo4Tuple(Tuple tuple, double input) {
        tuple.setResult(tuple.getResult()+input);
        tuple.setSize(tuple.getSize() + 1);
        return tuple;
    }

    @Override
    public Result calResult(Result result,long realSize,Tuple newTuple, Tuple oldTuple) {
        if(result == null){
            return new Result(Strategy.AVG,newTuple.getResult());
        }
        if(oldTuple == null){
            double sum = result.getResult() + newTuple.getResult();
            return new Result(Strategy.AVG,sum);
        }
        double oldResult = result.getResult();
        double newResult = oldResult - oldTuple.getResult()+newTuple.getResult();
        return new Result(Strategy.AVG,newResult);
    }

    @Override
    public String getResult(Result result, long size) {
        return result.getResult()/size +"";
    }
}
