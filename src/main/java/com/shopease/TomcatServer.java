package com.shopease;

import com.shopease.util.ConfigUtil;
import java.io.File;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Embedded Tomcat 9 server runner for development, automated verification, and testing.
 */
public class TomcatServer {
    private static final Logger logger = LoggerFactory.getLogger(TomcatServer.class);

    public static void main(String[] args) throws Exception {
        int port = ConfigUtil.getInt("app.port", 8080);
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.trim().isEmpty()) {
            try {
                port = Integer.parseInt(portEnv.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector(); // Trigger default connector creation

        File targetWebapp = new File("target/shopease");
        String docBase;
        if (targetWebapp.exists() && targetWebapp.isDirectory()) {
            docBase = targetWebapp.getAbsolutePath();
        } else {
            docBase = new File("src/main/webapp").getAbsolutePath();
        }

        Context ctx = tomcat.addWebapp("", docBase);
        ctx.setReloadable(true);
        ctx.setParentClassLoader(TomcatServer.class.getClassLoader());

        // Configure classes directory if using src/main/webapp
        File additionWebInfClasses = new File("target/classes");
        if (additionWebInfClasses.exists() && !targetWebapp.exists()) {
            org.apache.catalina.WebResourceRoot resources = new org.apache.catalina.webresources.StandardRoot(ctx);
            resources.addPreResources(new org.apache.catalina.webresources.DirResourceSet(
                    resources, "/WEB-INF/classes", additionWebInfClasses.getAbsolutePath(), "/"));
            ctx.setResources(resources);
        }

        logger.info("Starting ShopEase on http://localhost:{} from {}", port, docBase);
        tomcat.start();
        logger.info("ShopEase server is running! Press Ctrl+C to stop.");
        tomcat.getServer().await();
    }
}
