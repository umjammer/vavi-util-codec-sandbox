/*
 * Wavelet Audio Compression
 *
 * http://www.toblave.org/soundcompression/
 */

package vavi.util.codec.huffman.eaglejones;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * BitInputStream.
 *
 * @author Eagle Jones
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 080516 nsano initial version <br>
 */
class BitInputStream extends FilterInputStream {

    /** */
    private int buffer = 0;

    /** */
    private int bitsavail = 0;

    /** */
    public BitInputStream(InputStream in) {
        super(in);
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    /** read a single byte */
    @Override
    public int read() throws IOException {
        buffer = (buffer << 8) | in.read();
        return (buffer >> bitsavail) & 0xFF;

    }

    /** */
    public int readBit() throws IOException {
        if (bitsavail == 0) {
            buffer = in.read();
            bitsavail = 8;
        }
        bitsavail--;
        return (buffer >> bitsavail) & 1;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        bitsavail = 0;
        return in.read(b, off, len);
    }

    /** */
    public int read(int length) throws IOException {
        int l = length;
        while (l > bitsavail) {
            buffer = (buffer << 8) | in.read();
            bitsavail += 8;
        }
        bitsavail -= length;
        return (buffer >> bitsavail) & ((1 << length) - 1);
    }
}
