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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.servlets.ProxyServlet;

/**
* @author Andrej Golovnin
*/
final class TransparentProxy extends ProxyServlet {

    // Fields *************************************************************

    private String jbossWebHost;
    private Integer jbossWebPort;


    // Instance Creation **************************************************

    TransparentProxy(String jbossWebHost, Integer jbossWebPort) {
        this.jbossWebHost = jbossWebHost;
        this.jbossWebPort = jbossWebPort;
    }


    // Overriding default behavior ****************************************

    @Override
    protected HttpURI proxyHttpURI(String scheme, String serverName,
        int serverPort, String uri) throws MalformedURLException
    {
        try {
            URI dstURI = new URI(
                scheme + "://" + jbossWebHost + ":" + jbossWebPort + uri)
                .normalize();
            if (!validateDestination(dstURI.getHost(), dstURI.getPath())) {
                return null;
            }
            return new HttpURI(dstURI.toString());
        } catch (URISyntaxException e) {
            throw new MalformedURLException(e.getMessage());
        }
    }

}
