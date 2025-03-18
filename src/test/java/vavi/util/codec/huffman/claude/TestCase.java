/*
 * Copyright (c) 2025 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.codec.huffman.claude;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.util.Debug;
import vavi.util.StringUtil;


/**
 * TestCase.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2025-03-18 nsano initial version <br>
 */
class TestCase {

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test1() throws Exception {
        Path path = Path.of("/Users/nsano/src/vavi/vavi-sound/tmp/data.enc");
        byte[] b = new HuffmanReader(Files.newInputStream(path)).readAllBytes();
Debug.printf("%d -> %d: %d%%\n%s", Files.size(path), b.length, (int) (Files.size(path) * 100f / b.length), StringUtil.getDump(b, 128));
    }
}
