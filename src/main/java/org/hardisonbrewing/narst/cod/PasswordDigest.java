/**
 * Copyright (c) 2012 Martin M Reed
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
