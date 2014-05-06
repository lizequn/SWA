package uk.ac.ncl.cs.zequn.swa.filesystem;

import uk.ac.ncl.cs.zequn.swa.model.Tuple;

/**
 * @author ZequnLi
 *         Date: 14-5-4
 */
public interface TupleAccess {
    void insertTuple(Tuple tuple);
    Tuple getTuple();
    void init();
}
