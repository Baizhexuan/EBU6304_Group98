#!/bin/zsh
set -e
sh ./compile.sh
java -cp bin SystemSmokeTest
java -cp bin AuthFlowTest
java -cp bin WorkflowRulesTest
java -cp bin CsvPersistenceTest
java -cp bin NotificationFlowTest
java -cp bin ValidationUtilsTest
java -cp bin MatchingServiceTest
java -cp bin ModelStateTest
java -cp bin ScoringServiceTest
java -cp bin NotificationReadStateTest
java -cp bin FileStorageLookupTest
java -cp bin DemoMetadataTest
