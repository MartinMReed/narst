package org.hardisonbrewing.narst;

import java.security.PrivateKey;
import java.security.Signature;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

class SignedProperties extends Properties {

    private static final long serialVersionUID = 4366497003472343994L;

    public void sign( PrivateKey privatekey ) throws Exception {

        byte[] signature = B.a( encode( dsa( privatekey ) ) );
        setProperty( "Signature", new String( signature ) );
    }

    private String[] keyArray() {

        String[] keys = new String[size()];

        Enumeration<Object> enumerator = keys();
        for (int i = 0; enumerator.hasMoreElements(); i++) {
            keys[i] = (String) enumerator.nextElement();
        }

        Arrays.sort( keys );

        return keys;
    }

    private byte[] dsa( PrivateKey privatekey ) throws Exception {

        Signature signature = Signature.getInstance( "DSA" );
        signature.initSign( privatekey );
        for (String key : keyArray()) {
            signature.update( key.getBytes() );
            signature.update( getProperty( key ).getBytes() );
        }
        return signature.sign();
    }

    private byte[] encode( byte[] src ) {

        byte[] dest = new byte[40];
        int srcPos = 3;
        if ( ( src[srcPos] & 0x80 ) != 0 ) {
            return null;
        }
        byte byte0 = src[srcPos];
        if ( byte0 >= 20 ) {
            srcPos += ( byte0 - 20 ) + 1;
            System.arraycopy( src, srcPos, dest, 0, 20 );
            srcPos += 21;
        }
        else {
            srcPos++;
            int destPos = 20 - byte0;
            System.arraycopy( src, srcPos, dest, destPos, byte0 );
            srcPos += byte0 + 1;
        }
        if ( ( src[srcPos] & 0x80 ) != 0 ) {
            return null;
        }
        byte byte1 = src[srcPos];
        if ( byte1 >= 20 ) {
            srcPos += ( byte1 - 20 ) + 1;
            System.arraycopy( src, srcPos, dest, 20, 20 );
        }
        else {
            srcPos++;
            int destPos = 20 - byte1;
            System.arraycopy( src, srcPos, dest, 20 + destPos, byte1 );
        }
        return dest;
    }
}
