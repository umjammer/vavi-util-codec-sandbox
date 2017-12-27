/*
 * Copyright (c) 2012 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.codec.huffman;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;

import vavi.util.codec.huffman.HuffmanDecoder;
import vavi.util.codec.huffman.HuffmanEncoder;

import static org.junit.Assert.assertEquals;


/**
 * HuffmanDecoderTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2012/09/27 umjammer initial version <br>
 */
public class HuffmanDecoderTest {

    static String text = "文字データだけで構成されたファイル。どんな機種のコンピュータでも共通して利用できる数少ないファイル形式の一つ。" +
"ワープロソフトなどで文書を作成した際には、テキストファイルに変換すれば、他の機種やソフトウェアでもそのデータを利用することができる。" +
"ただし、テキストファイルは純粋に文字データのみで構成され、修飾情報や埋め込まれた画像など文字以外のデータは表現することができない。" +
"また、改行やタブなど、文章の作成に必要なものを除いて、制御コードを含むことはできない。このため、他の形式から変換する際には注意が必要。" +
"テキストファイルの編集のみを行うためのソフトウェアをテキストエディタという。" +
"HTMLファイルなどはコンピュータが解析してレイアウトできるようにタグ(付加情報)が埋め込まれているが、タグ自体は通常の文字コードの範囲内で表現されているため、HTMLファイルもテキストファイルの一種と言える。" +
"ただし、HTMLファイルはテキストエディタで開いたときとWebブラウザで開いたときに見え方がまったく異なるため、文章だけで構成されるファイルとは区別して扱う必要がある。";

    @Test
    public void test() throws Exception {
        final String encoding = "utf-8";
System.err.println("original: " + text.getBytes(encoding).length);
        HuffmanEncoder encoder = new HuffmanEncoder();
        byte[] encoded = encoder.encode(text.getBytes(encoding));
System.err.println("encoded: " + encoded.length);

        HuffmanDecoder decoder = new HuffmanDecoder();
        byte[] decoded = decoder.decode(encoded);
System.err.println("decoded: " + decoded.length);
System.err.print(new String(decoded, encoding));
        assertEquals(text, new String(decoded, encoding));
    }

    //----

    /** */
    public static void main(String[] args) {
        String inFile = null;

//      inFile = "projects/Huffman/figure32.bmp.hff";
        if (args.length != 0) {
            inFile = args[0];
        } else {
            System.err.println("引数がありません");
            System.exit(1);
        }

        DataInputStream dis = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            dis = new DataInputStream(new FileInputStream(inFile));

            int len = 0;
            byte[] buff = new byte[1024];

            while ((len = dis.read(buff, 0, 1024)) != -1) {
                baos.write(buff, 0, len);
            }
            dis.close();

            byte[] data = baos.toByteArray();

            HuffmanDecoder dec = new HuffmanDecoder();
            byte[] decoded = dec.decode(data);

            // 結果出力
            String outFile = inFile + ".dec";
            FileOutputStream fos = new FileOutputStream(outFile);
            fos.write(decoded, 0, decoded.length);
            fos.close();
        } catch (FileNotFoundException e) {
            System.err.println("そんなファイルありません");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}

/* */
