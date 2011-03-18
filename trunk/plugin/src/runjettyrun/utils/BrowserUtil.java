package runjettyrun.utils;

import java.net.URL;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

public class BrowserUtil {
	public static IWebBrowser openSystemBrowser(String browserid, String url,boolean embed) {
		try {
			IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
			IWebBrowser browser = null;
			if(embed){
				browser = browserSupport.createBrowser(browserid); //$NON-NLS-1$
			}else{
				browser = browserSupport.getExternalBrowser();
			}
			browser.openURL(new URL(url));
			return browser;
		} catch (Exception e) {
			return null;
		}
	}
}
