package org.struct.spring.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


/**
 * @author TinyZ.
 * @version 2020.07.18
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = StructScannerRegistrarTest.class)
@Configuration
@ComponentScan(basePackages = "org.struct.spring.support")
@StructScan(basePackages = "org.struct.spring.support")
public class StructScannerRegistrarTest implements ApplicationContextAware {

    private ApplicationContext ctx;

    @Test
    public void test() {
        Object bean = ctx.getBean("myStructStructMapper");
        System.out.println();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }
}