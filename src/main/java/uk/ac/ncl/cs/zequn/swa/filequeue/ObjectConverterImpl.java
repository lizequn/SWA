package uk.ac.ncl.cs.zequn.swa.filequeue;

import java.io.*;

/**
 * @author ZequnLi
 *         Date: 14-5-4
 */
public class ObjectConverterImpl<T extends Serializable> implements ObjectConverter<T> {

    @Override
    public T from(byte[] bytes) throws IOException {
        ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(bytes));
        T object =null;
        try {
            object = (T)stream.readUnshared(); // or readObject?
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return object;
    }

    @Override
    public void toStream(T o, OutputStream bytes) throws IOException {
        ObjectOutputStream stream = new ObjectOutputStream(bytes);
        stream.writeUnshared(o);
        stream.close();
    }
}
