# vavi.util.codec.huffman

Provides Easy Huffman related classes.

### Abstract

Mobile phones tend to have communication size limitations.

```
	Docomo : 10kB
	J-Phone : 
```

This tool allows you to encode data on a PC or server and then deploy it on a mobile device.

The following points have been taken into consideration to make it easy
to implement into mobile device Java applications.

 * Keep source changes to a minimum
 * The increase in source size is also minimized.


 * Caution 1:
   The compression rate is much poorer than gif or zip,
   so don't use it on files that have already been encoded.
   The file size will be larger than the original file.
 * Caution 2:
   Even with BMP, the more colors there are, the worse the compression rate becomes.
   If there are too many colors, the file size will end up being larger than the original file.
 * Caution 3:
   Encoded data has encoded data (up to about 1kB) in the header.
   Therefore, if you encode a file that is too small,
   the added header will be greater than the amount of data lost,
   and the file size will be larger than the original file.   

If you encode a BMP file with a file size of 10kB or more and 32 or fewer colors, you can expect some results.

### This is useful in situations like these.

The only textures that can be used with the 504 3D API are in BMP format (except D).
The maximum texture size is 128 x 128, so it is about 17kB. However, due to limitations in i-mode Java,
the amount of data that can be acquired via communication at one time is limited to 10kB,
so it is not possible to acquire a texture image at once. If the appropriate data is Huffman encoded,
the data size will be just under 10kB.
 
## Usage

### compression

 * From the command line
```
> java HuffmanEncoder Input file name
```
Output file name: Input file name.hff

### decompression

 *  From the command line
```
> java HuffmanDecoder Input file name
```
Output file name: input file name.dec

* From Doja, MIDP

   1 Copy the entire decode() method from Decoder.java
     to the iAppli, midlet source.

   2 Read the Huffman encoded data into a byte array.
     (Let's call this byte[] enc).

   3 byte[] dec = decode(enc);
     The decompressed data is stored in the byte array dec.

### Class Overview

 * HuffmanEncoder.java:
   Reads data from the input file, converts it to a byte array,
   and calls the encode() method of Encode.java.
   Then writes the return value (a byte array) to a file.
 
 * HuffmanDecoder.java:
   It reads data from the input file, converts it to a byte array,
   and calls the decode() method of Decode.java.
   Then it writes the return value (a byte array) to a file.
 
 * Encoder.java:
　 The compression program itself.
 
 * Decoder.java:
   The main body of the extraction program.
   When using with iAppli, just copy the decode() method as is.
   When I added the decode() method to the iAppli source and built it,
   the increase in the jar file was about 800 bytes.

### Data format

Data is recorded in the following order.

* Frequency information data format

1 byte

* Frequency information data

When frequency information data format is 0

int type x 256 = 1024 bytes

When frequency information data format is 1

(byte type + int type) x number of occurrences

(The smaller total size is automatically selected)

* Encoded data

### Compression ratio

I tried compressing the dog texture image of the sample 3D app.

* Original texture image (245 colors)

17462 bytes (before compression) → 14015 bytes (after compression) [80%]

* Reduced to 32 colors

16572 bytes (before compression) → 9178 bytes (after compression) [55%]

* Reduced to 16 colors

16508 bytes (before compression) → 7470 bytes (after compression) [45%]

## TODO
