package edu.stanford.nlp.sempre.interactive;

import java.awt.Point;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import edu.stanford.nlp.sempre.ContextValue;
import edu.stanford.nlp.sempre.interactive.robolurn.RoboWorld;

/**
 * @param <B> Represents block type
 */
public abstract class World<B extends Block> {
  
  public Set<? extends B> walls;
  public Set<? extends B> items;
  public Set<? extends Point> open;
  
//  public Map<String, Point> variables;
  
  public Optional<Set<Point>> selectedArea;
  public Optional<Point> selectedPoint;

  public static World<?> fromContext(String worldname, ContextValue context) {
    if (worldname.equals("RoboWorld"))
      return RoboWorld.fromContext(context);
    throw new RuntimeException("World does not exist: " + worldname);
  }

  public abstract World<B> clone();
  
  public abstract String toJSON();

  public abstract String getJSONPath();

  public abstract Point getHighCorner();

  public abstract Point getLowCorner();
  
  // Lazy eval for now. If the walls (they don't now) change, this will need to be updated.
  @SuppressWarnings("unchecked")
  public Set<Point> getOpenPoints() {
    if (this.open != null)
      return (Set<Point>) this.open;
    // TODO : this may not handle a non-enclosed space
    Point lc = this.getLowCorner();
    Point hc = this.getHighCorner();
    boolean[][] points = new boolean[hc.y - lc.y + 1][hc.x - lc.x + 1];
    Point p; 
    for (Iterator<? extends B> iter = walls.iterator(); iter.hasNext(); ) {
      p = iter.next().point;
      points[p.y - lc.y][p.x - lc.x] = true;
    }
    Set<Point> open = new HashSet<>();
    for (int i = lc.x; i < hc.x; ++i) {
      for (int j = lc.y; j < hc.y; ++j) {
        if (!points[j - lc.y][i - lc.x]) {
          open.add(new Point(i, j));
        }
      }
    }
    this.open = open;
    return open;
  }
  
  public abstract Set<? extends B> has(String rel, Set<Object> values);

  public abstract Set<Object> get(String rel, Set<Block> subset);

  //public abstract void update(String rel, Object value, Set<WorldBlock> selected);
  public abstract Set<? extends Object> universalSet(Object o);
  
  public Set<? extends B> allItems() {
    return items;
  }
    
  public Set<Point> getSelectedArea() {
    if (!selectedArea.isPresent())
      throw new RuntimeException("Selected area has not been set.");
    return selectedArea.get();
  }
  
  public Point getSelectedPoint() {
    if (!selectedPoint.isPresent())
      throw new RuntimeException("Selected point has not been set.");
    return selectedPoint.get();
  }
  
  public Point makePoint(int x, int y) {
    return new Point(x, y);
  }
  
  public Set<Point> makeArea() {
    return new HashSet<>();
  }
  
  public Set<Point> getAreaWithCorners(Point p1, Point p2) {
    int xHigh = Math.max(p1.x, p2.x);
    int xLow  = Math.min(p1.x, p2.x);
    int yHigh = Math.max(p1.y, p2.y);
    int yLow  = Math.min(p1.y, p2.y);
    return getOpenPoints().stream()
        .filter(p -> p.x >= xLow && p.x <= xHigh && p.y >= yLow && p.y <= yHigh)
        .collect(Collectors.toSet());
  }
  
  //public Set<Set<Point>> combineCollections(Set<Set<Point>> c1, Set<Set<Point>> c2) {
  public Set<? extends Object> combineCollections(Set<Object> c1, Set<Object> c2) {
    Object c1Elem;
    Object c2Elem;
    if (c1.isEmpty() && c2.isEmpty()) return new HashSet<>();
    if (c1.isEmpty()) {
      c2Elem = c2.iterator().next();
      if (c2Elem instanceof Set) return c2;
      else if (c2Elem instanceof Point) return new HashSet<>(Arrays.asList(c2));
      else throw new RuntimeException("Collection 2 of unknown type");
    }
    if (c2.isEmpty()) {
      c1Elem = c1.iterator().next();
      if (c1Elem instanceof Set) return c1;
      else if (c1Elem instanceof Point) return new HashSet<>(Arrays.asList(c1));
      else throw new RuntimeException("Collection 1 of unknown type");
    }
    c1Elem = c1.iterator().next();
    c2Elem = c2.iterator().next();
    if (c1Elem instanceof Set && c2Elem instanceof Set) {
      c1.addAll(c2);
      return c1;
    } else if (c1Elem instanceof Set) {
      c1.add(c2);
      return c1;
    } else if (c2Elem instanceof Set) {
      c2.add(c1);
      return c2;
    } else { // If c1 and c2 are both sets of points
      Set<Set<Object>> set = new HashSet<>();
      set.add(c1);
      set.add(c2);
      return set;
    }
  }
  
  public Point anyPoint(Set<Point> s) {
    Iterator<Point> iter = s.iterator();
    if (iter.hasNext())
      return iter.next();
    else
      return null;
  }
  
  public World() {
    this.walls = new HashSet<>();
    this.items = new HashSet<>();
  }
}
