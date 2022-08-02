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

package org.struct.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.struct.annotation.StructSheet;
import org.struct.core.StructWorker;
import org.struct.util.WorkerUtil;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FileWatcherServiceTest {

    @Test
    public void test() throws IOException, InterruptedException {
        FileWatcherService fws = FileWatcherService.newBuilder()
                .setScheduleInitialDelay(10L)
                .setScheduleDelay(1L)
                .setScheduleTimeUnit(TimeUnit.MILLISECONDS)
                .build();
        Runnable runnable = () -> {
            StructWorker<VipConfigSyncBean> worker = WorkerUtil.newWorker("./examples/", VipConfigSyncBean.class);
            List<VipConfigSyncBean> beans = worker.toList(ArrayList::new);
            Assertions.assertFalse(beans.isEmpty());
        };
        fws.register("./examples/")
                .registerHook("./examples/tpl_vip.xml", runnable)
                .bootstrap();
    }

    @XmlRootElement(name = "child")
    @StructSheet(fileName = "tpl_vip.xml", startOrder = 1)
    public static class VipConfigSyncBean {
        public int gold;
        public int lv;
        public int activityId;
        public int addExp;
        public int addRpgExp;
        public int goldPoint;
        public boolean isLoginBroadcast = false;   //  是否登录广播
        public boolean isForceEnterChannel = false;   //  是否挤房间
        /**
         * 减免买家税收. 单位：百分比
         */
        public BigDecimal taxDiscountForBuyer;
        /**
         * 减免卖家税收. 单位：百分比
         */
        public BigDecimal taxDiscountForSeller;
        /**
         * extra player buy gold amount every week.
         */
        public int extraMarketBuyGold;
        /**
         * 王者祝福使用次数加成百分比
         */
        public int addKingScuffleNum;
    }
}