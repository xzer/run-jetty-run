package runjettyrun.extensions;

import org.eclipse.jdt.launching.IRuntimeClasspathEntry;

public interface IJettyPackageProvider {

	public static final String JETTY_6_1_26 = "6.1.26";

	public static final int TYPE_JETTY_BUNDLE = 0;

	public static final int TYPE_UTIL = 1;

	public IRuntimeClasspathEntry[] getPackage(String version, int type);

	public boolean acceptType(int type);

	public String getJettyVersion();

	public boolean accpet(String ver);

	public String getName();

	public String getText();
}
