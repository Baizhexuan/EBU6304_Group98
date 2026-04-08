#!/bin/zsh
set -e
./compile.sh
java -cp bin Main
