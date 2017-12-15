package edu.stanford.nlp.sempre.interactive.rephrasingFormulas;
import edu.stanford.nlp.sempre.Derivation;
import edu.stanford.nlp.sempre.*;
import edu.stanford.nlp.sempre.ActionFormula;
import java.util.*;
import edu.stanford.nlp.sempre.interactive.InteractiveUtils;
import fig.basic.LogInfo;
import fig.basic.Option;

public class SimpleEquivalentRewriting{
	
	public static class Options {
	    @Option(gloss = "verbose")
	    public int verbose = 0;
	  }

	  public static Options opts = new Options();

	public LinkedList<Derivation> equivalentDerivations;
	public Derivation rewrittenEquivalentDerivation;
	
//	public findRepeatedActions()
//	{}
	
	private LinkedList<Formula> flattenActionsConstruct(Derivation deriv){
		LinkedList<Formula> actionList = new LinkedList<Formula>();
		Derivation d = deriv;
		if (!(d.getCat().equals("$Actions"))){
			return actionList;
		}
		else{
			
			while (d.children.get(0).getCat().equals("$Actions") && d.children.get(1).getCat().equals("$Action")){
				
					actionList.addFirst(d.children.get(1).getFormula());
					d = d.children.get(0);
				
			}
			actionList.addFirst(d.getFormula());
		}
		
		LogInfo.logs("printing the list");
		for (Formula printD : actionList){
			LogInfo.logs("\t----------element----------\n\t%s", printD.toString());
		}
		
		return actionList;
		
	}
	
	private Formula repeatedActionFormula(int numberOfRepetitions, Formula formulaToRepeat){
		LinkedList<Formula> formulaArguments = new LinkedList<Formula>();
		ActionFormula.Mode mode = ActionFormula.Mode.repeat;
		formulaArguments.add(new ValueFormula(new NumberValue(numberOfRepetitions)));
		

		LogInfo.logs("formulaTo repeat = %s", formulaToRepeat.toString());
		formulaArguments.add(formulaToRepeat);
//		Derivation derivationToAdd = new Derivation.Builder()
//	              								  .formula(new ActionFormula(mode, formulaArguments))
//	              								  .createDerivation();
		Formula formulaToAdd = new ActionFormula(mode, formulaArguments);
		return formulaToAdd;
	}
	  
	public SimpleEquivalentRewriting(Derivation deriv, List<String> headTokens){
		equivalentDerivations = new LinkedList<Derivation>();
		if (opts.verbose > 1){
			LogInfo.logs(deriv.toString());
			deriv.printDerivationRecursively();
			LogInfo.logs(headTokens.toString());
			LogInfo.logs("----------");
		}
		
		LinkedList<Formula> listOfActions = flattenActionsConstruct(deriv);
		LinkedList<Formula> reformulatedListOfActions = new LinkedList<Formula>();
		

		if (listOfActions.size() > 1){
			Formula currentCandidate = listOfActions.get(0);
			
			boolean anythingChanged = false;
			int numberOfRepetitions = 0;
			for (Formula f : listOfActions){
				
				
				if (f.equals(currentCandidate)){
					
					numberOfRepetitions++;
				
					
					
				}
				
				else {
					LogInfo.logs("was not equal!, num reps = %d", numberOfRepetitions);
					if (numberOfRepetitions > 1){
						
						
						reformulatedListOfActions.add(repeatedActionFormula(numberOfRepetitions, currentCandidate));
						anythingChanged = true;
						
						
					}
					else{
						reformulatedListOfActions.add(currentCandidate);
					}
					currentCandidate = f;
					numberOfRepetitions = 1;
				
				
				}
			}
			if (numberOfRepetitions > 1){
				reformulatedListOfActions.add(repeatedActionFormula(numberOfRepetitions, currentCandidate));
			} 
			else{
				reformulatedListOfActions.add(currentCandidate);
			}
			
			ActionFormula.Mode modeOfFormula = ActionFormula.Mode.sequential;
			LogInfo.logs("reformulated list: %s", reformulatedListOfActions.toString());
			Derivation reformulatedDerivation = new Derivation.Builder()
																		.formula( new ActionFormula(modeOfFormula, reformulatedListOfActions) )
																		.cat("$Actions")
																		.createDerivation();
			LogInfo.logs("reformulated derivations +++++++++++ %s", reformulatedDerivation);
			
			for (Formula f : reformulatedListOfActions){
				LogInfo.logs("printing reformulated formula: %s", f.toString());
			}
			
			
			LogInfo.logs("printing original formulas: %s", deriv.getFormula().toString());
			
			//LogInfo.logs("their children +++++++++++ %s", reformulatedDerivation.getChildren().toString());
			equivalentDerivations.add(reformulatedDerivation);
		}
		rewrittenEquivalentDerivation = InteractiveUtils.combine(equivalentDerivations);
		LogInfo.logs("recursive printing of derivation!=!=!=!=!+!=");
		rewrittenEquivalentDerivation.printDerivationRecursively();
		LogInfo.logs("equivalent derivations found: %s", rewrittenEquivalentDerivation.toString());
		
		
		
			
			
//			Derivation left, right;
//			Formula leftFormula, rightFormula, leftMoreGeneralFormula;
//			for (Derivation child : deriv.children){
//				child.printDerivationRecursively();
//				LogInfo.logs("category: %s", child.getCat());
//				LogInfo.logs("formula: %s", child.getFormula().toString());
			
//			left = deriv.children.get(0).children.get(0);
//			right = deriv.children.get(1);
//			leftFormula = deriv.children.get(0).children.get(0).getFormula();
//			leftMoreGeneralFormula = deriv.children.get(0).getFormula();
//			rightFormula = deriv.children.get(1).getFormula();
//			Boolean derivEqual = (left.equals(right));
//			Boolean formulaEqual = leftMoreGeneralFormula.equals(rightFormula);
//			LogInfo.logs("derivations equal: %s, formulas equal: %s", derivEqual.toString(), formulaEqual.toString());
	}
		
	
}
