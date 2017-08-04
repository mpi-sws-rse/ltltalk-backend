package edu.stanford.nlp.sempre.interactive;

import edu.stanford.nlp.sempre.ActionFormula;

public abstract class ActionInterface {

  public abstract void handleActionResult(World world, ActionFormula actionName, Object result);
  
}
