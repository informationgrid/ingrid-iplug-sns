SNS iPlug
========

The SNS-iPlug provides an interface to the SNS Semantic Network Service (http://www.semantic-network.de). It provides interfaces to:

- Thesaurus
- Gazeteer
- Environmental chronicle
 
The services are used in various InGrid components.

Instead of the SNS other interfaces can be used to provide the service functionalities.


Features
--------

- connects thesaurus, gazeteer and chronics to ingrid components
- abstracts different service providers
- GUI for easy administration


Requirements
-------------

- a running InGrid Software System

Installation
------------

Download from https://distributions.informationgrid.eu/ingrid-iplug-sns/
 
or

build from source with `mvn clean package`.

Execute

```
java -jar ingrid-iplug-sns-x.x.x-installer.jar
```

and follow the install instructions.

Obtain further information at http://www.ingrid-oss.eu/


Contribute
----------

- Issue Tracker: https://github.com/informationgrid/ingrid-iplug-sns/issues
- Source Code: https://github.com/informationgrid/ingrid-iplug-sns
 
### Setup Eclipse project

* import project as Maven-Project
* right click on project and select Maven -> Select Maven Profiles ... (Ctrl+Alt+P)
* choose profile "development"
* run "mvn compile" from Commandline (unpacks base-webapp) 
* run de.ingrid.iplug.sns.SnsPlug as Java Application
* in browser call "http://localhost:10018" with login "admin/admin"

### Setup IntelliJ IDEA project

* choose action "Add Maven Projects" and select pom.xml
* in Maven panel expand "Profiles" and make sure "development" is checked
* run de.ingrid.iplug.sns.SnsPlug
* in browser call "http://localhost:10018" with login "admin/admin"

Support
-------

If you are having issues, please let us know: info@informationgrid.eu

License
-------

The project is licensed under the EUPL license.
