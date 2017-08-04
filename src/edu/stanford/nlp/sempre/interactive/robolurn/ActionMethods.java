package edu.stanford.nlp.sempre.interactive.robolurn;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import edu.stanford.nlp.sempre.interactive.ActionInterface;
import edu.stanford.nlp.sempre.interactive.World;

/**
 * Singleton class containing _actions_ which can be performed on RoboWorld
 * @author brendonboldt
 *
 */
public class ActionMethods extends ActionInterface {
  
  private static ActionMethods instance = new ActionMethods();
  
  private ActionMethods() { }
  
  public static ActionMethods getInstance() {
    return instance;
  }

  /**
   * Move relative to the current position of the robot 
   */
  public boolean move(RoboWorld world, String dir) {
    Point p = new Point(world.robot.point.x, world.robot.point.y);
    if ("up".equals(dir))
      p.y += 1;
    else if ("down".equals(dir))
      p.y -= 1;
    else if ("right".equals(dir))
      p.x += 1;
    else if ("left".equals(dir))
      p.x -= 1;
    else
      throw new RuntimeException("Unknown direction " + dir);
    return world.gotoPoint(p, new HashSet<>());
  }
  
  /**
   * All action methods return whether the action was successfully completed or not
   */
  public boolean visit(RoboWorld world, Point p, Set<Point> avoidSet) {
    return world.gotoPoint(p, avoidSet);
  }
  
  public boolean visit(RoboWorld world, Point p) {
    return world.gotoPoint(p, new HashSet<>());
  }

  public boolean itemActionHandler(RoboWorld world, String act, ItemSet is) {
    return world.itemActions.get(act).apply(is);
  }

  public boolean noop() {
    return true;
  }

  public void handleActionResult(World<?> world, String actionName, Object resultObj) {
    RoboWorld roboWorld = (RoboWorld) world;
    boolean result = (boolean) resultObj;
    if (!result) {
      // TODO : need more information here
      roboWorld.unrealizableStatus = String.format("Could not complete the action: %s", actionName);
    }
  }
  
}
