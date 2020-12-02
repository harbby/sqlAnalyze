/*
 * Copyright (C) 2019 The Time2DB Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.harbby.time2db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.harbby.gadtry.ioc.Autowired;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.MultipartConfigElement;

import static java.util.Objects.requireNonNull;

public class JettyServer
{
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(JettyServer.class);
    private Server server;
    private final SqlAnalyzeContext sqlAnalyzeContext;

    @Autowired
    public JettyServer(
            SqlAnalyzeContext sqlAnalyzeContext
    )
    {
        this.sqlAnalyzeContext = requireNonNull(sqlAnalyzeContext, "time2dbContext is null");
    }

    public void start()
            throws Exception
    {
        int jettyPort = 8080;

        this.server = new Server(jettyPort);
        server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize",
                1000);

        HandlerList handlers = loadHandlers();  //加载路由
        server.setHandler(handlers);
        logger.info("web Server started... the port {}", jettyPort);
        server.start();
    }

    private HandlerList loadHandlers()
    {
        HandlerList handlers = new HandlerList();
        ServletHolder servlet = new ServletHolder(new ServletContainer(new WebApplication()));
        //1M = 1048576
        servlet.getRegistration().setMultipartConfig(new MultipartConfigElement("data/tmp", 1048576_00, 1048576_00, 262144));

        //--------------------plblic----------------------
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);  //NO_SESSIONS
        contextHandler.setContextPath("/");
        contextHandler.setAttribute("time2dbContext", sqlAnalyzeContext);

        //-------add jersey--------
        contextHandler.addServlet(servlet, "/api/*");

        final ServletHolder staticServlet = new ServletHolder(new DefaultServlet());
        contextHandler.addServlet(staticServlet, "/css/*");
        contextHandler.addServlet(staticServlet, "/js/*");
        contextHandler.addServlet(staticServlet, "/images/*");
        contextHandler.addServlet(staticServlet, "/fonts/*");
        contextHandler.addServlet(staticServlet, "/favicon.ico");
        contextHandler.addServlet(staticServlet, "/");
        contextHandler.setResourceBase("webapp");

        handlers.addHandler(contextHandler);
        return handlers;
    }
}
