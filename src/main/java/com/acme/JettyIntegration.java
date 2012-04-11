/*
 * Copyright (c) 2012 Andrej Golovnin. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of Andrej Golovnin nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.acme;

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * @author Andrej Golovnin
 */
public final class JettyIntegration implements ServletContextListener {

    // Fields *****************************************************************

    private Server server;


    // ServletContextListener Implementation **********************************

    public void contextInitialized(ServletContextEvent sce) {
        String jbossWebHost;
        Integer jbossWebPort;

        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName http =
                new ObjectName("jboss.as:socket-binding-group=standard" +
                            "-sockets,socket-binding=http");
            jbossWebHost =
                (String) mBeanServer.getAttribute(http, "boundAddress");
            jbossWebPort = (Integer) mBeanServer.getAttribute(http, "boundPort");
        } catch (Exception e) {
            throw new Error(e);
        }

        server = new Server();

        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setHost(jbossWebHost);
        connector.setPort(8181);
        server.addConnector(connector);

        ServletContextHandler proxy = new ServletContextHandler(
            ServletContextHandler.SESSIONS);
        proxy.setContextPath("/");
        proxy.addServlet(
            new ServletHolder(new TransparentProxy(jbossWebHost, jbossWebPort)),
            "/*");

        ServletContextHandler ws = new ServletContextHandler(
            ServletContextHandler.SESSIONS);
        ws.setContextPath("/test/ws/chat");
        ws.addServlet(new ServletHolder(new WebSocketChatServlet()), "/*");
        ws.setAllowNullPathInfo(true);

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] {proxy, ws});

        server.setHandler(contexts);

        try {
            server.start();
        } catch (Exception e) {
            sce.getServletContext().log(
                "An error occurred while starting Jetty", e);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                sce.getServletContext().log(
                    "An error occurred while shutting down Jetty", e);
            }
        }
    }

}
