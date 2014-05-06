package uk.ac.ncl.cs.zequn.swa.filequeue;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * @author ZequnLi
 *         Date: 14-5-4
 */
public class FileQueueImpl<T> implements FileQueue<T> {
    private final FileController queueFile;
    private final File file;
    private final ObjectConverter<T> converter;
    private final DirectByteArrayOutputStream bytes = new DirectByteArrayOutputStream();

    public FileQueueImpl(File file, ObjectConverter<T> converter) throws IOException {
        if(file.exists()){
            throw new IOException("file already exist");
        }
        this.queueFile = new FileController(file);
        this.file = file;
        this.converter = converter;
    }

    @Override
    public int size() {
        return queueFile.size();
    }

    @Override
    public final void add(T entry) {
        try {
            bytes.reset();

            converter.toStream(entry, bytes);
            queueFile.add(bytes.getArray(), 0, bytes.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public T peek() {
        try {
            byte[] bytes = queueFile.peek();
            if (bytes == null) return null;
            return converter.from(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



    /** Enables direct access to the internal array. Avoids unnecessary copying. */
    private static class DirectByteArrayOutputStream extends ByteArrayOutputStream {
        public DirectByteArrayOutputStream() {
            super();
        }
        public byte[] getArray() {
            return buf;
        }
    }
}
