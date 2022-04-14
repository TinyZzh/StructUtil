package org.struct.core.factory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.struct.annotation.StructField;
import org.struct.annotation.StructOptional;
import org.struct.annotation.StructSheet;
import org.struct.core.StructWorker;
import org.struct.util.WorkerUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

;

/**
 * @author TinyZ
 * @date 2022-04-14
 */
class RecordStructFactoryTest {

    @Test
    public void testNormal() {
        StructWorker<MapWithGroup1> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", MapWithGroup1.class);
        ArrayList<MapWithGroup1> list = worker.load(ArrayList::new);
        System.out.println();
    }

    @StructSheet(fileName = "Bean.xlsx", sheetName = "MapWithGroup1")
    public record MapWithGroup1(
            String vg,
            int group,
            int v
    ) {

    }

    @Test
    public void testRefField() {
        RefFieldRef ref = new RefFieldRef(1, 998);
        RefFieldBean var0 = new RefFieldBean(1, ref);

        StructWorker<RefFieldBean> worker = new StructWorker<>("classpath:/org/struct/core/", RefFieldBean.class);
        ArrayList<RefFieldBean> list = worker.toList(ArrayList::new);
        RefFieldBean bean = list.get(0);
        Assertions.assertEquals(var0, bean);
    }

    @StructSheet(fileName = "examples.xlsx", sheetName = "example_1")
    record RefFieldBean(
            @StructField(name = "id") int id,
            @StructField(ref = RefFieldRef.class, refUniqueKey = "id") RefFieldRef ref
    ) {
        static int ids;
    }

    @StructSheet(fileName = "examples.xlsx", sheetName = "example_2")
    record RefFieldRef(int id, long value) {
    }

    @Test
    public void testOptionalField() {
        OptionalFieldBean var0 = new OptionalFieldBean(1, new OptionalFieldRef1(1, 998));
        OptionalFieldBean var1 = new OptionalFieldBean(2, new OptionalFieldRef2(2, 997));

        StructWorker<OptionalFieldBean> worker = new StructWorker<>("classpath:/org/struct/core/", OptionalFieldBean.class);
        ArrayList<OptionalFieldBean> list = worker.toList(ArrayList::new);
        Assertions.assertEquals(var0, list.get(0));
        Assertions.assertEquals(var1, list.get(1));
    }

    @StructSheet(fileName = "examples.xlsx", sheetName = "example_1")
    record OptionalFieldBean(
            @StructField(name = "id")
            int id,
            @StructOptional(value = {
                    @StructField(ref = OptionalFieldRef1.class, refUniqueKey = "id"),
                    @StructField(ref = OptionalFieldRef2.class, refUniqueKey = "id")
            }) Object ref
    ) {
    }

    @StructSheet(fileName = "examples.xlsx", sheetName = "example_2")
    record OptionalFieldRef1(int id, long value) {
    }

    @StructSheet(fileName = "examples.xlsx", sheetName = "example_3")
    record OptionalFieldRef2(int id, long value) {
    }

    @Test()
    public void testBasicType() {
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

    @StructSheet(fileName = "examples.xlsx", sheetName = "example_1")
    record SimpleTypeFieldBean(
            //  primary type
            byte mByte,
            short mShort,
            int mInt,
            long mLong,
            float mFloat,
            double mDouble,
            boolean mBoolean,
            //  wrap type
            Short wShort,
            Integer wInteger,
            Long wLong,
            Float wFloat,
            Double wDouble,
            BigInteger wBigInteger,
            BigDecimal wBigDecimal,
            //  string
            String wString,
            //  primary type array
            byte[] mByteAry,
            short[] mShortAry,
            int[] mIntAry,
            long[] mLongAry,
            float[] mFloatAry,
            double[] mDoubleAry,
            //  wrap type array
            Short[] wShortAry,
            Integer[] wIntegerAry,
            Long[] wLongAry,
            Float[] wFloatAry,
            Double[] wDoubleAry,
            //  enum
            MyEnum numEnum,
            MyEnum strEnum,
            MyEnum lowerStrEnum
    ) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SimpleTypeFieldBean that = (SimpleTypeFieldBean) o;
            return mByte == that.mByte && mShort == that.mShort && mInt == that.mInt && mLong == that.mLong && Float.compare(that.mFloat, mFloat) == 0 && Double.compare(that.mDouble, mDouble) == 0 && mBoolean == that.mBoolean && Objects.equals(wShort, that.wShort) && Objects.equals(wInteger, that.wInteger) && Objects.equals(wLong, that.wLong) && Objects.equals(wFloat, that.wFloat) && Objects.equals(wDouble, that.wDouble) && Objects.equals(wBigInteger, that.wBigInteger) && Objects.equals(wBigDecimal, that.wBigDecimal) && Objects.equals(wString, that.wString) && Arrays.equals(mByteAry, that.mByteAry) && Arrays.equals(mShortAry, that.mShortAry) && Arrays.equals(mIntAry, that.mIntAry) && Arrays.equals(mLongAry, that.mLongAry) && Arrays.equals(mFloatAry, that.mFloatAry) && Arrays.equals(mDoubleAry, that.mDoubleAry) && Arrays.equals(wShortAry, that.wShortAry) && Arrays.equals(wIntegerAry, that.wIntegerAry) && Arrays.equals(wLongAry, that.wLongAry) && Arrays.equals(wFloatAry, that.wFloatAry) && Arrays.equals(wDoubleAry, that.wDoubleAry) && numEnum == that.numEnum && strEnum == that.strEnum && lowerStrEnum == that.lowerStrEnum;
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

    enum MyEnum {
        One,
        Two,
        Three;
    }
}