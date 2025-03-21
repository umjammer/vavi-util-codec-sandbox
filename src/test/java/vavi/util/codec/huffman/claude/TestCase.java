/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.codec.huffman.claude;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import vavi.util.Debug;
import vavi.util.StringUtil;
import vavix.util.Checksum;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * TestCase.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-03-18 nsano initial version <br>
 */
class TestCase {

    @Test
    void test1() throws Exception {
        Path in = Path.of(TestCase.class.getResource("/claude/data.enc").toURI());
        byte[] b = new HuffmanReader(Files.newInputStream(in)).readAllBytes();
        Path out = Path.of("tmp/claude.dec");
        Files.write(out, b);
Debug.printf("%d -> %d: %d%%\n%s", Files.size(in), b.length, (int) (Files.size(in) * 100f / b.length), StringUtil.getDump(b, 128));
        Path expected = Path.of(TestCase.class.getResource("/claude/data.dec").toURI());
        assertEquals(Checksum.getChecksum(expected), Checksum.getChecksum(out));
    }
}
