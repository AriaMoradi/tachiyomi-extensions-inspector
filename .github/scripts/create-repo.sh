#!/bin/bash

# Get last commit message
#last_commit_log=$(git log -1 --pretty=format:"%s")
#echo "last commit log: $last_commit_log"
#
#filter_count=$(echo "$last_commit_log" | grep -e '\[RELEASE CI\]' -e '\[CI RELEASE\]' | wc -c)
#echo "count is: $filter_count"

mkdir -p repo/
cp server/build/Inspector-*.jar repo/


#if [ "$filter_count" -gt 0 ]; then
#  cp server/build/Inspector-*.jar repo/
#fi