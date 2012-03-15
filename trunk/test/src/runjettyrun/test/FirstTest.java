package runjettyrun.test;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.inGroup;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import runjettyrun.test.widget.RJRSWTBotTree;

public class FirstTest {
	private SWTWorkbenchBot bot;

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateRunConfiguration() throws Exception {

		String projectName = "TestNormalJavaProject";

		bot.viewByTitle("Package Explorer").bot().tree(0)
				.getTreeItem(projectName).select();

		SWTBotMenu menu = bot.menu("Run");
		assertTrue(menu.isVisible());
		SWTBotMenu menu2 = menu.menu("Debug Configurations...");
		assertTrue(menu2.isVisible());
		menu2.click();

		bot.shell("Debug Configurations").bot().tree(0)
				.getTreeItem("Jetty Webapp").contextMenu("New").click();


		assertTrue(bot.text(1).getText().indexOf(projectName) != -1 );
		assertEquals(projectName,bot.text(2).getText());


		Matcher textsInWebApplicationGroup =allOf(
				inGroup("Web Application"),
				widgetOfType(Text.class)
		);



		List<Text> texts = bot.widgets(textsInWebApplicationGroup);
		assertEquals(3,texts.size()); //port context webapp dir

		assertEquals("8080",new SWTBotText(texts.get(0)).getText());
		assertEquals("/"+projectName,new SWTBotText(texts.get(1)).getText());
		assertEquals("webcontent",new SWTBotText(texts.get(2)).getText());

		new SWTBotText(texts.get(2)).setText("");


		bot.buttonInGroup("&Scan...","Web Application").click();

		assertEquals("webcontent",new SWTBotText(texts.get(2)).getText());


		RJRSWTBotTree tree = new RJRSWTBotTree((Tree) bot.getFocusedWidget());
		tree.getSelectedTreeItems().get(0).contextMenu("Delete").click();
		bot.button("Yes").click();

		bot.button("Close").click();
	}


	// @Test
	// public void canCreateAMessage() throws Exception {
	// SWTBotMenu menu = bot.menu("Run");
	// assertTrue(menu.isVisible());
	// SWTBotMenu menu2 = menu.menu("Debug Configurations...");
	// assertTrue(menu2.isVisible());
	//
	// menu2.click();
	// System.out.println("end");
	// }

	@Before
	public void setup() {
		bot = new SWTWorkbenchBot();
	}
}