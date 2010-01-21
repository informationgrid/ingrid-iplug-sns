Von Edisoft gelieferte GSSoilSematicService.jar modifiziert:
- log4j.* Dateien aus jar entfernt ! Sonst wird extrem viel geloggt !
- package de mit abstract API gelöscht !!! -> in separater lib
- umbenannt (Version dran) wegen Einbindung als system dependency in pom (mit systemPath) für mvn eclipse:eclipse
