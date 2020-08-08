/*
Copyright (c) 2020 John C. Franco

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.github.johncfranco.reactive.logger.examples.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] parameters) {
        final SpringApplication application = new SpringApplication(SpringConfiguration.class);
        application.setWebApplicationType(WebApplicationType.REACTIVE);

        final ConfigurableApplicationContext applicationContext = application.run(parameters);
        final EphemeralPortRetriever portRetriever = applicationContext.getBean(EphemeralPortRetriever.class);

        log.info("Listening on port {}.", portRetriever.getRetrievedPort());
    }
}
