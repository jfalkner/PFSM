#!/bin/sh

# run the example code
#java -cp .:../../ProteomeCommons.org-PFSM.jar:../../lib/ProteomeCommons.org-IO.jar:../../lib/ProteomeCommons.org-JAF.jar org.proteomecommons.pfsm.util.FASTACompressedSequenceMatcher search.xml
java -Xms128m -Xmx256m -cp .:../../ProteomeCommons.org-PFSM.jar:../../lib/ProteomeCommons.org-IO.jar:../../lib/ProteomeCommons.org-JAF.jar org.proteomecommons.pfsm.util.FASTASequenceMatcher search.xml

