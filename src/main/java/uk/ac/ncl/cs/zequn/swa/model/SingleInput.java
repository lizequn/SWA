package uk.ac.ncl.cs.zequn.swa.model;

/**
 * @author ZequnLi
 *         Date: 14-4-21
 */
public class SingleInput {
    private long id;
    private double time;

    public SingleInput(long id, double time) {
        this.id = id;
        this.time = time;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }
}
