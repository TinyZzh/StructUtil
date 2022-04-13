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

import java.io.PrintStream;

/**
 * Banner.
 *
 * @author TinyZ.
 * @date 2020-08-29.
 */
public enum StructBanner {

    INSTANCE;

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
    private static final String VERSION = "3.5.3.beta-SNAPSHOT";

    /**
     * Print struct store service banner.
     */
    public void print() {
        PrintStream ps = System.out;
        for (String line : BANNER) {
            ps.println(line);
        }
        ps.println(STRUCT_STORE_SERVICE + "    (" + VERSION + ")");
    }

    /**
     * Get struct library version.
     *
     * @return Struct library version.
     */
    public String getVersion() {
        return VERSION;
    }

}
