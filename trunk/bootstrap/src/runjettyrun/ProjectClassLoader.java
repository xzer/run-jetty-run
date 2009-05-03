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

import java.io.IOException;

import org.mortbay.jetty.webapp.WebAppClassLoader;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.resource.Resource;

/**
 * Uses the provided class path ONLY, rather than also supporting the adding of
 * the jars in the WEB-INF/lib directory, and the adding of the classes in the
 * WEB-INF/classes directory.
 * 
 * @author jsynge
 */
public class ProjectClassLoader extends WebAppClassLoader {
  private boolean initialized = false;

  public ProjectClassLoader(WebAppContext context, String projectClassPath)
      throws IOException {
    super(context);
    super.addClassPath(projectClassPath);
    initialized = true;
  }

  @Override
  public void addClassPath(String classPath) throws IOException {

    if (initialized) {
      /*
       * Disable the adding of directories to the class path after
       * initialization with the project class path. XXX Except for the addition
       * of the WEB-INF/classes
       */
      if (!classPath.endsWith("WEB-INF/classes/"))
        return;
    }
    super.addClassPath(classPath);
    return;
  }

  @Override
  public void addJars(Resource lib) {
    if (initialized) {
      /*
       * Disable the adding of jars (or folders of jars) to the class path after
       * initialization with the project class path.
       */
      return;
    }
    super.addJars(lib);
    return;
  }
}
