package runjettyrun.utils;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.FileEditorInput;

public class ProjectUtil {

	public static IResource getSelectedResource(IWorkbenchWindow window){
		IEditorInput editorinput = window.getActivePage().getActiveEditor().getEditorInput();
		FileEditorInput fileEditorInput = (FileEditorInput) editorinput.getAdapter(FileEditorInput.class);

		if( fileEditorInput == null || fileEditorInput.getFile()== null){
			return null;
		}
		return fileEditorInput.getFile();
	}
	public static IProject getProject(IWorkbenchWindow window){
		IEditorInput editorinput = window.getActivePage().getActiveEditor().getEditorInput();
		FileEditorInput fileEditorInput = (FileEditorInput) editorinput.getAdapter(FileEditorInput.class);

		if( fileEditorInput == null || fileEditorInput.getFile()== null){
			return null;
		}
		return fileEditorInput.getFile().getProject();
	}

	public static IProject getProject(IEditorInput editorinput){
		FileEditorInput fileEditorInput = (FileEditorInput) editorinput.getAdapter(FileEditorInput.class);

		if( fileEditorInput == null || fileEditorInput.getFile()== null){
			return null;
		}
		return fileEditorInput.getFile().getProject();
	}

	public static IContainer getWebappFolder(IProject project,String webappdir){
		
		IContainer folder = null;
		if("/".equals(webappdir))
			folder = project;
		else
			folder = project.getFolder(webappdir);
	
		return folder;
	}
}
