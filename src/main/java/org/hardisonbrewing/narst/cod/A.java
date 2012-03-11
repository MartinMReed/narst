package org.hardisonbrewing.narst.cod;

final class A {

    public static void a( byte[] src, byte[] dest, int offset, int destLength ) {

        int srcLength = src.length;

        byte[] a = new byte[256];
        for (int k = 0; k < 256; k++) {
            a[k] = (byte) k;
        }

        for (int i = 0, j = 0; j < 256; j++) {
            i = src[j % srcLength] + a[j] + i & 0xff;
            byte byte0 = a[j];
            a[j] = a[i];
            a[i] = byte0;
        }

        int i = 0;
        int j = 0;
        while (--destLength >= 0) {
            i = i + 1 & 0xff;
            j = a[i] + j & 0xff;
            byte byte0 = a[i];
            a[i] = a[j];
            a[j] = byte0;
            dest[offset++] ^= a[a[i] + a[j] & 0xff];
        }
    }
}
