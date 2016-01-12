#!/bin/bash

sitefolder=$1
currentFolder=`pwd`

if [ "$sitefolder"X = "X" ]
then
    echo $0 '<site folder>'
    exit 1
fi

# mvn clean package

if [ $? -ne 0 ]
then
    echo
    echo
    echo mvn failed.
    exit 1
fi

currentVersion=`head -n1 currentVersion`

head -n1 currentVersion | grep "\-SNAPSHOT"

if [ $? -eq 0 ]
then
    copyTarget=nightly
else
    copyTarget=current
fi

echo copyTarget is $copyTarget

cd $sitefolder/updatesite
rm -rf $copyTarget
cp -R $currentFolder/Runjettyrun/updatesite/target/repository .
mv repository $copyTarget

echo copied files to $sitefolder/updatesite/$copyTarget

if [ $copyTarget == "current" ]
then
    cp -R $copyTarget ./archive/.
    mv ./archive/$copyTarget ./archive/$currentVersion
    echo archied file to $sitefolder/updatesite/archive/$currentVersion
fi

echo release finished.




