package edu.stanford.nlp.sempre.interactive.robolurn;

import java.util.List;
import java.util.ArrayList;

import fig.basic.LogInfo;

public class Robot {
  
  public int x, y;
  public List<String> items;

  public Robot(int x, int y) {
    this.x = x;
    this.y = y;
    this.items = new ArrayList<>();
  }

  public Robot(int x, int y, List<String> items) {
    this(x,y);
    this.items = items;
  }
}
