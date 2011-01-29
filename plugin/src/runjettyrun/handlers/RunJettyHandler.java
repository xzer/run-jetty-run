package runjettyrun.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 *
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
@SuppressWarnings("restriction")
public class RunJettyHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public RunJettyHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	@SuppressWarnings("restriction")
	public Object execute(ExecutionEvent event) throws ExecutionException {

		ILaunchManager lnmanger = DebugPlugin.getDefault().getLaunchManager();

		WorkbenchWindow window = (WorkbenchWindow) HandlerUtil
				.getActiveWorkbenchWindowChecked(event);

		RunJettyRunLaunch.launch(window);
		return null;
	}

}
