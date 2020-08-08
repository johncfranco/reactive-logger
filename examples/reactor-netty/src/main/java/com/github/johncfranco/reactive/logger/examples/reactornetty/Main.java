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

package com.github.johncfranco.reactive.logger.examples.reactornetty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.net.InetAddress;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] parameters) throws Exception {
        final InetAddress localAddress = InetAddress.getLocalHost();
        final DelayService delayService = new DelayService();
        final LogContextHttpMethodWrapper logContext = LogContextHttpMethodWrapper.builder().build();
        final DisposableServer delayServer = HttpServer.create()
                .host(localAddress.getHostAddress())
                .port(0)
                .forwarded(true)
                .route(routes -> routes.get(DelayService.DELAY_PATH, logContext.wrap(delayService::delayRequest)))
                .bindNow();

        log.info("Listening on {}:{}...", delayServer.host(), delayServer.port());
        delayServer.onDispose().block();
    }
}
