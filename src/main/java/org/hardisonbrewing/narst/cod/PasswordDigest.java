package org.hardisonbrewing.narst.cod;

import java.security.DigestException;
import java.security.MessageDigest;

final class PasswordDigest {

    private MessageDigest messagedigest;
    private byte[] a;
    private byte[] b;

    public PasswordDigest(String password, MessageDigest messagedigest) {

        byte[] _password = password.getBytes();
        this.messagedigest = messagedigest;

        if ( _password.length > 64 ) {
            messagedigest.update( _password );
            _password = messagedigest.digest();
        }

        a = new byte[64];
        System.arraycopy( _password, 0, a, 0, _password.length );
        for (int i = 0; i < 64; i++) {
            a[i] ^= 0x36;
        }

        b = new byte[64];
        System.arraycopy( _password, 0, b, 0, _password.length );
        for (int j = 0; j < 64; j++) {
            b[j] ^= 0x5c;
        }

        reset();
    }

    public void reset() {

        messagedigest.reset();
        messagedigest.update( a );
    }

    public int getDigestLength() {

        return messagedigest.getDigestLength();
    }

    public void update( byte[] input ) {

        messagedigest.update( input );
    }

    public int digest( byte[] input, int offset ) throws DigestException {

        byte[] digest = messagedigest.digest();
        messagedigest.update( b );
        messagedigest.update( digest );
        int length = messagedigest.digest( input, offset, getDigestLength() );
        messagedigest.update( a );
        return length;
    }
}
