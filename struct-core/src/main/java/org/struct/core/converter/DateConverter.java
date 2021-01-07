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

package org.struct.core.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.struct.util.Strings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * {@link Date}'s converter.
 *
 * @author TinyZ.
 * @date 2020-09-15.
 * @deprecated
 */
@Deprecated
public class DateConverter implements Converter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateConverter.class);

    private String formatPattern = Strings.DATE_TIME_FORMAT_PATTERN;

    @Override
    public Object convert(Object originValue, Class<?> targetType) {
        if (Date.class != targetType
                || Date.class == originValue.getClass()) {
            return originValue;
        } else if (originValue instanceof Number) {
            long mills = ((Number) originValue).longValue();
            if (originValue instanceof Integer)
                mills *= 1000L;
            return new Date(mills);
        } else {
            String str = String.valueOf(originValue);
            try {
                return new Date(Date.parse(str));
            } catch (IllegalArgumentException e) {
                //  no-op
            }
            try {
                SimpleDateFormat format = new SimpleDateFormat(formatPattern);
                return format.parse(str);
            } catch (IllegalArgumentException | ParseException e1) {
                //  no-op
            }
            try {
                long mills = Long.parseLong(str);
                return mills < Integer.MAX_VALUE ? new Date(mills * 1000L) : new Date(mills);
            } catch (NumberFormatException e) {
                //  no-op
            }
        }
        LOGGER.warn("unresolvable originValue:{}, targetType:{}", originValue, targetType);
        return originValue;
    }

    public String getFormatPattern() {
        return formatPattern;
    }

    public void setFormatPattern(String formatPattern) {
        this.formatPattern = formatPattern;
    }
}
