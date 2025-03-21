/*
 * Copyright (c) 2012 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.codec.huffman.okumura;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vavi.util.Debug;
import vavi.util.StringUtil;
import vavix.util.Checksum;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


/**
 * HuffmanTest. (Okumura version)
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2012/10/10 umjammer initial version <br>
 */
class HuffmanTest {

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

    static Path tmp;

    @BeforeAll
    static void setup() throws IOException {
        tmp = Files.createDirectories(Paths.get("tmp"));
    }

    @Test
    @DisplayName("text")
    void test() throws Exception {
Debug.println("original: " + text.getBytes().length);
        Huffman encoder = new Huffman();
        byte[] encoded = encoder.encode(text.getBytes());
Debug.println("encoded: " + encoded.length + "\n" + StringUtil.getDump(encoded, 64));
Debug.printf("%d -> %d: %d%%", text.getBytes().length, encoded.length, (int) (encoded.length * 100f / text.getBytes().length));

        Huffman decoder = new Huffman();
        byte[] decoded = decoder.decode(encoded);
Debug.println("decoded: " + decoded.length);
Debug.print("decoded:\n" + new String(decoded));
        assertEquals(text, new String(decoded));
    }

    @Test
    @DisplayName("text")
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
Debug.printf("%d -> %d: %d%%", Files.size(file), Files.size(encoded), (int) (Files.size(encoded) * 100f / Files.size(file)));

        Path decoded = tmp.resolve("test2.dec");
        Huffman decoder = new Huffman();
        os = Files.newOutputStream(decoded);
        decoder.decode(Files.newInputStream(encoded), os);
        os.close();
Debug.println("decoded: " + Files.size(decoded));
        assertEquals(Checksum.getChecksum(file), Checksum.getChecksum(decoded));
    }

    // TODO has problem
    @Test
    @DisplayName("binary")
    void test3() throws Exception {
        Path original = Paths.get("src/test/resources/okumura/data.enc");
Debug.println("original: " + Files.size(original) + "\n" + StringUtil.getDump(new BufferedInputStream(Files.newInputStream(original)), 0, 64));
        Path expectedDecoded = Paths.get("src/test/resources/okumura/data.dec");
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

    @Test
    @DisplayName("stream")
    void test4() throws Exception {
        Path file = tmp.resolve("test4.txt");
        Writer writer = Files.newBufferedWriter(file);
        writer.write(text);
        writer.flush();
        writer.close();
Debug.println("original: " + Files.size(file));

        Path encoded = tmp.resolve("test4.enc");
        try (var o = new Huffman.HuffmanOutputStream(new BufferedOutputStream(Files.newOutputStream(encoded)))) {
            Files.copy(file, o); // not flush and close
        }
Debug.println("encoded: " + Files.size(encoded));
Debug.printf("%d -> %d: %d%%", Files.size(file), Files.size(encoded), (int) (Files.size(encoded) * 100f / Files.size(file)));
        assertNotEquals(0, Files.size(encoded));

        Path decoded = tmp.resolve("test4.dec");
        Files.copy(new Huffman.HuffmanInputStream(new BufferedInputStream(Files.newInputStream(encoded))), decoded, REPLACE_EXISTING);
Debug.println("decoded: " + Files.size(decoded));
        assertEquals(Checksum.getChecksum(file), Checksum.getChecksum(decoded));
    }

    @Test
    @DisplayName("when lengthBits is 32")
    void test5() throws Exception {
        Path in = Path.of(HuffmanTest.class.getResource("/claude/data.enc").toURI());
        byte[] b = new Huffman.HuffmanInputStream(Files.newInputStream(in), 32).readAllBytes();
        Path out = Path.of("tmp/okumura.dec");
        Files.write(out, b);
Debug.printf("%d -> %d: %d%%\n%s", Files.size(in), b.length, (int) (Files.size(in) * 100f / b.length), StringUtil.getDump(b, 128));
        Path expected = Path.of(HuffmanTest.class.getResource("/claude/data.dec").toURI());
        assertEquals(Checksum.getChecksum(expected), Checksum.getChecksum(out));
        Path out2 = Path.of("tmp/okumura.enc");
        var os = new Huffman.HuffmanOutputStream(Files.newOutputStream(out2), 32);
        os.write(b);
        os.flush();
        os.close();
Debug.printf("%d -> %d: %d%%\n%s", b.length, Files.size(out2), (int) (Files.size(out2) * 100f / b.length), StringUtil.getDump(b, 128));
        Path expected2 = Path.of(HuffmanTest.class.getResource("/claude/data.enc").toURI());
        assertEquals(Checksum.getChecksum(expected2), Checksum.getChecksum(out2));
    }

    /**
     * @param args 0: 'e' or 'd', 1: in, 2: out
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 3 || !(args[0].equals("e") || args[0].equals("d"))) {
            System.err.println("Usage: java Huffman (e|d) infile outfile");
            System.exit(-1);
        }
        Huffman huf = new Huffman();
        if (args[0].equals("e"))
            huf.encode(Files.newInputStream(Paths.get(args[1])), Files.newOutputStream(Paths.get(args[2]))); // 符号化
        else
            huf.decode(Files.newInputStream(Paths.get(args[1])), Files.newOutputStream(Paths.get(args[2]))); // 復号
    }
}
