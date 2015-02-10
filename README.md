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

- connects thesarus, gazeteer and chronics to ingrid components
- abstracts different service providers
- GUI for easy administration


Requirements
-------------

- a running InGrid Software System

Installation
------------

Download from https://dev.informationgrid.eu/ingrid-distributions/ingrid-iplug-sns/
 
or

build from source with `mvn package assembly:single`.

Execute

```
java -jar ingrid-iplug-sns-x.x.x-installer.jar
```

and follow the install instructions.

Obtain further information at https://dev.informationgrid.eu/


Contribute
----------

- Issue Tracker: https://github.com/informationgrid/ingrid-iplug-sns/issues
- Source Code: https://github.com/informationgrid/ingrid-iplug-sns
 
### Set up eclipse project

```
mvn eclipse:eclipse
```

and import project into eclipse.

### Debug under eclipse

TBD


Support
-------

If you are having issues, please let us know: info@informationgrid.eu

License
-------

The project is licensed under the EUPL license.
