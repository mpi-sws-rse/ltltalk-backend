package edu.stanford.nlp.sempre.interactive.robolurn;

import java.util.List;

import edu.stanford.nlp.sempre.Json;
import edu.stanford.nlp.sempre.interactive.Block;

public abstract class RoboBlock extends Block {

  static final String WALL_TYPE = "wall";
  static final String ITEM_TYPE = "item";
  
  @SuppressWarnings("unchecked")
  public static RoboBlock fromJSON(String json) {
    List<Object> props = Json.readValueHard(json, List.class);
    return fromJSONObject(props);
  }

//  @Override
//  public Object get(String property) {
//    Object propval;
//    if (property.equals("x"))
//      propval = this.x;
//    else if (property.equals("y"))
//      propval = this.y;
//    else if (property.equals("color"))
//      propval = this.color;
//    else if (property.equals("type"))
//      propval = this.type;
//    else if (property.equals("field"))
//      propval = new Point(this.x, this.y);
//    else
//      throw new RuntimeException("getting property " + property + " is not supported.");
//    return propval;
//  }

  public static RoboBlock fromJSONObject(List<Object> props) {
    if (WALL_TYPE.equals(props.get(2))) {
      return Wall.fromJSONObject(props);
    } else {
      return Item.fromJSONObject(props);
    }
  }
  
  public abstract Object toJSON();

//  public Object toJSON() {
//    List<Object> cube = Lists.newArrayList(x, y, type);
//    return cube;
//  }
//
//  @Override
//  public RoboBlock clone() {
//    RoboBlock c = new RoboBlock(this.x, this.y, this.type);
//    return c;
//  }
//
//  @Override
//  public int hashCode() {
//    final int prime = 19;
//    int result = 1;
//    result = prime * result + x;
//    result = prime * result + y;
//    result = prime * result + type.hashCode();
//
//    return result;
//  }
//
//  /**
//   * Two different cubes can be "equal" since multiple cubes of the same type
//   * can be in the same location.
//   */
//  @Override
//  public boolean equals(Object obj) {
//    if (this == obj)
//      return true;
//    if (obj == null)
//      return false;
//    if (getClass() != obj.getClass())
//      return false;
//    RoboBlock other = (RoboBlock) obj;
//
//    if (x != other.x)
//      return false;
//    if (y != other.y)
//      return false;
//    if (type != other.type)
//      return false;
//    if ((color != null && !color.equals(other.color)) || other.color != null)
//      return false;
//
//    return true;
//  }
//
//  @Override
//  public String toString() {
//    return this.toJSON().toString();
//  }
}
