package runjettyrun.test.widget;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.results.ListResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.hamcrest.SelfDescribing;

public class RJRSWTBotTree extends SWTBotTree{

	public RJRSWTBotTree(Tree tree, SelfDescribing description)
			throws WidgetNotFoundException {
		super(tree, description);
	}

	public RJRSWTBotTree(Tree tree) throws WidgetNotFoundException {
		super(tree);
	}


	public List<SWTBotTreeItem> getSelectedTreeItems(){
		return syncExec(new ListResult<SWTBotTreeItem>() {
			public List<SWTBotTreeItem> run() {
				TreeItem[] items = widget.getSelection();
				List<SWTBotTreeItem> results  = new ArrayList<SWTBotTreeItem>();
				if(items != null){
					for(TreeItem item : items){
						results.add(new SWTBotTreeItem(item));
					}

				}
				return results;
			}
		});
	}

}
