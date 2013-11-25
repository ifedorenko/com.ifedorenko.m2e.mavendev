# Maven Development Tools

Maven Development Tools is a collection of m2e extensions that enables 
end-to-end debugging of Maven Plugins, Maven Core and their dependencies from
m2e development workspace, without the need to install artifacts to the local
Maven repository.

For Maven Plugin developers this provides ability to run and debug plugin 
directly from Eclipse workspace. Changes to plugin sources, pom.xml 
dependencies, etc, are picked up during next Run/Debug as Maven Build or 
Maven IT launch. Breakpoints and sources lookup will automatically work in 
plugin,  plugin dependencies and maven runtime code.

For Maven Core developers this provides ability to run and debug maven 
directly from eclipse workspace. Changes to maven core source, dependencies,
etc, are picked up during next Run-As Maven Build or Maven IT launch. 
Additionally, Run/Debug as Maven IT launch configuration can be used to run
majority of Maven Core integration tests directly from Eclipse workspace.  

# Prerequisites

* Eclipse Kepler or Luna, "standard" or "for java developers" distributions
  are recommended.
* m2e 1.5.0.20131123-2243 or newer, available from http://nexus.tesla.io:8081/nexus/content/sites/m2e.extras/m2e/1.5.0/N/LATEST/ 
  p2 repository
* Maven 3.x runtime. m2e embedded, external and workspace runtimes are supported
* Maven Plugins using maven-plugin-plugin version 2.3 or newer.
* Debugging Maven Plugin integration tests requires maven-verifier version
  1.5-SNAPSHOT or newer. 

Do NOT use directories with spaces and other weird charactes for Eclipse 
installation!

# Installation

* github pages URL TDB
* eclipse marketplace link TDB

# Notes for Maven Plugin developers

* Use standard m2e project import wizards to import Maven Plugin projects in 
  Eclipse workspace.
* Maven Plugin metadata, i.e., META-INF/maven/plugin.xml is generated 
  automatically during workspace clean build only. Make sure to run 
  Project->Clean after change mojo annotations.  
* Workspace dependency resolution is disabled for Run/Debug as Maven Build by
  default. Enable it in launch configuration dialog to plugin from workspace.
* Use Run/Debug as Maven IT launch configuration to run verifier-based
  integration tests from Eclipse workspace.

# Notes for Maven Core developers

* Use standard m2e project import wizards to import Maven core sources in 
  Eclipse workspace. New "Maven Workspace" installation will become available 
  after the import.
* "Maven Workspace" installation can be used to Debug/Run as Maven Build 
  (configured on Main launch configuration dialog tab) and Maven IT (configured
  on Maven IT tab).
* To make Debug/Run as Maven IT work for Maven Core integration tests
** change maven-verifier version to 1.5-SNAPSHOT in core-it-suite/pom.xml
** add -Dmaven.it.global-settings.dir=<path to core-it-suite/target/test-classes>
   to launch configuration VM arguments

# Known problems and limitations

* Only Verifier embedded mode is currently supported. Integration tests that
  require forked mode will either fail or breakpoints won't work.
* Maven Plugin metadata, i.e., META-INF/maven/plugin.xml, is "disappears" in
  some cases. A partial fix is planned in maven-plugin-plugin 3.3.
