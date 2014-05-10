package uk.ac.ncl.cs.zequn.swa.monitor;

/**
 * @author ZequnLi
 *         Date: 14-4-3
 */
public class B2M {
    public static double byte2M(long b){
        double temp = b;
        return (temp/1024.0)/1024.0;
    }
}
