package edu.stanford.nlp.sempre.interactive.rephrasingFormulas;
import edu.stanford.nlp.sempre.Derivation;
import edu.stanford.nlp.sempre.*;
import edu.stanford.nlp.sempre.ActionFormula;
import java.util.*;
import edu.stanford.nlp.sempre.interactive.InteractiveUtils;
import fig.basic.LogInfo;
import fig.basic.Option;

public class SimpleEquivalentRewriting extends EquivalentFormulas {
	
	public static class Options {
	    @Option(gloss = "verbose")
	    public int verbose = 0;
	  }

	  public static Options opts = new Options();

	public Formula resultingEquivalentFormula;
	public List<Formula> getEquivalentFormulas(){
		LinkedList<Formula> l = new LinkedList<Formula>();
		l.add(resultingEquivalentFormula);
		
		return l;
	}
	
	
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
		
		
		
		return actionList;
		
	}
	
	private Formula repeatedActionFormula(int numberOfRepetitions, Formula formulaToRepeat){
		LinkedList<Formula> formulaArguments = new LinkedList<Formula>();
		ActionFormula.Mode mode = ActionFormula.Mode.repeat;
		formulaArguments.add(new ValueFormula(new NumberValue(numberOfRepetitions)));
		
		if (opts.verbose > 1){
			LogInfo.logs("formulaTo repeat = %s", formulaToRepeat.toString());
		}
		formulaArguments.add(formulaToRepeat);
		Formula formulaToAdd = new ActionFormula(mode, formulaArguments);
		return formulaToAdd;
	}
	  
	public SimpleEquivalentRewriting(Derivation deriv, List<String> headTokens){
	if (opts.verbose > 1){
		LogInfo.logs(deriv.toString());
		deriv.printDerivationRecursively();
		LogInfo.logs(headTokens.toString());
		LogInfo.logs("----------");
	}
		originalFormula = deriv.getFormula();
		
		//could this be replaced by working directly on the formula of derivation
		LinkedList<Formula> listOfActions = flattenActionsConstruct(deriv);
		LinkedList<Formula> reformulatedListOfActions = new LinkedList<Formula>();
	

		if (listOfActions.size() > 0){
			Formula currentCandidate = listOfActions.get(0);
			
			boolean anythingChanged = false;
			int numberOfRepetitions = 0;
			for (Formula f : listOfActions){
				
				
				if (f.equals(currentCandidate)){
					
					numberOfRepetitions++;
				
					
					
				}
				
				else {
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
			if (opts.verbose > 1){
				LogInfo.logs("EQUIVALENT REWRITING: reformulated list: %s", reformulatedListOfActions.toString());
			}
			if (reformulatedListOfActions.size() >  1){
				resultingEquivalentFormula = new ActionFormula(modeOfFormula, reformulatedListOfActions);
			}
			else if (reformulatedListOfActions.size() == 1){
				resultingEquivalentFormula = reformulatedListOfActions.get(0);
			}
			else {
				throw new RuntimeException("rewritten formula is empty list");
			}
		}
		else {
			resultingEquivalentFormula = originalFormula;
		}
		
		
	
	}
		
	
}
