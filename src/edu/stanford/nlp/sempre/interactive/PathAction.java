package edu.stanford.nlp.sempre.interactive;

/**
 * @author brendonboldt
 * Generic type A represents and action type
 * @param <A>
 */
public abstract class PathAction<A> {
  
  public A action;
  public int x;
  public int y;
  public boolean possible;
  
  public abstract Object get(String property);

  //public static PathAction fromJSON(String json);

  //public static PathAction fromJSONObject(List<Object> props);

  public abstract Object toJSON();

  public abstract PathAction<A> clone();
}
