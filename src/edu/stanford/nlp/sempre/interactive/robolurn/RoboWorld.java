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
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import edu.stanford.nlp.sempre.ContextValue;
import edu.stanford.nlp.sempre.Json;
import edu.stanford.nlp.sempre.NaiveKnowledgeGraph;
import edu.stanford.nlp.sempre.StringValue;
import edu.stanford.nlp.sempre.Unit;
import edu.stanford.nlp.sempre.interactive.ActionInterface;
import edu.stanford.nlp.sempre.interactive.Block;
import edu.stanford.nlp.sempre.interactive.World;
import edu.stanford.nlp.sempre.interactive.planner.PathFinder;
import fig.basic.Option;
import fig.basic.LogInfo;

public class RoboWorld extends World {
  public static class Options {
    @Option(gloss = "maximum number of cubes to convert")
    public int maxBlocks = 1024 ^ 2;
  }
  
  private ActionMethods actionMethods =  ActionMethods.getInstance();
  
  public ActionInterface getActionInterface() { return actionMethods; }
  
  // Eventually make a whole object for this
  // If this is left unset, everything was realizable
  public String unrealizableStatus = "";
  
  protected Point lowCorner;
  protected Point highCorner;
  
  protected Map<String, Set<Point>> rooms;
  // Store functions in a map so they do not need to be accessed via reflection.
  protected Map<String, Function<ItemSet, Boolean>> itemActions;
  
  public static Options opts = new Options();

  public static RoboWorld fromContext(ContextValue context) {
    if (context == null || context.graph == null) {
      return null;
    }
    NaiveKnowledgeGraph graph = (NaiveKnowledgeGraph) context.graph;
    String wallString = ((StringValue) graph.triples.get(0).e1).value;
    return fromJSON(wallString);
  }

  // The list of actions that will be sent as a response to the browser client
  protected List<PathElement> pathActions;
  protected Robot robot;
  
  public RoboWorld(Robot robot, Set<Wall> walls, Set<Item> items) {
    this(robot, walls, items, new HashMap<>());
  }
  
  public RoboWorld(Robot robot, Set<Wall> walls, Set<Item> items, Map<String, Set<Point>> rooms) {
    super();
    this.robot = robot;
    this.walls = walls;
    this.items = items;
    this.rooms = rooms;
    this.pathActions = new ArrayList<PathElement>();
    this.findCorners();
    this.selectedArea = Optional.empty();
    this.selectedPoint = Optional.empty();
    
    itemActions = new HashMap<>();
    itemActions.put("pick", (x) -> pick(x));
    itemActions.put("drop", (x) -> drop(x));
  }

  /**
   * Create a deep copy of the world
   * This is necessary for the `strict` and `possible` constructs.
   */
  public RoboWorld clone() {
    Set<Wall> newWalls = walls.stream().map(w -> (Wall) w.clone()).collect(Collectors.toSet());
    Set<Item> newItems = items.stream().map(i -> (Item) i.clone()).collect(Collectors.toSet());
    RoboWorld newWorld = new RoboWorld(
         robot.clone(),
         newWalls,
         newItems,
         new HashMap<>(rooms)
    );
    newWorld.selectedArea = this.selectedArea;
    newWorld.selectedPoint = this.selectedPoint;
    return newWorld;
  }

