#!/bin/zsh
set -e
mkdir -p javadocs
javadoc \
  -d javadocs \
  -overview docs/javadoc_overview.html \
  -windowtitle "BUPT TA Recruitment System API" \
  -doctitle "BUPT TA Recruitment System API" \
  -Xdoclint:all,-missing \
  src/*.java
