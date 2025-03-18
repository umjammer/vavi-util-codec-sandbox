package vavi.util.codec.huffman.claude;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * @see "https://claude.ai/chat/edc33e58-38f7-4ace-9e13-2012cbb02de6""
 */
public class BitReader {

    private final InputStream reader;
    private byte buf;
    private int rest;

    public BitReader(InputStream reader) {
        this.reader = reader;
        this.rest = 0;
    }

    public boolean readBit() throws IOException {
        if (rest == 0) {
            int readResult = reader.read();
            if (readResult == -1) {
                throw new IOException("End of stream reached");
            }
            buf = (byte) readResult;
            rest = 8;
        }
        // buf 01234567  rest=8
        // buf 1234567-  rest=7
        // buf 234567--  rest=6
        //  ⋮
        // buf 7-------  rest=1
        // buf --------  rest=0
        boolean result = (buf & 0x80) != 0;
        buf <<= 1;
        rest--;
        return result;
    }

    public byte readUint8() throws IOException {
        int readResult = reader.read();
        if (readResult == -1) {
            throw new IOException("End of stream reached");
        }
        byte buf2 = (byte) readResult;
        
        byte result = (byte) ((buf & 0xFF) | ((buf2 & 0xFF) >> rest));
        buf = (byte) (buf2 << (8 - rest));
        return result;
    }
}