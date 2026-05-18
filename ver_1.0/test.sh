#!/bin/zsh
set -e
sh ./compile.sh
java -cp bin SystemSmokeTest
java -cp bin AuthFlowTest
java -cp bin WorkflowRulesTest
java -cp bin CsvPersistenceTest
