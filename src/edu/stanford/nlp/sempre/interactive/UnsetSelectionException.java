package edu.stanford.nlp.sempre.interactive;

@SuppressWarnings("serial")
public class UnsetSelectionException extends Exception {

  public static enum Type {
    AREA, POINT;
  }
  
  private Type type;
  
  public UnsetSelectionException(Type type) {
    this.type = type;
  }

  public UnsetSelectionException() {
    
  }
  
  public Type getType() {
    return type;
  }
}
