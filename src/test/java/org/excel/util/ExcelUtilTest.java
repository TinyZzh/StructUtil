package org.excel.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class ExcelUtilTest {

    @Test
    public void testNewListOnlyException() throws Exception {
        //
        try {
            ExcelUtil.newListOnly(Object.class);
        } catch (Exception e) {
            return;
        }
        Assert.fail();
    }

    @Test
    public void testNewInterface() throws Exception {
        Collection<Object> objects = ExcelUtil.newListOnly(List.class);
        Assert.assertTrue(ArrayList.class.isAssignableFrom(objects.getClass()));
    }

    @Test
    public void testNewAbstractClass() throws Exception {
        Collection<Object> objects = ExcelUtil.newListOnly(AbstractList.class);
        Assert.assertTrue(ArrayList.class.isAssignableFrom(objects.getClass()));
    }

    @Test
    public void testNewArrayList() throws Exception {
        Collection<Object> objects = ExcelUtil.newListOnly(ArrayList.class);
        Assert.assertTrue(ArrayList.class.isAssignableFrom(objects.getClass()));
        Assert.assertTrue(HashSet.class.isAssignableFrom(ExcelUtil.newListOnly(HashSet.class).getClass()));
    }


}