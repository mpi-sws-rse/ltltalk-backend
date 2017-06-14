package edu.stanford.nlp.sempre.interactive;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.collections.Lists;

import edu.stanford.nlp.sempre.Json;

public abstract class PathAction {

  public abstract Object get(String property);

  //public static PathAction fromJSON(String json);

  //public static PathAction fromJSONObject(List<Object> props);

  public abstract Object toJSON();

  public abstract PathAction clone();
}
