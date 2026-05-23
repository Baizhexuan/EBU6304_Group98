#!/bin/zsh
set -e
mkdir -p bin
javac -encoding UTF-8 -d bin src/*.java
