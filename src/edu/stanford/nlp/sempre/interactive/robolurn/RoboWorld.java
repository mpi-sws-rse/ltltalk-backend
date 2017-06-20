package edu.stanford.nlp.sempre.interactive.robolurn;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.stanford.nlp.sempre.ContextValue;
import edu.stanford.nlp.sempre.Json;
import edu.stanford.nlp.sempre.NaiveKnowledgeGraph;
import edu.stanford.nlp.sempre.StringValue;
import edu.stanford.nlp.sempre.interactive.PathAction;
import edu.stanford.nlp.sempre.interactive.World;
import edu.stanford.nlp.sempre.interactive.planner.PathFinder;
import fig.basic.LogInfo;
import fig.basic.Option;

public class RoboWorld extends World {
  public static class Options {
    @Option(gloss = "maximum number of cubes to convert")
    public int maxBlocks = 1024 ^ 2;
  }

  enum BasicColor {
    Red(0), Orange(1), Yellow(2), Green(3), Blue(4), Purple(5), Pink(6), Brown(7);//, None(-5);
    private final int value;

    BasicColor(int value) {
      this.value = value;
    }
    
    public String toString() {
      return this.name().toLowerCase();
    }

    public BasicColor fromString(String color) {
      for (BasicColor c : BasicColor.values())
        if (c.name().equalsIgnoreCase(color))
          return c;
      return null;
    }
  };
  
  public String descriptionNot(String str) {
    Set<String> colors = new HashSet<>(Arrays.asList(str.split(",")));
    StringBuilder sb = new StringBuilder();
    for (BasicColor c : BasicColor.values()) {
      if (!colors.contains(c.toString())) {
        if (sb.length() != 0)
          sb.append(",");
        sb.append(c.toString());
      }
    }
    return sb.toString();
  }
  
  public String descriptionAnd(String str1, String str2) {
    Set<String> colors1 = new HashSet<>(Arrays.asList(str1.split(",")));
    Set<String> colors2 = new HashSet<>(Arrays.asList(str2.split(",")));
    StringBuilder sb = new StringBuilder();
    for (String c : colors1) {
      if (colors2.contains(c)) {
        if (sb.length() != 0)
          sb.append(",");
        sb.append(c.toString());
      }
    }
    return sb.toString();
  }
  
  public String descriptionOr(String str1, String str2) {
    Set<String> colors1 = new HashSet<>(Arrays.asList(str1.split(",")));
    List<String> colors2 = Arrays.asList(str2.split(","));
    colors1.addAll(colors2);
    StringBuilder sb = new StringBuilder();
    for (String c : colors1) {
      if (sb.length() != 0)
        sb.append(",");
      sb.append(c.toString());
    }
    return sb.toString();
  }
  
  
  public static Options opts = new Options();

  public static RoboWorld fromContext(ContextValue context) {
    if (context == null || context.graph == null) {
      return fromJSON("[[0,0,[]],[1,1,\"wall\",null]]");
    }
    NaiveKnowledgeGraph graph = (NaiveKnowledgeGraph) context.graph;
    String wallString = ((StringValue) graph.triples.get(0).e1).value;
    return fromJSON(wallString);
  }

  private List<RoboAction> pathActions;

  private Robot robot;

  public RoboWorld(Set<WorldBlock> blockset) {
    super();
    this.allBlocks = blockset;
    this.pathActions = new ArrayList<RoboAction>();
  }

  @Override
  public String toJSON() {
    return "NOT YET SUPPORTED";
  }

