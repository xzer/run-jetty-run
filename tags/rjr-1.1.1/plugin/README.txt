Run-Jetty-Run Build Instructions
--------------------------------
Author: James Synge

The lib directory contains the jars used by run-jetty-run-bootstrap (e.g. Jetty).
They aren't used by the classes of this plug-in, but are there because
that gets them into the plug-in jar, so that they can then be placed on the
class path of run-jetty-run-bootstrap.

To build Run Jetty Run, follow these steps:

1) Set the Target Platform (Window > Preferences > Plug-in Development > Target Platform)
	 to the minimum supported platform version (Eclipse 3.3 at this time).

2) Fetch the sources from the svn repository into 4 projects,
		run-jetty-run							(the Eclipse plug-in)
		run-jetty-run-bootstrap		(the main that configures/starts Jetty)
		run-jetty-run-feature			(the Eclipse feature)
		run-jetty-run-updatesite	(the Eclipse update site)

3) If you're changing Run Jetty Run, don't forget to bump the version number of
	 the plug-in and the feature.

4) Build and test the projects (e.g. select Project > Build Automatically).  If
	 there are errors, resolve them before continuing.

5) Disable automatic building (unselect Project > Build Automatically).

6) Build run-jetty-run-bootstrap:

		Right-click run-jetty-run-bootstrap/build.xml
		Select Run As > Ant Build ...
		In the dialog, select targets "clean" and "build".
		Click Run

   This will add run-jetty-run-bootstrap.jar to run-jetty-run/lib.

6) Create the Ant build script for the plug-in and feature.
			Right-click on run-jetty-run/plugin.xml and select PDE Tools > Create Ant Build File 
			Right-click on run-jetty-run-feature/feature.xml and select PDE Tools > Create Ant Build File 

7) Build the feature:

		Right-click run-jetty-run-feature/build.xml
		Select Run As > Ant Build ...
		On the targets tab, select targets "clean" and "build.update.jar"
		On the JRE tab, select "Run in the same JRE as the workspace"
		Click Run

8) Update the update site:

		Open run-jetty-run-updatesite/site.xml
		If you're creating a new version,
			Click Add Feature...
			Then follow the steps to add the new version of the feature.
			Save site.xml.
			Right-click site.xml, select "PDE Tools > Build Site"

		Else if you're updating an existing version,
			Select that version in the list of features under category Jetty Integration,
			Click Synchronize...
			Select "Synchronize selected features only"
			Click Finish

10) Test in another Eclipse configuration, updating from the update site just
    modified, restarting that Eclipse, then launching a web application.

11) Clean all projects (Project > Clean ...), and delete remaining intermediate files
	  such as run-jetty-run/@dot and the plug-in and feature jars (in run-jetty-run/
	  and run-jetty-run-feature, respectively).

12) Commit changes back to SVN.
