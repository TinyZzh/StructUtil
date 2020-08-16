package org.struct.core.examples;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.struct.annotation.StructSheet;
import org.struct.core.StructWorker;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author TinyZ.
 * @version 2020.08.16
 */
public class SimpleFieldBeanTest {

    @Test()
    public void test() {
        SimpleTypeFieldBean t = new SimpleTypeFieldBean(
                (byte) 1, (short) 2, 3, 4L, 5F, 6D, true, (short) 7, 8, 9L, 10F, 11D,
                BigInteger.valueOf(12), BigDecimal.valueOf(13), "str",
                new byte[]{0x0, 0x1, 0x0},
                new short[]{1, 2, 3},
                new int[]{4, 5, 6, 7},
                new long[]{8, 9},
                new float[]{10F, 11F, 12F},
                new double[]{13D, 14D},
                new Short[]{15, 16},
                new Integer[]{17, 18, 19},
                new Long[]{20L, 21L},
                new Float[]{22F, 23F},
                new Double[]{24D, 25D, 26D},
                MyEnum.Three,
                MyEnum.Two,
                MyEnum.One
        );
        StructWorker<SimpleTypeFieldBean> worker = new StructWorker<>("classpath:/org/struct/core/", SimpleTypeFieldBean.class);
        ArrayList<SimpleTypeFieldBean> list = worker.toList(ArrayList::new);
        SimpleTypeFieldBean bean = list.get(0);
        Assertions.assertEquals(t, bean);
    }

    enum MyEnum {
        One,
        Two,
        Three;
    }

    @StructSheet(fileName = "examples.xlsx", sheetName = "example_1")
    static class SimpleTypeFieldBean {

        //  primary type
        private byte mByte;
        private short mShort;
        private int mInt;
        private long mLong;
        private float mFloat;
        private double mDouble;
        private boolean mBoolean;
        //  wrap type
        private Short wShort;
        private Integer wInteger;
        private Long wLong;
        private Float wFloat;
        private Double wDouble;
        private BigInteger wBigInteger;
        private BigDecimal wBigDecimal;
        //  string
        private String wString;
        //  primary type array
        private byte[] mByteAry;
        private short[] mShortAry;
        private int[] mIntAry;
        private long[] mLongAry;
        private float[] mFloatAry;
        private double[] mDoubleAry;
        //  wrap type array
        private Short[] wShortAry;
        private Integer[] wIntegerAry;
        private Long[] wLongAry;
        private Float[] wFloatAry;
        private Double[] wDoubleAry;
        //  enum
        private MyEnum numEnum;
        private MyEnum strEnum;
        private MyEnum lowerStrEnum;

        public SimpleTypeFieldBean() {
        }

        public SimpleTypeFieldBean(byte mByte, short mShort, int mInt, long mLong, float mFloat, double mDouble, boolean mBoolean, Short wShort, Integer wInteger, Long wLong, Float wFloat, Double wDouble, BigInteger wBigInteger, BigDecimal wBigDecimal, String wString, byte[] mByteAry, short[] mShortAry, int[] mIntAry, long[] mLongAry, float[] mFloatAry, double[] mDoubleAry, Short[] wShortAry, Integer[] wIntegerAry, Long[] wLongAry, Float[] wFloatAry, Double[] wDoubleAry, MyEnum numEnum, MyEnum strEnum, MyEnum lowerStrEnum) {
            this.mByte = mByte;
            this.mShort = mShort;
            this.mInt = mInt;
            this.mLong = mLong;
            this.mFloat = mFloat;
            this.mDouble = mDouble;
            this.mBoolean = mBoolean;
            this.wShort = wShort;
            this.wInteger = wInteger;
            this.wLong = wLong;
            this.wFloat = wFloat;
            this.wDouble = wDouble;
            this.wBigInteger = wBigInteger;
            this.wBigDecimal = wBigDecimal;
            this.wString = wString;
            this.mByteAry = mByteAry;
            this.mShortAry = mShortAry;
            this.mIntAry = mIntAry;
            this.mLongAry = mLongAry;
            this.mFloatAry = mFloatAry;
            this.mDoubleAry = mDoubleAry;
            this.wShortAry = wShortAry;
            this.wIntegerAry = wIntegerAry;
            this.wLongAry = wLongAry;
            this.wFloatAry = wFloatAry;
            this.wDoubleAry = wDoubleAry;
            this.numEnum = numEnum;
            this.strEnum = strEnum;
            this.lowerStrEnum = lowerStrEnum;
        }

