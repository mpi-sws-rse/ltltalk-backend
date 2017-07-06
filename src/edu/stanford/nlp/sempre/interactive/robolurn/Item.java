package edu.stanford.nlp.sempre.interactive.robolurn;

import java.awt.Point;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.testng.collections.Lists;

public class Item extends RoboBlock {

  public final String color;
  private boolean carried;
  
  public static Item fromJSONObject(List<Object> props) {
    Item i = new Item(
        new Point((Integer) props.get(0), (Integer) props.get(1)),
        props.get(3).toString(),
        false
    );
    return i;
  }
  
  public Item(Point point, String color, boolean carried) {
    if (point == null)
      this.point = new Point(0, 0);
    else
      this.point = point;
    this.color = color;
    this.carried = carried;
  }
  
  @Override
  public Object get(String property) {
    Object propval;
    if (property.equals("color"))
      propval = this.color.toString();
    else if (property.equals("carried"))
      propval = this.carried;
    else if (property.equals("point"))
      propval = this.point;
    else
      throw new RuntimeException("getting property " + property + " is not supported.");
    return propval;
  }

  public Object toJSON() {
    List<Object> cube = Lists.newArrayList(point.x, point.y, color, carried);
    return cube;
  }

  public boolean isCarried() {
    return carried;
  }
  
  public void setCarried(boolean state) {
    if (state == false)
      point = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
    carried = state;
  }
  
  public boolean isIn(Set<Item> s) {
    Item item;
    for (Iterator<Item> iter = s.iterator(); iter.hasNext(); ) {
      item = iter.next();
      if (item.point.equals(this.point)
          && item.color == this.color)
        return true;
    }
    return false;
  }
  
  @Override
  public RoboBlock clone() {
    RoboBlock c = new Item(new Point(point.x, point.y), color, carried);
    return c;
  }

  /*
  @Override
  public int hashCode() {
    final int prime = 19;
    int result = 1;
    result = prime * result + point.x;
    result = prime * result + point.y;
    result = prime * result + color.toString().hashCode();

    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Item other = (Item) obj;

    if (!point.equals(other.point))
      return false;
    if (color != other.color)
      return false;

    return true;
  }
  */

  @Override
  public String toString() {
    return this.toJSON().toString();
  }
}
