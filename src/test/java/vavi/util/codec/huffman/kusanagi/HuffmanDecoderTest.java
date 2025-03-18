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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import vavi.util.Debug;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * HuffmanDecoderTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2012/09/27 umjammer initial version <br>
 */
public class HuffmanDecoderTest {

    static final String text_jp = """
        文字データだけで構成されたファイル。どんな機種のコンピュータでも共通して利用できる数少ないファイル形式の一つ。
        ワープロソフトなどで文書を作成した際には、テキストファイルに変換すれば、他の機種やソフトウェアでもそのデータを
        利用することができる。
        ただし、テキストファイルは純粋に文字データのみで構成され、修飾情報や埋め込まれた画像など文字以外のデータは表現
        することができない。
        また、改行やタブなど、文章の作成に必要なものを除いて、制御コードを含むことはできない。このため、他の形式から変換
        する際には注意が必要。
        テキストファイルの編集のみを行うためのソフトウェアをテキストエディタという。
        HTMLファイルなどはコンピュータが解析してレイアウトできるようにタグ(付加情報)が埋め込まれているが、
        タグ自体は通常の文字コードの範囲内で表現されているため、HTMLファイルもテキストファイルの一種と言える。
        ただし、HTMLファイルはテキストエディタで開いたときとWebブラウザで開いたときに見え方がまったく異なるため、
        文章だけで構成されるファイルとは区別して扱う必要がある。
        """;

    static final String text_en = """
        Synthetic music Mobile Application Format, abbreviated SMAF, is a music data format specified
        by Yamaha for portable electronic devices, such as cell phones and PDAs. The file extension for
        SMAF is .MMF and is common as ringtones for mobile phones with one of five sound chips.
        SMAF resembles MIDI, but also supports graphics and PCM sound playback. Its MIDI playback is
        produced via FM synthesis or PCM sample-based synthesis, where instrument data
        (parameters and/or PCM samples) is stored within the .MMF file itself, similar to module files.
        This enables users to create custom instruments, which will sound exactly the same on devices with
        the same chip. The feature set used in SMAF files usually orients itself at the chips produced
        by Yamaha for playback:
        """;

    static final String text = text_en;

    @Test
    public void test() throws Exception {
Debug.println("original: " + text.getBytes(UTF_8).length);
        HuffmanEncoder encoder = new HuffmanEncoder();
        byte[] encoded = encoder.encode(text.getBytes(UTF_8));
Debug.println("encoded: " + encoded.length);

        HuffmanDecoder decoder = new HuffmanDecoder();
        byte[] decoded = decoder.decode(encoded);
Debug.println("decoded: " + decoded.length);
Debug.print(new String(decoded, UTF_8));
        assertEquals(text, new String(decoded, UTF_8));
    }

    //----

    /** */
    public static void main(String[] args) throws IOException {
        String inFile = null;

        if (args.length != 0) {
            inFile = args[0];
        } else {
            throw new IllegalArgumentException("Missing Arguments");
        }

        DataInputStream dis;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        dis = new DataInputStream(new FileInputStream(inFile));

        int len;
        byte[] buff = new byte[1024];

        while ((len = dis.read(buff, 0, 1024)) != -1) {
            baos.write(buff, 0, len);
        }
        dis.close();

        byte[] data = baos.toByteArray();

        HuffmanDecoder dec = new HuffmanDecoder();
        byte[] decoded = dec.decode(data);

        // Result output
        String outFile = inFile + ".dec";
        FileOutputStream fos = new FileOutputStream(outFile);
        fos.write(decoded, 0, decoded.length);
        fos.close();
    }
}
