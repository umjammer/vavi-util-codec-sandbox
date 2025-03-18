/*
 * Copyright (c) 2012 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.codec.huffman.eaglejones;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

import vavi.util.Debug;
import vavi.util.StringUtil;


/**
 * HuffmanInputStreamTest. 
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2012/10/10 umjammer initial version <br>
 */
public class HuffmanInputStreamTest {

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

    // TODO not coded
    @Test
    public void test() throws Exception {
System.err.println("original: " + text.getBytes(UTF_8).length);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HuffmanOutputStream hos = new HuffmanOutputStream(baos, 2);
        hos.write(text.getBytes(UTF_8));
        hos.flush();
        hos.close();

        byte[] b = baos.toByteArray();
Debug.printf("%d -> %d: %d%%\n%s", text.getBytes(UTF_8).length, b.length, (int) (b.length * 100f / text.getBytes(UTF_8).length), StringUtil.getDump(b, 32));
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
System.err.println("encoded: " + bais.available());
        HuffmanInputStream his = new HuffmanInputStream(bais, 16);
        Reader reader = new InputStreamReader(his, UTF_8);
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
