package runjettyrun.webapp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;

public class RJRMetaInfoConfiguration extends MetaInfConfiguration{

    public void preConfigure(final WebAppContext context) throws Exception
    {
    	super.preConfigure(context);
        ArrayList<Resource> jars = new ArrayList<Resource>();
        jars.addAll(context.getMetaData().getContainerResources());
        jars.addAll(context.getMetaData().getWebInfJars());
        for(Resource r:jars){
        	if(r.isDirectory()){
        		processClassFolderMetaEntry(context,r);
        	}
        }
    }


    protected void processClassFolderMetaEntry(WebAppContext context,Resource r) throws IOException{
    	File metainf = new File(r.getFile(), "META-INF");

    	if(!(metainf.exists() && metainf.isDirectory())){
    		return ;
    	}

        try
        {
        	File fragXml = new File(metainf,"web-fragment.xml");
        	if(fragXml.exists() && context.isConfigurationDiscovered()){
                 Map<Resource, Resource> fragments = (Map<Resource,Resource>)context.getAttribute(METAINF_FRAGMENTS);
                 if (fragments == null)
                 {
                     fragments = new HashMap<Resource, Resource>();
                     context.setAttribute(METAINF_FRAGMENTS, fragments);
                 }
                 fragments.put(r, Resource.newResource(fragXml));    
        	}

        	File resourcesDir = new File(metainf,"resources");
        	if(resourcesDir.exists() && context.isConfigurationDiscovered()){
        		 //addResource(context,METAINF_RESOURCES,Resource.newResource(resources));
                 Set<Resource> dirs = (Set<Resource>)context.getAttribute(METAINF_RESOURCES);
                 if (dirs == null)
                 {
                     dirs = new HashSet<Resource>();
                     context.setAttribute(METAINF_RESOURCES, dirs);
                 }
                 dirs.add(Resource.newResource(resourcesDir));
        	}

        }
        catch(Exception e)
        {
            context.getServletContext().log(r.getFile().getAbsolutePath(),e);
        }
    }




}
