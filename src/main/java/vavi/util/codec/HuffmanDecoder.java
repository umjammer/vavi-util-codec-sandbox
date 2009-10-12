/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * HuffmanDecoder.
 * 
 * @author Tomonori Kusanagi
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 1.00 020330 T.K inital version <br>
 *          2.00 031001 nsano refine <br>
 */
public class HuffmanDecoder {
    /** */
    public byte[] decode(byte[] data) throws IOException {
        byte[] infr = null;

        // Huffman �ؗp�z��
        int[] parent = new int[512];
        int[] l_node = new int[512];
        int[] r_node = new int[512];
        int[] freq = new int[512];

        // ������
        for (int i = 0; i < 512; i++) {
            parent[i] = -1;
            l_node[i] = -1;
            r_node[i] = -1;
            freq[i] = 0;
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);

        // --- �f�[�^�ǂݍ���
        // �������^�C�v
        int type = dis.readByte();

        // �p�x�z��
        if (type == 0) {
            for (int i = 0; i < 256; i++) {
                freq[i] = dis.readInt();
            }
        } else if (type == 1) {
            type = dis.readByte() + 128;
            for (int i = 0; i < type; i++) {
                //int b = dis.readByte() + 128;
                freq[dis.readByte() + 128] = dis.readInt();
            }
        }

        // �f�[�^��
        int len = dis.readInt();

        // Huffman �؂����
        // �n�t�}���؂��쐬
        int target;

        // Huffman �؂����
        // �n�t�}���؂��쐬
        int min;
        for (int i = 256; i < (512 - 1); i++) {
            for (int j = 0; j < 2; j++) {
                // �e�̂Ȃ��v�f�ōŏ��̂��̂�T�����V�����m�[�h
                min = -1;
                target = -1;

                for (int k = 0; k < i; k++) {
                    if (parent[k] != -1) {
                        continue;
                    }

                    if ((target == -1) || ((target != -1) && (freq[k] < min))) {
                        target = k;
                        min = freq[k];
                    }
                }

                if (j == 0) {
                    l_node[i] = target;
                    parent[target] = -i;
                    freq[i] = freq[target];
                } else {
                    r_node[i] = target;
                    parent[target] = i;
                    freq[i] += freq[target];
                }
            }
        }

        //-------------
        // �𓀂���
        infr = new byte[len];

        int curr_byte;
        byte rest_bits = 0;
        byte[] bits = new byte[8];
        byte curr_bit;

        for (int i = 0; i < len; i++) {
            target = 510; // 510(���[�g�m�[�h)����X�^�[�g
            while (true) {
                // �Ώۃr�b�g�𓾂�
                if ((rest_bits % 8) == 0) {
                    // 1�o�C�g�ǂݍ���Ńr�b�g�ɕ���
                    curr_byte = dis.readByte();
                    for (int j = 0; j < 8; j++) {
                        bits[j] = (byte) (curr_byte % 2);
                        curr_byte >>= 1;
                    }
                    rest_bits = 8;
                }
                rest_bits--;
                curr_bit = (byte) Math.abs(bits[rest_bits]);

                if (curr_bit == 1) {
                    target = l_node[target];
                } else {
                    target = r_node[target];
                }

                if (l_node[target] < 0) {
                    break;
                }
            }

            infr[i] = (byte) (target - 128);
        }
        dis.close();

        return infr;
    }

    //----

    /** */
    public static void main(String[] args) {
        String inFile = null;

//  	inFile = "projects/Huffman/figure32.bmp.hff";
        if (args.length != 0) {
            inFile = args[0];
        } else {
            System.err.println("����������܂���");
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

            // ���ʏo��
            String outFile = inFile + ".dec";
            FileOutputStream fos = new FileOutputStream(outFile);
            fos.write(decoded, 0, decoded.length);
            fos.close();
        } catch (FileNotFoundException e) {
            System.err.println("����ȃt�@�C������܂���");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}

/* */
