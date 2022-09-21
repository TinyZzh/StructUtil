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

package org.struct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.struct.spring.support.StructStoreService;

/**
 * @author TinyZ.
 * @date 2020-07-22.
 */
@SpringBootApplication(scanBasePackages = "org.struct",
        exclude = {
                DataSourceAutoConfiguration.class
        }
)
// @PropertySource("file:config/version.properties")
@ImportResource(value = {
        "/org/struct/examples/beans.xml"
})
public class MyApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(MyApplication.class, args);
        StructStoreService service = ctx.getBean(StructStoreService.class);
        System.out.println();
    }
}
