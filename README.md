# Route Lab

A Java 17 desktop demo of Dijkstra and A*. Run `grafer.Main` in IntelliJ to open the two tabs. You can also use these commands from the project folder.

```sh
mvn compile
java -cp target/classes grafer.Main
```

Choose the start and goal above the map. **Step** advances once, **Play** advances automatically, and **Reset** restarts the selected search. The arrow in the queue marks the next choice.

## Dijkstra road network

The nine towns are connected by 12 one-way roads. Arrowheads show the direction and the number beside each road is its length in kilometres. Dijkstra picks the town with the smallest known distance `g` from the start. The map shows how these distances change. The shortest route turns green when the goal is selected.

The default route is **23 km** from Skovby → Engby → Søby → Lilleby → Højby → Fjordby → Havneby. Try going from Havneby to Skovby to see what happens when the queue runs out.

## A* cheese maze

The maze has 10 × 10 tiles. The mouse can move horizontally or vertically on open tiles, at a cost of one move per tile. Nordby, Østby, Vestby and Sydby are the selectable rooms.

- `g` is the number of moves from the start along the best route found so far.
- `h` is the Manhattan distance to the cheese. Every open tile shows its `h`, and warmer tiles represent a stronger scent and a smaller `h`.
- `f = g + h` is the queue priority. A* picks the smallest `f` first.

Scent spreads across walls, so it estimates distance even when the mouse needs a detour. From Nordby the cheese at Østby starts at **h = 7**, and the shortest route takes **15 moves**. During the search, the blue outline marks the tile A* is exploring. Once A* finds a route, **Step** moves the mouse along it one tile at a time until it reaches the cheese.

`Main` opens the window. The search classes take a graph or grid and return a read-only `SearchProgress` snapshot with the queue, known distances, visited nodes and route. They can run without Swing. [Dijkstra.java](src/main/java/grafer/Dijkstra.java) · [AStar.java](src/main/java/grafer/AStar.java) · [SearchProgress.java](src/main/java/grafer/SearchProgress.java)

The `grafer.ui` package contains the desktop controls, the road and maze drawings, and the mouse animation. The maze panel starts moving the mouse when the search returns a route. [RouteLabPanel.java](src/main/java/grafer/ui/RouteLabPanel.java) · [RoadMapPanel.java](src/main/java/grafer/ui/RoadMapPanel.java) · [MazePanel.java](src/main/java/grafer/ui/MazePanel.java)
