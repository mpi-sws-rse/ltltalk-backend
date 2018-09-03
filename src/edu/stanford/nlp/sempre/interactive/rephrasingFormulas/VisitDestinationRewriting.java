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
	
	private JoinFormula createItemsWithPropertyFormula(String property) {
		ValueFormula propertyFormula = new ValueFormula(new NameValue(property, null));
		JoinFormula itemsWithPropertyFormula = new JoinFormula("items?property", propertyFormula);
		return itemsWithPropertyFormula;
		
	}
	
	private Formula createFormulaFromTwoProperties(String property1, String property2) {
		JoinFormula itemsWithPropertyFormula1 = createItemsWithPropertyFormula(property1);
		JoinFormula itemsWithPropertyFormula2 = createItemsWithPropertyFormula(property2);
		MergeFormula propertiesCombinationFormula = new MergeFormula(MergeFormula.Mode.and, itemsWithPropertyFormula1, itemsWithPropertyFormula2);
		ValueFormula worldFormula = Formulas.newNameFormula("world");
		CallFormula worldWithItems = new CallFormula("filterArea", Arrays.asList(worldFormula, propertiesCombinationFormula));
		ValueFormula visitAreaFormula = Formulas.newNameFormula("visitArea");
		ActionFormula visitOccupiedPoint = new ActionFormula(ActionFormula.Mode.primitive, 
															 Arrays.asList(visitAreaFormula, worldWithItems));
		return visitOccupiedPoint;
	}
	
	private Formula createFormulaFromProperty(String property) {
		if (property == null) {
			ValueFormula worldFormula = new ValueFormula(new NameValue("world", null));
			CallFormula allItemsFormula = new CallFormula("allItems", new ArrayList<Formula>());
			
			CallFormula worldWithItems = new CallFormula("filterArea", Arrays.asList(worldFormula, allItemsFormula));
			ValueFormula visitAreaFormula = new ValueFormula(new NameValue("visitArea", null));
			ActionFormula visitOccupiedPoint = new ActionFormula(ActionFormula.Mode.primitive, 
																 Arrays.asList(visitAreaFormula, worldWithItems));
			return visitOccupiedPoint;
		}
		else {
			JoinFormula itemsWithPropertyFormula = createItemsWithPropertyFormula(property);
			ValueFormula worldFormula = Formulas.newNameFormula("world");
			CallFormula worldWithItems = new CallFormula("filterArea", Arrays.asList(worldFormula, itemsWithPropertyFormula));
			ValueFormula visitAreaFormula = Formulas.newNameFormula("visitArea");
			ActionFormula visitOccupiedPoint = new ActionFormula(ActionFormula.Mode.primitive, 
																 Arrays.asList(visitAreaFormula, worldWithItems));
			return visitOccupiedPoint;
		}
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
				Formula visitPointWithColorPropertyFormula = createFormulaFromProperty(itemAtPoint.color);
				equivalentFormulas.add(visitPointWithColorPropertyFormula);
				
				Formula visitPointWithShapePropertyFormula = createFormulaFromProperty(itemAtPoint.shape);
				equivalentFormulas.add(visitPointWithShapePropertyFormula);
				
				Formula visitPointWithBothShapeAndColorFormula = createFormulaFromTwoProperties(itemAtPoint.color, itemAtPoint.shape);
				equivalentFormulas.add(visitPointWithBothShapeAndColorFormula);
								
			}
			
			if (itemsAtLastPoint.size() > 0) {
				Formula visitPointWithItemFormula = createFormulaFromProperty(null);
				equivalentFormulas.add(visitPointWithItemFormula);
			}

		}
		
//		if (isFormulaSequenceOfMoves(deriv.getFormula()) == true) {
//			Object lastElementObject = path.get(path.size()-1);
//			ArrayList lastElement = (ArrayList)lastElementObject;
//			int x =  ((Integer)lastElement.get(0)).intValue();
//			int y = ((Integer) lastElement.get(1)).intValue();
//			ActionFormula visitFormula = createVisitConstantFieldFormula(x, y);
//			if (opts.verbose > 0) {
//				LogInfo.logs("created formula = %s", visitFormula);
//			}
//			equivalentFormulas.add(visitFormula);
//		}
		
			
			
	}

}
