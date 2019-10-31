from z3 import *
import pdb
import logging
try:
    from utils.SimpleTree import SimpleTree, Formula
    import constants
except:
    from encoding.utils.SimpleTree import SimpleTree, Formula



def get_models_with_safety_restrictions(safety_restrictions, traces, final_depth, literals, encoder, operators,
                                         start_value = 1, step=1, max_num_solutions = 10):
    raise NotImplementedError





def get_models(finalDepth, traces, startValue, step, encoder, literals, maxNumModels=1, maxSolutionsPerDepth = 1):

    results = []
    i = startValue
    fg = encoder(i, traces, literals=literals)
    fg.encodeFormula(hintVariablesWithWeights=traces.hints_with_weights)
    maxSolutionsPerDepth = maxSolutionsPerDepth
    solutionsPerDepth = 0
    while len(results) < maxNumModels and i < finalDepth:

        solverRes = fg.solver.check()
        if not solverRes == sat or solutionsPerDepth >= maxSolutionsPerDepth:
            #pdb.set_trace()
            if solutionsPerDepth >= maxSolutionsPerDepth:
                logging.info("enough solutions for depth {0}".format(i))
            else:
                logging.info("not sat for i = {}".format(i))
            #pdb.set_trace()
            i += step
            solutionsPerDepth = 0
            fg = encoder(i, traces, literals=literals)

            fg.encodeFormula(hintVariablesWithWeights=traces.hints_with_weights)
        else:

            solverModel = fg.solver.model()
            formula = fg.reconstructWholeFormula(solverModel)

            logging.info("found formula {}".format(formula.prettyPrint()))
            formula = Formula.normalize(formula)
            logging.info("normalized formula {}\n=============\n".format(formula))


            if formula not in results:
                results.append(formula)
                solutionsPerDepth += 1

            # prevent current result from being found again
            block = []

            infVariables = fg.getInformativeVariables()

            logging.debug("informative variables of the model:")
            for v in infVariables:
                logging.debug((v, solverModel[v]))
            logging.debug("===========================")
            for d in solverModel:
                # d is a declaration
                if d.arity() > 0:
                    raise Z3Exception("uninterpreted functions are not supported")
                # create a constant from declaration
                c = d()
                if is_array(c) or c.sort().kind() == Z3_UNINTERPRETED_SORT:
                    raise Z3Exception("arrays and uninterpreted sorts are not supported")
                block.append(c != solverModel[d])

            fg.solver.add(Or(block))
    return results
