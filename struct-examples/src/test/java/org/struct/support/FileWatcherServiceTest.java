package org.struct.support;

import org.junit.Test;
import org.struct.annotation.StructSheet;
import org.struct.core.StructWorker;
import org.struct.util.WorkerUtil;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;

public class FileWatcherServiceTest {

    @Test
    public void test() throws IOException, InterruptedException {
        FileWatcherService fws = new FileWatcherService();
        Runnable runnable = () -> {
            StructWorker<VipConfigSyncBean> worker = WorkerUtil.newWorker("./examples/", VipConfigSyncBean.class);
            List<VipConfigSyncBean> beans = worker.toList(ArrayList::new);
            assertFalse(beans.isEmpty());
        };
        fws.register("./examples/")
                .registerHook("./examples/tpl_vip.xml", runnable)
                .bootstrap();
//        while (true) {
//            Thread.sleep(10000);
//        }
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