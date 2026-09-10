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

package org.struct.store;

/**
 * The {@link StructStore}'s options. a plain mutable java bean so that it can be
 * injected by any container (spring, guice) or built by hand.
 * <p>
 * NOTE: this class intentionally has no {@code generate(...)} factory. assembling the
 * options from a framework specific configuration is the framework's responsibility,
 * see {@code org.struct.spring.support.StoreOptionsFactory}.
 *
 * @author TinyZ.
 * @version 2020.09.19
 */
public class StoreOptions {

    private String workspace = StoreConstant.STRUCT_WORKSPACE;

    private boolean lazyLoad = false;

    private boolean waitForInit = false;

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public boolean isLazyLoad() {
        return lazyLoad;
    }

    public void setLazyLoad(boolean lazyLoad) {
        this.lazyLoad = lazyLoad;
    }

    public boolean isWaitForInit() {
        return waitForInit;
    }

    public void setWaitForInit(boolean waitForInit) {
        this.waitForInit = waitForInit;
    }
}
