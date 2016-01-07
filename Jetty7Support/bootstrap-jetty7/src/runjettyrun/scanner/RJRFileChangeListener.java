package runjettyrun.scanner;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jetty.util.Scanner;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.webapp.WebAppContext;

import runjettyrun.Configs;
import runjettyrun.ProjectClassLoader;

/**
 * For more details controll we need,
 * ex. when add a folder , it's unncessory to restart.
 * @author tony
 *
 */
public class RJRFileChangeListener implements Scanner.BulkListener,Scanner.DiscreteListener {
	/**
	 * Logger for this class
	 */
	private static final Logger logger =  Log.getLogger(RJRFileChangeListener.class);
	private WebAppContext web;
	private  Configs config;
	private boolean init = false;
	private Set<String> folderSet;
	private boolean isDirty = false;
	private Set<String> dirtylist = new HashSet<String>();

	public RJRFileChangeListener(WebAppContext web, Configs config) {
		super();
		folderSet= new HashSet<String>();
		this.web = web;
		this.config = config;
	}

	public void fileChanged(String filename) throws Exception {
		if(!init){	//do nothing when init.
			return ;
		}
		//2011/12/19 TonyQ:If the sub files or folders is added or removed , the parent folder will
		//be notified as changed , so we have to ignore parent folder!

		if (logger.isDebugEnabled()) {
			File f = new File(filename);
			logger.debug("fileChanged(String) - changed:" + filename + ":"
					+ f.exists() + ":" + f.isDirectory());
		}

		if(isIgnored(filename)){
			return;
		}

		boolean isFolder = (folderSet.contains(filename));
		isDirty = isDirty || !isFolder; // if user change a file, we should restart the server, but not for folder.
		if(!isFolder){
			dirtylist.add("*"+filename);
		}
	}

	public void fileAdded(String filename) throws Exception {
		File f = new File(filename);

		if(f.isDirectory()){
			folderSet.add(filename);
			if(!init ){
				if (logger.isDebugEnabled()) {
					logger.debug("fileAdded(String) - init add folder:"
							+ filename);
				}
			}else{
				if (logger.isDebugEnabled()) {
					logger.debug("fileAdded(String) - add folder:" + filename);
				}
			}
		}

		if(!init){
			return ;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("fileAdded(String) - added:" + filename);
		}

		if(isIgnored(filename)){
			return;
		}

		if(f.isFile()){
			dirtylist.add("+"+filename);
		}
		isDirty = isDirty || f.isFile(); //if user adding a file , we should restart the server, but not for folders.
	}

	private boolean isIgnored(String filename){
		return (config.getIgnoreScanClassFile() && filename.endsWith(".class") && !folderSet.contains(filename));
	}


	public void fileRemoved(String filename) throws Exception {
		if(!init){ //do nothing when init.
			return ;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("fileRemoved(String) - removed:" + filename);
		}

		if(isIgnored(filename)){
			return;
		}

		boolean isFolder = folderSet.contains(filename);
		if(isFolder){
			folderSet.remove(filename);
		}else{
			dirtylist.add("-"+filename);
		}
		isDirty = isDirty || !isFolder; // if user adding a file , we should restart the server , but not for folders.

	}

	/**
	 * @param filenames it's actually a string list.
	 * @throws Exception
	 */
	public void filesChanged(@SuppressWarnings("rawtypes") List changes) throws Exception {

		if(!init){
			init = true; //ignore first time
			return  ;
		}
		try {

			if(!isDirty) return ;

			System.err.println("Stopping webapp ...");
			System.err.println("File changed:");
			for(String str:dirtylist){
				System.err.println(str);
			}

			web.stop();

			if (config.getWebAppClassPath() != null) {
				ProjectClassLoader loader = new ProjectClassLoader(web,
						config.getWebAppClassPath(), config.getExcludedclasspath(), false);
				web.setClassLoader(loader);
			}
			System.err.println("Restarting webapp ...");
			web.start();
			System.err.println("Restart completed.");
		} catch (Exception e) {
			System.err.println(
					"Error reconfiguring/restarting webapp after change in watched files");
			e.printStackTrace();
		}finally{
			isDirty = false;
			dirtylist.clear();
		}
	}


}
