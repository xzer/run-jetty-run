#!/bin/bash

mvn org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=$1

echo $1 > currentVersion