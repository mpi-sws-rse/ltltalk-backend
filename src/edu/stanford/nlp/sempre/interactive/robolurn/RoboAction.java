package edu.stanford.nlp.sempre.interactive.robolurn;

import java.awt.Point;
import java.util.List;

import org.testng.collections.Lists;

import edu.stanford.nlp.sempre.Json;
import edu.stanford.nlp.sempre.interactive.PathAction;

public class RoboAction extends PathAction<RoboAction.Action> {

  public enum Action {
    PICKITEM, DROPITEM, PATH, DESTINATION;

    public static Action fromString(String s) {
      if (s.toLowerCase().equals("path"))
        return PATH;
      else if (s.toLowerCase().equals("pickitem"))
        return PICKITEM;
      else if (s.toLowerCase().equals("dropitem"))
        return DROPITEM;
      else if (s.toLowerCase().equals("destination"))
        return DESTINATION;
      else
        return PATH;
    }

    @Override
    public String toString() {
      switch(this) {
        case PATH:        return "\"path\"";
        case PICKITEM:    return "\"pickitem\"";
        case DROPITEM:    return "\"dropitem\"";
        case DESTINATION: return "\"destination\"";
        default:          return "\"ERROR\"";
      }
    }
  }
  
  public String spec;

  public RoboAction(Point point, Action action, String spec, boolean possible) {
    this(point, action);
    this.spec = spec;
    this.possible = possible;
  }

  public RoboAction(Point point, Action action) {
    this();
    this.point = point;
    this.action = action;
    this.possible = true;
  }

  public RoboAction() { }


  @Override
  public Object get(String property) {
    Object propval;
    if (property.equals("field"))
      propval = this.point;
    else if (property.equals("action"))
      propval = this.action;
    else if (property.equals("spec"))
      propval = this.spec;
    else if (property.equals("possible"))
      propval = this.possible;
    else
      throw new RuntimeException("getting property " + property + " is not supported.");
    return propval;
  }

  @SuppressWarnings("unchecked")
  public static RoboAction fromJSON(String json) {
    List<Object> props = Json.readValueHard(json, List.class);
    return fromJSONObject(props);
  }

  public static RoboAction fromJSONObject(List<Object> props) {
    RoboAction act = new RoboAction();
    act.point = new Point((Integer) props.get(0), (Integer) props.get(1));
    act.action = Action.fromString(((String) props.get(2)));
    if (props.get(3) == null)
      act.spec = null;
    else
//      act.spec = Color.BasicColor.fromString((String) props.get(3));
      act.spec = props.get(3).toString();
    return act;
  }

  @Override
  public Object toJSON() {	
    @SuppressWarnings("unchecked")
    List<? extends Object> cube =
        Lists.newArrayList(point.x, point.y, action.toString(), "\"" + spec + "\"", possible);
    return cube;
  }

  @Override
  public RoboAction clone() {
    RoboAction c = new RoboAction(new Point(point.x, point.y), this.action, this.spec, this.possible);
    return c;
  }

  /*
  public int hashCode() {
    final int prime = 19;
    int result = 1;
    result = prime * result + point.x;
    result = prime * result + point.y;
    result = prime * result + action.hashCode();

    return result;
  }

  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RoboAction other = (RoboAction) obj;

    if (x != other.x)
      return false;
    if (y != other.y)
      return false;
    if (action != other.action)
      return false;
    if (!spec.equals(other.spec))
      return false;

    return true;
  }
   */

  @Override
  public String toString() {
    return this.toJSON().toString();
  }

}
