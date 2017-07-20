package edu.stanford.nlp.sempre.interactive.robolurn;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import edu.stanford.nlp.sempre.ContextValue;
import edu.stanford.nlp.sempre.Json;
import edu.stanford.nlp.sempre.NaiveKnowledgeGraph;
import edu.stanford.nlp.sempre.StringValue;
import edu.stanford.nlp.sempre.interactive.Block;
import edu.stanford.nlp.sempre.interactive.VariablePoint;
import edu.stanford.nlp.sempre.interactive.World;
import edu.stanford.nlp.sempre.interactive.annotations.ActionMethod;
import edu.stanford.nlp.sempre.interactive.planner.PathFinder;
import fig.basic.Option;

public class RoboWorld extends World<RoboBlock> {
  public static class Options {
    @Option(gloss = "maximum number of cubes to convert")
    public int maxBlocks = 1024 ^ 2;
  }
  

  
  private Point lowCorner;
  private Point highCorner;
  
  private Map<String, Set<Point>> rooms;
  
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
  
  public RoboWorld(Robot robot, Set<Wall> walls, Set<Item> items) {
    this(robot, walls, items, new HashMap<>());
  }
  
  public RoboWorld(Robot robot, Set<Wall> walls, Set<Item> items, Map<String, Set<Point>> rooms) {
    super();
    this.robot = robot;
    this.walls = walls;
    this.items = items;
    this.rooms = rooms;
    this.pathActions = new ArrayList<RoboAction>();
    this.findCorners();
    this.selectedArea = Optional.empty();
    this.selectedPoint = Optional.empty();
  }

  @SuppressWarnings("unchecked")
  public RoboWorld clone() {
    Set<Wall> newWalls = walls.stream().map(w -> (Wall) w.clone()).collect(Collectors.toSet());
    Set<Item> newItems = items.stream().map(i -> (Item) i.clone()).collect(Collectors.toSet());
    return new RoboWorld(
         robot.clone(),
         newWalls,
         newItems,
         new HashMap<>(rooms)
    );
  }
  
