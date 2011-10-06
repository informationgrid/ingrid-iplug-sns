This project has a "gssoil" profile in the pom !!!
Call the "gssoil" profile to create an installer suited for GS Soil !

mvn -Pgssoil ...

Then the libraries and the resources needed for the GS Soil implementation of the Thesaurus,
Gazetteer and FullClassify API are downloaded and unpacked to target/gssoil and are included
in the assembly (when installer is generated).
To activate GSSoil implementation of API you have to inject it via Spring in the file 
spring/external-services.xml (instead of SNS implementation).

Eclipse:
To run the GS Soil unit tests you have to add the gs soil stuff under target/gssoil
(present if "mvn -Pgssoil ..." is executed !) to your eclipse classpath and libs.
- add jars in target/gssoil/lib to your libraries
- add directory target/gssoil/conf as source path (NOTICE: this one includes the properties files for settings !)

Further changes for GS Soil:
- set property "gazetteerService.getLocationsFromText.ignoreCase" to "true" in spring/external-services.xml (initial lookup in extended search)