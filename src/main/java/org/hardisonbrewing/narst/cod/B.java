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

final class B {

    private static final byte a[] = { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47 };

    public static byte[] a( byte[] src ) {

        int srcLength = src.length;

        int destLength = srcLength;
        if ( destLength % 3 != 0 ) {
            destLength = ( ( destLength * 4 ) / 3 + 4 ) - destLength % 3;
        }
        else {
            destLength = ( destLength * 4 ) / 3;
        }

        byte[] dest = new byte[destLength];
        int srcPos = 0;
        int destPos = 0;

        while (srcLength > 0) {
            if ( srcLength >= 3 ) {
                dest[destPos++] = a[( src[srcPos] & 0xfc ) >>> 2];
                dest[destPos++] = a[( src[srcPos] & 3 ) << 4 | ( src[srcPos + 1] & 0xf0 ) >>> 4];
                dest[destPos++] = a[( src[srcPos + 1] & 0xf ) << 2 | ( src[srcPos + 2] & 0xc0 ) >>> 6];
                dest[destPos++] = a[src[srcPos + 2] & 0x3f];
                srcLength -= 3;
                srcPos += 3;
            }
            else if ( srcLength == 2 ) {
                dest[destPos++] = a[( src[srcPos] & 0xfc ) >>> 2];
                dest[destPos++] = a[( src[srcPos] & 3 ) << 4 | ( src[srcPos + 1] & 0xf0 ) >>> 4];
                dest[destPos++] = a[( src[srcPos + 1] & 0xf ) << 2];
                dest[destPos++] = 61;
                srcLength -= 2;
            }
            else if ( srcLength == 1 ) {
                dest[destPos++] = a[( src[srcPos] & 0xfc ) >>> 2];
                dest[destPos++] = a[( src[srcPos] & 3 ) << 4];
                dest[destPos++] = 61;
                dest[destPos++] = 61;
                srcLength--;
            }
        }

        return dest;
    }

    public static byte[] b( byte[] src ) {

        byte[] dest = new byte[b( src, 0, src.length )];
        if ( b( src, 0, src.length, dest, 0 ) ) {
            return dest;
        }
        return null;
    }

    private static int b( byte[] src, int srcPos, int length ) {

        int k = ( length * 3 ) / 4;
        if ( length % 4 != 0 ) {
            k += length % 4;
        }
        else if ( length > 0 && src[( srcPos + length ) - 1] == 61 ) {
            if ( length > 1 && src[( srcPos + length ) - 2] == 61 ) {
                k -= 2;
            }
            else {
                k--;
            }
        }
        return k;
    }

    private static boolean b( byte[] src, int srcPos, int length, byte[] dest, int destPos ) {

        do {
            if ( length <= 0 ) {
                break;
            }
            if ( length < 4 ) {
                return false;
            }
            for (int i1 = 0; i1 < 4; i1++) {
                if ( b( src[srcPos + i1] ) == -1 ) {
                    return false;
                }
            }
            dest[destPos++] = (byte) ( ( b( src[srcPos] ) & 0x3f ) << 2 | ( b( src[srcPos + 1] ) & 0x30 ) >>> 4 );
            if ( src[srcPos + 2] == 61 ) {
                if ( src[srcPos + 3] != 61 ) {
                    return false;
                }
                break;
            }
            dest[destPos++] = (byte) ( ( b( src[srcPos + 1] ) & 0xf ) << 4 | ( b( src[srcPos + 2] ) & 0x3c ) >>> 2 );
            if ( src[srcPos + 3] == 61 ) {
                break;
            }
            dest[destPos++] = (byte) ( ( b( src[srcPos + 2] ) & 3 ) << 6 | b( src[srcPos + 3] ) & 0x3f );
            srcPos += 4;
            length -= 4;
        }
        while (true);
        return true;
    }

    private static int b( byte b ) {

        if ( 65 <= b && b <= 90 ) {
            return b - 65;
        }
        if ( 97 <= b && b <= 122 ) {
            return ( b - 97 ) + 26;
        }
        if ( 48 <= b && b <= 57 ) {
            return ( b - 48 ) + 52;
        }
        if ( b == 43 ) {
            return 62;
        }
        if ( b == 47 ) {
            return 63;
        }
        return b != 61 ? -1 : 64;
    }
}
