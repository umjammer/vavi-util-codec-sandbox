/*
 * Copyright (c) 2012 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.huffman;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.junit.jupiter.api.Test;

import vavix.util.Checksum;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * HuffmanTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2012/10/10 umjammer initial version <br>
 */
public class HuffmanTest {

    static final String text = "文字データだけで構成されたファイル。どんな機種のコンピュータでも共通して利用できる数少ないファイル形式の一つ。" +
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
        Huffman encoder = new Huffman();
        byte[] encoded = encoder.encode(text.getBytes(encoding));
System.err.println("encoded: " + encoded.length);

        Huffman decoder = new Huffman();
        byte[] decoded = decoder.decode(encoded);
System.err.println("decoded: " + decoded.length);
System.err.print(new String(decoded, encoding));
        assertEquals(text, new String(decoded, encoding));
    }

    @Test
    public void test2() throws Exception {
        String file = "/tmp/test2.txt";
        Writer writer = new FileWriter(file);
        writer.write(text);
        writer.flush();
        writer.close();
System.err.println("original: " + new File(file).length());

        String encoded = "/tmp/test2.enc";
        Huffman encoder = new Huffman();
        encoder.encode(file, encoded);
System.err.println("encoded: " + new File(encoded).length());

        String decoded = "/tmp/test2.dec";
        Huffman decoder = new Huffman();
        decoder.decode(encoded, decoded);
System.err.println("decoded: " + new File(decoded).length());
        assertEquals(Checksum.getChecksum(new File(file)), Checksum.getChecksum(new File(decoded)));
    }

    @Test
    public void test3() throws Exception {
        String actualDecoded = "src/test/resources/data.dec";
        String decoded = "/tmp/test3.dec";
        String encoded = "src/test/resources/data.enc";
        Huffman decoder = new Huffman();
        decoder.decode(encoded, decoded);
        assertEquals(Checksum.getChecksum(new File(actualDecoded)), Checksum.getChecksum(new File(decoded)));

        String actualEncoded = "src/test/resources/data.enc";
        decoded = "src/test/resources/data.dec";
        encoded = "/tmp/test3.enc";
        Huffman encoder = new Huffman();
        encoder.encode(decoded, encoded);
        assertEquals(Checksum.getChecksum(new File(actualEncoded)), Checksum.getChecksum(new File(encoded))); // TODO failed

        decoded = "/tmp/test3.dec";
        encoded = "/tmp/test3.enc";
        decoder.decode(encoded, decoded);
        assertEquals(Checksum.getChecksum(new File(actualDecoded)), Checksum.getChecksum(new File(decoded)));
    }

    /* メイン */
    public static void main(String[] args) throws IOException {
        if (args.length != 3 || !(args[0].equals("e") || args[0].equals("d"))) {
            System.err.println("使用法: java Huffman (e|d) infile outfile");
            System.exit(-1);
        }
        Huffman huf = new Huffman();
        if (args[0].equals("e"))
            huf.encode(args[1], args[2]); // 符号化
        else
            huf.decode(args[1], args[2]); // 復号
    }
}

/* */
