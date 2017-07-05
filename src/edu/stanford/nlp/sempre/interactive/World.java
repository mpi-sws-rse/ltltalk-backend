package edu.stanford.nlp.sempre.interactive;

import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

  public abstract String toJSON();

  public abstract String getJSONPath();

  public abstract Point getHighCorner();

  public abstract Point getLowCorner();
  
  // Lazy eval for now. If the walls (they don't now) change, this will need to be updated.
  @SuppressWarnings("unchecked")
  public Set<Point> getOpenPoints() {
    if (this.open != null)
      return (Set<Point>) this.open;
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
  
  //public Set<Set<Point>> combineCollections(Set<Set<Point>> c1, Set<Set<Point>> c2) {
  public Set<? extends Object> combineCollections(Set<Object> c1, Set<Object> c2) {
    if (c1.isEmpty() && c2.isEmpty()) return new HashSet<>();
    if (c1.isEmpty()) return c2;
    if (c2.isEmpty()) return c1;
    Class<?> c1Type = c1.iterator().next().getClass();
    Class<?> c2Type = c1.iterator().next().getClass();
    System.out.println(c1Type);
    System.out.println(c2Type);
    if (c1Type == Set.class && c2Type == Set.class) {
      c1.addAll(c2);
      return c1;
    } else if (c1Type == Set.class) {
      c1.add(c2);
      return c1;
    } else if (c2Type == Set.class) {
      c2.add(c1);
      return c2;
    } else {
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
