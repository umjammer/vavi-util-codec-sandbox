/**
 * BitOutputStream.java -- Bit Output Stream
 *
 * @version $Revision: 1.7 $, $Date: 2003/03/22 02:31:14 $
 */

package vavi.util.codec.huffman.okumura;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * This is a bit output routine used in the Huffman algorithm, etc.
 *
 * @see BitInputStream
 * @see Huffman
 */
class BitOutputStream extends FilterOutputStream {

    /** Maximum number of bits that can be written */
    public static final int MAX_BITS = 31;
    /** Bit Output Counter */
    protected int putCount = 8;
    /** Bit Output Buffer */
    protected int bitBuf = 0;
    /** Byte Output Counter */
    protected int outCount = 0;

    /**
     * Constructor
     *
     * @param out Output Stream
     */
    public BitOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * Returns the number of output bytes
     */
    public int outCount() {
        return outCount;
    }

    /**
     * Returns the right n bits of x
     */
    private static int rightBits(int n, int x) {
        return x & ((1 << n) - 1);
    }

    /**
     * Output 1 bit
     */
    public void putBit(boolean bit) throws IOException {
        putCount--;
        if (bit) {
            bitBuf |= (1 << putCount);
        }
        if (putCount == 0) {
            out.write(bitBuf);
            bitBuf = 0;
            putCount = 8;
            outCount++;
        }
    }

    /**
     * Output the right n bits of x
     */
    public void putBits(int n, int x) throws IOException {
        while (n >= putCount) {
            n -= putCount;
            bitBuf |= rightBits(putCount, x >>> n);
            out.write(bitBuf);
            bitBuf = 0;
            putCount = 8;
            outCount++;
        }
        putCount -= n;
        bitBuf |= rightBits(n, x) << putCount;
    }

    @Override
    public void close() throws IOException {
        putBits(7, 0);
        super.close(); // Flush the buffer
    }
}
