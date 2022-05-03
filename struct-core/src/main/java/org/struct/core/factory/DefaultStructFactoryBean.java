package org.struct.core.factory;

import org.struct.core.StructWorker;
import org.struct.spi.SPI;

/**
 * @author TinyZ
 * @date 2022-04-14
 */
@SPI(name = "default", order = 0)
public class DefaultStructFactoryBean implements StructFactoryBean {

    private static boolean enableRecord;

    static {
        try {
            Class.forName("java.lang.Record");
            enableRecord = true;
        } catch (Exception e) {
            //  no-op
            enableRecord = false;
        }
    }

    @Override
    public <T> StructFactory newInstance(Class<T> clzOfStruct, StructWorker<T> worker) {
        if (enableRecord && clzOfStruct.isRecord()) {
            return new RecordStructFactory(clzOfStruct, worker);
        }
        return new JdkStructFactory(clzOfStruct, worker);
    }

}
