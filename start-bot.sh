#!/bin/sh

./gradlew clean distZip distTar installDist;

echo '\n';
echo '   ****************************';
echo '   ***   Running Github Bot ***';
echo '   ****************************';
echo '\n';

sh ./build/install/github-bot/bin/github-bot
