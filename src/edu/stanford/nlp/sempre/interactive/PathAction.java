package edu.stanford.nlp.sempre.interactive;

public abstract class PathAction {
  
  public interface Action { }
  
  public Action action;
  public int x;
  public int y;

  public abstract Object get(String property);

  //public static PathAction fromJSON(String json);

  //public static PathAction fromJSONObject(List<Object> props);

  public abstract Object toJSON();

  public abstract PathAction clone();
}
