/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.iris.wss.framework;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import org.apache.log4j.Logger;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;

/**
 *
 * @author mike
 */
public class MyContainerLifecycleListener implements ContainerLifecycleListener {
    public static final Logger LOGGER =
          Logger.getLogger(MyContainerLifecycleListener.class);

    @Context 	ServletContext context;

    @Override
    public void onStartup(Container cntnr) {
        System.out.println("*****************  "
              + MyContainerLifecycleListener.class.getSimpleName() + " onStartup");
        System.out.println("*****************  "
              + MyContainerLifecycleListener.class.getSimpleName() + " onStartup, context path: " + context.getContextPath());

        // relying on application and container to instantiate AppContextListener
        // before onStartup and to exist until after onShutdown.
        WssSingleton sw = AppContextListener.sw;
        LOGGER.info("my container started for app: " + sw.appConfig.getAppName());
        System.out.println("*****************  " + MyContainerLifecycleListener.class.getSimpleName() + " sw1: " + sw);

        System.out.println("*****************  " + MyContainerLifecycleListener.class.getSimpleName() + " sw:2 " + cntnr.getConfiguration().getProperty("swobj"));

        // bind objects as needed to make them available to the framework
        // via a CONTEXT annotation
        ServiceLocator serviceLocator = cntnr.getApplicationHandler().getServiceLocator();
        DynamicConfiguration dc = Injections.getConfiguration(serviceLocator);
        Injections.addBinding(
            Injections.newBinder(sw).to(WssSingleton.class), dc);
        dc.commit();
    }

    @Override
    public void onReload(Container cntnr) {
        WssSingleton sw = AppContextListener.sw;
        LOGGER.info("my container reloaded for app: " + sw.appConfig.getAppName());
    }

    @Override
    public void onShutdown(Container cntnr) {
        WssSingleton sw = AppContextListener.sw;
        LOGGER.info("my container shutdown for app: " + sw.appConfig.getAppName());
        if (WssSingleton.rabbitAsyncPublisher != null) {
            try {
                // RabbitMQ shutdown just before container goes away
                LOGGER.info("RABBIT_ASYNC shutdown(10000) started");
                Thread.sleep(250); // help prevent loss of message
                WssSingleton.rabbitAsyncPublisher.shutdown(10000);
                LOGGER.info("RABBIT_ASYNC shutdown(10000) returned");
            } catch (Exception ex) {
                String msg = "*** MyContainerLifecycleListener, rabbitAsyncPublisher"
                        + " shutdown exception: " + ex
                        + "  msg: " + ex.getMessage();

                System.out.println(msg);
                LOGGER.info(msg);
            }
        }
    }
    
}