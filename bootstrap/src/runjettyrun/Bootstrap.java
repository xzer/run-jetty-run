/*
 * $Id$
 * $HeadURL$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package runjettyrun;

import java.lang.management.ManagementFactory;
import java.util.Collections;

import javax.management.MBeanServer;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.management.MBeanContainer;

/**
 * Started up by the plugin's runner. Starts Jetty.
 * 
 * @author hillenius, jsynge
 */
public class Bootstrap {

  /**
   * Main function, starts the jetty server.
   * 
   * @param args
   */
  public static void main(String[] args) throws Exception {

    String context = System.getProperty("rjrcontext");
    String webAppDir = System.getProperty("rjrwebapp");
    Integer port = Integer.getInteger("rjrport");
    Integer sslport = Integer.getInteger("rjrsslport");
    String webAppClassPath = System.getProperty("rjrclasspath");
    String keystore = System.getProperty("rjrkeystore");
    String password = System.getProperty("rjrpassword");
    String keyPassword = System.getProperty("rjrkeypassword");

    if (context == null) {
      throw new IllegalStateException(
          "you need to provide argument -Drjrcontext");
    }
    if (webAppDir == null) {
      throw new IllegalStateException(
          "you need to provide argument -Drjrwebapp");
    }
    if (port == null && sslport == null) {
      throw new IllegalStateException(
          "you need to provide argument -Drjrport and/or -Drjrsslport");
    }

    Server server = new Server();

    if (port != null) {
      SelectChannelConnector connector = new SelectChannelConnector();
      connector.setPort(port);

      if (sslport != null) {
        connector.setConfidentialPort(sslport);
      }

      server.addConnector(connector);
    }

    if (sslport != null) {
      if (keystore == null) {
        throw new IllegalStateException(
            "you need to provide argument -Drjrkeystore with -Drjrsslport");
      }
      if (password == null) {
        throw new IllegalStateException(
            "you need to provide argument -Drjrpassword with -Drjrsslport");
      }
      if (keyPassword == null) {
        throw new IllegalStateException(
            "you need to provide argument -Drjrkeypassword with -Drjrsslport");
      }

      SslSocketConnector sslConnector = new SslSocketConnector();
      sslConnector.setKeystore(keystore);
      sslConnector.setPassword(password);
      sslConnector.setKeyPassword(keyPassword);

      sslConnector.setMaxIdleTime(30000);
      sslConnector.setPort(sslport);

      server.addConnector(sslConnector);
    }

    WebAppContext web = new WebAppContext();
    web.setContextPath(context);
    web.setWar(webAppDir);

    // Fix issue 7, File locking on windows/Disable Jetty's locking of static files
    //    http://code.google.com/p/run-jetty-run/issues/detail?id=7
    // by disabling the use of the file mapped buffers.  The default Jetty behavior is
    // intended to provide a performance boost, but run-jetty-run is focused on
    // development (especially debugging) of web apps, not high-performance production
    // serving of static content.  Therefore, I'm not worried about the performance
    // degradation of this change.  My only concern is that there might be a need to
    // test this feature that I'm disabling.
    web.setInitParams(Collections.singletonMap("org.mortbay.jetty.servlet.Default.useFileMappedBuffer", "false"));

    if (webAppClassPath != null) {
      ProjectClassLoader loader = new ProjectClassLoader(web, webAppClassPath);
      web.setClassLoader(loader);
    }

    server.addHandler(web);

    MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    MBeanContainer mBeanContainer = new MBeanContainer(mBeanServer);
    server.getContainer().addEventListener(mBeanContainer);
    mBeanContainer.start();

    try {
      server.start();
      server.join();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(100);
    }
    return;
  }
}
