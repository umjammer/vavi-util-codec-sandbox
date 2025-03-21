// Reference: http://oku.edu.mie-u.ac.jp/~okumura/algo/

package vavi.util.codec.huffman.claude;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.lang.System.getLogger;


/**
 * this is equivalent for {@code vavi.util.codec.hufman.okumura} when lengthBits is 32
 * @see "https://claude.ai/chat/edc33e58-38f7-4ace-9e13-2012cbb02de6"
 */
public class HuffmanReader extends InputStream {

    private static final Logger logger = getLogger(HuffmanDecoder.class.getName());

    /** */
    static class HuffmanDecoder {

        private static final int N = 256;

        private final BitReader reader;
        private int avail;
        private final int[] left = new int[2 * N - 1];
        private final int[] right = new int[2 * N - 1];

        public HuffmanDecoder(InputStream reader) {
            this.reader = new BitReader(reader);
        }

        private int readTree() throws IOException {
            boolean bit = reader.readBit();
//logger.log(Level.TRACE, String.valueOf(bit));

            if (bit) {
                int i = avail;
                avail++;
                if (2 * N - 1 <= i) {
                    throw new IOException("Invalid huffman table");
                }
                left[i] = readTree(); // read left branch
                right[i] = readTree(); // read right branch
                return i; // return node
            } else {
                byte value = reader.readUint8();
                return value & 0xff; // return leaf
            }
        }

        public int read(byte[] p) throws IOException {
            avail = 256;
            int root = readTree();

            int size = p.length;
//logger.log(Level.TRACE, "left: " + Arrays.toString(left));
//logger.log(Level.TRACE, "right: " + Arrays.toString(right));
//logger.log(Level.TRACE, "size: " + size);

            for (int k = 0; k < size; k++) {
                int j = root;
                while (N <= j) {
                    boolean b = reader.readBit();
                    if (b) {
                        j = right[j];
                    } else {
                        j = left[j];
                    }
                }
                p[k] = (byte) j;
            }
            return size;
        }
    }

    private final InputStream reader;
    private final HuffmanDecoder decoder;
    private byte[] buf;
    private int position = 0;

    public HuffmanReader(InputStream rdr) {
        this.reader = rdr;
        this.decoder = new HuffmanDecoder(rdr);
    }

    private void cache() throws IOException {
        if (buf != null) {
            return;
        }
logger.log(Level.TRACE, "Decompressing huffman code");

        // Read size as big-endian uint32
        byte[] sizeBytes = new byte[4];
        if (reader.read(sizeBytes) != 4) {
            throw new IOException("Couldn't read size");
        }

        int size = ByteBuffer.wrap(sizeBytes).order(ByteOrder.BIG_ENDIAN).getInt() & 0x7fff_ffff;
logger.log(Level.TRACE, "size: %04x".formatted(size));
        buf = new byte[size];
        try {
            decoder.read(buf);
        } catch (IOException e) {
            logger.log(Level.TRACE, e.getMessage());
        }
    }

    public int getRest() throws IOException {
        cache();
        return buf.length - position;
    }

    @Override
    public int read(byte[] p) throws IOException {
        cache();

        int size = p.length;
        boolean eof = false;

        if (buf.length - position < size) {
            size = buf.length - position;
            eof = true;
        }

        System.arraycopy(buf, position, p, 0, size);
        position += size;

        return eof ? -1 : size;
    }

    @Override
    public int read() throws IOException {
        byte[] oneByte = new byte[1];
        int bytesRead = read(oneByte);

        if (bytesRead == -1) {
            return -1;
        }

        return oneByte[0] & 0xff;
    }
}