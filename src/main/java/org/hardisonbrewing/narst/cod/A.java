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
