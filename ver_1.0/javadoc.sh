#!/bin/zsh
set -e
mkdir -p javadocs
javadoc -d javadocs src/*.java
