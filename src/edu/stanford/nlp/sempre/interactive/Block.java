package edu.stanford.nlp.sempre.interactive;

import java.awt.Point;

public abstract class Block<T> extends Point {
  
  public T type;
  
  public abstract String toString();

  public abstract Object get(String property);

}