  /**
   * Calculate the dimensions of the map from the context
   */
  protected void findCorners() {
    int maxX = 0, maxY = 0, minX = 0, minY = 0;
    for (Block w : walls) {
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
  
  /*
   * This is not curently needed because the world only has to return the
   * robot's path (i.e., the delta to the next state of the wolrd).
   */
  @Override
  public String toJSON() {
    return "NOT YET SUPPORTED";
  }

  @Override
  public String getJSONResponse() {
    StringBuilder sb = new StringBuilder();
    sb.append("{\"status\":\"");
    sb.append(this.unrealizableStatus);
    sb.append("\",\"path\":[");
    boolean first = true;
    for (PathElement pa : pathActions) {
      if (first)
        first = false;
      else
        sb.append(",");
      sb.append(pa.toJSON());
    }
    sb.append("]}");
    return sb.toString();
  }

  @SuppressWarnings("unchecked")
  protected static RoboWorld fromJSON(String jsonString) {
    Map<String, Object> ctxMap = Json.readValueHard(jsonString, Map.class);
    List<Object> rawRobot = (List<Object>) ctxMap.get("robot");
    
    Point robotPoint = new Point((int) rawRobot.get(0), (int) rawRobot.get(1));
    // Items held by the robot are listed as part of the robot state in the JSON
    Set<Item> robotItems = ((Collection<String>) rawRobot.get(2)).stream()
        .map(c -> new Item(null, c, true))
        .collect(Collectors.toSet());
    
    Robot robot = new Robot(robotPoint);
    Set<Wall> walls = new HashSet<>();
    Set<Item> items = robotItems;
    ((List<Object>) ctxMap.get("world")).stream().forEach(c -> {
      RoboBlock rb = RoboBlock.fromJSONObject((List<Object>) c);
      if (rb instanceof Item)
        items.add((Item) rb);
      else if (rb instanceof Wall)
        walls.add((Wall) rb);
    });
    RoboWorld world = new RoboWorld(robot, walls, items);
    
    // Load the room definitions as specified by the client
    Set<Point> points;
    for (Entry<String, List<List<Integer>>> entry
        : ((Map<String, List<List<Integer>>>) ctxMap.get("rooms")).entrySet()) {
      points = entry.getValue().stream()
          .map(p -> new Point(p.get(0), p.get(1))).collect(Collectors.toSet());
      world.rooms.put(entry.getKey(), points);
    }
    
    return world;
  }

  /**
   * Return the set of objects filtered by the specified `rel` and `values`
   */
  @Override
  public Set<? extends Block> has(String rel, Set<Object> values) {
	  
    String[] qualifiedRel = rel.split("\\?");
    if (qualifiedRel.length < 2)
      throw new RuntimeException(rel + " must be qualified with items?rel or walls?rel");

    if ("items".equals(qualifiedRel[0])) {
      if ("color".equals(qualifiedRel[1])
          || "type".equals(qualifiedRel[1])
          || "carried".equals(qualifiedRel[1])
          || "point".equals(qualifiedRel[1])) {
        @SuppressWarnings("unchecked")
        Set<Item> set = (Set<Item>) items.stream()
            .filter(i -> values.contains(i.get(qualifiedRel[1])))
            .collect(Collectors.toSet());
        return new ItemSet(set);
      }
    }
    throw new RuntimeException("getting property " + rel + " is not supported.");
  }

  /**
   * Return values of `rel` present in `subset`
   */
  @Override
  public Set<Object> get(String rel, Set<Block> subset) {
    String[] qualifiedRel = rel.split("\\?");
    if (qualifiedRel.length < 2)
      throw new RuntimeException("'rel' must be qualified with items?rel or walls?rel");
    return subset.stream().map(i -> i.get(qualifiedRel[1])).collect(Collectors.toSet());
  }

  public Set<? extends Object> getSpecialSet(String name) {
    if ("world".equals(name)) {
      return getOpenPoints();
    } else if ("all_rooms".equals(name)) {
      return new HashSet<>(rooms.values());
    } else if ("items".equals(name)) {
      return allItems();
    } else if (rooms.containsKey(name)) {
      return rooms.get(name);
    }
    return null;
  }

  @Override
  public Set<? extends Object> universalSet(Class<?> clazz) {
    if (clazz == Item.class)
      return this.items;
    else if (clazz == Wall.class)
      return this.walls;
    else if (clazz == Point.class)
      return this.getOpenPoints();
    else  if (clazz == Unit.class)
      return Sets.newHashSet(Unit.get());
    else 
      return Sets.newHashSet();
  }

  @SuppressWarnings("unchecked")
  public ItemSet allItems() {
    return new ItemSet((Set<Item>) items);
  }


  public Set<?> itemAt(Set<Item> s, Set<Point> points) {
    if (s.stream().anyMatch(i -> points.contains(i.point) && !i.isCarried()))
      return Unit.trueSet();
    else
      return Unit.falseSet();
  }
  
  public Set<?> robotHas(Set<Item> s) {
    if (s.stream().anyMatch(i -> i.isCarried()))
      return Unit.trueSet();
    else
      return Unit.falseSet();
  }
  
  public Set<?> robotAt(Set<Point> points) {
    if (points.contains(robot.point))
      return Unit.trueSet();
    else
      return Unit.falseSet();
  }
  
  public ItemSet setLocationFilter(Point p, Set<Item> s) {
    ItemSet is;
    if (s instanceof ItemSet)
      is = (ItemSet) s;
    else
      is = new ItemSet(s);
    return setLocationFilter(new HashSet<>(Arrays.asList(p)), is);
  }
  
  /**
   * Specify a filter that will be applied when the item set is evaluated
   */
  public ItemSet setLocationFilter(Set<Point> filter, Set<Item> s) {
    ItemSet is;
    if (s instanceof ItemSet)
      is = (ItemSet) s;
    else
      is = new ItemSet(s);
    is.locFilter = Optional.of(filter);
    return is;
  }
  
  /**
   * 
   * Limit the number of items a set will contain when evaluated 
   */
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

  /* returns all points specified by `itemSet` that are also in the `area` */
  public Set<Point> filterArea(Set<Point> area, Set<Item> itemSet) {
	  
//    Set<Point> itemArea = itemSet.stream()
//        .filter(i -> ! i.isCarried())
//        .map(i -> i.point).collect(Collectors.toSet());
	Set<Point> itemArea = pointsFromItems(itemSet);
    return Sets.intersection(area, itemArea);
  }
  
  /* returns all points containing items specified by itemSet */
  public Set<Point> pointsFromItems(Set<Item> itemSet){
	  
	  Set<Point> itemArea = itemSet.stream()
			  .filter(i -> ! i.isCarried())
			  .map(i -> i.point)
			  .collect(Collectors.toSet());
	  return itemArea;
  }

  public Set<Point> roomContainingItem(Item i){
	  for (Map.Entry<String, Set<Point>> entry : this.rooms.entrySet()) {
		  if (entry.getValue().contains(i.point)){
			  return entry.getValue();
		  }
	  }
	  return null;
			  
  }
  
  /* returns all rooms containing items specified by itemSet */
  public Set<Set<Point>> roomsFromItems(Set<Item> itemSet){
	  
	  Set<Set<Point>> roomsWithItems = itemSet.stream()
			  .filter(i -> ! i.isCarried())
			  .map(i -> roomContainingItem(i))
			  .collect(Collectors.toSet());
	  
	  return roomsWithItems;
  }
  

 
  
  public Set<Set<Point>> areasFromItems(Set<Set<Point>> areaSet, Set<Item> itemSet){
		    Set<Point> itemArea = itemSet.stream()
		        .filter(i -> ! i.isCarried())
		        .map(i -> i.point).collect(Collectors.toSet());
		    Set<Set<Point>> retAreas = areaSet.stream()
		        .filter(a -> ! Sets.intersection(a, itemArea).isEmpty()).collect(Collectors.toSet());
		    return retAreas;
  }

  
  public Set<Set<Point>> roomsComplement(Set<Set<Point>> roomToComplement){
	  Set<Set<Point>> complement = this.rooms.values().stream()
			  .filter(r -> !roomToComplement.contains(r))
			  .collect(Collectors.toSet());
	
	return complement;
  }
  
  public Set<Set<Point>> areasDifference(Set<Set<Point>> a1, Set<Set<Point>> a2){
	  Set<Set<Point>> difference = a1.stream()
			  .filter(r -> !a2.contains(r))
			  .collect(Collectors.toSet());
	
	return difference;
  }
  
  public Set<?> setOfLocationsDifference(Set<Object> a1, Set<Object> a2){
	  LogInfo.logs("a1 = %s, a2 = %s", a1, a2);
	  Set<?> difference = a1.stream()
			  .filter(r -> !a2.contains(r))
			  .collect(Collectors.toSet());
	  //LogInfo.logs("returning difference %s", difference);
	  return difference;
	
  }

  
  /**
   * Specify how to filter items based on their being carried by the robot
   */
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
  
  protected boolean gotoPoint(Point point, Set<Point> avoidSet) {
    avoidSet.addAll(walls.stream().map(w -> w.point).collect(Collectors.toList()));
    HashSet<Point> goalPoints = new HashSet<Point>();
    goalPoints.add(point);
    List<PathElement> path = PathFinder.findPath(
            avoidSet,
            new Point(robot.point.x, robot.point.y),
            goalPoints,
            this.getLowCorner(),
            this.getHighCorner())
        .stream().map(p -> new PathElement(p, PathElement.Action.PATH))
        .collect(Collectors.toList());
    if (pathActions.size() > 0 && path.size() > 1) {
      path.remove(0);
    }
    
    if (path.size() > 0) {
      // Remove the last point to avoid the destination of the previous action
      // being duplicated in the list of path actions
      PathElement last = path.get(path.size() - 1);
      last.action = PathElement.Action.DESTINATION;
      robot.point = last.point;
      
      // The action is successful iff the robot is now at the specified point
      if (robot.point.equals(point)) {
        pathActions.addAll(path);
        return true;
      } else {
        return false;
      }
    } else 
      return false;
  }
  
  protected boolean pick(ItemSet is) {
    is.isCarried = Optional.of(false);
    is.locFilter = Optional.of(new HashSet<>(Arrays.asList(robot.point)));
    Set<Item> restricted = is.eval();
    if (restricted.isEmpty()) {
      pathActions.add(new PathElement(robot.point, PathElement.Action.PICKITEM, null, false));
      return false;
    }
    Item item;
    // Since items are passed by reference, the items in the restricted set reference the same
    // items that are stored in the world.
    for (Iterator<Item> iter = restricted.iterator(); iter.hasNext(); ) {
      item = iter.next();
      item.setCarried(true);
      pathActions.add(new PathElement(robot.point, PathElement.Action.PICKITEM, item.color, true));
    }
    keyConsistency();
    return true;
  }
  
  protected boolean gotoSetOfAreas(Set<Set<Point>> setOfAreas, Set<Point> avoidSet){
	  // I don't want to make a decision about which set of Areas to visit up until this point
	  for (Set<Point> area : setOfAreas){
		  if (gotoSetOfPoints(area, avoidSet) == true){
			  return true;
		  } 
	  }
	  return false;
  }
  protected boolean gotoSetOfPoints(Set<Point> goalSet, Set<Point> avoidSet) {
	    avoidSet.addAll(walls.stream().map(w -> w.point).collect(Collectors.toList()));
	    List<PathElement> path = PathFinder.findPath(
	            avoidSet,
	            new Point(robot.point.x, robot.point.y),
	            new HashSet<Point>(goalSet),
	            this.getLowCorner(),
	            this.getHighCorner())
	        .stream().map(p -> new PathElement(p, PathElement.Action.PATH))
	        .collect(Collectors.toList());
	    if (pathActions.size() > 0 && path.size() > 1) {
	      path.remove(0);
	    }
	    
	    if (path.size() > 0) {
	      // Remove the last point to avoid the destination of the previous action
	      // being duplicated in the list of path actions
	      PathElement last = path.get(path.size() - 1);
	      last.action = PathElement.Action.DESTINATION;
	      robot.point = last.point;
	      
	      // The action is successful iff the robot is now at the specified point
	      if (goalSet.contains(robot.point)) {
	        pathActions.addAll(path);
	        return true;
	      } else {
	        return false;
	      }
	    } else 
	      return false;
	  }
	  
	  
  
  protected boolean drop(ItemSet is) {
    is.isCarried = Optional.of(true);
    is.locFilter = Optional.empty();
    Set<Item> restricted = is.eval();
    if (restricted.isEmpty()) {
      pathActions.add(new PathElement(robot.point, PathElement.Action.DROPITEM, null, false));
      return false;
    }
    Item item;
    for (Iterator<Item> iter = restricted.iterator(); iter.hasNext(); ) {
      item = iter.next();
      item.setCarried(false);
      item.point = robot.point;
      pathActions.add(new PathElement(robot.point, PathElement.Action.DROPITEM, item.color, true));
    }
    keyConsistency();
    return true;
  }

  protected <T> void refreshSet(Set<T> set) {
    List<T> s = new ArrayList<>(set);
    set.clear();
    set.addAll(s);
  }

  /**
   * If the item states have changed, the hash set must be updated
   */
  protected void keyConsistency() {
    refreshSet(walls);
    refreshSet(items);
    if (open != null)
      refreshSet(open);
  }
}
