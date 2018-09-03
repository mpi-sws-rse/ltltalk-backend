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
public class VisitDestinationRewriting extends EquivalentFormulas {

	public static class Options {
		@Option(gloss = "verbose")
		public int verbose = 0;
	}

	public static Options opts = new Options();

	public List<Formula> equivalentFormulas = new LinkedList<Formula>();

	public List<Formula> getEquivalentFormulas() {
		return equivalentFormulas;
		}
	
	private boolean isPathSequenceOfMoves(ArrayList path) {
		for (Object pathObj : path) {
			ArrayList pathElement = (ArrayList)pathObj;
			if (! (pathElement.get(2).equals("path") || pathElement.get(2).equals("destination"))) {
				return false;
			}
		}
		return true;
	}
	
	
	
	public VisitDestinationRewriting(Derivation deriv, List<String> headTokens, String executionAnswer, Session session){
		if (opts.verbose > 0) {
			LogInfo.logs("sent deriv = %s, headTokens = %s, executionAnswer = %s, context = %s", deriv, headTokens, executionAnswer, session.context);
			LogInfo.logs("formula = %s", deriv.getFormula());
		}
		
		LispTree answerTree = LispTree.proto.parseFromString(executionAnswer);
		String jsonPath = answerTree.child(1).value;
		Map<String, Object> jsonMapping = Json.readMapHard(jsonPath);
		ArrayList path = (ArrayList)jsonMapping.get("path");
		if (isPathSequenceOfMoves(path)) {
			RoboWorld world = RoboWorld.fromContext(session.context);
			Object lastElementObject = path.get(path.size()-1);
			ArrayList lastElement = (ArrayList)lastElementObject;
			int x =  ((Integer)lastElement.get(0)).intValue();
			int y = ((Integer) lastElement.get(1)).intValue();
			Set<Item> itemsAtLastPoint = world.itemsAtPoint(new Point(x,y));
			
			
			for (Item itemAtPoint : itemsAtLastPoint) {
				Formula visitPointWithColorPropertyFormula = Formulas.createFormulaFromProperty(itemAtPoint.color);
				equivalentFormulas.add(visitPointWithColorPropertyFormula);
				
				Formula visitPointWithShapePropertyFormula = Formulas.createFormulaFromProperty(itemAtPoint.shape);
				equivalentFormulas.add(visitPointWithShapePropertyFormula);
				
				Formula visitPointWithBothShapeAndColorFormula = Formulas.createFormulaFromTwoProperties(itemAtPoint.color, itemAtPoint.shape);
				equivalentFormulas.add(visitPointWithBothShapeAndColorFormula);
								
			}
			
			if (itemsAtLastPoint.size() > 0) {
				Formula visitPointWithItemFormula = Formulas.createFormulaFromProperty(null);
				equivalentFormulas.add(visitPointWithItemFormula);
			}

		}
		

		
			
			
	}

}
