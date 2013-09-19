#!/bin/sh

# remove the JAR
rm -f dist/ProteomeCommons.org-PFSM.jar

# remove the compiled code
rm -fr dist/org

# remove snapshots of source-code
rm -fr dist/src

# remove snapshots of api docs
rm -fr dist/docs/api

# remove the archive
rm -f Archive.zip
