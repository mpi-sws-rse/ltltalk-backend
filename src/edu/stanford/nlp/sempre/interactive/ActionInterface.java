package edu.stanford.nlp.sempre.interactive;

public abstract class ActionInterface {

  public abstract void handleActionResult(World<?> world, String actionName, Object result);
  
}
