package runjettyrun.tabs.action.helper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Extracted from DefaultLabelProvider , only for IProject .
 * @author tony
 *
 */
public class ProjectLabelProvider implements ILabelProvider{

	private Map<ImageDescriptor, Image> fImages = new HashMap<ImageDescriptor, Image>();

	public void addListener(ILabelProviderListener listener) {

	}

	public void dispose() {
		Iterator<Image> iterator = fImages.values().iterator();
		while (iterator.hasNext()) {
			Image image = (Image) iterator.next();
			image.dispose();
		}
		fImages.clear();
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {

	}

	private Image getImage(ImageDescriptor descriptor) {
		Image image = (Image) fImages.get(descriptor);
		if (image != null) {
			return image;
		}
		image = descriptor.createImage();
		if (image != null) {
			fImages.put(descriptor, image);
		}
		return image;
	}

	public Image getImage(Object element) {
		if (element instanceof IAdaptable) {
			IWorkbenchAdapter de= (IWorkbenchAdapter) ((IAdaptable) element).getAdapter(IWorkbenchAdapter.class);
			if (de != null) {
				ImageDescriptor descriptor= de.getImageDescriptor(element);
				if( descriptor != null) {
					return getImage(descriptor);
				}
			}
			return null;
		}
		return null;
	}

	public String getText(Object element) {
		if (element instanceof IAdaptable) {
			IWorkbenchAdapter de= (IWorkbenchAdapter) ((IAdaptable) element).getAdapter(IWorkbenchAdapter.class);
			if (de != null) {
				return de.getLabel(element);
			}
		}
		return "";
	}


}
