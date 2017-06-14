package edu.stanford.nlp.sempre.interactive;

import java.util.HashSet;
import java.util.Set;

import edu.stanford.nlp.sempre.ContextValue;
import edu.stanford.nlp.sempre.interactive.robolurn.RoboWorld;
import edu.stanford.nlp.sempre.interactive.robolurn.WorldBlock;

import fig.basic.LogInfo;

public abstract class World {
  // supports variables, and perhaps scoping
  public Set<WorldBlock> allBlocks;

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

  public abstract Set<WorldBlock> has(String rel, Set<Object> values);

  public abstract Set<Object> get(String rel, Set<WorldBlock> subset);

  //public abstract void update(String rel, Object value, Set<WorldBlock> selected);

  public World() {
    this.allBlocks = new HashSet<>();
  }

  public Set<WorldBlock> all() {
    return allBlocks;
  }

  public Set<WorldBlock> empty() {
    return new HashSet<>();
  }

}
