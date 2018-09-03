package edu.stanford.nlp.sempre.interactive.rephrasingFormulas;

import edu.stanford.nlp.sempre.Derivation;
import edu.stanford.nlp.sempre.*;
import edu.stanford.nlp.sempre.interactive.robolurn.RoboWorld;
import edu.stanford.nlp.sempre.interactive.robolurn.Item;
import edu.stanford.nlp.sempre.ActionFormula;
import java.util.*;
import edu.stanford.nlp.sempre.interactive.InteractiveUtils;
import fig.basic.LogInfo;
import fig.basic.Option;
import edu.stanford.nlp.sempre.Session;
import edu.stanford.nlp.sempre.Json;
import fig.basic.LispTree;
import java.awt.Point;
public class PickingAndDroppingRewriting extends EquivalentFormulas {

	public static class Options {
		@Option(gloss = "verbose")
		public int verbose = 0;
	}

	public static Options opts = new Options();

	public List<Formula> equivalentFormulas = new LinkedList<Formula>();

	public List<Formula> getEquivalentFormulas() {
		return equivalentFormulas;
		}
	
	private boolean isEveryActionOfType(String actionType, ArrayList path) {
		for (Object pathObj : path) {
			ArrayList pathElement = (ArrayList)pathObj;
			if (! (pathElement.get(2).equals(actionType))) {
				return false;
			}
		}
		return true;
	}
	
	public PickingAndDroppingRewriting(Derivation deriv, List<String> headTokens, String executionAnswer, Session session){
		RoboWorld worldBefore = RoboWorld.fromContext(session.context);
		RoboWorld worldAfter = worldBefore.clone();
		
		LispTree answerTree = LispTree.proto.parseFromString(executionAnswer);
		String jsonPath = answerTree.child(1).value;
		Map<String, Object> jsonMapping = Json.readMapHard(jsonPath);
		ArrayList path = (ArrayList)jsonMapping.get("path");
		
		Point robotsPoint = worldBefore.getRobotInfo().point;
		
		if (isEveryActionOfType("pickitem", path)) {
			Set<Item> pointsAtRobotLocation = worldBefore.itemsAtPoint(robotsPoint);
			LogInfo.logs("points at robot location = %s", pointsAtRobotLocation);
		}
		else if (isEveryActionOfType("dropitem", path)) {
			LogInfo.logs("bla");
		}
		
	}

}
