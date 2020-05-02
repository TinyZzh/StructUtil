/*
 *
 *
 *          Copyright (c) 2020. - TinyZ.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.struct.util;

import org.struct.core.converter.EmbeddedBasicTypeConverter;
import org.struct.core.converter.EnumConverter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public final class ConverterUtil {

    private static final BigInteger LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);

    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    /**
     * Embedded primitive type converter.
     */
    private static final EmbeddedBasicTypeConverter EMBEDDED_PRIMITIVE_TYPE_CONVERTER = new EmbeddedBasicTypeConverter();
    /**
     * Embedded enum converter.
     */
    private static final EnumConverter ENUM_CONVERTER = new EnumConverter();

    private ConverterUtil() {
        //  no-op
    }

    public static Object covert(Object value, Class requiredType) {
        if (Object.class == requiredType) {
            return value;
        } else if (String.class == requiredType) {
            return null == value ? "" : value.toString();
        } else if (Boolean.class == requiredType) {
            return null != value && isBooleanTrue(value.toString());
        } else if (requiredType.isPrimitive()) {
            return convertStringToPrimitiveType(value, requiredType);
        } else if (Number.class.isAssignableFrom(requiredType)) {
            if (value instanceof Number) {
                return convertNumberToTargetClass(((Number) value), requiredType);
            } else if (null == value) {
                return convertStringToPrimitiveType(null, requiredType);
            } else {
                return parseNumber(value.toString(), requiredType);
            }
        } else if (requiredType.isEnum()) {
            return ENUM_CONVERTER.convert(value, requiredType);
        }
        return value;
    }

    public static Object convertStringToPrimitiveType(Object value, Class<?> requiredType) {
        return EMBEDDED_PRIMITIVE_TYPE_CONVERTER.convert(value, requiredType);
    }

    public static boolean isBooleanTrue(String str) {
        return "true".equalsIgnoreCase(str)
                || "1".equalsIgnoreCase(str)
                || "y".equalsIgnoreCase(str)
                || "yes".equalsIgnoreCase(str);
    }

    /// Spring Framework's NumberUtils

    public static Object convertNumberToTargetClass(Number number, Class<?> targetClass) throws IllegalArgumentException {
        Objects.requireNonNull(number, "Number must not be null");
        Objects.requireNonNull(targetClass, "Target class must not be null");

        if (targetClass.isInstance(number)) {
            return number;
        } else if (Byte.class == targetClass) {
            long value = checkedLongValue(number, targetClass);
            if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                raiseOverflowException(number, targetClass);
            }
            return Byte.valueOf(number.byteValue());
        } else if (Short.class == targetClass) {
            long value = checkedLongValue(number, targetClass);
            if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                raiseOverflowException(number, targetClass);
            }
            return Short.valueOf(number.shortValue());
        } else if (Integer.class == targetClass) {
            long value = checkedLongValue(number, targetClass);
            if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                raiseOverflowException(number, targetClass);
            }
            return Integer.valueOf(number.intValue());
        } else if (Long.class == targetClass) {
            long value = checkedLongValue(number, targetClass);
            return Long.valueOf(value);
        } else if (BigInteger.class == targetClass) {
            if (number instanceof BigDecimal) {
                // do not lose precision - use BigDecimal's own conversion
                return ((BigDecimal) number).toBigInteger();
            } else {
                // original value is not a Big* number - use standard long conversion
                return BigInteger.valueOf(number.longValue());
            }
        } else if (Float.class == targetClass) {
            return Float.valueOf(number.floatValue());
        } else if (Double.class == targetClass) {
            return Double.valueOf(number.doubleValue());
        } else if (BigDecimal.class == targetClass) {
            // always use BigDecimal(String) here to avoid unpredictability of BigDecimal(double)
            // (see BigDecimal javadoc for details)
            return new BigDecimal(number.toString());
        } else {
            throw new IllegalArgumentException("Could not convert number [" + number + "] of type [" +
                    number.getClass().getName() + "] to unsupported target class [" + targetClass.getName() + "]");
        }
    }

    /**
     * Determine whether the given {@code value} String indicates a hex number,
     * i.e. needs to be passed into {@code Integer.decode} instead of
     * {@code Integer.valueOf}, etc.
     */
    public static boolean isHexNumber(String value) {
        int index = (value.startsWith("-") ? 1 : 0);
        return (value.startsWith("0x", index) || value.startsWith("0X", index) || value.startsWith("#", index));
    }

    /**
     * Parse the given {@code text} into a {@link Number} instance of the given
     * target class, using the corresponding {@code decode} / {@code valueOf} method.
     * <p>Trims the input {@code String} before attempting to parse the number.
     * <p>Supports numbers in hex format (with leading "0x", "0X", or "#") as well.
     *
     * @param text        the text to convert
     * @param targetClass the target class to parse into
     * @return the parsed number
     * @throws IllegalArgumentException if the target class is not supported
     *                                  (i.e. not a standard Number subclass as included in the JDK)
     * @see Byte#decode
     * @see Short#decode
     * @see Integer#decode
     * @see Long#decode
     * @see #decodeBigInteger(String)
     * @see Float#valueOf
     * @see Double#valueOf
     * @see java.math.BigDecimal#BigDecimal(String)
     */
    public static Object parseNumber(String text, Class<?> targetClass) {
        Objects.requireNonNull(text, "Text must not be null");
        Objects.requireNonNull(targetClass, "Target class must not be null");
        String trimmed = text.trim();

        if (Byte.class == targetClass) {
            return (isHexNumber(trimmed) ? Byte.decode(trimmed) : Byte.valueOf(trimmed));
        } else if (Short.class == targetClass) {
            return (isHexNumber(trimmed) ? Short.decode(trimmed) : Short.valueOf(trimmed));
        } else if (Integer.class == targetClass) {
            return (isHexNumber(trimmed) ? Integer.decode(trimmed) : Integer.valueOf(trimmed));
        } else if (Long.class == targetClass) {
            return (isHexNumber(trimmed) ? Long.decode(trimmed) : Long.valueOf(trimmed));
        } else if (BigInteger.class == targetClass) {
            return (isHexNumber(trimmed) ? decodeBigInteger(trimmed) : new BigInteger(trimmed));
        } else if (Float.class == targetClass) {
            return Float.valueOf(trimmed);
        } else if (Double.class == targetClass) {
            return Double.valueOf(trimmed);
        } else if (BigDecimal.class == targetClass || Number.class == targetClass) {
            return new BigDecimal(trimmed);
        } else {
            throw new IllegalArgumentException(
                    "Cannot convert String [" + text + "] to target class [" + targetClass.getName() + "]");
        }
    }

    /**
     * Decode a {@link java.math.BigInteger} from the supplied {@link String} value.
     * <p>Supports decimal, hex, and octal notation.
     *
     * @see BigInteger#BigInteger(String, int)
     */
    private static BigInteger decodeBigInteger(String value) {
        int radix = 10;
        int index = 0;
        boolean negative = false;

        // Handle minus sign, if present.
        if (value.startsWith("-")) {
            negative = true;
            index++;
        }

        // Handle radix specifier, if present.
        if (value.startsWith("0x", index) || value.startsWith("0X", index)) {
            index += 2;
            radix = 16;
        } else if (value.startsWith("#", index)) {
            index++;
            radix = 16;
        } else if (value.startsWith("0", index) && value.length() > 1 + index) {
            index++;
            radix = 8;
        }

        BigInteger result = new BigInteger(value.substring(index), radix);
        return (negative ? result.negate() : result);
    }

    /**
     * Check for a {@code BigInteger}/{@code BigDecimal} long overflow
     * before returning the given number as a long value.
     *
     * @param number      the number to convert
     * @param targetClass the target class to convert to
     * @return the long value, if convertible without overflow
     * @throws IllegalArgumentException if there is an overflow
     * @see #raiseOverflowException
     */
    private static long checkedLongValue(Number number, Class<?> targetClass) {
        BigInteger bigInt = null;
        if (number instanceof BigInteger) {
            bigInt = (BigInteger) number;
        } else if (number instanceof BigDecimal) {
            bigInt = ((BigDecimal) number).toBigInteger();
        }
        // Effectively analogous to JDK 8's BigInteger.longValueExact()
        if (bigInt != null && (bigInt.compareTo(LONG_MIN) < 0 || bigInt.compareTo(LONG_MAX) > 0)) {
            raiseOverflowException(number, targetClass);
        }
        return number.longValue();
    }

    /**
     * Raise an <em>overflow</em> exception for the given number and target class.
     *
     * @param number      the number we tried to convert
     * @param targetClass the target class we tried to convert to
     * @throws IllegalArgumentException if there is an overflow
     */
    private static void raiseOverflowException(Number number, Class<?> targetClass) {
        throw new IllegalArgumentException("Could not convert number [" + number + "] of type [" +
                number.getClass().getName() + "] to target class [" + targetClass.getName() + "]: overflow");
    }
}
