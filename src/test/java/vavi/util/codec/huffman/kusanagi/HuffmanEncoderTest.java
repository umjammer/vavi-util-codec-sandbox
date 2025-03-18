/*
 * Copyright (c) 2012 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.codec.huffman.kusanagi;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * HuffmanEncoderTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2012/09/27 umjammer initial version <br>
 */
public class HuffmanEncoderTest {

    /** */
    public static void main(String[] args) throws IOException {
        String inFile = null;

//      inFile = "projects/Huffman/figure16.bmp";
        if (args.length != 0) {
            inFile = args[0];
        } else {
            throw new IllegalArgumentException("Missing Arguments");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataInputStream dis = new DataInputStream(new FileInputStream(inFile));

        int len;
        byte[] buff = new byte[1024];

        while ((len = dis.read(buff, 0, 1024)) != -1) {
            baos.write(buff, 0, len);
        }
        dis.close();

        byte[] data = baos.toByteArray();

// Test Output
//int min = data[0], max = data[0];
//for (int i = 0; i < data.length; i++) {
// if (data[i] > max)
//  max = data[i];
// if (data[i] < min)
//  min = data[i];
// System.out.print(data[i] + " ");
// if (i % 16 == 0)
//  System.out.println();
//}
//System.out.println("size: " + data.length);
//System.out.println("max: " + max);
//System.out.println("min: " + min);

        HuffmanEncoder enc = new HuffmanEncoder();
        byte[] encoded = enc.encode(data);

        // Result output
        String outFile = inFile + ".hff";
        FileOutputStream fos = new FileOutputStream(outFile);
        fos.write(encoded, 0, encoded.length);
        fos.close();
    }
}
