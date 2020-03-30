package org.struct.binary;

import org.junit.Test;
import org.struct.core.StructWorker;
import org.struct.core.handler.XmlStructHandlerTest;
import org.struct.util.WorkerUtil;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertFalse;

public class FileWatcherServiceTest {

    @Test
    public void test() throws IOException, InterruptedException {
        FileWatcherService fws = new FileWatcherService();
        Runnable runnable = () -> {
            StructWorker<XmlStructHandlerTest.VipConfigSyncBean> worker = WorkerUtil.newWorker("./examples/", XmlStructHandlerTest.VipConfigSyncBean.class);
            ArrayList<XmlStructHandlerTest.VipConfigSyncBean> beans = worker.toList(ArrayList::new);
            assertFalse(beans.isEmpty());
        };
        fws.register("./examples/")
                .registerHook("./examples/tpl_vip.xml", runnable)
                .bootstrap();
//        while (true) {
//            Thread.sleep(10000);
//        }
    }

}