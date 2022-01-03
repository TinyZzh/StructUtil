/*
 *
 *
 *          Copyright (c) 2022. - TinyZ.
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

package org.struct.core.converter;

import org.struct.exception.UnSupportConvertOperationException;
import org.struct.spi.SPI;
import org.struct.util.ConverterUtil;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内置的基础的转换器.
 */
@SPI(name = "embedded", order = 0)
public class EmbeddedConverters implements Converters {

    private static final Map<Class<?>, Converter> CLASS_CONVERTER_MAP = new ConcurrentHashMap<>();

    static {
        register(new IntegerConverter(), int.class, Integer.class);
        register(new LongConverter(), long.class, Long.class);
        register(new BooleanConverter(), boolean.class, Boolean.class);
        register(new ShortConverter(), short.class, Short.class);
        register(new ByteConverter(), byte.class, Byte.class);
        register(new FloatConverter(), float.class, Float.class);
        register(new DoubleConverter(), double.class, Double.class);

        register(new BigIntegerConverter(), BigInteger.class);
        register(new BigDecimalConverter(), BigDecimal.class);

        register(new EnumConverter(), Enum.class);
        register(new StringConverter(), String.class);
        //  basic array converter.
        register(new ArrayConverter(), Array.class);

        //  date, datetime
        register(new DateConverter(), Date.class);
        register(new LocalDateConverter(), LocalDate.class);
        register(new LocalDateTimeConverter(), LocalDateTime.class);
    }

    @Override
    public Map<Class<?>, Converter> getConverters() {
        return Collections.unmodifiableMap(CLASS_CONVERTER_MAP);
    }

    public static void register(Converter converter, Class<?>... clzArray) {
        for (Class<?> clzOfTarget : clzArray) {
            CLASS_CONVERTER_MAP.putIfAbsent(clzOfTarget, converter);
        }
    }

    public static UnSupportConvertOperationException raiseUnSupportConvert(Object originValue, Class<?> targetType) {
        throw new UnSupportConvertOperationException("un support convert operation. target:" + targetType.getName() + ", origin:" + originValue);
    }

    static class IntegerConverter implements Converter {

        @Override
        public Object convert(Object originValue, Class<?> targetType) {
            if (null == originValue) {
                return 0;
            } else if (originValue instanceof String) {
                String s = (String) originValue;
                return ConverterUtil.isHexNumber(s) ? Integer.decode(s) : Integer.valueOf(s);
            } else if (originValue instanceof Number) {
                Number number = (Number) originValue;
                long value = ConverterUtil.checkedLongValue(number, targetType);
                if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                    ConverterUtil.raiseOverflowException(number, targetType);
                }
                return Integer.valueOf(number.intValue());
            }
            throw raiseUnSupportConvert(originValue, targetType);
        }
    }

    static class LongConverter implements Converter {

        @Override
        public Object convert(Object originValue, Class<?> targetType) {
            if (null == originValue) {
                return 0L;
            } else if (originValue instanceof String) {
                String s = (String) originValue;
                return ConverterUtil.isHexNumber(s) ? Long.decode(s) : Long.valueOf(s);
            } else if (originValue instanceof Number) {
                Number number = (Number) originValue;
                long value = ConverterUtil.checkedLongValue(number, targetType);
                return Long.valueOf(value);
            }
            throw raiseUnSupportConvert(originValue, targetType);
        }
    }

    static class BooleanConverter implements Converter {

        @Override
        public Object convert(Object originValue, Class<?> targetType) {
            if (null == originValue) {
                return false;
            } else if (originValue.getClass() == Boolean.class) {
                return originValue;
            } else if (originValue.getClass().isPrimitive()) {
                return 1 == (int) originValue;
            } else if (originValue instanceof String) {
                return ConverterUtil.isBooleanTrue((String) originValue);
            } else if (originValue instanceof Number) {
                return 1 == ((Number) originValue).intValue();
            }
            throw raiseUnSupportConvert(originValue, targetType);
        }
    }

    static class ShortConverter implements Converter {

        @Override
        public Object convert(Object originValue, Class<?> targetType) {
            if (null == originValue) {
                return (short) 0;
            } else if (originValue instanceof String) {
                String s = (String) originValue;
                return ConverterUtil.isHexNumber(s) ? Short.decode(s) : Short.valueOf(s);
            } else if (originValue instanceof Number) {
                Number number = (Number) originValue;
                long value = ConverterUtil.checkedLongValue(number, targetType);
                if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                    ConverterUtil.raiseOverflowException(number, targetType);
                }
                return Short.valueOf(number.shortValue());
            }
            throw raiseUnSupportConvert(originValue, targetType);
        }
    }

    static class ByteConverter implements Converter {

        @Override
        public Object convert(Object originValue, Class<?> targetType) {
            if (null == originValue) {
                return 0x00;
            } else if (originValue instanceof String) {
                String s = (String) originValue;
                return ConverterUtil.isHexNumber(s) ? Byte.decode(s) : Byte.valueOf(s);
            } else if (originValue instanceof Number) {
                Number number = (Number) originValue;
                long value = ConverterUtil.checkedLongValue(number, targetType);
                if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                    ConverterUtil.raiseOverflowException(number, targetType);
                }
                return Byte.valueOf(number.byteValue());
            }
            throw raiseUnSupportConvert(originValue, targetType);
        }
    }

    static class FloatConverter implements Converter {

        @Override
        public Object convert(Object originValue, Class<?> targetType) {
            if (null == originValue) {
                return 0.0F;
            } else if (originValue instanceof String) {
                return Float.valueOf((String) originValue);
            } else if (originValue instanceof Number) {
                return Float.valueOf(((Number) originValue).floatValue());
            }
            throw raiseUnSupportConvert(originValue, targetType);
        }
    }

    static class DoubleConverter implements Converter {

        @Override
        public Object convert(Object originValue, Class<?> targetType) {
            if (null == originValue) {
                return 0.0D;
            } else if (originValue instanceof String) {
                return Double.valueOf((String) originValue);
            } else if (originValue instanceof Number) {
                return Double.valueOf(((Number) originValue).doubleValue());
            }
            throw raiseUnSupportConvert(originValue, targetType);
        }
    }

    static class BigIntegerConverter implements Converter {

        @Override
        public Object convert(Object originValue, Class<?> targetType) {
            if (null == originValue) {
                return BigInteger.ZERO;
            } else if (originValue instanceof String) {
                String trimmed = (String) originValue;
                return ConverterUtil.isHexNumber(trimmed) ? ConverterUtil.decodeBigInteger(trimmed) : new BigInteger(trimmed);
            } else if (originValue instanceof Number) {
                Number number = (Number) originValue;
                if (number instanceof BigDecimal) {
                    return ((BigDecimal) number).toBigInteger();
                } else {
                    return BigInteger.valueOf(number.longValue());
                }
            }
            throw raiseUnSupportConvert(originValue, targetType);
        }
    }

    static class BigDecimalConverter implements Converter {

        @Override
        public Object convert(Object originValue, Class<?> targetType) {
            if (null == originValue) {
                return BigDecimal.ZERO;
            } else if (originValue instanceof String) {
                return new BigDecimal((String) originValue);
            } else if (originValue instanceof Number) {
                return new BigDecimal(originValue.toString());
            }
            throw raiseUnSupportConvert(originValue, targetType);
        }
    }
}
