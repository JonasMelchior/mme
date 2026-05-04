package com.github.jonasmelchior.mymme.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "mme.s6a")
public interface S6aConfig {

    @WithDefault("hss.test.com")
    String destinationHost();

    @WithDefault("hss.test.com")
    String destinationRealm();

    @WithDefault("mme.test.com")
    String originHost();

    @WithDefault("mme.test.com")
    String originRealm();

    /**
     * Visited PLMN ID in hex format (e.g. 32F810)
     */
    @WithDefault("32F810")
    String visitedPlmnId();

    @WithDefault("10415")
    long vendorId();

    @WithDefault("16777251")
    long authAppId();
}
