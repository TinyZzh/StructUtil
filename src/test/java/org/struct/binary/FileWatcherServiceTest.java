package org.struct.binary;

import org.junit.Assert;
import org.junit.Test;
import org.struct.core.StructWorker;
import org.struct.core.handler.XmlStructHandlerTest;
import org.struct.util.WorkerUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class FileWatcherServiceTest {

    @Test
    public void test() throws IOException, InterruptedException {
        FileWatcherService fws = new FileWatcherService();
        fws.withWorkspace("./examples/");
         Runnable runnable = () -> {
            StructWorker<XmlStructHandlerTest.VipConfigSyncBean> worker = WorkerUtil.newWorker("./examples/", XmlStructHandlerTest.VipConfigSyncBean.class);
            ArrayList<XmlStructHandlerTest.VipConfigSyncBean> beans = worker.toList(ArrayList::new);
            assertFalse(beans.isEmpty());
        };
        fws.registerHook("tpl_vip.xml", runnable);
        runnable.run();
        while (true) {
            Thread.sleep(10000);
        }
    }

}