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
</html>
</richcontent>
<icon BUILTIN="button_ok"/>
</node>
<node CREATED="1342353907575" ID="ID_1444126652" MODIFIED="1342354281641" TEXT="forkJvm=false when running from tycho-insitu">
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
</html>
</richcontent>
<node CREATED="1342353970573" ID="ID_1879659168" MODIFIED="1342353982251" TEXT="verify this works for each individual test suite"/>
<node CREATED="1342355002335" ID="ID_1939901801" MODIFIED="1342355026034" TEXT="see if there is an easy way to make cli tests work with fork=false"/>
</node>
<node CREATED="1342354051170" ID="ID_1180395400" MODIFIED="1342354063349" TEXT="m2e workspace dependency resolution"/>
<node CREATED="1342354209155" ID="ID_661426993" MODIFIED="1342354219573" TEXT="pde/bundle workspace dependency resolution"/>
<node CREATED="1342354892283" ID="ID_814665796" MODIFIED="1342354903556" TEXT="RunAs and DebugAs shortcuts"/>
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
</html>
</richcontent>
</node>
<node CREATED="1342355308218" ID="ID_1030545337" MODIFIED="1342355324165" POSITION="right" TEXT="remove workspace preference, tycho-insitu is on by default"/>
</node>
</map>
