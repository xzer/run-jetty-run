package runjettyrun.annotation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.annotations.AnnotationParser;
import org.eclipse.jetty.annotations.ClassNameResolver;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.DiscoveredAnnotation;
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
	static{
		logger.setDebugEnabled(true);
	}

	public void parseWebInfClasses(final WebAppContext context,
			final AnnotationParser parser) throws Exception {
		if (logger.isDebugEnabled()) logger.debug("Scanning classes in WEB-INF/classes");
		if (context.getWebInf() != null) {
			Resource classesDir = context.getWebInf().addPath("classes/");
			handleClasses(context, parser, classesDir);
		}

		List<String> rjrClasspaths = ProjectClassLoader.getClasspaths();
		for (String path : rjrClasspaths) {
			File file = new File(path);
			if (file.isDirectory()) {
				if (logger.isDebugEnabled()) logger.debug("scanning RJR classes for annotation:" + file.getAbsolutePath());
				handleClasses(context, parser, new FileResource(file.toURI().toURL()));
			}
		}
	}

	/* private helper */

	private void handleClasses(final WebAppContext context,
			final AnnotationParser parser, Resource classesDir)
			throws Exception {
		if (classesDir.exists()) {
			clearAnnotationList(parser.getAnnotationHandlers());
			parser.parse(classesDir, new ClassNameResolver() {
				public boolean isExcluded(String name) {
					if (context.isSystemClass(name))
						return true;
					if (context.isServerClass(name))
						return false;
					return false;
				}

				public boolean shouldOverride(String name) {
					// looking at webapp classpath, found already-parsed
					// class of same name - did it come from system or
					// duplicate in webapp?
					if (context.isParentLoaderPriority())
						return false;
					return true;
				}
			});

			List<DiscoveredAnnotation> annotations = new ArrayList<DiscoveredAnnotation>();
			gatherAnnotations(annotations, parser.getAnnotationHandlers());
			context.getMetaData().addDiscoveredAnnotations(annotations);
		}
	}
}
