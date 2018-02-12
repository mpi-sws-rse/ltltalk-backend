package edu.stanford.nlp.sempre;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Used to access rules efficiently by walking down their RHS.
 * Stores all rules we are currently using
 * @author Percy Liang
 */
public class Trie {
  public ArrayList<Rule> rules = new ArrayList<>();
  
  // list of rules where String appears on the RHS
  public Map<String, Trie> children = new LinkedHashMap<>();
  // Set of LHS categories of all rules in this subtree
  public Set<String> cats = new LinkedHashSet<>();

  public Trie next(String item) { return children.get(item); }

  public void add(Rule rule) { add(rule, 0); }
  private void add(Rule rule, int i) {
    cats.add(rule.lhs);

    if (i == rule.rhs.size()) {
      if (!rules.contains(rule)) // filter exact match
        rules.add(rule);
      return;
    }

    String item = rule.rhs.get(i);
    Trie child = children.get(item);
    if (child == null)
      children.put(item, child = new Trie());
    child.add(rule, i + 1);
  }
  
  public String toString(){
	  String s = "rules: "+rules.toString()+"\n";
	  for (Map.Entry<String, Trie> entry : children.entrySet()){
		  s = s + "mapKey = "+entry.getKey();
		  s = s + "\t "+entry.getValue().toString()+"\n";
	  }
	  return s;
  }
}
