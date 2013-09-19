#!/bin/sh

# compile the code
#/opt/jdk1.5.0/bin/javac -d dist -classpath lib/ProteomeCommons.org-IO.jar:lib/ProteomeCommons.org-JAF.jar -sourcepath src src/org/proteomecommons/pfsm/*.java src/org/proteomecommons/pfsm/*/*.java
javac -d dist -classpath dist/lib/ProteomeCommons.org-PFF.jar:dist/lib/ProteomeCommons.org-IO.jar:dist/lib/ProteomeCommons.org-JAF.jar -sourcepath src src/org/proteomecommons/pfsm/*.java src/org/proteomecommons/pfsm/*/*.java

# make the JAR file
cd dist
zip -9r Falkner-PFSM.jar org/proteomecommons/*/*.class org/proteomecommons/*/*/*.class META-INF

# make the archive
#zip -9r ../Archive.zip *
cd ..
