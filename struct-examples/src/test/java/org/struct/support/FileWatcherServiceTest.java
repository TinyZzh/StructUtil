package org.struct.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.struct.annotation.StructSheet;
import org.struct.core.StructWorker;
import org.struct.util.WorkerUtil;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class FileWatcherServiceTest {

    @Test
    public void test() throws IOException, InterruptedException {
        FileWatcherService fws = new FileWatcherService();
        Runnable runnable = () -> {
            StructWorker<VipConfigSyncBean> worker = WorkerUtil.newWorker("./examples/", VipConfigSyncBean.class);
            List<VipConfigSyncBean> beans = worker.toList(ArrayList::new);
            Assertions.assertFalse(beans.isEmpty());
        };
        fws.register("./examples/")
                .registerHook("./examples/tpl_vip.xml", runnable)
                .setScheduleInitialDelay(10L)
                .setScheduleDelay(1L)
                .setScheduleTimeUnit(TimeUnit.MILLISECONDS)
                .bootstrap();
//        while (true) {
//            Thread.sleep(10000);
//        }
    }

    @Test
    public void process() throws IOException, NoSuchFieldException, IllegalAccessException {
        FileWatcherService fws = new FileWatcherService();

        WatchService ws = mock(WatchService.class);
        {
            Field field = FileWatcherService.class.getDeclaredField("ws");
            field.setAccessible(true);
            field.set(fws, ws);
        }
        Map<WatchKey, Path> keys = spy(new ConcurrentHashMap<>());
        {
            Field field = FileWatcherService.class.getDeclaredField("keys");
            field.setAccessible(true);
            field.set(fws, keys);
        }
        WatchKey wk = mock(WatchKey.class);
        Path thePath = mock(Path.class, RETURNS_DEEP_STUBS);
        WatchEvent<Path> we = mock(WatchEvent.class, RETURNS_DEEP_STUBS);
        doReturn(Collections.singletonList(we)).when(wk).pollEvents();
        doReturn(wk).when(ws).poll();
        doReturn(thePath).when(keys).get(any(WatchKey.class));
        fws.run();
    }

    @Test
    public void testRegisterAll() throws IOException {
        FileWatcherService fws = new FileWatcherService();
        String path = "./";
        fws.registerAll(path);
        fws.registerAll(Paths.get(path));
    }

    @Test
    public void testRegisterHook() throws IOException {
        FileWatcherService fws = new FileWatcherService();
        String path = "./";
        fws.registerHook(Paths.get(path), () -> {
        });
        fws.registerHook(path, () -> {
        });
        fws.deregisterHook(Paths.get(path));
        fws.deregisterHook(path);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetScheduleInitialDelay() throws IOException {
        FileWatcherService fws = new FileWatcherService();
        fws.setScheduleInitialDelay(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetScheduleDelay() throws IOException {
        FileWatcherService fws = new FileWatcherService();
        fws.setScheduleDelay(-1);
    }

    @Test(expected = NullPointerException.class)
    public void testSetScheduleTimeUnit() throws IOException {
        FileWatcherService fws = new FileWatcherService();
        fws.setScheduleTimeUnit(null);
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