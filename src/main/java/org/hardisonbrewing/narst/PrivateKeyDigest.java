package org.hardisonbrewing.narst;

import java.security.DigestException;
import java.security.MessageDigest;

class PrivateKeyDigest {

    private MessageDigest messagedigest;

    public PrivateKeyDigest(MessageDigest messagedigest) {

        this.messagedigest = messagedigest;
    }

    public byte[] digest( String password, byte[] src, int i, int length ) throws DigestException {

        if ( i < 1 || length < 1 ) {
            throw new IllegalArgumentException();
        }

        PasswordDigest passwordDigest = new PasswordDigest( password, messagedigest );
        int digestLength = passwordDigest.getDigestLength();

        byte[] a = new byte[digestLength];
        byte[] b = new byte[digestLength];
        int c = ( ( length + digestLength ) - 1 ) / digestLength;
        byte[] d = new byte[length];
        for (int j = 0; j < c; j++) {
            passwordDigest.update( src );
            passwordDigest.update( i2b( j + 1 ) );
            passwordDigest.digest( b, 0 );
            System.arraycopy( b, 0, a, 0, digestLength );
            while (--i > 0) {
                passwordDigest.update( b );
                passwordDigest.digest( b, 0 );
                passwordDigest.reset();
                for (int k = 0; k < digestLength; k++) {
                    a[k] ^= b[k];
                }
            }
            int m = length - ( c - 1 ) * digestLength;
            int n = j == c - 1 ? m : digestLength;
            System.arraycopy( a, 0, d, j * digestLength, n );
        }
        return d;
    }

    private static byte[] i2b( int i ) {

        byte[] bytes = new byte[4];
        bytes[0] = (byte) ( i >> 24 );
        bytes[1] = (byte) ( i >> 16 );
        bytes[2] = (byte) ( i >> 8 );
        bytes[3] = (byte) i;
        return bytes;
    }
}
