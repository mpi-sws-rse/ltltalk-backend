package edu.stanford.nlp.sempre;

import java.util.HashSet;
import java.util.Set;

/**
 * A proper unit type (a la Scala)
 * @author brendonboldt
 *
 */
public final class Unit {
  
  private static Unit instance = new Unit();
  
  public static Unit get() { return instance; }
  
  public static Set<Unit>  trueSet() {
    Set<Unit> s = new HashSet<>();
    s.add(instance);
    return s;
  }
  
  public static TypedEmptySet falseSet() {
    return new TypedEmptySet(Unit.class);
  }
  
  private Unit() { }
}
