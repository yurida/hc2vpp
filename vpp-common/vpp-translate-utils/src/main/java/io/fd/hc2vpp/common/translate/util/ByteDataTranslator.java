/*
 * Copyright (c) 2016 Cisco and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fd.hc2vpp.common.translate.util;

import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Trait providing logic for working with binary/hex-based data.
 */
public interface ByteDataTranslator {

    ByteDataTranslator INSTANCE = new ByteDataTranslator() {
    };

    byte BYTE_FALSE = 0;
    byte BYTE_TRUE = 1;

    /**
     * Returns 0 if argument is null or false, 1 otherwise.
     *
     * @param value Boolean value to be converted
     * @return byte value equal to 0 or 1
     */
    default byte booleanToByte(@Nullable final Boolean value) {
        return value != null && value
                ? BYTE_TRUE
                : BYTE_FALSE;
    }

    /**
     * Checks if provided array contains only zeros
     */
    default boolean isArrayZeroed(final byte[] arr) {
        return Arrays.equals(arr, new byte[arr.length]);
    }

    /**
     * Returns Boolean.TRUE if argument is 0, Boolean.FALSE otherwise.
     *
     * @param value byte value to be converted
     * @return Boolean value
     * @throws IllegalArgumentException if argument is neither 0 nor 1
     */
    @Nonnull
    default Boolean byteToBoolean(final byte value) {
        if (value == BYTE_FALSE) {
            return Boolean.FALSE;
        } else if (value == BYTE_TRUE) {
            return Boolean.TRUE;
        }
        throw new IllegalArgumentException(String.format("0 or 1 was expected but was %d", value));
    }

    /**
     * Return (interned) string from byte array while removing \u0000. Strings represented as fixed length byte[] from
     * vpp contain \u0000.
     */
    default String toString(final byte[] vppString) {
        return new String(vppString).replaceAll("\\u0000", "").intern();
    }

    /**
     * Converts signed byte(filled with unsigned value from vpp) to java integer.
     * For example unsigned C byte 128 is converted by jvpp to -128, this will return 128.
     */
    default int toJavaByte(final byte vppByte) {
        return Byte.toUnsignedInt(vppByte);
    }

    default String printHexBinary(@Nonnull final byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes array should not be null");
        return printHexBinary(bytes, 0, bytes.length);
    }

    default String printHexBinary(@Nonnull final byte[] bytes, final int startIndex, final int endIndex) {
        StringBuilder str = new StringBuilder();

        Impl.appendHexByte(str, bytes[startIndex]);
        for (int i = startIndex + 1; i < endIndex; i++) {
            str.append(":");
            Impl.appendHexByte(str, bytes[i]);
        }

        return str.toString();
    }

    final class Impl {

        private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

        private static void appendHexByte(final StringBuilder sb, final byte b) {
            final int v = b & 0xFF;
            sb.append(HEX_CHARS[v >>> 4]);
            sb.append(HEX_CHARS[v & 15]);
        }
    }
}