  @Override
  public String getJSONPath() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    boolean first = true;
    for (RoboAction pa : pathActions) {
      if (first)
        first = false;
      else
        sb.append(",");
      sb.append(pa.toJSON());
    }
    sb.append("]");
    return sb.toString().replace(" ","");
  }

  private static RoboWorld fromJSON(String wallString) {
    @SuppressWarnings("unchecked")
    List<List<Object>> blockstr = Json.readValueHard(wallString, List.class);
    List<Object> rawRobot = blockstr.get(0);
    Robot robot = new Robot(
      (int) rawRobot.get(0),
      (int) rawRobot.get(1),
      (List<String>) rawRobot.get(2)
    );
    Set<WorldBlock> blocks = blockstr.subList(1, blockstr.size()).stream().map(c -> {
      return WorldBlock.fromJSONObject(c);
    }).collect(Collectors.toSet());
    RoboWorld world = new RoboWorld(blocks);
    world.robot = robot;
    return world;
  }

  @Override
  public Set<WorldBlock> has(String rel, Set<Object> values) {
    return this.allBlocks.stream().filter(i -> values.contains(i.get(rel))).collect(Collectors.toSet());
  }

  @Override
  public Set<Object> get(String rel, Set<WorldBlock> subset) {
    return subset.stream().map(i -> i.get(rel)).collect(Collectors.toSet());
  }

  public void noop() {
  }
  
  public void visit(int x, int y) {
    List<Point> walls = allBlocks.stream()
        .filter(b -> b.type == WorldBlock.Type.WALL)
        .map(b -> new Point(b.x, b.y))
        .collect(Collectors.toList()
    );
    int maxX = 0, maxY = 0, minX = 0, minY = 0;
    for (Point p : walls) {
      if (p.x > maxX)
        maxX = p.x;
      else if (p.x < minX)
        minX = p.x;

      if (p.y > maxY)
        maxY = p.y;
      else if (p.y < minY)
        minY = p.y;
    }
    // Is this unclear? It is quite beautiful, though.
    pathActions.addAll(
        PathFinder.findPath(
            walls,
            new Point(robot.x, robot.y),
            new Point(x,y),
            new Point(minX,minY),
            new Point(maxX,maxY))
        .stream().map(p -> new RoboAction(p.x, p.y, RoboAction.Action.PATH))
        .collect(Collectors.toList())
    );
    if (pathActions.size() > 1) {
      RoboAction last = pathActions.get(pathActions.size() - 1);
      last.action = RoboAction.Action.DESTINATION;
      robot.x = last.x;
      robot.y = last.y;
    }
  }
  
  private Set<String> parseSpec(String spec) {
    Set<String> colors = new HashSet<>(Arrays.asList(spec.split(",")));
//    LogInfo.logs("~~~~~~~");
//    LogInfo.logs(spec);
    return colors;
  }
  
//  public void pickSingle(String spec) {
//    pick(spec, true);
//  }
//
//  public void pickAll(String spec) {
//    pick(spec, false);
//  }
//  
//    if (cardinality.equals("single"))
//      pick(spec, true);
//    else
//      pick(spec, false);
//  }
  
  public void pick(String cardinality, String spec) {
    boolean single;
    if (cardinality.equals("single"))
      single = true;
    else
      single = false;
    Set<String> colors = parseSpec(spec);
    boolean match = false;
    Iterator<WorldBlock> iter = allBlocks.iterator();
    WorldBlock b;
    while (iter.hasNext()) {
      b = iter.next();
      if (b.x == robot.x && b.y == robot.y && colors.contains(b.color)) {
        match = true;
        robot.items.add(b.color);
        pathActions.add(
            new RoboAction(robot.x, robot.y, RoboAction.Action.PICKITEM, b.color, true));
        iter.remove();
        if (single)
          break;
      }
    }
    if (!match) {
      pathActions.add(new RoboAction(robot.x, robot.y, RoboAction.Action.PICKITEM, spec, false));
    }
  }

//  public void dropSingle(String spec) {
//    drop(spec, true);
//  }
//
//  public void dropAll(String spec) {
//    drop(spec, false);
//  }
  
  public void drop(String cardinality, String spec) {
    boolean single;
    if (cardinality.equals("single"))
      single = true;
    else
      single = false;
    Set<String> colors = parseSpec(spec);
    boolean match = false;
    Iterator<String> iter = robot.items.iterator();
    String item;
    while (iter.hasNext()) {
      item = iter.next();
      if (colors.contains(item)) {
        match = true;
        allBlocks.add(new WorldBlock(robot.x, robot.y, WorldBlock.Type.ITEM));
        pathActions.add(
            new RoboAction(robot.x, robot.y, RoboAction.Action.DROPITEM, item, true));
        iter.remove();
        if (single)
          break;
      }
    }
    if (!match) {
      pathActions.add(new RoboAction(robot.x, robot.y, RoboAction.Action.DROPITEM, spec, false));
    }
  }

  private void refreshSet(Set<WorldBlock> set) {
    List<WorldBlock> s = new ArrayList<>(set);
    set.clear();
    set.addAll(s);
  }

  @SuppressWarnings("unused")
  private void keyConsistency() {
    refreshSet(allBlocks);
  }
}
