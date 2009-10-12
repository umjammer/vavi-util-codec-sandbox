/*
 * Copyright (c) 2003 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.util.codec;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * HuffmanEncoder.
 * 
 * @author Tomonori Kusanagi
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 1.00 020330 T.K inital version <br>
 *          2.00 031001 nsano refine <br>
 */
public class HuffmanEncoder {
    /**
     * �n�t�}������������
     */
    public byte[] encode(byte[] data) throws IOException {
        int[] freq = new int[256];

        // �p�x�z��
        for (int i = 0; i < 256; i++) {
            freq[i] = 0;
        }
        for (int i = 0; i < data.length; i++) {
            freq[data[i] + 128]++;
        }

/*
// �e�X�g�o��
for (int i = 0; i < 256; i++) {
 System.out.print(freq[i] + " ");
 if (i % 16 == 15)
  System.out.println();
}
*/

        // �n�t�}���؂��쐬
        int[] parent = new int[512];
        int[] l_node = new int[512];
        int[] r_node = new int[512];
        buildTree(freq, parent, l_node, r_node);

/*
// �e�X�g�o��
for (int i = 0; i < 512 - 1; i++)
 System.out.println("[" + i + "] l:" + l_node[i] + " r:" + r_node[i] + " p:" + parent[i]);
*/

        // �������쐬
        int n;
        byte[][] code = new byte[256][];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < 256; i++) {
            if (freq[i] == 0) {
                continue;
            }

            // ������
            n = i;
            baos.reset();
            while (parent[n] != -1) {
                if (parent[n] < 0) {
                    baos.write(1);
                } else {
                    baos.write(0);
                }
                n = Math.abs(parent[n]);
            }

            byte[] rev_code = baos.toByteArray();
            code[i] = new byte[rev_code.length];

            // ���]
            for (int j = 0; j < rev_code.length; j++) {
                code[i][j] = rev_code[rev_code.length - j - 1];
            }
        }

/*
// �e�X�g�o��
for (int i = 0; i < 256; i++) {
 System.out.print("["+i+"]:"+freq[i]+" - ");
 if (freq[i] > 0) {
  for (int j = 0; j < code[i].length; j++) {
   System.out.print(code[i][j]);
  }
 }
 System.out.println();
}
*/
        // �����𓾂�
        baos.reset();

        for (int i = 0; i < data.length; i++) {
            baos.write(code[data[i] + 128], 0, code[data[i] + 128].length);

/*
n = data[i] + 128;
for (int j = 0; j < code[n].length; j++)
 System.out.print(code[n][j]);
*/
        }

        byte[] tmp = baos.toByteArray();

        byte[] huff = new byte[(tmp.length / 8) + 1];
        for (int i = 0; i < huff.length; i++) {
            // �������o�C�g��쐬
            huff[i] = 0;
            for (int j = 0; j < 8; j++) {
                huff[i] <<= 1;
                if (((i * 8) + j) < tmp.length) {
                    huff[i] += tmp[(i * 8) + j];
                }
            }
        }

        baos.reset();

        DataOutputStream dos = new DataOutputStream(baos);

        // ���������
        // type 0�F
        //	 �S�v�f�̏o���p�x��int�^�ł���
        //   4[byte] * 256 = 1024[byte]	
        // type 1�F
        //	 �o���p�x��0�łȂ��v�f�����ɂ��āA
        //   �v�f�ԍ��Əo���p�x�iint�^�j������
        //   1[byte] + (1+4)[byte] * 256
        // type 1�̏ꍇ�̕��������f�[�^��
        n = 0;
        for (int i = 0; i < 256; i++) {
            if (freq[i] != 0) {
                n++;
            }
        }

        // �f�[�^�����Ȃ��Ă��ޕ��������I�ɑI��
        int type;
        if ((1 + (n * 5)) > (256 * 4)) {
            type = 0;
        } else {
            type = 1;
        }

//System.out.println("type : " + type);
        if (type == 0) {
            dos.writeByte(0); // �������^�C�v
            for (int i = 0; i < 256; i++) {
                dos.writeInt(freq[i]);
            }
        } else if (type == 1) {
            dos.writeByte(1);
            dos.writeByte(n - 128);
            for (int i = 0; i < 256; i++) {
                if (freq[i] != 0) {
                    dos.writeByte(i - 128);
                    dos.writeInt(freq[i]);
                }
            }
        } else {
            throw new IllegalArgumentException("���������ԍ�: " + type);
        }

        // ������
        dos.writeInt(data.length);

        // �������f�[�^
        dos.write(huff, 0, huff.length);

        dos.close();

        byte[] out = baos.toByteArray();

        // �e�X�g�o��
        int prep = data.length;
        int postp = out.length;
System.err.println(prep + " �� " + postp + " : " + ((postp * 100) / prep) + "%");

        return out;
    }

    /**
     * Huffman�؂�����
     */
    private void buildTree(int[] freq, int[] parent, int[] l_node, int[] r_node) {
        int[] freq_node = new int[512];

        // ������
        for (int i = 0; i < 512; i++) {
            parent[i] = -1;
            l_node[i] = -1;
            r_node[i] = -1;
            if (i < 256) {
                freq_node[i] = freq[i];
            } else {
                freq_node[i] = 0;
            }
        }

        // Huffman�؂��쐬
        int minId;
        for (int i = 256; i < (512 - 1); i++) {
            // �e�̂Ȃ��v�f�ōŏ��̂��̂�T�����V�������m�[�h
            minId = findSmallest(i, freq_node, parent);
            l_node[i] = minId;
            parent[minId] = -i;
            freq_node[i] = freq_node[minId];

            // �e�̂Ȃ��v�f�ōŏ��̂��̂�T�����V�����E�m�[�h
            minId = findSmallest(i, freq_node, parent);
            r_node[i] = minId;
            parent[minId] = i;
            freq_node[i] += freq_node[minId];
        }
    }

    /**
     * �z��̐e�������Ȃ��v�f�̒��ōŏ��̂��̂�T���A���̔ԍ���Ԃ�
     */
    private int findSmallest(int n, int[] freq_node, int[] parent) {
        int min = -1;
        int minId = -1;

        for (int i = 0; i < n; i++) {
            if (parent[i] != -1) {
                continue;
            }

            if ((minId == -1) || ((minId != -1) && (freq_node[i] < min))) {
                minId = i;
                min = freq_node[i];
            }
        }
        return minId;
    }

    //----

    /** */
    public static void main(String[] args) {
        String inFile = null;

//  	inFile = "projects/Huffman/figure16.bmp";
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

/*
// �e�X�g�o��
int min = data[0], max = data[0];
for (int i = 0; i < data.length; i++) {
 if (data[i] > max)
  max = data[i];
 if (data[i] < min)
  min = data[i];
 System.out.print(data[i] + " ");
 if (i % 16 == 0)
  System.out.println();
}
System.out.println("size: " + data.length);
System.out.println("max: " + max);
System.out.println("min: " + min);
*/
            HuffmanEncoder enc = new HuffmanEncoder();
            byte[] encoded = enc.encode(data);

            // ���ʏo��
            String outFile = inFile + ".hff";
            FileOutputStream fos = new FileOutputStream(outFile);
            fos.write(encoded, 0, encoded.length);
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
