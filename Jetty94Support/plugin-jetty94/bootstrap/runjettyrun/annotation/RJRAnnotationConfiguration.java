package runjettyrun.annotation;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jetty.annotations.AbstractDiscoverableAnnotationHandler;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.annotations.AnnotationParser;
import org.eclipse.jetty.annotations.AnnotationParser.Handler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.statistic.CounterStatistic;
import org.eclipse.jetty.webapp.FragmentDescriptor;
import org.eclipse.jetty.webapp.WebAppContext;

import runjettyrun.ProjectClassLoader;

/**
 * Since the original design for AnnotationConfiguration only scan WEB-INF/classes , WEB-INF/libs.
 * We create a specific implemenation for Run-Jetty-Run application to have a better support.
 * @author TonyQ
 *
 */
public class RJRAnnotationConfiguration extends AnnotationConfiguration {
	private static Logger logger =  Log.getLogger(RJRAnnotationConfiguration.class);

	/**
	 * 
	 */
	public void parseWebInfClasses(final WebAppContext context,
			final AnnotationParser parser) throws Exception {
		//allow original "WEB-INF/classes" parse to be prior to our project classes
		super.parseWebInfClasses(context, parser);
		
        List<Resource> classesDirs = new LinkedList<>();
        //Hacked , add RJR classpaths
		List<String> rjrClasspaths = ProjectClassLoader.getClasspaths();
		for (String path : rjrClasspaths) {
			File file = new File(path);
			if (file.isDirectory()) {
				if (logger.isDebugEnabled()) logger.debug("scanning RJR classes for annotation:" + file.getAbsolutePath());
				FileResource folder = (new FileResource(file.toURI().toURL()));
				if(folder.isDirectory()){
					classesDirs.add(folder);
				}
			}
		}
		
		if(!classesDirs.isEmpty()){
			parseClasses(context,parser, classesDirs);
		}
		
	}
	
    /**
     * Scan classes in WEB-INF/classes
     * 
     * @param context
     * @param parser
     * @throws Exception
     */
    protected void parseClasses (final WebAppContext context,final AnnotationParser parser, List<Resource> classesDirs)
    throws Exception
    {
        Set<Handler> handlers = new HashSet<Handler>();
        handlers.addAll(_discoverableAnnotationHandlers);
        if (_classInheritanceHandler != null)
            handlers.add(_classInheritanceHandler);
        handlers.addAll(_containerInitializerAnnotationHandlers);

        _webInfClassesStats = new CounterStatistic();

        for (Resource dir : classesDirs)
        {
            if (_parserTasks != null)
            {
                ParserTask task = new ParserTask(parser, handlers, dir);
                _parserTasks.add(task);
                _webInfClassesStats.increment();
                if (logger.isDebugEnabled())
                    task.setStatistic(new TimeStatistic());
            }
        }
    }
}
