package edu.stanford.nlp.sempre.interactive.robolurn;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.stanford.nlp.sempre.ContextValue;
import edu.stanford.nlp.sempre.Json;
import edu.stanford.nlp.sempre.NaiveKnowledgeGraph;
import edu.stanford.nlp.sempre.StringValue;
import edu.stanford.nlp.sempre.interactive.Block;
import edu.stanford.nlp.sempre.interactive.PathAction;
import edu.stanford.nlp.sempre.interactive.VariablePoint;
import edu.stanford.nlp.sempre.interactive.World;
import edu.stanford.nlp.sempre.interactive.planner.PathFinder;
import fig.basic.LogInfo;
import fig.basic.Option;

public class RoboWorld extends World<RoboBlock> {
  public static class Options {
    @Option(gloss = "maximum number of cubes to convert")
    public int maxBlocks = 1024 ^ 2;
  }
  
  public Set<Item> items;
  public Set<Wall> walls;
  
  private Point lowCorner;
  private Point highCorner;
  
  public String descriptionNot(String str) {
    Set<String> colors = new HashSet<>(Arrays.asList(str.split(",")));
    StringBuilder sb = new StringBuilder();
    for (Color.BasicColor c : Color.BasicColor.values()) {
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

  public RoboWorld(Set<Wall> walls, Set<Item> items) {
    super();
    this.walls = walls;
    this.items = items;
    this.pathActions = new ArrayList<RoboAction>();
    this.selectedField = new Point();
    this.findCorners();
    this.variables = new HashMap<>();
  }

  private void findCorners() {
    int maxX = 0, maxY = 0, minX = 0, minY = 0;
    for (Wall w : walls) {
      if (w.point.x > maxX)
        maxX = w.point.x;
      else if (w.point.x < minX)
        minX = w.point.x;

      if (w.point.y > maxY)
        maxY = w.point.y;
      else if (w.point.y < minY)
        minY = w.point.y;
    }
    this.lowCorner = new Point(minX, minY);
    this.highCorner = new Point(maxX, maxY);
  }
  
  public Point getLowCorner() {
    return this.lowCorner;
  }
  
  public Point getHighCorner() {
    return this.highCorner;
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
    
    Point robotPoint = new Point((int) rawRobot.get(0), (int) rawRobot.get(1));
    Set<Item> robotItems = ((Collection<String>) rawRobot.get(2)).stream()
        .map(c -> new Item(null, Color.BasicColor.fromString(c), true))
        .collect(Collectors.toSet());
    
//    Robot robot = new Robot(robotPoint, robotItems);
    Robot robot = new Robot(robotPoint);
    Set<Wall> walls = new HashSet<>();
    Set<Item> items = robotItems;
    blockstr.subList(1, blockstr.size()).stream().forEach(c -> {
      RoboBlock rb = RoboBlock.fromJSONObject(c);
      if (rb instanceof Item)
        items.add((Item) rb);
      else
        walls.add((Wall) rb);
    });
    RoboWorld world = new RoboWorld(walls, items);
    world.robot = robot;
    return world;
  }

  @Override
  public Set<? extends RoboBlock> has(String rel, Set<Object> values) {
    if ("color".equals(rel) || "type".equals(rel) || "field".equals(rel)) {
      return items.stream()
          .filter(i -> values.contains(i.get(rel)))
          .collect(Collectors.toSet());
    }
    throw new RuntimeException("getting property " + rel + " is not supported.");
  }

  @Override
  public Set<Object> get(String rel, Set<Block> subset) {
    return subset.stream().map(i -> i.get(rel)).collect(Collectors.toSet());
  }
  
  public Set<Item> allItems() {
    return this.items;
  }

  @SuppressWarnings("unchecked")
  public Set<Color.BasicColor> allColors() {
    return (Set<Color.BasicColor>) universalSet(Color.BasicColor.class);
  }
  
  @Override
  public Set<? extends Object> universalSet(Object o) {
    if (o instanceof Item) {
      return this.items;
    } else if (o instanceof Wall) {
      return this.walls;
    }  else if (o instanceof Point) {
      return this.getOpenFields();
    }
    return new HashSet<>();
  }

  public Point getRobotLocation() {
    return new Point(robot.point.x, robot.point.y);
  }
  
  public void noop() {
  }
  
  public void visit(Point p, Set<Point> avoidSet) {
    if (p instanceof VariablePoint)
      p = variables.get(((VariablePoint) p).name);
    this.selectedField = p;
    gotoSelectedField(avoidSet);
  }
  
  public void visit(Point p) {
    visit(p, new HashSet<>());
  }
  
  public void visit() {
    gotoSelectedField(new HashSet<>());
  }
  
  private void gotoSelectedField(Set<Point> avoidSet) {
    int x = selectedField.x;
    int y = selectedField.y;
    
    avoidSet.addAll(walls.stream().map(w -> w.point).collect(Collectors.toList()));
    // Is this unclear? It is quite beautiful, though.
    pathActions.addAll(
        PathFinder.findPath(
            avoidSet,
            new Point(robot.point.x, robot.point.y),
            new Point(x,y),
            this.getLowCorner(),
            this.getHighCorner())
        .stream().map(p -> new RoboAction(p, RoboAction.Action.PATH))
        .collect(Collectors.toList())
    );
    if (pathActions.size() > 0) {
      RoboAction last = pathActions.get(pathActions.size() - 1);
      last.action = RoboAction.Action.DESTINATION;
      robot.point = last.point;
    }
  }
  
  public void pick(int cardinality, Set<Item> blocks) {
    if (cardinality == -1)
      cardinality = Integer.MAX_VALUE;
    boolean match = false;
    Item item;
    for (Iterator<Item> iter = items.iterator(); iter.hasNext(); ) {
      item = iter.next();
      if (item.isCarried())
        continue;
      if (item.point.x == robot.point.x && item.point.y == robot.point.y && item.isIn(blocks)) {
        match = true;
//        robot.items.add(item);
        item.setCarried(true);
        pathActions.add(new RoboAction(robot.point, RoboAction.Action.PICKITEM, item.color, true));
//        iter.remove();
        if (--cardinality == 0)
          break;
      }
    }
    if (!match) {
      pathActions.add(new RoboAction(robot.point, RoboAction.Action.PICKITEM, null, false));
    }
    keyConsistency();
  }
  
  public void drop(int cardinality, Set<Item> blocks) {
    if (cardinality == -1)
      cardinality = Integer.MAX_VALUE;
    boolean match = false;
    Item item;
    for (Iterator<Item> iter =  blocks.iterator(); iter.hasNext(); ) {
      item = iter.next();
      if (!item.isCarried())
        continue;
      if (item.isIn(blocks)) {
//      if (blocks.contains(item)) {
        match = true;
//        item.point = robot.point;
        assert(robot.point != null);
        item.setCarried(false);
//        items.add(item);
        pathActions.add(
            new RoboAction(robot.point, RoboAction.Action.DROPITEM, item.color, true));
        iter.remove();
        if (--cardinality == 0)
          break;
      }
    }
    if (!match) {
      pathActions.add(new RoboAction(robot.point, RoboAction.Action.DROPITEM, null, false));
    }
    keyConsistency();
  }

  public VariablePoint getVariable(String name) {
    return new VariablePoint(0, 0, name);
  }

  private <T> void refreshSet(Set<T> set) {
    List<T> s = new ArrayList<>(set);
    set.clear();
    set.addAll(s);
  }

  private void keyConsistency() {
    refreshSet(walls);
    refreshSet(items);
    if (open != null)
      refreshSet(open);
  }
}
