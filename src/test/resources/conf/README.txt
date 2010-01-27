This directory is needed for the GS Soil Implementation of the Thesaurus API and is only used, if this implementation is injected via Spring !
Then the ThesaurusConfig.properties file is used for Thesaurus settings.
Additional data will be downloaded and unpacked to target/gssoil by maven dependency mechanism, see "gssoil" profile in pom.
To run the GS Soil unit tests you have to add the stuff under target/gssoil to your eclipse classpath and libs.
