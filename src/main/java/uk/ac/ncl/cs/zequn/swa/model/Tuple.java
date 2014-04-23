package uk.ac.ncl.cs.zequn.swa.model;

/**
 * @author ZequnLi
 *         Date: 14-4-20
 */
public class Tuple {
    private int size;
    private double result;

    public Tuple(int size, double result) {
        this.size = size;
        this.result = result;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }


}
