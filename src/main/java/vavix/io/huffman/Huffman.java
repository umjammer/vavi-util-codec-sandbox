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
import java.util.logging.Level;

import vavi.util.Debug;


/**
 * Huffman.
 *
 * @author Haruhiko Okumura
 * @see "http://oku.edu.mie-u.ac.jp/~okumura/compression/huffman/index.html"
 */
public class Huffman {

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
        boolean[] codeBit = new boolean[N]; // 符号語

        for (int i = 0; i < N; i++) {
            freq[i] = 0; // 頻度の初期化
        }
        for (byte b : data) {
            freq[b & 0xff]++; // 頻度数え
        }
        heap[1] = 0; // 長さ0のファイルに備える
        heapSize = 0;
        for (int i = 0; i < N; i++) {
            if (freq[i] != 0) {
                heap[++heapSize] = i; // 優先待ち行列に登録
            }
        }
        for (int i = heapSize / 2; i >= 1; i--) {
            downHeap(i); // ヒープ作り
        }
        int k = heap[1]; // 以下のループが1回も実行されない場合に備える
        avail = N; // 以下のループでHuffman木を作る
        while (heapSize > 1) { // 2個以上残りがある間
            int i = heap[1]; // 最小の要素を取り出す
            heap[1] = heap[heapSize--];
            downHeap(1); // ヒープ再構成
            int j = heap[1]; // 次に最小の要素を取り出す
            k = avail++; // 新しい節を生成する
            freq[k] = freq[i] + freq[j]; // 頻度を合計
            heap[1] = k;
            downHeap(1); // 待ち行列に登録
            parent[i] = k;
            parent[j] = -k; // 木を作る
            left[k] = i;
            right[k] = j; // ditto
        }
        parent[k] = 0; // root
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitOutputStream out = new BitOutputStream(baos);
        out.putBits(BitOutputStream.MAX_BITS, freq[k]); // 入力ファイルのバイト数を出力
        writeTree(out, k); // 木を出力
int tableSize = out.outCount(); // 表の大きさ
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
//    Debug.println(Level.FINE, out.outCount() + "/" + data.length + ", " + (int) b + ", " + k); // 結果報告
//}
                out.putBit(codeBit[k]);
            }
if (Debug.isLoggable(Level.FINE)) {
 if ((++inCount & 1023) == 0) {
  System.err.print('.'); // 状況報告
 }
}
        }
if (Debug.isLoggable(Level.FINE)) {
 System.err.println();
}
Debug.println(Level.FINE, "In : " + inCount + " bytes"); // 結果報告
Debug.println(Level.FINE, "Out: " + out.outCount() + " bytes (table: " + tableSize + " bytes)");
if (inCount != 0) { // 圧縮比を求めて報告
 long cr = (1000L * out.outCount() + inCount / 2) / inCount;
 Debug.println(Level.FINE, "Out/In: " + (cr / 1000) + "." + (cr % 1000));
}
        out.flush();
        out.close();
        return baos.toByteArray();
    }

    /** Huffman木を読む */
    private int readTree(BitInputStream in) throws IOException {
        if (in.getBit()) { // bit=1: 葉でない節
            int i;
            if ((i = avail++) >= 2 * N - 1) {
                throw new IllegalStateException("表が間違っています");
            }
            left[i] = readTree(in); // 左の枝を読む
            right[i] = readTree(in); // 右の枝を読む
            return i; // 節を返す
        } else
            return in.getBits(8); // 文字
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
Debug.println("encoded: " + encoded.length + " bytes");
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

    /* 復号 */
    public void decode(InputStream is, OutputStream os) throws IOException {
        BitInputStream in = new BitInputStream(new BufferedInputStream(is));
        BufferedOutputStream out = new BufferedOutputStream(os);
        int size = in.getBits(BitInputStream.MAX_BITS); // 元のバイト数
        avail = N;
        int root = readTree(in); // 木を読む
        for (int k = 0; k < size; k++) { // 各文字を復号
            int j = root; // 根
            while (j >= N) {
                if (in.getBit()) {
                    j = right[j];
                } else {
                    j = left[j];
                }
            }
            out.write(j);
if (Debug.isLoggable(Level.FINE)) {
 if ((k & 1023) == 0)
  System.err.print('.');
}
        }
if (Debug.isLoggable(Level.FINE)) {
 System.err.println();
}
Debug.println(Level.FINE, "Out: " + size + " bytes"); // 復号したバイト数
        out.flush();
    }
}
