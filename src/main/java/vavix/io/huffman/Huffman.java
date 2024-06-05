/**
 * Huffman.java -- Huffman符号化
 *
 * @version $Revision: 1.18 $, $Date: 2003/03/22 02:37:57 $
 * BitInputStream, BitOutputStreamを使う
 */

package vavix.io.huffman;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import static java.lang.System.getLogger;


/**
 * Huffman.
 *
 * @author Haruhiko Okumura
 * @see "http://oku.edu.mie-u.ac.jp/~okumura/compression/huffman/index.html"
 */
public class Huffman {

    private static final Logger logger = getLogger(Huffman.class.getName());

    /** size of alphabet (character = 0..N-1) */
    static final int N = 256;

    private int heapSize, avail;

    /** heap for queue */
    private int[] heap = new int[2 * N - 1];

    /** data structure of Huffman tree */
    private int[] parent = new int[2 * N - 1];

    /** ditto */
    private int[] left = new int[2 * N - 1];

    /** ditto */
    private int[] right = new int[2 * N - 1];

    /** letter frequency */
    private int[] freq = new int[2 * N - 1];

    /** insert primary queue */
    private void downHeap(int i) {
        int j, k = heap[i];

        while ((j = 2 * i) <= heapSize) {
            if (j < heapSize && freq[heap[j]] > freq[heap[j + 1]]) {
                j++;
            }
            if (freq[k] <= freq[heap[j]]) {
                break;
            }
            heap[i] = heap[j];
            i = j;
        }
        heap[i] = k;
    }

    /** output Huffman tree */
    private void writeTree(BitOutputStream out, int i) throws IOException {
        if (i < N) { // leaf
            out.putBit(false);
            out.putBits(8, i); // the letter
        } else { // edge
            out.putBit(true);
            writeTree(out, left[i]); // left branch
            writeTree(out, right[i]); // right branch
        }
    }

    /** Encodes bytes */
    public byte[] encode(byte[] data) throws IOException {
        boolean[] codeBit = new boolean[N]; // Codeword

        for (int i = 0; i < N; i++) {
            freq[i] = 0; // Frequency Initialization
        }
        for (byte b : data) {
            freq[b & 0xff]++; // Frequency counting
        }
        heap[1] = 0; // Prepare for zero-length files
        heapSize = 0;
        for (int i = 0; i < N; i++) {
            if (freq[i] != 0) {
                heap[++heapSize] = i; // Join the priority queue
            }
        }
        for (int i = heapSize / 2; i >= 1; i--) {
            downHeap(i); // Heap Creation
        }
        int k = heap[1]; // In case the following loop never executes
        avail = N; // Create a Huffman tree using the following loop:
        while (heapSize > 1) { // While there are 2 or more remaining
            int i = heap[1]; // Take the smallest element
            heap[1] = heap[heapSize--];
            downHeap(1); // Heap Reorganization
            int j = heap[1]; // Take the next smallest element
            k = avail++; // Create a new section
            freq[k] = freq[i] + freq[j]; // Sum the frequency
            heap[1] = k;
            downHeap(1); // Join the queue
            parent[i] = k;
            parent[j] = -k; // Make a tree
            left[k] = i;
            right[k] = j; // ditto
        }
        parent[k] = 0; // root
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitOutputStream out = new BitOutputStream(baos);
        out.putBits(BitOutputStream.MAX_BITS, freq[k]); // Output the number of bytes in the input file
        writeTree(out, k); // Print the tree
int tableSize = out.outCount(); // Table size
int inCount = 0;
        for (byte b : data) {
            k = 0;
            int j = b & 0xff;
            while ((j = parent[j]) != 0) {
                if (j > 0) {
                    codeBit[k++] = false;
                } else {
                    codeBit[k++] = true;
                    j = -j;
                }
            }
            while (--k >= 0) {
//if (out.outCount() >= 0x7949) {
//    logger(Level.DEBUG, out.outCount() + "/" + data.length + ", " + (int) b + ", " + k); // Reporting the Results
//}
                out.putBit(codeBit[k]);
            }
if (logger.isLoggable(Level.DEBUG)) {
 if ((++inCount & 1023) == 0) {
  System.err.print('.'); // Status Report
 }
}
        }
if (logger.isLoggable(Level.DEBUG)) {
 System.err.println();
}
logger.log(Level.DEBUG, "In : " + inCount + " bytes"); // Reporting the Results
logger.log(Level.DEBUG, "Out: " + out.outCount() + " bytes (table: " + tableSize + " bytes)");
if (inCount != 0) { // Determine and report compression ratio
 long cr = (1000L * out.outCount() + inCount / 2) / inCount;
 logger.log(Level.DEBUG, "Out/In: " + (cr / 1000) + "." + (cr % 1000));
}
        out.flush();
        out.close();
        return baos.toByteArray();
    }

    /** Reading Huffman trees */
    private int readTree(BitInputStream in) throws IOException {
        if (in.getBit()) { // bit=1: Non-leaf nodes
            int i;
            if ((i = avail++) >= 2 * N - 1) {
                throw new IllegalStateException("The table is incorrect");
            }
            left[i] = readTree(in); // Read the left branch
            right[i] = readTree(in); // Read the right branch
            return i; // Return the clause
        } else
            return in.getBits(8); // character
    }

    /** Decodes bytes */
    public byte[] decode(byte[] data) throws IOException {
        InputStream is = new ByteArrayInputStream(data);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        decode(is, os);
        is.close();
        os.close();
        return os.toByteArray();
    }

    /** Encodes streams */
    public void encode(InputStream is, OutputStream os) throws IOException {
        BufferedInputStream in = new BufferedInputStream(is);
        byte[] encoded = encode(toBytes(in));
logger.log(Level.DEBUG, "encoded: " + encoded.length + " bytes");
        os.write(encoded);
        os.flush();
    }

    /** */
    private static byte[] toBytes(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        while (true) {
            int r = in.read(buf);
            if (r < 0) {
                break;
            }
            baos.write(buf, 0, r);
        }
        return baos.toByteArray();
    }

    /* Decryption */
    public void decode(InputStream is, OutputStream os) throws IOException {
        BitInputStream in = new BitInputStream(new BufferedInputStream(is));
        BufferedOutputStream out = new BufferedOutputStream(os);
        int size = in.getBits(BitInputStream.MAX_BITS); // Original number of bytes
        avail = N;
        int root = readTree(in); // Reading Trees
        for (int k = 0; k < size; k++) { // Decode each character
            int j = root; // root
            while (j >= N) {
                if (in.getBit()) {
                    j = right[j];
                } else {
                    j = left[j];
                }
            }
            out.write(j);
if (logger.isLoggable(Level.DEBUG)) {
 if ((k & 1023) == 0)
  System.err.print('.');
}
        }
if (logger.isLoggable(Level.DEBUG)) {
 System.err.println();
}
logger.log(Level.DEBUG, "Out: " + size + " bytes"); // Number of decrypted bytes
        out.flush();
    }
}
