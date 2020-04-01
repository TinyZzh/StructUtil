package org.struct.core.handler;

import org.junit.Assert;
import org.junit.Test;
import org.struct.annotation.StructSheet;
import org.struct.core.StructWorker;
import org.struct.core.filter.StructBeanFilter;
import org.struct.util.WorkerUtil;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.function.Consumer;

public class XmlStructHandlerTest {

    @Test
    public void test() {
        StructWorker<VipConfigSyncBean> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", VipConfigSyncBean.class);
        ArrayList<VipConfigSyncBean> beans = worker.toList(ArrayList::new);
        Assert.assertEquals(10, beans.size());
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


    @Test
    public void testLoadWithBeanFilter() {
        StructWorker<VipConfigSyncBeanWithFilter> worker = WorkerUtil.newWorker("classpath:/org/struct/core/", VipConfigSyncBeanWithFilter.class);
        ArrayList<VipConfigSyncBeanWithFilter> beans = worker.toList(ArrayList::new);
        Assert.assertEquals(8, beans.size());
    }

    public static class MyFilter extends StructBeanFilter<VipConfigSyncBeanWithFilter> {

        /**
         * the constructor must be implement.
         *
         * @param cellHandler the real cell handler.
         */
        public MyFilter(Consumer<VipConfigSyncBeanWithFilter> cellHandler) {
            super(cellHandler);
        }

        @Override
        public boolean test(VipConfigSyncBeanWithFilter vipConfigSyncBean) {
            return vipConfigSyncBean.lv > 2;
        }
    }

    @XmlRootElement(name = "child")
    @StructSheet(fileName = "tpl_vip.xml", startOrder = 2, filter = MyFilter.class)
    public static class VipConfigSyncBeanWithFilter {
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