/**
 * BitInputStream.java -- ビット入力ストリーム
 *
 * @version $Revision: 1.7 $, $Date: 2003/03/22 02:31:14 $
 */

package vavix.io.huffman;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * This is a bit input routine used in the Huffman method, etc.
 * 
 * @see BitOutputStream
 * @see Huffman
 */
class BitInputStream extends FilterInputStream {

    /** Maximum number of bits that can be read */
    public static final int MAX_BITS = 31;
    /** Bit Input Counter */
    protected int getCount = 0;
    /** Bit Input Buffer */
    protected int bitBuf = 0;

    /**
     * Constructor
     * 
     * @param in Input Stream
     */
    public BitInputStream(InputStream in) {
        super(in);
    }

    /**
     * Returns the right n bits of x
     */
    private static int rightBits(int n, int x) {
        return x & ((1 << n) - 1);
    }

    /**
     * Read 1 bit
     */
    public boolean getBit() throws IOException {
        if (--getCount >= 0) {
            return ((bitBuf >>> getCount) & 1) == 1;
        }
        getCount = 7;
        bitBuf = in.read();
        return ((bitBuf >>> 7) & 1) == 1;
    }

    /**
     * Read n bits
     */
    public int getBits(int n) throws IOException {
        int x = 0;
        while (n > getCount) {
            n -= getCount;
            x |= rightBits(getCount, bitBuf) << n;
            bitBuf = in.read();
            getCount = 8;
        }
        getCount -= n;
        return x | rightBits(n, bitBuf >>> getCount);
    }
}
