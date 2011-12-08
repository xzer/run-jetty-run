package runjettyrun.tabs.action;


import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionListenerAction;

import runjettyrun.tabs.classpath.IClasspathViewer;

/**
 * Removes selected enries in a runtime classpath viewer.
 */
public class RemoveAction extends RuntimeClasspathAction {

	public RemoveAction(IClasspathViewer viewer) {
		super("Re&move", viewer);
	}
	/**
	 * Removes all selected entries.
	 *
	 * @see IAction#run()
	 */
	public void run() {
		List<?> targets = getOrderedSelection();
		List<?> list = getCustomEntriesAsList();
		list.removeAll(targets);
		setCustomEntries(list);
	}

	/**
	 * @see SelectionListenerAction#updateSelection(IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		if (selection.isEmpty()) {
			return false;
		}
		return getViewer().updateSelection(getActionType(), selection);
	}

	protected int getActionType() {
		return REMOVE;
	}
}
