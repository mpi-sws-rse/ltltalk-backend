package edu.stanford.nlp.sempre.interactive;

import java.awt.Point;

public class VariablePoint extends Point {

  private static final long serialVersionUID = 1L;

  public String name;
  
  public VariablePoint(int x, int y, String name) {
    super(x,y);
    this.name = name;
  }
}
