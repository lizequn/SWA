package uk.ac.ncl.cs.zequn.swa.filequeue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.NoSuchElementException;
import static java.lang.Math.min;

/**
 * @author ZequnLi
 *         Date: 14-5-4
 */
public class FileController {
    //initial length
    private static final int INITIAL_LENGTH = 1024;

    // temp
    private static final byte[] EMPTY = new byte[INITIAL_LENGTH];

    /**
     *   Header:
     *     File Length            (4 bytes)
     *     Element Count          (4 bytes)
     *     First Element Position (4 bytes)
     *     Last Element Position  (4 bytes)
     */
    //the length of header
    static final int HEADER_LENGTH = 16;

    // access to file. or file chanel???
    final RandomAccessFile raf;

    //file length
    int fileLength;

    // number of element
    private int elementCount;

    //point to first element
    private Element first;

    //point to last element
    private Element last;

    // used to write header
    private final byte[] buffer = new byte[16];

    // connect to the file and check the reader, if header exists, variables recover from header.
    public FileController(File file) throws IOException {
        if (!file.exists()) initialize(file);
        raf = new RandomAccessFile(file,"rwd");
        readHeader();
    }


    // store int in buffer, 4 byte.
    private static void writeInt(byte[] buffer, int offset, int value) {

        buffer[offset] = (byte) (value >> 24);
        buffer[offset + 1] = (byte) (value >> 16);
        buffer[offset + 2] = (byte) (value >> 8);
        buffer[offset + 3] = (byte) value;
    }

    //used to store more than one int.
    private static void writeInts(byte[] buffer, int... values) {
        int offset = 0;
        for (int value : values) {
            writeInt(buffer, offset, value);
            offset += 4;
        }
    }

    //convert byte to int.
    private static int readInt(byte[] buffer, int offset) {
        return ((buffer[offset] & 0xff) << 24)
                + ((buffer[offset + 1] & 0xff) << 16)
                + ((buffer[offset + 2] & 0xff) << 8)
                + (buffer[offset + 3] & 0xff);
    }
    /**
     *   Header:
     *     File Length            (4 bytes)
     *     Element Count          (4 bytes)
     *     First Element Position (4 bytes, =0 if null)
     *     Last Element Position  (4 bytes, =0 if null)
     */
    //analyse header
    private void readHeader() throws IOException {
        raf.seek(0);
        raf.readFully(buffer);
        fileLength = readInt(buffer, 0); //file length
        if (fileLength > raf.length()) {
            throw new IOException("file has been modified unexpected (file length)");
        } else if (fileLength == 0) {
            throw new IOException("unexpected length");
        }
        elementCount = readInt(buffer, 4); //element count
        int firstOffset = readInt(buffer, 8); //first element position
        int lastOffset = readInt(buffer, 12); // last element position
        first = readElement(firstOffset);
        last = readElement(lastOffset);
    }

    //write header.
    private void writeHeader(int fileLength, int elementCount, int firstPosition, int lastPosition) throws IOException {
        writeInts(buffer, fileLength, elementCount, firstPosition, lastPosition);
        raf.seek(0);
        raf.write(buffer);
    }

    // return element
    private Element readElement(int position) throws IOException {
        // should not be 0.  should more than 16
        if (position == 0) return Element.NULL;
        raf.seek(position);
        //the length of element is stored in element header.
        return new Element(position, raf.readInt());
    }

    //create new file
    private static void initialize(File file) throws IOException {
        File newFile = new File(file.getPath() + ".fq");
        RandomAccessFile raf = new RandomAccessFile(newFile, "rwd");
        try {
            raf.setLength(INITIAL_LENGTH);
            raf.seek(0);
            byte[] headerBuffer = new byte[16];
            writeInts(headerBuffer, INITIAL_LENGTH, 0, 0, 0); //init header
            raf.write(headerBuffer);
        } finally {
            raf.close();
        }

        if (!newFile.renameTo(file)) throw new IOException("Rename failed!");
    }


    //position change,  if it reached the EOF. return to head
    private int wrapPosition(int position) {
        return position < fileLength ? position: HEADER_LENGTH + position - fileLength;
    }

