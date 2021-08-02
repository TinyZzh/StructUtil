/*
 *
 *
 *          Copyright (c) 2021. - TinyZ.
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

package org.struct.core;

import org.struct.annotation.StructSheet;
import org.struct.core.filter.StructBeanFilter;
import org.struct.core.matcher.WorkerMatcher;
import org.struct.util.AnnotationUtils;

import java.io.Serializable;
import java.util.Objects;

public class StructDescriptor implements Serializable {

    private static final long serialVersionUID = -6216313967633389888L;

    private String fileName;
    private String sheetName;
    private int startOrder;
    private int endOrder;
    private Class<? extends WorkerMatcher> matcher;
    private Class<? extends StructBeanFilter> filter;

    public StructDescriptor() {
    }

    public StructDescriptor(Class<?> clzOfStruct) {
        StructSheet annotation = AnnotationUtils.findAnnotation(StructSheet.class, clzOfStruct);
        if (null == annotation) {
            throw new IllegalArgumentException("clazz:" + clzOfStruct.getName() + " must be annotated by @StructSheet");
        }
        this.fileName = annotation.fileName();
        this.sheetName = annotation.sheetName();
        this.startOrder = annotation.startOrder();
        this.endOrder = annotation.endOrder();
        this.matcher = annotation.matcher();
        this.filter = annotation.filter();
    }

    public StructDescriptor(String fileName, String sheetName, int startOrder, int endOrder,
                            Class<? extends WorkerMatcher> matcher, Class<? extends StructBeanFilter> filter) {
        this.fileName = fileName;
        this.sheetName = sheetName;
        this.startOrder = startOrder;
        this.endOrder = endOrder;
        this.matcher = matcher;
        this.filter = filter;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public int getStartOrder() {
        return startOrder;
    }

    public void setStartOrder(int startOrder) {
        this.startOrder = startOrder;
    }

    public int getEndOrder() {
        return endOrder;
    }

    public void setEndOrder(int endOrder) {
        this.endOrder = endOrder;
    }

    public Class<? extends WorkerMatcher> getMatcher() {
        return matcher;
    }

    public void setMatcher(Class<? extends WorkerMatcher> matcher) {
        this.matcher = matcher;
    }

    public Class<? extends StructBeanFilter> getFilter() {
        return filter;
    }

    public void setFilter(Class<? extends StructBeanFilter> filter) {
        this.filter = filter;
    }

    @Override
    public String toString() {
        return "StructDescriptor{" +
                "fileName='" + fileName + '\'' +
                ", sheetName='" + sheetName + '\'' +
                ", startOrder=" + startOrder +
                ", endOrder=" + endOrder +
                ", matcher=" + matcher +
                ", filter=" + filter +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StructDescriptor that = (StructDescriptor) o;
        return startOrder == that.startOrder &&
                endOrder == that.endOrder &&
                Objects.equals(fileName, that.fileName) &&
                Objects.equals(sheetName, that.sheetName) &&
                Objects.equals(matcher, that.matcher) &&
                Objects.equals(filter, that.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, sheetName, startOrder, endOrder, matcher, filter);
    }
}
