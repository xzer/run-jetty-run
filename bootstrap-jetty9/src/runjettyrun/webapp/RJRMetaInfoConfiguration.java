package runjettyrun.webapp;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;

public class RJRMetaInfoConfiguration extends MetaInfConfiguration{

    public void preConfigure(final WebAppContext context) throws Exception
    {
    	super.preConfigure(context);
        ArrayList<Resource> jars = new ArrayList<Resource>();
        jars.addAll(context.getMetaData().getOrderedContainerJars());
        jars.addAll(context.getMetaData().getWebInfJars());
        for(Resource r:jars){
        	if(r.isDirectory()){
        		processClassFolderMetaEntry(context,r.getFile());
        	}
        }
    }


    protected void processClassFolderMetaEntry(WebAppContext context,File classFolder){
    	File metainf = new File(classFolder, "META-INF");

    	if(!(metainf.exists() && metainf.isDirectory())){
    		return ;
    	}

        try
        {
        	File webFrag = new File(metainf,"web-fragment.xml");
        	if(webFrag.exists() && context.isConfigurationDiscovered()){
        		 addResource(context,METAINF_FRAGMENTS,Resource.newResource(classFolder));
        	}

        	File resources = new File(metainf,"resources");
        	if(resources.exists() && context.isConfigurationDiscovered()){
        		 addResource(context,METAINF_RESOURCES,Resource.newResource(resources));
        	}

        }
        catch(Exception e)
        {
            context.getServletContext().log(classFolder.getAbsolutePath(),e);
        }
    }

    protected void processClassFolderMetaEntryForTld(WebAppContext context,File root){
    	File[] files = root.listFiles();

    	for(File f:files){
    		if(f.isDirectory()){
    			processClassFolderMetaEntryForTld(context,f);
    		}else{
                String lcname = f.getName().toLowerCase();
                if (lcname.endsWith(".tld"))
                {
                    try {
						addResource(context,METAINF_TLDS,Resource.newResource(f));
                    }
                    catch(Exception e)
                    {
                        context.getServletContext().log(f.getAbsolutePath(),e);
                    }
                }
    		}
    	}
    }


}
