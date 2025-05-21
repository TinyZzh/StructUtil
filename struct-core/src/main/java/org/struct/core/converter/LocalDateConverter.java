/*
 *
 *
 *          Copyright (c) 2024. - TinyZ.
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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * @author TinyZ.
 * @date 2020-09-15.
 */
public class LocalDateConverter implements Converter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalDateConverter.class);

    private ZoneId zoneId = ZoneId.systemDefault();

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Strings.DATE_FORMAT_PATTERN);

    @Override
    public Object convert(ConvertContext ctx, Object originValue, Class<?> targetType) {
        if (LocalDate.class != targetType
                || LocalDate.class == originValue.getClass()) {
            return originValue;
        } else if (originValue instanceof Number) {
            //  convert timestamp to local date.
            long mills = ((Number) originValue).longValue();
            if (originValue instanceof Integer)
                mills *= 1000L;
            return this.parseInstant(Instant.ofEpochMilli(mills));
        } else {
            String str = String.valueOf(originValue);
            try {
                return LocalDate.parse(str, formatter);
            } catch (DateTimeParseException e1) {
                //  no-op
            }
            try {
                long mills = Long.parseLong(str);
                return this.parseInstant(mills < Integer.MAX_VALUE ? Instant.ofEpochSecond(mills) : Instant.ofEpochMilli(mills));
            } catch (NumberFormatException e) {
                //  no-op
            }
        }
        LOGGER.warn("unresolvable originValue:{}, targetType:{}", originValue, targetType);
        return originValue;
    }

    private LocalDate parseInstant(Instant instant) {
        return instant.atZone(zoneId).toLocalDate();
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }

    public void setFormatter(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }
}
