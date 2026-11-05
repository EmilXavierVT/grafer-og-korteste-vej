# Grafer og korteste vej

To rutesøgninger i Java 17. Dijkstra finder den billigste rute i et rettet vejnet. A* finder den korteste rute gennem et grid med veje og forhindringer.

## Kør programmet

Kør fra repoets rod med JDK 17 eller nyere og Maven.

```sh
mvn compile
java -cp target/classes grafer.Main
```

Vælg `1` for Dijkstra eller `2` for A*. Vælg startby og målby, eller tryk **Enter** for at bruge standardruten. Under søgningen viser **Enter** ét trin mere, og `a` viser resten af trinnene uden pauser.

Du kan også køre alle trin direkte fra kommandolinjen.

```sh
java -cp target/classes grafer.Main dijkstra Skovby Havneby
java -cp target/classes grafer.Main astar Nordby Østby
```

## Hvad viser visualiseringen?

Kortet viser, **hvordan søgningen breder sig**. En by eller celle går fra uopdaget `.` til opdaget `+` og bliver markeret med `@`, når algoritmen tager den ud af køen. På de følgende trin betyder `x`, at den korteste afstand til den er fastlagt. Til sidst fremhæver `*` ruten, så man kan se, hvilke af de undersøgte byer eller celler ruten faktisk går igennem.

Køen viser, **hvorfor den næste by eller celle vælges**. Første række er markeret med `→`, og tallene i tabellen gør det muligt at sammenligne valget med alternativerne. Kortet og køen opdateres efter, at den aktuelle by eller celles naboer er undersøgt. Når målet tages ud af køen, slutter søgningen.

### Dijkstra

Vejnettet har ni byer og 12 ensrettede veje. Pilene på kortet viser kørselsretningen, og tallet ved hver pil er vejens længde i kilometer. Dijkstra vælger den by med mindst `g`, den hidtil korteste kendte afstand fra startbyen. Bynavnet afgør rækkefølgen, hvis to byer har samme `g`.

Fra Skovby opdager søgningen først Lilleby til 12 km og Engby til 4 km. Engby bliver valgt først. Senere kommer søgningen til Søby på 7 km og finder en vej på 2 km videre til Lilleby. Det sænker Lillebys `g` fra 12 til 9 km og gør Søby til Lillebys forgænger.

**Standardruten på 23 km** går fra Skovby → Engby → Søby → Lilleby → Højby → Fjordby → Havneby. Havneby har ingen udgående veje, så man kan afprøve en søgning uden resultat ved at bytte om på start og mål.

### A*

Gridet er 10 × 10 med Nordby `(2,2)`, Østby `(2,9)`, Vestby `(8,2)` og Sydby `(9,8)`. Koordinaterne angiver række og kolonne. `#` er en spærret celle. Vandrette og lodrette træk koster ét hver.

A* vælger cellen med lavest `f = g + h`. `g` tæller træk fra start, og `h` er Manhattan-afstanden til målet, altså forskellen i række plus forskellen i kolonne regnet som positive afstande. Ved samme `f` prioriteres laveste `h`, derefter række og kolonne. Hvert træk kan højst sænke `h` med én, så `h` er en nedre grænse for antallet af træk til målet.

Fra Nordby har cellerne `(2,3)` og `(3,2)` begge `g = 1`. Cellen `(2,3)` vælges først, fordi dens `h` er 6 og giver `f = 7`, mens `(3,2)` har `h = 8` og `f = 9`. Senere tvinger spærringerne søgningen ned til række 6. **Den korteste rute fra Nordby til Østby tager 15 træk.**

## Koden

- [DemoMaps.java](src/main/java/grafer/DemoMaps.java) indeholder de to kort, bynavne og vejvægte.
- [Dijkstra.java](src/main/java/grafer/Dijkstra.java) og [AStar.java](src/main/java/grafer/AStar.java) implementerer søgningerne. Begge gemmer en forgænger, når en kortere vej findes, og følger forgængerne baglæns fra målet for at finde ruten.
- [ConsoleView.java](src/main/java/grafer/ConsoleView.java) viser valg, kø og rute, og [ConsoleMaps.java](src/main/java/grafer/ConsoleMaps.java) tegner kortene ud fra den aktuelle søgetilstand.
