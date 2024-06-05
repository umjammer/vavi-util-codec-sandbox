/*
 * Copyright (c) 2012 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavix.io.huffman1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import vavix.io.huffman1.HuffmanInputStream;
import vavix.io.huffman1.HuffmanOutputStream;


/**
 * HuffmanInputStreamTest. 
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2012/10/10 umjammer initial version <br>
 */
public class HuffmanInputStreamTest {

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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HuffmanOutputStream hos = new HuffmanOutputStream(baos, 16);
        hos.write(text.getBytes(encoding));
        hos.flush();
        hos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
System.err.println("encoded: " + bais.available());
        HuffmanInputStream his = new HuffmanInputStream(bais, 16);
        Reader reader = new InputStreamReader(his, encoding);
        StringBuilder sb = new StringBuilder();
        while (true) {
            int c = reader.read();
            if (c < 0) {
                break;
            }
            System.err.print((char) c);
            sb.append((char) c);
        }
        reader.close();
        assertEquals(text, sb.toString());
    }
}
