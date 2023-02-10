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

package org.struct.spring.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

/**
 * Struct Banner.
 *
 * @author TinyZ.
 * @date 2020-08-29.
 */
public enum StructBanner {

    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(StructBanner.class);

    /**
     * font: doom
     * content: "*Struct>>>"
     */
    private static final String[] BANNER = {
            "    _    _____ _                   _  ______   ",
            " /\\| |/\\/  ___| |                 | | \\ \\ \\ \\  ",
            " \\ ` ' /\\ `--.| |_ _ __ _   _  ___| |_ \\ \\ \\ \\ ",
            "|_     _|`--. \\ __| '__| | | |/ __| __| > > > >",
            " / , . \\/\\__/ / |_| |  | |_| | (__| |_ / / / / ",
            " \\/|_|\\/\\____/ \\__|_|   \\__,_|\\___|\\__/_/_/_/  "
    };

    private static final String STRUCT_STORE_SERVICE = " :: Struct Store Service :: ";
    private static final String VERSION = loadVersionProperties();

    /**
     * Print struct store service banner.
     */
    public void print() {
        PrintStream ps = System.out;
        for (String line : BANNER) {
            ps.println(line);
        }
        ps.println(STRUCT_STORE_SERVICE + "    (" + getVersion() + ")");
    }

    /**
     * Get struct library version.
     *
     * @return Struct library version.
     */
    public String getVersion() {
        return VERSION;
    }

    static String loadVersionProperties() {
        Properties prop = new Properties();
        try (InputStream in = StructBanner.class.getResourceAsStream("/META-INF/maven/org.structutil/struct-spring/pom.properties")) {
            prop.load(in);
            return prop.getProperty("version", "Unknown");
        } catch (Throwable e) {
            LOGGER.error("");
        }
        return "Unknown";
    }

}
