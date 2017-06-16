package edu.stanford.nlp.sempre.interactive.planner;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import es.usc.citius.hipster.algorithm.Algorithm;
import es.usc.citius.hipster.algorithm.Algorithm.SearchResult;
import es.usc.citius.hipster.algorithm.Hipster;
import es.usc.citius.hipster.model.Transition;
import es.usc.citius.hipster.model.function.CostFunction;
import es.usc.citius.hipster.model.function.impl.StateTransitionFunction;
import es.usc.citius.hipster.model.problem.ProblemBuilder;
import es.usc.citius.hipster.model.problem.SearchProblem;
import es.usc.citius.hipster.util.examples.maze.Maze2D;
import es.usc.citius.hipster.util.examples.maze.Mazes;
import fig.basic.LogInfo;


public class PathFinder {
    public static void main(String[] args) throws InterruptedException {
      Mazes.TestMaze example = Mazes.TestMaze.MAZE1;
      String[] myMaze = new String[]{
          "XXXXXXXXXXX",
          "X         X",
          "X         X",
          "X S       X",
          "X         X",
          "XXXXX  XXXX",
          "X         X",
          "X         X",
          "X       G X",
          "X         X", 
          "XXXXXXXXXXX"};

          
//      final Maze2D maze = example.getMaze();
      final Maze2D maze = new Maze2D(myMaze);
      
      Point origin = maze.getInitialLoc();
      Point goal = maze.getGoalLoc();
      
      
      SearchProblem p = ProblemBuilder.create()
          .initialState(origin)
          .defineProblemWithExplicitActions()
          .useTransitionFunction(new StateTransitionFunction<Point>() {
            @Override
            public Iterable<Point> successorsOf(Point state) {
              return maze.validLocationsFrom(state);
            }
          })
          .useCostFunction(new CostFunction<Void, Point, Double>() {
            @Override
            public Double evaluate(Transition<Void, Point> transition) {
              Point source = transition.getFromState();
              Point destination = transition.getState();
              return source.distance(destination);
            }
          })
          .build();

       //System.out.println(Hipster.createAStar(p).search(goal).getOptimalPaths());
       SearchResult result = Hipster.createAStar(p).search(goal);
       System.out.println(result.toString());
    }
    
    /** Finds a path from start to end given a set of wall coordinates.
     * low/highCorner give size of the map.
     */
    public static List<Point> findPath(Collection<Point> map, Point origin, Point goal, Point lowCorner, Point highCorner) {
      int xSize = highCorner.x - lowCorner.x + 1;
      int ySize = highCorner.y - lowCorner.y + 1;
      origin.x -= lowCorner.x;
      origin.y -= lowCorner.y;
      goal.x -= lowCorner.x;
      goal.y -= lowCorner.y;
      
      if (goal.x < 0)
        goal.x = 0;
      else if (goal.x >= xSize)
        goal.x = xSize - 1;

      if (goal.y < 0)
        goal.y = 0;
      else if (goal.y >= ySize)
        goal.y = ySize -1;
      
      char[][] charMap =
          new char[ySize][xSize];
      
      for (Point p : map) {
        charMap[p.y - lowCorner.y][p.x - lowCorner.x] = Maze2D.Symbol.OCCUPIED.value();
      }
      // TODO Check if the space is a wall?
      charMap[origin.y][origin.x] = Maze2D.Symbol.START.value();
      charMap[goal.y][goal.x] = Maze2D.Symbol.GOAL.value();
      for (int i = 0; i < ySize; i++) {
        for (int j = 0; j < xSize; j++) {
          if (charMap[i][j] == '\u0000') {
            charMap[i][j] = Maze2D.Symbol.EMPTY.value();
          }
        }
      }
      Maze2D maze = new Maze2D(charMap);
//      LogInfo.logs(maze.toString());

      SearchProblem problem = ProblemBuilder.create()
          .initialState(origin)
          .defineProblemWithExplicitActions()
          .useTransitionFunction(new StateTransitionFunction<Point>() {
            @Override
            public Iterable<Point> successorsOf(Point state) {
//              /return maze.validLocationsFrom(state);
                Collection<Point> validMoves = new HashSet<Point>();
                // Check for all valid movements
                try {
                    if (maze.isFree(new Point(state.x + 1, state.y)))
                        validMoves.add(new Point(state.x + 1, state.y));
                    if (maze.isFree(new Point(state.x - 1, state.y)))
                        validMoves.add(new Point(state.x - 1, state.y));
                    if (maze.isFree(new Point(state.x, state.y + 1)))
                        validMoves.add(new Point(state.x, state.y + 1));
                    if (maze.isFree(new Point(state.x, state.y - 1)))
                        validMoves.add(new Point(state.x, state.y - 1));
                } catch (ArrayIndexOutOfBoundsException ex) {
                    // Invalid move!
                }

                return validMoves;
              }
          })
          .useCostFunction(new CostFunction<Void, Point, Double>() {
            @Override
            public Double evaluate(Transition<Void, Point> transition) {
              Point source = transition.getFromState();
              Point destination = transition.getState();
              return source.distance(destination);
            }
          })
          .build();

      SearchResult result = Hipster.createAStar(problem).search(goal);
      
      // Readjust coordinates
      List<Point> path = (List<Point>) result.getOptimalPaths().get(0);
      return path
          .stream().map(p -> {
            p.x += lowCorner.x;
            p.y += lowCorner.y;
            return p;
          }).collect(Collectors.toList());
    }
}
