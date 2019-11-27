import pdb
try:
    from smtEncoding.dagSATEncoding import DagSATEncoding
    from smtEncoding.SATOfLTLEncoding import SATOfLTLEncoding
    import encodingConstants
    from . import constants
except:
    from encoding.smtEncoding.dagSATEncoding import DagSATEncoding
    from encoding.smtEncoding.SATOfLTLEncoding import SATOfLTLEncoding
    from encoding import encodingConstants
    import constants

from pytictoc import TicToc
try:
    from formulaBuilder.satQuerying import get_models#, get_models_with_safety_restrictions
except:
    from encoding.formulaBuilder.satQuerying import get_models  # , get_models_with_safety_restrictions

from z3 import sat
import logging, os



def run_solver(finalDepth, traces, maxNumOfFormulas=1, startValue=1, step=1, q=None, encoder=DagSATEncoding,
               maxSolutionsPerDepth = 1, testing=False):


    if q is not None:
        separate_process = True
    else:
        separate_process = False

    t = TicToc()
    t.tic()
    if not testing:
        results = get_models(finalDepth=finalDepth, traces=traces, startValue=startValue, step=step,
                             encoder=encoder, literals=traces.literals, maxNumModels=maxNumOfFormulas, maxSolutionsPerDepth=maxSolutionsPerDepth, testing=testing)
    else:
        results, num_attempts = get_models(finalDepth=finalDepth, traces=traces, startValue=startValue, step=step,
                             encoder=encoder, literals=traces.literals, maxNumModels=maxNumOfFormulas, maxSolutionsPerDepth=maxSolutionsPerDepth, testing=testing)

    time_passed = t.tocvalue()

    if testing:
        ret = [results, time_passed, num_attempts]
    else:
        ret = [results, time_passed]

    if separate_process == True:
        q.put(ret)
    else:
        return ret


def get_finite_witness(f, trace_length=5, operators=[encodingConstants.G, encodingConstants.F, encodingConstants.LAND, encodingConstants.LOR, encodingConstants.ENDS, encodingConstants.LNOT, encodingConstants.BEFORE, encodingConstants.STRICTLY_BEFORE, encodingConstants.UNTIL], wall_locations = [], water_locations=None, robot_position = None, items_locations=None):

    t = TicToc()
    solvingTic = TicToc()
    t.tic()
    all_variables = [str(v) for v in f.getAllVariables()]

    fg = SATOfLTLEncoding(f, trace_length, 0, operators=None, literals=all_variables, wall_positions=wall_locations, water_locations=water_locations, robot_position=robot_position, items_locations=items_locations)
    fg.encodeFormula()
    logging.info("creation time was {}".format(t.tocvalue()))
    solvingTic.tic()
    solverRes = fg.solver.check()
    logging.info("solving time was {}".format(solvingTic.tocvalue()))

    if solverRes == sat:
        solverModel = fg.solver.model()



        (cex_trace, init_world, path) = fg.reconstructWitnessTrace(solverModel)
        return (cex_trace, init_world, path)
    else:
        # logging.debug(solverRes)
        # pdb.set_trace()
        if constants.DEBUG_UNSAT_CORE is True:
            filename = "debug_files/unsatCore"
            os.makedirs(os.path.dirname(filename), exist_ok=True)
            with open(filename, "w") as unsat_core_file:
                unsat_core_file.write(str(fg.solver.unsat_core()))
        return "unsat"


