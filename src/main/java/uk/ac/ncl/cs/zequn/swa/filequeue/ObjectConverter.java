package uk.ac.ncl.cs.zequn.swa.filequeue;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author ZequnLi
 *         Date: 14-5-4
 */
public interface ObjectConverter<T> {
    T from(byte[] bytes) throws IOException;
    void toStream(T o, OutputStream bytes) throws IOException;
}
