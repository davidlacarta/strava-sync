package com.davidlacarta.strava.sync;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * StravaSync Application Servlet Initializer. Required to generate and load a war instead of a jar.
 */
public class StravaSyncApplicationServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(StravaSyncApplication.class);
    }

}
