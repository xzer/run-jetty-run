package runjettyrun.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.FileEditorInput;
import org.osgi.framework.Bundle;

import runjettyrun.Plugin;

public class ProjectUtil {
	public static String MAVEN_NATURE_ID = "org.maven.ide.eclipse.maven2Nature";

	public static IResource getSelectedResource(ISelection selection) {

		if (selection instanceof TreeSelection) {// could be project explorer
			Object first = ((TreeSelection) selection).getFirstElement();
			if (first instanceof IResource)
				return (IResource) first;
			else if (first instanceof IJavaProject)
				return ((IJavaProject) first).getResource();
			else if (first instanceof IProject) {
				try {
					return ((IProject) first).members()[0];
				} catch (CoreException e) {
					e.printStackTrace();
					return null;
				}
			}
		}

		return null;
	}

	public static IResource getSelectedResource(IWorkbenchWindow window) {

		ISelection selection = window.getSelectionService().getSelection();

		IResource ir = getSelectedResource(selection);
		if (ir != null)
			return ir;

		IEditorInput editorinput = window.getActivePage().getActiveEditor()
				.getEditorInput();
		FileEditorInput fileEditorInput = (FileEditorInput) editorinput
				.getAdapter(FileEditorInput.class);
		if (fileEditorInput == null || fileEditorInput.getFile() == null) {
			return null;
		}
		return fileEditorInput.getFile();
	}

	/**
	 *
	 * @param editorinput
	 * @return null if not found
	 */
	public static IFile getFile(IEditorInput editorinput) {
		FileEditorInput fileEditorInput = (FileEditorInput) editorinput
				.getAdapter(FileEditorInput.class);

		if (fileEditorInput == null || fileEditorInput.getFile() == null) {
			return null;
		}
		return fileEditorInput.getFile();
	}

	public static IProject getProject(IWorkbenchWindow window) {
		IEditorInput editorinput = window.getActivePage().getActiveEditor()
				.getEditorInput();
		FileEditorInput fileEditorInput = (FileEditorInput) editorinput
				.getAdapter(FileEditorInput.class);

		if (fileEditorInput == null || fileEditorInput.getFile() == null) {
			return null;
		}
		return fileEditorInput.getFile().getProject();
	}

	public static IProject getProject(IEditorInput editorinput) {
		FileEditorInput fileEditorInput = (FileEditorInput) editorinput
				.getAdapter(FileEditorInput.class);

		if (fileEditorInput == null || fileEditorInput.getFile() == null) {
			return null;
		}
		return fileEditorInput.getFile().getProject();
	}

	public static IContainer getWebappFolder(IProject project, String webappdir) {

		IContainer folder = null;
		if ("/".equals(webappdir))
			folder = project;
		else
			folder = project.getFolder(webappdir);

		return folder;
	}

	public static boolean isMavenProject(IProject project) {
		try {
			return project != null && project.hasNature(MAVEN_NATURE_ID);
		} catch (CoreException e) {
			return false;
		}
	}

	public static IRuntimeClasspathEntry[] getLibs(Bundle bundle, String[] filelist)
			throws MalformedURLException, URISyntaxException {

		List<IRuntimeClasspathEntry> entries = new ArrayList<IRuntimeClasspathEntry>();
		URL installUrl = bundle.getEntry("/");

		try {
			for(String filepath:filelist){
				//Note the FileLocator will generate a file for us when we use FileLocator.toFileURL ,
				//it's very important.
				URL fileUrl = FileLocator.toFileURL(new URL(installUrl,filepath));
				entries.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(fileUrl.getPath())));
			}
			if(entries.size() == 0 ){
				throw new IllegalStateException("RJR finding jar failed");
			}

		} catch (IOException e) {
			Plugin.logError(e);
		}
		return entries.toArray(new IRuntimeClasspathEntry[0]);
	}
}
