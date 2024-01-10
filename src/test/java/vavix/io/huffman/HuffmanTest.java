/*
 * Copyright (c) 2012 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.huffman;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import vavi.util.Debug;
import vavi.util.StringUtil;
import vavix.util.Checksum;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * HuffmanTest. (Okumura version)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2012/10/10 umjammer initial version <br>
 */
class HuffmanTest {

    static final String text = "文字データだけで構成されたファイル。どんな機種のコンピュータでも共通して利用できる数少ないファイル形式の一つ。" +
"ワープロソフトなどで文書を作成した際には、テキストファイルに変換すれば、他の機種やソフトウェアでもそのデータを利用することができる。" +
"ただし、テキストファイルは純粋に文字データのみで構成され、修飾情報や埋め込まれた画像など文字以外のデータは表現することができない。" +
"また、改行やタブなど、文章の作成に必要なものを除いて、制御コードを含むことはできない。このため、他の形式から変換する際には注意が必要。" +
"テキストファイルの編集のみを行うためのソフトウェアをテキストエディタという。" +
"HTMLファイルなどはコンピュータが解析してレイアウトできるようにタグ(付加情報)が埋め込まれているが、タグ自体は通常の文字コードの範囲内で表現されているため、HTMLファイルもテキストファイルの一種と言える。" +
"ただし、HTMLファイルはテキストエディタで開いたときとWebブラウザで開いたときに見え方がまったく異なるため、文章だけで構成されるファイルとは区別して扱う必要がある。";

    static Path tmp;

    @BeforeAll
    static void setup() throws IOException {
        tmp = Files.createDirectories(Paths.get("tmp"));
    }

    @Test
    void test() throws Exception {
Debug.println("original: " + text.getBytes().length);
        Huffman encoder = new Huffman();
        byte[] encoded = encoder.encode(text.getBytes());
Debug.println("encoded: " + encoded.length);

        Huffman decoder = new Huffman();
        byte[] decoded = decoder.decode(encoded);
Debug.println("decoded: " + decoded.length + "\n" + StringUtil.getDump(decoded, 64));
Debug.print(new String(decoded));
        assertEquals(text, new String(decoded));
    }

    @Test
    void test2() throws Exception {
        Path file = tmp.resolve("test2.txt");
        Writer writer = Files.newBufferedWriter(file);
        writer.write(text);
        writer.flush();
        writer.close();
Debug.println("original: " + Files.size(file));

        Path encoded = tmp.resolve("test2.enc");
        Huffman encoder = new Huffman();
        OutputStream os = Files.newOutputStream(encoded);
        encoder.encode(Files.newInputStream(file), os);
        os.close();
Debug.println("encoded: " + Files.size(encoded));

        Path decoded = tmp.resolve("test2.dec");
        Huffman decoder = new Huffman();
        os = Files.newOutputStream(decoded);
        decoder.decode(Files.newInputStream(encoded), os);
        os.close();
Debug.println("decoded: " + Files.size(decoded));
        assertEquals(Checksum.getChecksum(file), Checksum.getChecksum(decoded));
    }

    @Test
    void test3() throws Exception {
        Path original = Paths.get("src/test/resources/data.enc");
Debug.println("original: " + Files.size(original) + "\n" + StringUtil.getDump(new BufferedInputStream(Files.newInputStream(original)), 0, 64));
        Path expectedDecoded = Paths.get("src/test/resources/data.dec");
        Path actualDecoded = tmp.resolve("test3.dec");
        Huffman decoder = new Huffman();
        OutputStream os = Files.newOutputStream(actualDecoded);
        decoder.decode(Files.newInputStream(original), os);
        os.close();
Debug.println("actualDecoded: " + Files.size(actualDecoded) + "\n" + StringUtil.getDump(new BufferedInputStream(Files.newInputStream(actualDecoded)), 0, 64));
        assertEquals(Checksum.getChecksum(expectedDecoded), Checksum.getChecksum(actualDecoded));

        Path actualEncoded = tmp.resolve("test3.enc");
        Huffman encoder = new Huffman();
        os = Files.newOutputStream(actualEncoded);
        encoder.encode(Files.newInputStream(actualDecoded), Files.newOutputStream(actualEncoded));
        os.close();
Debug.println("actualEncoded: " + Files.size(actualEncoded) + "\n" + StringUtil.getDump(new BufferedInputStream(Files.newInputStream(actualEncoded)), 0, 64));
// after 0x7949 meaningless data -1 ...
//        assertEquals(Checksum.getChecksum(original), Checksum.getChecksum(actualEncoded)); // TODO failed

        Path actualDecoded2 = tmp.resolve("test3_2.dec");
        decoder = new Huffman();
        os = Files.newOutputStream(actualDecoded2);
        decoder.decode(Files.newInputStream(actualEncoded), os);
        os.close();
Debug.println("actualDecoded2: " + Files.size(actualDecoded2) + "\n" + StringUtil.getDump(new BufferedInputStream(Files.newInputStream(actualDecoded2)), 0, 64));
        assertEquals(Checksum.getChecksum(expectedDecoded), Checksum.getChecksum(actualDecoded2));
    }

    /**
     * @param args 0: 'e' or 'd', 1: in, 2: out
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 3 || !(args[0].equals("e") || args[0].equals("d"))) {
            System.err.println("使用法: java Huffman (e|d) infile outfile");
            System.exit(-1);
        }
        Huffman huf = new Huffman();
        if (args[0].equals("e"))
            huf.encode(Files.newInputStream(Paths.get(args[1])), Files.newOutputStream(Paths.get(args[2]))); // 符号化
        else
            huf.decode(Files.newInputStream(Paths.get(args[1])), Files.newOutputStream(Paths.get(args[2]))); // 復号
    }
}

/* */
