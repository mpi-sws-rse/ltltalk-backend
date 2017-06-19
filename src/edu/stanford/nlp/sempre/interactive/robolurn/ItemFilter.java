package edu.stanford.nlp.sempre.interactive.robolurn;

import java.util.List;

public class ItemFilter {

  public static enum Type {
    NOT, AND, OR, HAS, NIL;
  }
  
  Type type;
  String spec;
  List<ItemFilter> children;
  


  public boolean eval(String str) {
    Boolean b;
    
    switch (type) {
      case NOT:
        return !children.get(0).eval(str);
      case AND:
        return children.get(0).eval(str) && children.get(1).eval(str);
      case OR:
        return children.get(0).eval(str) || children.get(1).eval(str);
      case HAS:
        return str.equals(spec);
      default:
        return true;
    }
  }
}