    // ring write , if reach to the end of file back to header.
    private void ringWrite(int position, byte[] buffer, int offset, int count) throws IOException {
        position = wrapPosition(position);
        if (position + count <= fileLength) {
            raf.seek(position);
            raf.write(buffer, offset, count);
        } else {
            int beforeEof = fileLength - position;
            raf.seek(position);
            raf.write(buffer, offset, beforeEof);
            raf.seek(HEADER_LENGTH);
            raf.write(buffer, offset + beforeEof, count - beforeEof);
        }
    }

    private void ringErase(int position, int length) throws IOException {
        while (length > 0) {
            int chunk = min(length, EMPTY.length);
            ringWrite(position, EMPTY, 0, chunk);
            length -= chunk;
            position += chunk;
        }
    }


    private void ringRead(int position, byte[] buffer, int offset, int count) throws IOException {
        position = wrapPosition(position);
        if (position + count <= fileLength) {
            raf.seek(position);
            raf.readFully(buffer, offset, count);
        } else {
            int beforeEof = fileLength - position;
            raf.seek(position);
            raf.readFully(buffer, offset, beforeEof);
            raf.seek(HEADER_LENGTH);
            raf.readFully(buffer, offset + beforeEof, count - beforeEof);
        }
    }

    /**
     * Adds an element to the end of the queue.
     *
     */
    public void add(byte[] data) throws IOException {
        add(data, 0, data.length);
    }

    /**
     * Adds an element to the end of the queue.
     */
    public synchronized void add(byte[] data, int offset, int count) throws IOException {
        nonNull(data, "buffer");
        if ((offset | count) < 0 || count > data.length - offset) {
            throw new IndexOutOfBoundsException();
        }

        expandIfNecessary(count);

        boolean wasEmpty = isEmpty();
        int position = wasEmpty ? HEADER_LENGTH : wrapPosition(last.position + Element.HEADER_LENGTH + last.length);
        Element newLast = new Element(position, count);

        writeInt(buffer, 0, count);
        ringWrite(newLast.position, buffer, 0, Element.HEADER_LENGTH);

        ringWrite(newLast.position + Element.HEADER_LENGTH, data, offset, count);

        int firstPosition = wasEmpty ? newLast.position : first.position;
        writeHeader(fileLength, elementCount + 1, firstPosition, newLast.position);
        last = newLast;
        elementCount++;
        if (wasEmpty) first = last;
    }

    private int usedBytes() {
        if (elementCount == 0) return HEADER_LENGTH;

        if (last.position >= first.position) {
            // Contiguous queue.
            return (last.position - first.position)
                    + Element.HEADER_LENGTH + last.length
                    + HEADER_LENGTH;
        } else {
            // tail < head.
            return last.position
                    + Element.HEADER_LENGTH + last.length
                    + fileLength - first.position;
        }
    }

    private int remainingBytes() {
        return fileLength - usedBytes();
    }

    public synchronized boolean isEmpty() {
        return elementCount == 0;
    }


    private void expandIfNecessary(int dataLength) throws IOException {
        int elementLength = Element.HEADER_LENGTH + dataLength;
        int remainingBytes = remainingBytes();
        if (remainingBytes >= elementLength) return;

        int previousLength = fileLength;
        int newLength;
        // Double the length until we can fit the new data.
        do {
            remainingBytes += previousLength;
            newLength = previousLength << 1;
            previousLength = newLength;
        } while (remainingBytes < elementLength);

        setLength(newLength);

        int endOfLastElement = wrapPosition(last.position + Element.HEADER_LENGTH + last.length);

        if (endOfLastElement <= first.position) {
            FileChannel channel = raf.getChannel();
            channel.position(fileLength);
            int count = endOfLastElement - Element.HEADER_LENGTH;
            if (channel.transferTo(HEADER_LENGTH, count, channel) != count) {
                throw new AssertionError("Copied insufficient number of bytes!");
            }
        }

        if (last.position < first.position) {
            int newLastPosition = fileLength + last.position - HEADER_LENGTH;
            writeHeader(newLength, elementCount, first.position, newLastPosition);
            last = new Element(newLastPosition, last.length);
        } else {
            writeHeader(newLength, elementCount, first.position, last.position);
        }

        fileLength = newLength;
    }

    private void setLength(int newLength) throws IOException {
        raf.setLength(newLength);
        raf.getChannel().force(true);
    }

