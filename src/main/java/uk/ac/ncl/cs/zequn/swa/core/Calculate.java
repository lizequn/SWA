package uk.ac.ncl.cs.zequn.swa.core;

import uk.ac.ncl.cs.zequn.swa.model.Tuple;

/**
 * @author ZequnLi
 *         Date: 14-4-21
 */
public interface Calculate {
    Tuple updateCurrentTuple(Tuple tuple, double input);
    double updateResult(double result, long realSize, Tuple newTuple, Tuple oldTuple);
    String getResult(double result,long size);
}