  private void findCorners() {
    int maxX = 0, maxY = 0, minX = 0, minY = 0;
    for (RoboBlock w : walls) {
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

  @SuppressWarnings("unchecked")
  private static RoboWorld fromJSON(String jsonString) {
    //List<List<Object>> blockstr = Json.readValueHard(jsonString, List.class);
    Map<String, Object> ctxMap = Json.readValueHard(jsonString, Map.class);
    //List<Object> rawRobot = blockstr.get(0);
    List<Object> rawRobot = (List<Object>) ctxMap.get("robot");
    
    Point robotPoint = new Point((int) rawRobot.get(0), (int) rawRobot.get(1));
    Set<Item> robotItems = ((Collection<String>) rawRobot.get(2)).stream()
        .map(c -> new Item(null, c, true))
        .collect(Collectors.toSet());
    
//    Robot robot = new Robot(robotPoint, robotItems);
    Robot robot = new Robot(robotPoint);
    Set<Wall> walls = new HashSet<>();
    Set<Item> items = robotItems;
    //blockstr.subList(1, blockstr.size()).stream().forEach(c -> {
    ((List<Object>) ctxMap.get("world")).stream().forEach(c -> {
      RoboBlock rb = RoboBlock.fromJSONObject((List<Object>) c);
      if (rb instanceof Item)
        items.add((Item) rb);
      else if (rb instanceof Wall)
        walls.add((Wall) rb);
//      else if (rb instanceof Point)
//        rooms.add((Wall) rb);
    });
    RoboWorld world = new RoboWorld(robot, walls, items);
    
    Set<Point> points;
    for (Entry<String, List<List<Integer>>> entry : ((Map<String, List<List<Integer>>>) ctxMap.get("rooms")).entrySet()) {
      points = entry.getValue().stream().map(p -> new Point(p.get(0), p.get(1))).collect(Collectors.toSet());
      world.rooms.put(entry.getKey(), points);
    }
    
    return world;
  }

  @Override
  public Set<? extends RoboBlock> has(String rel, Set<Object> values) {
    String[] qualifiedRel = rel.split("\\?");
    if (qualifiedRel.length < 2)
      throw new RuntimeException(rel + " must be qualified with items?rel or walls?rel");

    if ("items".equals(qualifiedRel[0])) {
      if ("color".equals(qualifiedRel[1])
          || "type".equals(qualifiedRel[1])
          || "carried".equals(qualifiedRel[1])
          || "point".equals(qualifiedRel[1])) {
        Set<Item> set = (Set<Item>) items.stream()
            .filter(i -> values.contains(i.get(qualifiedRel[1])))
            .collect(Collectors.toSet());
        return new ItemSet(set);
      }
    }
    throw new RuntimeException("getting property " + rel + " is not supported.");
  }

  @Override
  public Set<Object> get(String rel, Set<Block> subset) {
    String[] qualifiedRel = rel.split("\\?");
    if (qualifiedRel.length < 2)
      throw new RuntimeException("'Rel' must be qualified with items?rel or walls?rel");
    return subset.stream().map(i -> i.get(qualifiedRel[1])).collect(Collectors.toSet());
  }

  public Set<? extends Object> getSpecialSet(String name) {
    if ("world".equals(name)) {
      return getOpenPoints();
    } else if ("items".equals(name)) {
      return allItems();
    } else if (rooms.containsKey(name)) {
      return rooms.get(name);
    }
    return null;
  }
  
  public ItemSet allItems() {
    return new ItemSet((Set<Item>) items);
  }
  
 @Override
  public Set<? extends Object> universalSet(Object o) {
    if (o instanceof Item) {
      return this.items;
    } else if (o instanceof Wall) {
      return this.walls;
    }  else if (o instanceof Point) {
      return this.getOpenPoints();
    }
    return new HashSet<>();
  }

  public ItemSet setLocationFilter(Point p, Set<Item> s) {
    ItemSet is;
    if (s instanceof ItemSet)
      is = (ItemSet) s;
    else
      is = new ItemSet(s);
    return setLocationFilter(new HashSet<>(Arrays.asList(p)), is);
  }
  
  public ItemSet setLocationFilter(Set<Point> filter, Set<Item> s) {
    ItemSet is;
    if (s instanceof ItemSet)
      is = (ItemSet) s;
    else
      is = new ItemSet(s);
    is.locFilter = Optional.of(filter);
    return is;
  }
  
  public ItemSet setLimit(int limit, Set<Item> s) {
    ItemSet is;
    if (s instanceof ItemSet)
      is = (ItemSet) s;
    else
      is = new ItemSet(s);

    if (limit < 0)
      is.limit = Optional.empty();
    else
      is.limit = Optional.of(limit);
    return is;
  }

  public Set<Point> filterArea(Set<Point> area, Set<Item> itemSet) {
    Set<Point> itemArea = itemSet.stream()
        .filter(i -> ! i.isCarried())
        .map(i -> i.point).collect(Collectors.toSet());
    return Sets.intersection(area, itemArea);
  }
  
  public Set<Set<Point>> filterCollection(Set<Set<Point>> collection, Set<Item> itemSet) {
    Set<Point> itemArea = itemSet.stream()
        .filter(i -> ! i.isCarried())
        .map(i -> i.point).collect(Collectors.toSet());
    return collection.stream()
        .filter(a -> ! Sets.intersection(a, itemArea).isEmpty()).collect(Collectors.toSet());
  }
  
  
  public void setIsCarried(int isCarried, ItemSet is) {
    if (isCarried < 0)
      is.isCarried = Optional.empty();
    else if (isCarried == 0)
      is.isCarried = Optional.of(false);
    else
      is.isCarried = Optional.of(true);
  }

  public void setIsCarried(boolean isCarried, ItemSet is) {
    setIsCarried(isCarried ? 1 : 0, is);
  }
  
  public Point getRobotLocation() {
    return new Point(robot.point.x, robot.point.y);
  }
  
  @ActionMethod
  public boolean noop() {
    return true;
  }
  
  /** All action methods return whether the action was successfully completed or not
   */
  @ActionMethod
  public boolean visit(Point p, Set<Point> avoidSet) {
    return gotoPoint(p, avoidSet);
  }
  
  @ActionMethod
  public boolean visit(Point p) {
    return gotoPoint(p, new HashSet<>());
  }
  
  @ActionMethod
  public boolean visit() {
    if (selectedPoint.isPresent())
      return gotoPoint(selectedPoint.get(), new HashSet<>());
    else
      throw new RuntimeException("No point has been selected to visit.");
  }
  
  private boolean gotoPoint(Point point, Set<Point> avoidSet) {
    avoidSet.addAll(walls.stream().map(w -> w.point).collect(Collectors.toList()));
    List<RoboAction> path = PathFinder.findPath(
            avoidSet,
            new Point(robot.point.x, robot.point.y),
            new Point(point),
            this.getLowCorner(),
            this.getHighCorner())
        .stream().map(p -> new RoboAction(p, RoboAction.Action.PATH))
        .collect(Collectors.toList());
    if (path.size() > 0) {
      RoboAction last = path.get(path.size() - 1);
      last.action = RoboAction.Action.DESTINATION;
      robot.point = last.point;
      if (robot.point.equals(point)) {
        pathActions.addAll(path);
        return true;
      } else {
        return false;
      }
    } else 
      return false;
  }
  
  @ActionMethod
  public boolean pick(ItemSet is) {
    is.isCarried = Optional.of(false);
    is.locFilter = Optional.of(new HashSet<>(Arrays.asList(robot.point)));
    Set<Item> restricted = is.eval();
    if (restricted.isEmpty()) {
      pathActions.add(new RoboAction(robot.point, RoboAction.Action.PICKITEM, null, false));
      return false;
    }
    Item item;
    // Since items are passed by reference, the items in the restricted set reference the same
    // items that are stored in the world.
    for (Iterator<Item> iter = restricted.iterator(); iter.hasNext(); ) {
      item = iter.next();
      item.setCarried(true);
      pathActions.add(new RoboAction(robot.point, RoboAction.Action.PICKITEM, item.color, true));
    }
    keyConsistency();
    return true;
  }
  
  @ActionMethod
  public boolean drop(ItemSet is) {
    is.isCarried = Optional.of(true);
    is.locFilter = Optional.empty();
    Set<Item> restricted = is.eval();
    if (restricted.isEmpty()) {
      pathActions.add(new RoboAction(robot.point, RoboAction.Action.DROPITEM, null, false));
      return false;
    }
    Item item;
    for (Iterator<Item> iter = restricted.iterator(); iter.hasNext(); ) {
      item = iter.next();
      item.setCarried(false);
      pathActions.add(new RoboAction(robot.point, RoboAction.Action.DROPITEM, item.color, true));
    }
    keyConsistency();
    return true;
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
