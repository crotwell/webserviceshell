/*******************************************************************************
 * Copyright (c) 2014 IRIS DMC supported by the National Science Foundation.
 *  
 * This file is part of the Web Service Shell (WSS).
 *  
 * The WSS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * The WSS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * A copy of the GNU Lesser General Public License is available at
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package edu.iris.wss.framework;

import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.io.IOException;
import java.net.URI;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author mike
 */
public class ServiceConfigTest  {
    public static final Logger logger = Logger.getLogger(ServiceConfigTest.class);

    private static final String BASE_HOST = "http://localhost";
    private static final Integer BASE_PORT = 8093;
    
    // set notional webapp name
    private static final String SOME_CONTEXT = "/testservice/dataselect/1";
    
    private static final URI BASE_URI = URI.create(BASE_HOST + ":"
        + BASE_PORT + SOME_CONTEXT);

    private static GrizzlyWebServer grizzlyWebServer;
    
    public ServiceConfigTest() {
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        // setup config dir for test environment
        System.setProperty(AppConfigurator.wssConfigDirSignature,
            "target/test-classes/ServiceConfigTest");
        
        grizzlyWebServer = new GrizzlyWebServer(BASE_PORT);
        ServletAdapter jerseyAdapter = new ServletAdapter();

        // specify the package where resources or prodcuers are located
        jerseyAdapter.addInitParameter("com.sun.jersey.config.property.packages",
            "edu.iris.wss");
        jerseyAdapter.setContextPath(SOME_CONTEXT);
        jerseyAdapter.setServletInstance(new ServletContainer());
        
        grizzlyWebServer.addGrizzlyAdapter(jerseyAdapter, new String[] { "/" });

        logger.info("*********** starting grizzlyWebServer, BASE_URI: "
            + BASE_URI);
        grizzlyWebServer.start();

        // for manual test of server, uncomment this code then mvn clean install
//        System.out.println("***** Application started, try: " + BASE_URI);
//        System.out.println("***** control-c to stop its...");
//        System.in.read();
    }
    
    @AfterClass
    public static void tearDownClass() {
        logger.info("*********** stopping grizzlyWebServer");
        grizzlyWebServer.stop();
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testGet_wssversion() throws Exception {
        Client c = Client.create();
        WebResource webResource = c.resource(BASE_URI);
        String responseMsg = webResource.path("wssversion").get(String.class);

        // start with a basic test, that the URL exists and returns something
        assertNotNull(responseMsg);
    }

    @Test
    public void testGet_status() throws Exception {
        
        Client c = Client.create();
        WebResource webResource = c.resource(BASE_URI);
        String responseMsg = webResource.path("status").get(String.class);

        // test that the URL exists and returns something
        assertNotNull(responseMsg);
        
        // test for some basic known content
        assertTrue(
            responseMsg.indexOf("<TD>URL</TD><TD>" + SOME_CONTEXT + "/status</TD>") > -1);
        assertTrue(
            responseMsg.indexOf("<TD>Port</TD><TD>" + BASE_PORT + "</TD>") > -1);
    }

}