package uk.ac.ncl.cs.zequn.swa.core;

import uk.ac.ncl.cs.zequn.swa.model.Result;
import uk.ac.ncl.cs.zequn.swa.model.SingleInput;
import uk.ac.ncl.cs.zequn.swa.model.Tuple;

/**
 * @author ZequnLi
 *         Date: 14-4-21
 */
public interface Calculate {
    Tuple calInfo4Tuple(Tuple tuple,SingleInput input);
    Result calResult(Result result,long realSize,Tuple newTuple,Tuple oldTuple);
    String getResult(Result result,long size);
}
