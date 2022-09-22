/**
 * @author TinyZ.
 * @version 2022.08.17
 */
module struct.spring.boot {
    exports org.struct.spring.boot.autoconfigure;

    // internal
    requires struct.core;
    requires struct.spring;

    requires org.slf4j;
    requires spring.context;
    requires spring.core;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.starter;
    requires spring.boot.actuator;
    requires spring.boot.actuator.autoconfigure;
    requires spring.boot.autoconfigure;
}