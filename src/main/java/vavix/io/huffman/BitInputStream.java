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
 * Huffman法などで使うビット入力ルーチンです。
 * 
 * @see BitOutputStream
 * @see Huffman
 */
class BitInputStream extends FilterInputStream {

    /** 読み込み可能な最大ビット数 */
    public static final int MAX_BITS = 31;
    /** ビット入力カウンタ */
    protected int getCount = 0;
    /** ビット入力バッファ */
    protected int bitBuf = 0;

    /**
     * コンストラクタ
     * 
     * @param in 入力ストリーム
     */
    public BitInputStream(InputStream in) {
        super(in);
    }

    /**
     * x の右側 n ビットを返す
     */
    private static int rightBits(int n, int x) {
        return x & ((1 << n) - 1);
    }

    /**
     * 1ビットを読み込み
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
     * nビットを読み込み
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
