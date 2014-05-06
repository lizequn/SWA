package uk.ac.ncl.cs.zequn.swa.filequeue;

/**
 * @author ZequnLi
 *         Date: 14-5-4
 */
public interface FileQueue<T> {
    int size();
    void add(T entry);
    T peek();
}
