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

package org.struct.spring.boot.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.struct.spring.support.StructStoreService;

import static org.mockito.Mockito.*;

/**
 * @author TinyZ.
 * @version 2022.05.03
 */
class StructStoreServiceHealthIndicatorTest {

    @Test
    public void test() throws Exception {
        StructStoreService service = mock(StructStoreService.class);
        doReturn(true, false).when(service).isEmpty();
        StructStoreServiceHealthIndicator indicator = new StructStoreServiceHealthIndicator(service);
        Health.Builder builder = spy(new Health.Builder());
        indicator.doHealthCheck(builder);
        verify(builder, times(1)).outOfService();

        indicator.doHealthCheck(builder);
        verify(builder, times(1)).up();
    }

}