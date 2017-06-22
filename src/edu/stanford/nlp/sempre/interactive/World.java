package edu.stanford.nlp.sempre.interactive;

import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.stanford.nlp.sempre.ContextValue;
import edu.stanford.nlp.sempre.interactive.robolurn.RoboWorld;

/**
 * @param <B> Represents block type
 */
public abstract class World<B extends Block> {
  
  public Set<B> walls;
  public Set<B> items;
  
  // Should this be a set or a single Point?
  public Point selectedField;

  public static World fromContext(String worldname, ContextValue context) {
    if (worldname.equals("RoboWorld"))
      return RoboWorld.fromContext(context);
    throw new RuntimeException("World does not exist: " + worldname);
  }

  // there are some annoying issues with mutable objects.
  // The current strategy is to keep allitems up to date on each mutable
  // operation
  // bboldt: ^ I wish I knew what those annoying issues were
  public abstract String toJSON();

  public abstract String getJSONPath();

  public abstract Point getHighCorner();

  public abstract Point getLowCorner();
  
  // Maybe run this at construction time (or lazy eval)
  public Set<Point> getOpenFields() {
    Point lc = this.getLowCorner();
    Point hc = this.getHighCorner();
    boolean[][] fields = new boolean[hc.y - lc.y + 1][hc.x - lc.x + 1];
    Point p; 
    for (Iterator<B> iter = walls.iterator(); iter.hasNext(); ) {
      p = iter.next();
      fields[p.y - lc.y][p.x - lc.x] = true;
    }
    Set<Point> open = new HashSet<>();
    for (int i = lc.x; i < hc.x; ++i) {
      for (int j = lc.y; j < hc.y; ++j) {
        if (!fields[j - lc.y][i - lc.x]) {
          open.add(new Point(i, j));
        }
      }
    }
    return open;
  }
  
//  public abstract Set<Block<?>> has(String rel, Set<Object> values);

//  public abstract Set<Object> get(String rel, Set<Block<?>> subset);

  //public abstract void update(String rel, Object value, Set<WorldBlock> selected);

  public World() {
    this.walls = new HashSet<>();
    this.items = new HashSet<>();
  }
}
