<map version="1.0.0">
<!-- To view this file, download free mind mapping software FreeMind from http://freemind.sourceforge.net -->
<node CREATED="1342289129365" ID="ID_494318342" MODIFIED="1342289747654" TEXT="tycho-insitu">
<node CREATED="1342289213321" ID="ID_1139120808" MODIFIED="1342289832628" POSITION="right" STYLE="fork" TEXT="debug/run as maven">
<node CREATED="1342353729997" ID="ID_1531015681" MODIFIED="1342353762188" TEXT="pde (dynamic) source lookup"/>
<node CREATED="1342353738279" ID="ID_632227083" MODIFIED="1342353753314" TEXT="m2e dynamic source lookup"/>
<node CREATED="1342353777667" ID="ID_790696798" MODIFIED="1342353791945" TEXT="m2e workspace dependency resolution"/>
<node CREATED="1342353797823" ID="ID_1022516569" MODIFIED="1342353808833" TEXT="pde/bundle workspace dependency resolution"/>
</node>
<node CREATED="1342289231274" HGAP="29" ID="ID_1380920308" MODIFIED="1342289531736" POSITION="left" TEXT="debug/run as tycho IT" VSHIFT="-3">
<node CREATED="1342353849336" ID="ID_929278007" MODIFIED="1342354262320" TEXT="maven-verifier 1.3">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      not strictly necessary, but why not tease EMO a little?
    </p>
  </body>
</html></richcontent>
<icon BUILTIN="button_ok"/>
</node>
<node CREATED="1342353907575" ID="ID_1444126652" MODIFIED="1342988007404" TEXT="forkJvm=false when running from tycho-insitu">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      The idea is to allow debugging both the code from integration tests and the code executed in the launched maven build.
    </p>
    <p>
      
    </p>
    <p>
      This works for some/manyindividual test but does not work for all test suites because of OOME permgen.
    </p>
  </body>
</html></richcontent>
<icon BUILTIN="button_ok"/>
<node CREATED="1342353970573" ID="ID_1879659168" MODIFIED="1342958720796" TEXT="verify this works for each individual test suite">
<icon BUILTIN="button_ok"/>
</node>
<node CREATED="1342355002335" ID="ID_1939901801" MODIFIED="1342355026034" TEXT="see if there is an easy way to make cli tests work with fork=false"/>
</node>
<node CREATED="1342354051170" ID="ID_1180395400" MODIFIED="1342983650694" TEXT="m2e workspace dependency resolution">
<icon BUILTIN="button_ok"/>
<node CREATED="1342699656299" ID="ID_1033964005" MODIFIED="1342699721710" TEXT="set -Dm2eclipse.workspace.state">
<icon BUILTIN="button_ok"/>
</node>
<node CREATED="1342787288737" ID="ID_964935601" MODIFIED="1342960052708" TEXT="TestMojo plugin dependencies workspace resolution">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      some plugin dependencies are OSGi bundles that are fed into test Equinox runtime
    </p>
    <p>
      these bundles need to be resolved from workspace and added to dev.properties
    </p>
  </body>
</html>
</richcontent>
<icon BUILTIN="button_ok"/>
</node>
<node CREATED="1342699686936" ID="ID_560831749" MODIFIED="1342963926000" TEXT="force org.eclipse.m2e.cliresolver30.jar into maven core">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      used -Dmaven.ext.class.path=&lt;path-to-cliresolver30.jar&gt;
    </p>
    <p>
      using custom classworlds.conf was not possible because Verifier does not provide a way to configure it for embedded maven runtime
    </p>
  </body>
</html>
</richcontent>
<icon BUILTIN="button_ok"/>
</node>
<node CREATED="1342780818303" ID="ID_384005358" MODIFIED="1342983594136" TEXT="maven plugin classloading issues with verifier fork=false">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      DefaultClassRealmManager uses system classloader as the parent of maven plugin classrealms
    </p>
    <p>
      
    </p>
    <p>
      when running from m2e, IT jvm system classpath includes most of tycho classes
    </p>
    <p>
      either change maven to use extensions classloader as parent of plugin classloaders
    </p>
    <p>
      &#160;&#160;beware of http://jira.codehaus.org/browse/MNG-4747, see comments about system classloader in http://docs.oracle.com/javase/6/docs/api/java/lang/instrument/package-summary.html
    </p>
    <p>
      or change m2e junit launcher to use near-empty system classpath http://maven.apache.org/plugins/maven-surefire-plugin/examples/class-loading.html
    </p>
  </body>
