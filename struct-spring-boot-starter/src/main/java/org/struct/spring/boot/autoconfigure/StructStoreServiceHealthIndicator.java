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

package org.struct.spring.boot.autoconfigure;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.struct.spring.support.StructBanner;
import org.struct.spring.support.StructStoreService;

/**
 * @author TinyZ.
 * @date 2021-06-30.
 */
public class StructStoreServiceHealthIndicator extends AbstractHealthIndicator {

    private StructStoreService storeService;

    public StructStoreServiceHealthIndicator(StructStoreService storeService) {
        super("StructStoreService health check failed");
        this.storeService = storeService;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        if (storeService.isEmpty()) {
            builder.outOfService().withDetail("service is empty", storeService);
        } else {
            builder.up()
                    .withDetail("version", StructBanner.INSTANCE.getVersion())
                    .withDetail("enabled", !storeService.isEmpty());
        }
    }
}
