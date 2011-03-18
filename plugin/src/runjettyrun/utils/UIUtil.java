package runjettyrun.utils;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;

public class UIUtil {
	
	public static Link createLink(Composite parent,int style ,String text){
		Link link = new Link( parent, style );
		link.setText(text);
		link.addSelectionListener(new OpenUrlListener());
		return link;
	}
	
	private static class OpenUrlListener implements SelectionListener{
		public void widgetSelected(SelectionEvent e) {
			if(e.text!=null && !"".equals(e.text)){
				try {
					BrowserUtil.openSystemBrowser(null, e.text,false);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		public void widgetDefaultSelected(SelectionEvent e) {
			//do nothing
		}
	}
}
