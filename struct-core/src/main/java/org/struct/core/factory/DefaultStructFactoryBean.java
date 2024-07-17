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

package org.struct.core.factory;

import org.struct.core.StructWorker;
import org.struct.spi.SPI;

/**
 * @author TinyZ
 * @date 2022-04-14
 */
@SPI(name = "default", order = 0)
public class DefaultStructFactoryBean implements StructFactoryBean {

    @Override
    public <T> StructFactory newInstance(Class<T> clzOfStruct, StructWorker<T> worker) {
        return new JdkStructFactory(clzOfStruct, worker);
    }
}
