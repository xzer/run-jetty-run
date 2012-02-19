package runjettyrun.utils;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

public class ResourceUtil {

	/**
	 * Looking resource from path , which might be absolute path and workspace relative path
	 * @param path
	 * @return
	 */
	public static File lookingFileFromPath(IPath path){
		if(path == null){
			throw new IllegalStateException("path shouldn't be null");
		}

		if(path.toFile().exists()){
			return path.toFile();
		}

		{
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IResource resourceInRuntimeWorkspace = root.findMember(path);
			File file = new File(resourceInRuntimeWorkspace.getLocationURI());

			if(file.exists()){
				return file;
			}
		}

		return path.toFile();

	}

	public static File lookingFileFromPathString(String path){
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resourceInRuntimeWorkspace = root.findMember(path);
		if( resourceInRuntimeWorkspace == null){
			return new File(path);
		}
		File file = new File(resourceInRuntimeWorkspace.getLocationURI());

		return file;
	}
}
