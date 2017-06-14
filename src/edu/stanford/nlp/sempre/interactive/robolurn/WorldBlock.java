package edu.stanford.nlp.sempre.interactive.robolurn;

import java.util.List;
import java.util.Arrays;

import org.testng.collections.Lists;

import fig.basic.LogInfo;

import edu.stanford.nlp.sempre.Json;

public class WorldBlock {

  public enum Type {
    WALL, ITEM;

    public static Type fromString(String s) {
      if (s.toLowerCase().equals("wall"))
        return WALL;
      else if (s.toLowerCase().equals("item"))
        return ITEM;
      else
        return WALL;
    }
  }

  int x, y;
  Type type;
  String color;

  public WorldBlock(int x, int y, Type type, String color) {
    this(x, y, type);
    this.color = color;
  }

  public WorldBlock(int x, int y, Type type) {
    this();
    this.x = x;
    this.y = y;
    this.type = type;
  }

  private WorldBlock() { }

  @SuppressWarnings("unchecked")
  public static WorldBlock fromJSON(String json) {
    List<Object> props = Json.readValueHard(json, List.class);
    return fromJSONObject(props);
  }

  public Object get(String property) {
    Object propval;
    if (property.equals("x"))
      propval = this.x;
    else if (property.equals("y"))
      propval = this.y;
    else if (property.equals("type"))
      propval = this.type;
    else
      throw new RuntimeException("getting property " + property + " is not supported.");
    return propval;
  }
  @SuppressWarnings("unchecked")
  public static WorldBlock fromJSONObject(List<Object> props) {
    WorldBlock wb = new WorldBlock();
    wb.x = ((Integer) props.get(0));
    wb.y = ((Integer) props.get(1));
    wb.type = Type.fromString((props.get(2).toString()));
    if (props.get(3) == null)
      wb.color = null;
    else
      wb.color = props.get(3).toString();
    return wb;
  }

  public Object toJSON() {
    List<Object> cube = Lists.newArrayList(x, y, type);
    return cube;
  }

  @Override
  public WorldBlock clone() {
    WorldBlock c = new WorldBlock(this.x, this.y, this.type);
    return c;
  }

  @Override
  public int hashCode() {
    final int prime = 19;
    int result = 1;
    result = prime * result + x;
    result = prime * result + y;
    result = prime * result + type.hashCode();

    return result;
  }

  @Override
  public boolean equals(Object obj) {
    // Multiple items can share the same location, type, color, etc.
    return this == obj;
  }

  @Override
  public String toString() {
    return this.toJSON().toString();
  }
}
