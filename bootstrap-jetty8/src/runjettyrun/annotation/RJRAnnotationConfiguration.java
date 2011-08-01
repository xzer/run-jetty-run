package runjettyrun.annotation;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.annotations.AnnotationParser;
import org.eclipse.jetty.annotations.ClassNameResolver;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.DiscoveredAnnotation;
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
	public void parseWebInfLib(final WebAppContext context,
			final AnnotationParser parser) throws Exception {
		List<FragmentDescriptor> frags = context.getMetaData().getFragments();

		List<Resource> jars = context.getMetaData().getOrderedWebInfJars();

		// No ordering just use the jars in any order
		if (jars == null || jars.isEmpty())
			jars = context.getMetaData().getWebInfJars();

		Set<String> items = new HashSet<String>();
		for (Resource r : jars) {
			items.add(r.getFile().getAbsolutePath());
			handleJar(context, parser, r, frags);
		}

		List<String> rjrClasspaths = ProjectClassLoader.getClasspaths();

		for (String path : rjrClasspaths) {
			if (path.endsWith(".jar")) {
				 Resource resource = new FileResource(new File(path).toURI().toURL());
				 if(!items.contains(resource.getFile().getAbsolutePath())){
					if (Log.isDebugEnabled()) Log.debug("scanning RJR jar for annotation:" + path);
					handleJar(context, parser,resource, frags);
				 }else{
					if (Log.isDebugEnabled()) Log.debug("skip scanning RJR jar which is already in WEB-INF/lib:" + path);
				 }
			}
		}

	}

	public void parseWebInfClasses(final WebAppContext context,
			final AnnotationParser parser) throws Exception {
		Log.debug("Scanning classes in WEB-INF/classes");
		if (context.getWebInf() != null) {
			Resource classesDir = context.getWebInf().addPath("classes/");
			handleClasses(context, parser, classesDir);
		}

		List<String> rjrClasspaths = ProjectClassLoader.getClasspaths();
		for (String path : rjrClasspaths) {
			File file = new File(path);
			if (file.isDirectory()) {

				if (Log.isDebugEnabled()) Log.debug("scanning RJR classes for annotation:" + file.getAbsolutePath());
				handleClasses(context, parser, new FileResource(file.toURI()
						.toURL()));
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

			// TODO - where to set the annotations discovered from
			// WEB-INF/classes?
			List<DiscoveredAnnotation> annotations = new ArrayList<DiscoveredAnnotation>();
			gatherAnnotations(annotations, parser.getAnnotationHandlers());
			context.getMetaData().addDiscoveredAnnotations(annotations);
		}
	}

	private void handleJar(final WebAppContext context,
			final AnnotationParser parser, Resource r,
			List<FragmentDescriptor> frags) throws Exception {
		// clear any previously discovered annotations from handlers
		clearAnnotationList(parser.getAnnotationHandlers());

		URI uri = r.getURI();
		FragmentDescriptor f = getFragmentFromJar(r, frags);

		// if a jar has no web-fragment.xml we scan it (because it is not
		// exluded by the ordering)
		// or if it has a fragment we scan it if it is not metadata complete
		if (f == null || !isMetaDataComplete(f)) {
			parser.parse(uri, new ClassNameResolver() {
				public boolean isExcluded(String name) {
					if (context.isSystemClass(name))
						return true;
					if (context.isServerClass(name))
						return false;
					return false;
				}

				public boolean shouldOverride(String name) {
					// looking at webapp classpath, found already-parsed class
					// of same name - did it come from system or duplicate in
					// webapp?
					if (context.isParentLoaderPriority())
						return false;
					return true;
				}
			});
			List<DiscoveredAnnotation> annotations = new ArrayList<DiscoveredAnnotation>();
			gatherAnnotations(annotations, parser.getAnnotationHandlers());
			context.getMetaData().addDiscoveredAnnotations(r, annotations);
		}
	}
}