        @Override
        public String toString() {
            return "SimpleTypeFieldBean{" +
                    "mByte=" + mByte +
                    ", mShort=" + mShort +
                    ", mInt=" + mInt +
                    ", mLong=" + mLong +
                    ", mFloat=" + mFloat +
                    ", mDouble=" + mDouble +
                    ", mBoolean=" + mBoolean +
                    ", wShort=" + wShort +
                    ", wInteger=" + wInteger +
                    ", wLong=" + wLong +
                    ", wFloat=" + wFloat +
                    ", wDouble=" + wDouble +
                    ", wBigInteger=" + wBigInteger +
                    ", wBigDecimal=" + wBigDecimal +
                    ", wString='" + wString + '\'' +
                    ", mByteAry=" + Arrays.toString(mByteAry) +
                    ", mShortAry=" + Arrays.toString(mShortAry) +
                    ", mIntAry=" + Arrays.toString(mIntAry) +
                    ", mLongAry=" + Arrays.toString(mLongAry) +
                    ", mFloatAry=" + Arrays.toString(mFloatAry) +
                    ", mDoubleAry=" + Arrays.toString(mDoubleAry) +
                    ", wShortAry=" + Arrays.toString(wShortAry) +
                    ", wIntegerAry=" + Arrays.toString(wIntegerAry) +
                    ", wLongAry=" + Arrays.toString(wLongAry) +
                    ", wFloatAry=" + Arrays.toString(wFloatAry) +
                    ", wDoubleAry=" + Arrays.toString(wDoubleAry) +
                    ", numEnum=" + numEnum +
                    ", strEnum=" + strEnum +
                    ", lowerStrEnum=" + lowerStrEnum +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SimpleTypeFieldBean that = (SimpleTypeFieldBean) o;
            return mByte == that.mByte &&
                    mShort == that.mShort &&
                    mInt == that.mInt &&
                    mLong == that.mLong &&
                    Float.compare(that.mFloat, mFloat) == 0 &&
                    Double.compare(that.mDouble, mDouble) == 0 &&
                    mBoolean == that.mBoolean &&
                    Objects.equals(wShort, that.wShort) &&
                    Objects.equals(wInteger, that.wInteger) &&
                    Objects.equals(wLong, that.wLong) &&
                    Objects.equals(wFloat, that.wFloat) &&
                    Objects.equals(wDouble, that.wDouble) &&
                    Objects.equals(wBigInteger, that.wBigInteger) &&
                    Objects.equals(wBigDecimal, that.wBigDecimal) &&
                    Objects.equals(wString, that.wString) &&
                    Arrays.equals(mByteAry, that.mByteAry) &&
                    Arrays.equals(mShortAry, that.mShortAry) &&
                    Arrays.equals(mIntAry, that.mIntAry) &&
                    Arrays.equals(mLongAry, that.mLongAry) &&
                    Arrays.equals(mFloatAry, that.mFloatAry) &&
                    Arrays.equals(mDoubleAry, that.mDoubleAry) &&
                    Arrays.equals(wShortAry, that.wShortAry) &&
                    Arrays.equals(wIntegerAry, that.wIntegerAry) &&
                    Arrays.equals(wLongAry, that.wLongAry) &&
                    Arrays.equals(wFloatAry, that.wFloatAry) &&
                    Arrays.equals(wDoubleAry, that.wDoubleAry) &&
                    numEnum == that.numEnum &&
                    strEnum == that.strEnum &&
                    lowerStrEnum == that.lowerStrEnum;
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(mByte, mShort, mInt, mLong, mFloat, mDouble, mBoolean, wShort, wInteger, wLong, wFloat, wDouble, wBigInteger, wBigDecimal, wString, numEnum, strEnum, lowerStrEnum);
            result = 31 * result + Arrays.hashCode(mByteAry);
            result = 31 * result + Arrays.hashCode(mShortAry);
            result = 31 * result + Arrays.hashCode(mIntAry);
            result = 31 * result + Arrays.hashCode(mLongAry);
            result = 31 * result + Arrays.hashCode(mFloatAry);
            result = 31 * result + Arrays.hashCode(mDoubleAry);
            result = 31 * result + Arrays.hashCode(wShortAry);
            result = 31 * result + Arrays.hashCode(wIntegerAry);
            result = 31 * result + Arrays.hashCode(wLongAry);
            result = 31 * result + Arrays.hashCode(wFloatAry);
            result = 31 * result + Arrays.hashCode(wDoubleAry);
            return result;
        }

    }

}
