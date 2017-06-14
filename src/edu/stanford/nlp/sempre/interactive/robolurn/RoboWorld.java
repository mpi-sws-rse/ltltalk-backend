package edu.stanford.nlp.sempre.interactive.robolurn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import edu.stanford.nlp.sempre.ContextValue;
import edu.stanford.nlp.sempre.Json;
import edu.stanford.nlp.sempre.NaiveKnowledgeGraph;
import edu.stanford.nlp.sempre.StringValue;
import edu.stanford.nlp.sempre.interactive.PathAction;
import edu.stanford.nlp.sempre.interactive.World;
import fig.basic.LogInfo;
import fig.basic.Option;

public class RoboWorld extends World {
  public static class Options {
    @Option(gloss = "maximum number of cubes to convert")
    public int maxBlocks = 1024 ^ 2;
  }

  public static Options opts = new Options();

  public static RoboWorld fromContext(ContextValue context) {
    if (context == null || context.graph == null) {
      return fromJSON("[[0,0,[]],[1,1,\"wall\",null]]");
    }
    NaiveKnowledgeGraph graph = (NaiveKnowledgeGraph) context.graph;
    String wallString = ((StringValue) graph.triples.get(0).e1).value;
    return fromJSON(wallString);
  }

  private List<PathAction> pathActions;

  private Robot robot;

  @SuppressWarnings("unchecked")
  public RoboWorld(Set<WorldBlock> blockset) {
    super();
    this.allBlocks = blockset;
    this.pathActions = new ArrayList<PathAction>();
  }

  @Override
  public String toJSON() {
    return "NOT YET SUPPORTED";
  }

  @Override
  public String getJSONPath() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    boolean first = true;
    for (PathAction pa : pathActions) {
      if (first)
        first = false;
      else
        sb.append(",");
      sb.append(pa.toJSON());
    }
    sb.append("]");
    return sb.toString().replace(" ","");
  }

  private static RoboWorld fromJSON(String wallString) {
    @SuppressWarnings("unchecked")
    List<List<Object>> blockstr = Json.readValueHard(wallString, List.class);
    List<Object> rawRobot = blockstr.get(0);
    Robot robot = new Robot(
      (int) rawRobot.get(0),
      (int) rawRobot.get(1),
      (List<String>) rawRobot.get(2)
    );
    Set<WorldBlock> blocks = blockstr.subList(1, blockstr.size()).stream().map(c -> {
      return WorldBlock.fromJSONObject(c);
    }).collect(Collectors.toSet());
    RoboWorld world = new RoboWorld(blocks);
    world.robot = robot;
    return world;
  }

  @Override
  public Set<WorldBlock> has(String rel, Set<Object> values) {
    return this.allBlocks.stream().filter(i -> values.contains(i.get(rel))).collect(Collectors.toSet());
  }

  @Override
  public Set<Object> get(String rel, Set<WorldBlock> subset) {
    return subset.stream().map(i -> i.get(rel)).collect(Collectors.toSet());
  }

  public void visit(int x, int y) {
    pathActions.add(new RoboAction(robot.x, robot.y, RoboAction.Action.PATH));
    pathActions.add(new RoboAction(x, y, RoboAction.Action.DESTINATION));
  }

  private void refreshSet(Set<WorldBlock> set) {
    List<WorldBlock> s = new ArrayList<>(set);
    set.clear();
    set.addAll(s);
  }

  private void keyConsistency() {
    refreshSet(allBlocks);
  }
}
