#!/bin/bash

cp ../master/repo/* .

echo $(ls -la)

new_jar_build=$(ls *.jar| tail -1)
echo "last jar build file name: $new_jar_build"

cp -f $new_jar_build Inspector-latest.jar

git config --global user.email "github-actions[bot]@users.noreply.github.com"
git config --global user.name "github-actions[bot]"
git status
if [ -n "$(git status --porcelain)" ]; then
    git add .
    git commit -m "Update repo"
    git push
else
    echo "No changes to commit"
fi