    //Reads the eldest element. Returns null if the queue is empty.
    public synchronized byte[] peek() throws IOException {
        if (isEmpty()) return null;
        int length = first.length;
        byte[] data = new byte[length];
        ringRead(first.position + Element.HEADER_LENGTH, data, 0, length);
        return data;
    }

    // don't need
    public synchronized void peek(ElementReader reader) throws IOException {
        if (elementCount > 0) {
            reader.read(new ElementInputStream(first), first.length);
        }
    }

    // all element
    public synchronized void forEach(ElementReader reader) throws IOException {
        int position = first.position;
        for (int i = 0; i < elementCount; i++) {
            Element current = readElement(position);
            reader.read(new ElementInputStream(current), current.length);
            position = wrapPosition(current.position + Element.HEADER_LENGTH + current.length);
        }
    }

    //check not null
    private static <T> T nonNull(T t, String name) {
        if (t == null) throw new NullPointerException(name);
        return t;
    }

    // stream
    private final class ElementInputStream extends InputStream {
        private int position;
        private int remaining;

        private ElementInputStream(Element element) {
            position = wrapPosition(element.position + Element.HEADER_LENGTH);
            remaining = element.length;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            nonNull(buffer, "buffer");
            if ((offset | length) < 0 || length > buffer.length - offset) {
                throw new ArrayIndexOutOfBoundsException();
            }
            if (remaining > 0) {
                if (length > remaining) length = remaining;
                ringRead(position, buffer, offset, length);
                position = wrapPosition(position + length);
                remaining -= length;
                return length;
            } else {
                return -1;
            }
        }

        @Override
        public int read() throws IOException {
            if (remaining == 0) return -1;
            raf.seek(position);
            int b = raf.read();
            position = wrapPosition(position + 1);
            remaining--;
            return b;
        }
    }

    public synchronized int size() {
        return elementCount;
    }

    //remove the eldest one
    public synchronized void remove() throws IOException {
        if (isEmpty()) throw new NoSuchElementException();
        if (elementCount == 1) {
            clear();
        } else {
            int firstTotalLength = Element.HEADER_LENGTH + first.length;

            ringErase(first.position, firstTotalLength);

            int newFirstPosition = wrapPosition(first.position + firstTotalLength);
            ringRead(newFirstPosition, buffer, 0, Element.HEADER_LENGTH);
            int length = readInt(buffer, 0);
            writeHeader(fileLength, elementCount - 1, newFirstPosition, last.position);
            elementCount--;
            first = new Element(newFirstPosition, length);
        }
    }

    // clear queen
    public synchronized void clear() throws IOException {
        raf.seek(0);
        raf.write(EMPTY);
        writeHeader(INITIAL_LENGTH, 0, 0, 0);
        elementCount = 0;
        first = Element.NULL;
        last = Element.NULL;
        if (fileLength > INITIAL_LENGTH) setLength(INITIAL_LENGTH);
        fileLength = INITIAL_LENGTH;
    }

    //close random access
    public synchronized void close() throws IOException {
        raf.close();
    }

    @Override public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName()).append('[');
        builder.append("fileLength=").append(fileLength);
        builder.append(", size=").append(elementCount);
        builder.append(", first=").append(first);
        builder.append(", last=").append(last);
        builder.append(", element lengths=[");
        try {
            forEach(new ElementReader() {
                boolean first = true;

                @Override public void read(InputStream in, int length) throws IOException {
                    if (first) {
                        first = false;
                    } else {
                        builder.append(", ");
                    }
                    builder.append(length);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        builder.append("]]");
        return builder.toString();
    }


    static class Element {
        /*
        *   Element:
        *     Length (4 bytes)
        *     Data   (Length bytes)
        */

        //length of header
        static final int HEADER_LENGTH = 4;

        /** Null element. */
        static final Element NULL = new Element(0, 0);

        final int position;

        final int length;


        Element(int position, int length) {
            this.position = position;
            this.length = length;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "["
                    + "position = " + position
                    + ", length = " + length + "]";
        }
    }

    //used for foreach iterator
    public interface ElementReader {

        /**
         * Called once per element.
         *
         * @param in
         * @param length
         */
        void read(InputStream in, int length) throws IOException;
    }
}

