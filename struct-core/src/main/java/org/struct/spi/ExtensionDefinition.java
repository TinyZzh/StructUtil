package org.struct.spi;

/**
 * The SPI extension definition
 *
 * @param service      the extension's name
 * @param clzOfService the extension's class
 * @param order        the extension's order'
 * @author TinyZ
 * @date 2022-05-07
 * @see SPI
 * @see ServiceLoader
 */
public record ExtensionDefinition(
        String service,
        Class<?> clzOfService,
        int order
) implements Comparable<ExtensionDefinition> {

    @Override
    public int compareTo(ExtensionDefinition o) {
        return Integer.compare(this.order(), o.order());
    }
}
