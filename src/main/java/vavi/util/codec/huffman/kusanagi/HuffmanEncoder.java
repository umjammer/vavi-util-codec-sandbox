/*
 * Copyright (c) 2003 by Tomonori Kusanagi, All rights reserved.
 *
 * Programmed by Tomonori Kusanagi
 */

package vavi.util.codec.huffman.kusanagi;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;


/**
 * HuffmanEncoder.
 *
 * @author Tomonori Kusanagi
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 1.00 020330 T.K initial version <br>
 *          2.00 031001 nsano refine <br>
 */
public class HuffmanEncoder {
    /**
     * Huffman encoding
     */
    public byte[] encode(byte[] data) throws IOException {
        int[] freq = new int[256];

        // Frequency array
        for (int i = 0; i < 256; i++) {
            freq[i] = 0;
        }
        for (byte b : data) {
            freq[b + 128]++;
        }

// Test Output
//for (int i = 0; i < 256; i++) {
// System.out.print(freq[i] + " ");
// if (i % 16 == 15)
//  System.out.println();
//}

        // Create a Huffman tree
        int[] parent = new int[512];
        int[] l_node = new int[512];
        int[] r_node = new int[512];
        buildTree(freq, parent, l_node, r_node);

// Test Output
//for (int i = 0; i < 512 - 1; i++)
// System.out.println("[" + i + "] l:" + l_node[i] + " r:" + r_node[i] + " p:" + parent[i]);

        // Create a code
        int n;
        byte[][] code = new byte[256][];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < 256; i++) {
            if (freq[i] == 0) {
                continue;
            }

            // Initialization
            n = i;
            baos.reset();
            while (parent[n] != -1) {
                if (parent[n] < 0) {
                    baos.write(1);
                } else {
                    baos.write(0);
                }
                n = Math.abs(parent[n]);
            }

            byte[] rev_code = baos.toByteArray();
            code[i] = new byte[rev_code.length];

            // Invert
            for (int j = 0; j < rev_code.length; j++) {
                code[i][j] = rev_code[rev_code.length - j - 1];
            }
        }

// Test Output
//for (int i = 0; i < 256; i++) {
// System.out.print("["+i+"]:"+freq[i]+" - ");
// if (freq[i] > 0) {
//  for (int j = 0; j < code[i].length; j++) {
//   System.out.print(code[i][j]);
//  }
// }
// System.out.println();
//}

        // Get the sign
        baos.reset();

        for (byte datum : data) {
            baos.write(code[datum + 128], 0, code[datum + 128].length);

//n = data[i] + 128;
//for (int j = 0; j < code[n].length; j++)
// System.out.print(code[n][j]);
        }

        byte[] tmp = baos.toByteArray();

        byte[] huff = new byte[(tmp.length / 8) + 1];
        for (int i = 0; i < huff.length; i++) {
            // Create encoded byte sequence
            huff[i] = 0;
            for (int j = 0; j < 8; j++) {
                huff[i] = (byte) (huff[i] << 1);
                if (((i * 8) + j) < tmp.length) {
                    huff[i] = (byte) (huff[i] + tmp[(i * 8) + j]);
                }
            }
        }

        baos.reset();

        DataOutputStream dos = new DataOutputStream(baos);

        // Encoding information
        // type 0:
        //  Contains the occurrence frequency of all elements in int type
        //   4[byte] * 256 = 1024[byte]
        // type 1:
        //  Contains the element number and occurrence frequency (int type)
        //  for only elements with a non-zero occurrence frequency
        //   1[byte] + (1+4)[byte] * 256
        // Encoding information data volume for type 1
        n = 0;
        for (int i = 0; i < 256; i++) {
            if (freq[i] != 0) {
                n++;
            }
        }

        // Automatically choose the option that requires less data
        int type;
        if ((1 + (n * 5)) > (256 * 4)) {
            type = 0;
        } else {
            type = 1;
        }

//System.out.println("type : " + type);
        if (type == 0) {
            dos.writeByte(0); // Code Information Type
            for (int i = 0; i < 256; i++) {
                dos.writeInt(freq[i]);
            }
        } else if (type == 1) {
            dos.writeByte(1);
            dos.writeByte(n - 128);
            for (int i = 0; i < 256; i++) {
                if (freq[i] != 0) {
                    dos.writeByte(i - 128);
                    dos.writeInt(freq[i]);
                }
            }
        } else {
            throw new IllegalArgumentException("Encoding information number: " + type);
        }

        // Number of characters
        dos.writeInt(data.length);

        // Encoded Data
        dos.write(huff, 0, huff.length);

        dos.close();

        byte[] out = baos.toByteArray();

        // Test Output
        int prep = data.length;
        int postp = out.length;
System.err.println(prep + " -> " + postp + ": " + ((postp * 100) / prep) + "%");

        return out;
    }

    /**
     * Creating a Huffman tree
     */
    private static void buildTree(int[] freq, int[] parent, int[] l_node, int[] r_node) {
        int[] freq_node = new int[512];

        // Initialization
        for (int i = 0; i < 512; i++) {
            parent[i] = -1;
            l_node[i] = -1;
            r_node[i] = -1;
            if (i < 256) {
                freq_node[i] = freq[i];
            } else {
                freq_node[i] = 0;
            }
        }

        // Create a Huffman tree
        int minId;
        for (int i = 256; i < (512 - 1); i++) {
            // Find the smallest element without a parent → new left node
            minId = findSmallest(i, freq_node, parent);
            l_node[i] = minId;
            parent[minId] = -i;
            freq_node[i] = freq_node[minId];

            // Find the smallest element without a parent → new right node
            minId = findSmallest(i, freq_node, parent);
            r_node[i] = minId;
            parent[minId] = i;
            freq_node[i] += freq_node[minId];
        }
    }

    /**
     * Finds the smallest orphaned element in an array and returns its index.
     */
    private static int findSmallest(int n, int[] freq_node, int[] parent) {
        int min = -1;
        int minId = -1;

        for (int i = 0; i < n; i++) {
            if (parent[i] != -1) {
                continue;
            }

            if ((minId == -1) || ((minId != -1) && (freq_node[i] < min))) {
                minId = i;
                min = freq_node[i];
            }
        }
        return minId;
    }
}
