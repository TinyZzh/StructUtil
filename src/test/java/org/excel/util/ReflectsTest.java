package org.excel.util;

import org.junit.Assert;
import org.junit.Test;

public class ReflectsTest {

    @Test
    public void testNewInstance() {
        //  [fail] nested class
        try {
            Reflects.newInstance(Apple.class);
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), IllegalArgumentException.class);
        }
        //  [suc] static class
        StaticApple apple = Reflects.newInstance(StaticApple.class);
        Assert.assertNotNull(apple);
        //  [suc] parameter class
        Pyrus pyrus = Reflects.newInstance(Pyrus.class, 10);
        Assert.assertNotNull(pyrus);
        //  [fail] wrong parameter count
        try {
            Reflects.newInstance(Pyrus.class, 10, 1000);
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), IllegalArgumentException.class);
        }
        //  [fail] wrong parameter type
        try {
            Reflects.newInstance(Pyrus.class, "11");
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), IllegalArgumentException.class);
        }
        //  [suc]
        Orange orange = Reflects.newInstance(Orange.class);
        Assert.assertNotNull(orange);
        //  [fail]  constructor throw exception
        try {
            Reflects.newInstance(OnlyException.class);
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), Exception.class);
        }
        //  [fail]  private constructor throw exception
        try {
            Reflects.newInstance(OnlyPriParamException.class, "xx");
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), Exception.class);
        }
    }

    public class Apple {
    }

    public static class StaticApple {
    }

    public static class Pyrus {
        private Integer weight;

        public Pyrus(Integer weight) {
            this.weight = weight;
        }
    }

    public static class Orange {
        private Orange() {
        }
    }

    public static class OnlyException {
        public OnlyException() throws Exception {
            throw new Exception();
        }
    }

    public static class OnlyPriException {
        private OnlyPriException() throws Exception {
            throw new Exception();
        }
    }

    public static class OnlyPriParamException {
        private String str;

        private OnlyPriParamException(String str) throws Exception {
            this.str = str;
            throw new Exception();
        }
    }
}