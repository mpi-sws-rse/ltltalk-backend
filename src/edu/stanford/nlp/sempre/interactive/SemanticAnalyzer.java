package edu.stanford.nlp.sempre.interactive;

import java.util.HashSet;
import java.util.Set;

import edu.stanford.nlp.sempre.Derivation;

/**
 * Detects if derivation contains a loop variables used out of scope.
 * @author brendonboldt
 *
 */
public class SemanticAnalyzer {

  public static boolean checkVariables(Derivation d) {
    return helper(d, new HashSet<>());
  }
  
  // TODO probably bug here; undefine 
  private static boolean helper(Derivation d, Set<String> variables) {
    String define = d.rule.getInfoTag("defines");
    String require =  d.rule.getInfoTag("requires");

    // Set to true if a loop variable is being redefined
    boolean reDef = false;
    if (!define.isEmpty()) {
      if (variables.contains(define))
        reDef = true;
      else
        variables.add(define);
    }
    
    if (!require.isEmpty() && !variables.contains(require))
      return false;
      
    boolean result = true;
    for (Derivation child : d.children)
      result &= helper(child, variables);
    
    if (!reDef && !define.isEmpty())
      variables.remove(define);
    
    return result;
  }
  
}
