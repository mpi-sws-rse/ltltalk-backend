from z3 import *
import pdb
import logging
import constants

try:
    from utils.SimpleTree import SimpleTree, Formula
    import constants
except:
    from encoding.utils.SimpleTree import SimpleTree, Formula
from logger_initialization import stats_log

from pytictoc import TicToc


def get_models_with_safety_restrictions(safety_restrictions, traces, final_depth, literals, encoder, operators,
                                        start_value=1, step=1, max_num_solutions=10):
    raise NotImplementedError


def get_models(finalDepth, traces, step, encoder, literals, maxNumModels=1, maxSolutionsPerDepth=1,
               testing=False, criterion=None):
    formula_generation_times = []
    results = []

    generation_tic = TicToc()
    generation_tic.tic()

    fg = encoder(finalDepth, traces, literals=literals, testing=testing,
                 hintVariablesWithWeights=traces.hints_with_weights, criterion=criterion)
    fg.encodeFormula()
    formula_generation_times.append(generation_tic.tocvalue())



    num_attemts = 0
    solver_solving_times = []


    while len(results) < maxNumModels and num_attemts < maxNumModels + 2:

        num_attemts += 1


        solverTic = TicToc()
        solverTic.tic()
        solverRes = fg.solver.check()
        solver_solving_times.append(solverTic.tocvalue())

        if solverRes == unsat:
            logging.info("unsat!")
            break

        elif solverRes == unknown:
            results = [constants.UNKNOWN_SOLVER_RES]
            break


        else:

            solverModel = fg.solver.model()
            found_formula_depth = solverModel[fg.guessed_depth].as_long()

            formula = fg.reconstructWholeFormula(solverModel, depth=found_formula_depth)
            table = fg.reconstructTable(solverModel, depth=found_formula_depth)
            logging.info("found formula {} of depth {}".format(formula.prettyPrint(), found_formula_depth))
            formula = Formula.normalize(formula)
            if not os.path.exists("debug_models/"):
                os.makedirs("debug_models/")
            model_filename = "debug_models/" + str(num_attemts) + ".model"
            table_filename = "debug_models/" + str(num_attemts) + ".table"
            with open(table_filename, "w") as table_file:
                table_file.write(str(table))
            with open(model_filename, "w") as model_file:
                model = solverModel
                for idx in range(len(model)):
                    model_file.writelines("{}: {}\n".format(model[idx], model[model[idx]]))

            logging.info("normalized formula {}\n=============\n".format(formula))
            if formula not in results:
                results.append(formula)
                logging.info(
                    "added formula {} to the set. Currently we have {} formulas and looking for total of {}".format(
                        formula, len(results), maxNumModels))

            block = []

            infVariables = fg.getInformativeVariables(depth=found_formula_depth, model=solverModel)

            logging.debug("informative variables of the model:")
            for v in infVariables:
                print("{}: {}".format(v, solverModel[v]))
                block.append(Not(v))
            logging.debug("===========================")
            print("blocking {}".format(block))
            fg.solver.add(Or(block))
    stats_log.info("number of initial candidates: {}".format(len(results)))
    stats_log.debug("number of candidates per depth: {}".format(constants.NUM_CANDIDATE_FORMULAS_OF_SAME_DEPTH))
    stats_log.info("number of attempts to get initial candidates: {}".format(num_attemts))
    stats_log.info("propositional formula building times are {}".format(formula_generation_times))

    if testing:
        return results, num_attemts, solver_solving_times
    else:
        return results
