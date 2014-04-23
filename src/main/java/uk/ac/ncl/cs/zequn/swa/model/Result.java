package uk.ac.ncl.cs.zequn.swa.model;

import uk.ac.ncl.cs.zequn.swa.core.Strategy;

/**
 * @author ZequnLi
 *         Date: 14-4-21
 */
public class Result {
    private final Strategy strategy;
    private final double result;

    public Result(Strategy strategy, double result) {
        this.strategy = strategy;
        this.result = result;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public double getResult() {
        return result;
    }
}
