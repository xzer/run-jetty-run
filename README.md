# Run Jetty Run

```
This repository has been decided to be the official successor of the original "Run Jetty Run" at
https://code.google.com/p/run-jetty-run/.

New feature and bug fix will be performed here.
```

- Run web applications with Jetty and Eclipse in one click!

- Full maven support , simply run maven J2EE project without any config.

- [GettingStarted](https://github.com/xzer/run-jetty-run/wiki/GettingStarted) explains it all.  Please see [FutureDirectionsDiscussion](https://github.com/xzer/run-jetty-run/wiki/FutureDirectionsDiscussion) to participate in the evolution of Run Jetty Run.

## Install

[update site list](http://xzer.github.io/run-jetty-run/).

***Due to the Java 8 lambda issue, the nightly build is recommended.***

## About Java 9

The official Java 9 support of Jetty is start from version 10 which has not been released yet, but Jetty has released a version 9.4.8 with earlier Java 9 support, which has been supported by RJR too. 

***IMPORTANT***
To use RJR with jetty 9.4.8 Java 9 support, you need add the following vm args to your launch configuration or you will get error as "java.lang.NoClassDefFoundError: javax/xml/bind/JAXBException":

```
--add-modules java.xml.bind
```

### Migrate Note

Directly upgrading from old version to the current nightly build is not supported. Uninstall the old version via eclipse's plugin manager at first, then follow the [GettingStarted](https://github.com/xzer/run-jetty-run/wiki/GettingStarted) to install the newest nightly version.

The current 1.3.5 nightly build can be automatically updated.

## News

- Version 1.3.5 is under developing.
    - 2018/02/10 Jetty 9.4.8 support is added (for earlier Java 9 support)
    - 2017/08/01 Jetty 9.4.6 support is added
- 2016/01/16 RJR is officially migrated to github now.
- 2016/01/12 1.3.4 has been released, which includes all unreleased fixes at original https://code.google.com/p/run-jetty-run/.

## Why this plugin? ##

Because running a web application in Eclipse should be as simple as 'clicking run'. No additional setup required.


## The difference with WTP Jetty Integration ##

We think there are some benefits to use Run-Jetty-Run instead of WTP:

- Performance
    WTP always copy all the resources to a temp folder , that slow down the process and  you have to stop server then clean the resource sometimes if the resource locked. WTP solution also takes more time to start and terminate a web application.

- The maven support
    It's a annoying to run maven based web application in WTP. Since we use same project classpath with default Eclipse JDT , so we could support maven dependency management (M2Eclipse Plug-in) easily. (Note that RunJettyRun did not required to install a maven plug-in , it's optional.)

- Less steps to setup and run
    For using WTP Jetty ,  you have to install Jetty\_WTP\_Plugin , then find a runtime for it , that took your time to download a runtime and config it. You still have 3-4 steps after you install WTP plugin.But if you use RunJettyRun , there's already a built-in Jetty lib in the plug-in , simply "run" a project with Jetty by one click.

    <a href='http://www.youtube.com/watch?feature=player_embedded&v=Dtj1YBy9LKw' target='_blank'><img src='http://img.youtube.com/vi/Dtj1YBy9LKw/0.jpg' width='425' height=344 /></a>

- Also support Java project.
    Easy to use Jetty for every Java project but also a Dynamic Web Project , if you are writing some web widget , it's easy to test it with RJR.


## Other benefits ##

- Working with Eclipse JDT debugging/ hot deploy
    When you running RJR in debug mode , you could modify the code and apply it directly. Of course, all the breakpoint/inspector for java resource is working fine.

## Contributors

- Eelco Hillenius
- James Synge
- TonyQ Wang
- Xzer
- ffoysal
- Chao Chang

## LICENSE

Apache License, Version 2.0

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
