mvn clean
mvn scm:update -o
mvn release:prepare -Dmaven.test.skip=true -o 
mvn release:perform -Dmaven.test.skip=true -o
