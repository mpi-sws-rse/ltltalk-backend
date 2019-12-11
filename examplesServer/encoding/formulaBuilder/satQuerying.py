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


def get_models(finalDepth, traces, startValue, step, encoder, literals, maxNumModels=1, maxSolutionsPerDepth=1,
               testing=False):
    formula_generation_times = []
    results = []
    i = startValue
    generation_tic = TicToc()
    generation_tic.tic()
    fg = encoder(finalDepth, traces, literals=literals, testing=testing, hintVariablesWithWeights=traces.hints_with_weights)
    fg.encodeFormula(depth=i)
    formula_generation_times.append(generation_tic.tocvalue())


    maxSolutionsPerDepth = maxSolutionsPerDepth
    solutionsPerDepth = 0
    num_attemts = 0
    num_attemts_per_depth = 0
    blocking_constraints = []
    solver_solving_times = []


    while len(results) < maxNumModels and i < finalDepth:


        num_attemts_per_depth += 1
        num_attemts += 1
        logging.info("ATTEMPT {}, running solver for depth = {}".format(num_attemts, i))

        if num_attemts_per_depth > constants.NUM_ATTEMPTS_PER_DEPTH:
            logging.info("enough attempts for depth {0}".format(i))
            num_attemts_per_depth = 0

            solutionsPerDepth = 0
            i += step
            generation_tic.tic()
            #fg = encoder(i, traces, literals=literals, testing=testing)
            fg.encodeFormula(depth=i)
            formula_generation_times.append(generation_tic.tocvalue())
        solverTic = TicToc()
        solverTic.tic()
        solverRes = fg.solver.check()
        solver_solving_times.append(solverTic.tocvalue())

        if solverRes == unsat:

            logging.info("not sat for i = {}".format(i))

            i += step
            solutionsPerDepth = 0

            num_attemts_per_depth = 0
            generation_tic.tic()
            fg.encodeFormula(depth=i)
            formula_generation_times.append(generation_tic.tocvalue())
        elif solverRes == unknown:

            results = [constants.UNKNOWN_SOLVER_RES]
            break

        else:

            solverModel = fg.solver.model()

            formula = fg.reconstructWholeFormula(solverModel, depth=i)
            table = fg.reconstructTable(solverModel, depth=i)
            logging.info("found formula {}".format(formula.prettyPrint()))
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
                solutionsPerDepth += 1

            if solutionsPerDepth < maxSolutionsPerDepth:
                logging.info("blocking the solution {} from appearing again".format(formula))
                # prevent current result from being found again
                block = []

                infVariables = fg.getInformativeVariables(depth=i, model = solverModel)


                logging.debug("informative variables of the model:")
                for v in infVariables:
                    block.append(Not(v))
                logging.debug("===========================")



                fg.solver.add(Or(block))

            # reset the solver
            else:
                if len(results) > maxNumModels:
                    break
                logging.info("enough solutions for depth {0}".format(i))
                i += step
                solutionsPerDepth = 0
                num_attemts_per_depth = 0
                generation_tic.tic()
                fg.encodeFormula(depth=i)
                formula_generation_times.append(generation_tic.tocvalue())

    stats_log.info("number of initial candidates: {}".format(len(results)))
    stats_log.debug("number of candidates per depth: {}".format(constants.NUM_CANDIDATE_FORMULAS_OF_SAME_DEPTH))
    stats_log.info("number of attempts to get initial candidates: {}".format(num_attemts))
    stats_log.info("propositional formula building times are {}".format(formula_generation_times))
    if testing:
        return results, num_attemts, solver_solving_times
    else:
        return results