</html>
</richcontent>
<icon BUILTIN="button_ok"/>
</node>
</node>
<node CREATED="1342697473193" ID="ID_325656276" MODIFIED="1342987919417" TEXT="many ITs require local eclipse installation as target platform">
<icon BUILTIN="button_ok"/>
</node>
<node CREATED="1342354892283" ID="ID_814665796" MODIFIED="1342995169819" TEXT="RunAs and DebugAs shortcuts">
<icon BUILTIN="button_ok"/>
</node>
<node CREATED="1342870559138" ID="ID_754914029" MODIFIED="1342987977556" TEXT="ITs projects are copied to tycho-its/projects instead of tycho-its/target/projects"/>
<node CREATED="1342966146533" ID="ID_1370258272" MODIFIED="1342966165745" TEXT="can&apos;t use EMBEDDED m2e maven runtime to lauch Tycho ITs"/>
<node CREATED="1342886154910" ID="ID_575204933" MODIFIED="1342886176022" TEXT="when running ITs from Eclipse, tee maven output too console"/>
<node CREATED="1342887220165" ID="ID_1083280679" MODIFIED="1342887236994" TEXT="don&apos;t enable source code lookup javaagent unless in debug mode"/>
<node CREATED="1342886742805" ID="ID_302914518" MODIFIED="1342886766629" TEXT="Source lookup is messed up for classes from JRE"/>
<node CREATED="1342889862027" ID="ID_867121639" MODIFIED="1342959044477" TEXT="consolidate handling of dev.properties and platform properties">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      embedded and external eclipse runtimes use different code to generate dev.properties
    </p>
    <p>
      
    </p>
    <p>
      WorkspaceP2RuntimeLocator#addPlatformProperties is used in embedded runtime
    </p>
    <p>
      
    </p>
    <p>
      DefaultEquinoxInstallationFactory#createDevProperties is used for external runtimes
    </p>
  </body>
</html>
</richcontent>
</node>
<node CREATED="1342991132780" ID="ID_1051388209" MODIFIED="1342991161882" TEXT="Make Maven runtime configurable in launch configuration GUI"/>
<node CREATED="1342958776486" ID="ID_215756189" MODIFIED="1342966344964" TEXT="can&apos;t run all ITs due to OOME permgen">
<node CREATED="1342886693958" ID="ID_63117409" MODIFIED="1342983587230" TEXT="MavenCli does not dispose plexus container when embedded in verifier"/>
<node CREATED="1342886541433" ID="ID_5933656" MODIFIED="1342886600608" TEXT="DefaultArtifactResolver is not properly disposed and leaks threads, workaround -Dmaven.artifact.threads=1"/>
<node CREATED="1342889686665" ID="ID_98395800" MODIFIED="1342889715636" TEXT="use plexus-utils 3.0.2 which fixes classloader leak via jvm shutdown hook"/>
<node CREATED="1342886445802" ID="ID_1159767228" MODIFIED="1342959178111" TEXT="Verifier holds single static embedded maven runtime instance"/>
<node CREATED="1342964757301" ID="ID_1276329036" LINK="https://bugs.eclipse.org/bugs/show_bug.cgi?id=385673" MODIFIED="1342965455989" TEXT="p2 (or ECF?) leak commons-httpclient threads">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      https://bugs.eclipse.org/bugs/show_bug.cgi?id=385673
    </p>
    <p>
      
    </p>
    <p>
      &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;try {
    </p>
    <p>
      &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;for (Bundle bundle : frameworkContext.getBundles()) {
    </p>
    <p>
      &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;if (&quot;org.apache.commons.httpclient&quot;.equals(bundle.getSymbolicName())) {
    </p>
    <p>
      &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;Class&lt;?&gt; c = bundle
    </p>
    <p>
      &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160; .loadClass(&quot;org.apache.commons.httpclient.MultiThreadedHttpConnectionManager&quot;);
    </p>
    <p>
      &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;Method m = c.getMethod(&quot;shutdownAll&quot;);
    </p>
    <p>
      &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;m.invoke(null);
    </p>
    <p>
      &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;}
    </p>
    <p>
      &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;}
    </p>
    <p>
      &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;} catch (Exception e) {
    </p>
    <p>
      &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;//
    </p>
    <p>
      &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;}
    </p>
  </body>
</html>
</richcontent>
</node>
</node>
<node CREATED="1342703892799" ID="ID_1312341174" MODIFIED="1342703932289" TEXT="Share state location between MavenLaunchParticipant and TychoITLaunchConfigurationDelegate"/>
</node>
<node CREATED="1342289371861" HGAP="23" ID="ID_1664503299" MODIFIED="1342353690436" POSITION="left" TEXT="source lookup can&apos;t determine GAV of clsses form target/test-classes">
<icon BUILTIN="button_ok"/>
</node>
<node CREATED="1342355097617" ID="ID_95971448" MODIFIED="1342355297844" POSITION="right" TEXT="option to disable mangle of standard java jsr45 strata">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      mangled default strata results in bad stack traces.
    </p>
    <p>
      either find a way to suppress jdt behaviour that causes bug 368212 or introduce UI opton to choose between two bad alteratives
    </p>
  </body>
</html></richcontent>
</node>
<node CREATED="1342355308218" ID="ID_1030545337" MODIFIED="1342355816318" POSITION="right" TEXT="remove workspace preference, tycho-insitu is on by default">
<icon BUILTIN="button_ok"/>
</node>
<node CREATED="1342710046358" ID="ID_379314167" MODIFIED="1342710097755" POSITION="right" TEXT="Import is binary maven project">
<richcontent TYPE="NOTE"><html>
  <head>
    
  </head>
  <body>
    <p>
      This is needed to be able to evaluate breakpoint expressions inside dependencies
    </p>
  </body>
</html>
</richcontent>
</node>
<node CREATED="1342753985673" ID="ID_617962526" MODIFIED="1342754364089" POSITION="left" TEXT="tycho-p2 runtime needs config.ini to start bundles when launching p2 in external jvm"/>
</node>
</map>
