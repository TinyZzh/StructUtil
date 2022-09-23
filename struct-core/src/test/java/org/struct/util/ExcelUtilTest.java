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

package org.struct.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
            WorkerUtil.newListOnly(Object.class);
        } catch (Exception e) {
            return;
        }
        Assertions.fail();
    }

    @Test
    public void testNewInterface() throws Exception {
        Collection<Object> objects = WorkerUtil.newListOnly(List.class);
        Assertions.assertTrue(ArrayList.class.isAssignableFrom(objects.getClass()));
    }

    @Test
    public void testNewAbstractClass() throws Exception {
        Collection<Object> objects = WorkerUtil.newListOnly(AbstractList.class);
        Assertions.assertTrue(ArrayList.class.isAssignableFrom(objects.getClass()));
    }

    @Test
    public void testNewArrayList() throws Exception {
        Collection<Object> objects = WorkerUtil.newListOnly(ArrayList.class);
        Assertions.assertTrue(ArrayList.class.isAssignableFrom(objects.getClass()));
        Assertions.assertTrue(HashSet.class.isAssignableFrom(WorkerUtil.newListOnly(HashSet.class).getClass()));
    }
}