/*
 * Copyright (c) 2003 by Tomonori Kusanagi, All rights reserved.
 *
 * Programmed by Tomonori Kusanagi
 */

package vavi.util.codec.huffman.kusanagi;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;


/**
 * HuffmanDecoder.
 *
 * @author Tomonori Kusanagi
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 1.00 020330 T.K initial version <br>
 *          2.00 031001 nsano refine <br>
 */
public class HuffmanDecoder {

    /** */
    public byte[] decode(byte[] data) throws IOException {
        byte[] infr;

        // Huffman tree array
        int[] parent = new int[512];
        int[] l_node = new int[512];
        int[] r_node = new int[512];
        int[] freq = new int[512];

        // Initialization
        for (int i = 0; i < 512; i++) {
            parent[i] = -1;
            l_node[i] = -1;
            r_node[i] = -1;
            freq[i] = 0;
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);

        // Data Import

        // Code Information Type
        int type = dis.readByte();

        // Frequency array
        if (type == 0) {
            for (int i = 0; i < 256; i++) {
                freq[i] = dis.readInt();
            }
        } else if (type == 1) {
            type = dis.readByte() + 128;
            for (int i = 0; i < type; i++) {
                //int b = dis.readByte() + 128;
                freq[dis.readByte() + 128] = dis.readInt();
            }
        }

        // Data Length
        int len = dis.readInt();

        // Creating a Huffman Tree
        // Create a Huffman tree
        int target;

        // Creating a Huffman Tree
        // Create a Huffman tree
        int min;
        for (int i = 256; i < (512 - 1); i++) {
            for (int j = 0; j < 2; j++) {
                // Find the smallest element without a parent → new node
                min = -1;
                target = -1;

                for (int k = 0; k < i; k++) {
                    if (parent[k] != -1) {
                        continue;
                    }

                    if ((target == -1) || ((target != -1) && (freq[k] < min))) {
                        target = k;
                        min = freq[k];
                    }
                }

                if (j == 0) {
                    l_node[i] = target;
                    parent[target] = -i;
                    freq[i] = freq[target];
                } else {
                    r_node[i] = target;
                    parent[target] = i;
                    freq[i] += freq[target];
                }
            }
        }

        // Unzip
        infr = new byte[len];

        int curr_byte;
        byte rest_bits = 0;
        byte[] bits = new byte[8];
        byte curr_bit;

        for (int i = 0; i < len; i++) {
            target = 510; // Start from 510 (root node)
            while (true) {
                // Get the target bit
                if ((rest_bits % 8) == 0) {
                    // Read one byte and break it down into bits
                    curr_byte = dis.readByte();
                    for (int j = 0; j < 8; j++) {
                        bits[j] = (byte) (curr_byte % 2);
                        curr_byte >>= 1;
                    }
                    rest_bits = 8;
                }
                rest_bits--;
                curr_bit = (byte) Math.abs(bits[rest_bits]);

                if (curr_bit == 1) {
                    target = l_node[target];
                } else {
                    target = r_node[target];
                }

                if (l_node[target] < 0) {
                    break;
                }
            }

            infr[i] = (byte) (target - 128);
        }
        dis.close();

        return infr;
    }
}
