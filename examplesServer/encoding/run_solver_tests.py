import pdb

import argparse
import itertools

try:
    from solverRuns import run_solver, get_finite_witness
    from utils.SimpleTree import Formula
    import encodingConstants
    from . import constants
except:
    from encoding.solverRuns import run_solver, get_finite_witness
    from encoding.utils.SimpleTree import Formula
    from encoding import encodingConstants
    import constants


try:
    from utils.Traces import Trace, ExperimentTraces
except:
    from encoding.utils.Traces import Trace, ExperimentTraces


from world import World

import logging


def get_path(f, wall_locations, water_locations, robot_location, items_locations=None):
    f = Formula.convertTextToFormula(f)

    for tr in itertools.chain(range(constants.MIN_FINE_RANGE, constants.MAX_FINE_RANGE, constants.STEP_FINE_RANGE),
                              range(constants.MIN_COARSE_RANGE, constants.MAX_COARSE_RANGE, constants.STEP_COARSE_RANGE)):
        disambiguation = get_finite_witness(f=f, wall_locations=wall_locations, trace_length=tr, water_locations=water_locations, robot_position=robot_location, items_locations=items_locations)

        if disambiguation == "unsat":
            continue
        else:
            (disambiguation_example, init_world, path) = disambiguation
            return path
    return False

def disambiguate(f_1, f_2, min_trace_length, max_trace_length, wall_locations=[], all_vars_to_consider = []):

    for tr_length in range(min_trace_length, max_trace_length+1):
        difference_formula = Formula([encodingConstants.LOR,
                                      Formula([encodingConstants.LAND, f_1, Formula([encodingConstants.LNOT, f_2])]),
                                      Formula([encodingConstants.LAND, Formula([encodingConstants.LNOT, f_1]), f_2])
                                      ])

        #difference_formula = Formula(["&", f_1, Formula([encodingConstants.LNOT, f_2])])
        #difference_formula = Formula(["&", f_2, Formula([encodingConstants.LNOT, f_1])])


        # if tr_length > 8:
        #     pdb.set_trace()
        disambiguation = get_finite_witness(f = difference_formula, trace_length=tr_length, wall_locations = wall_locations)

        if disambiguation == "unsat":
            continue
        else:
            (disambiguation_example, init_world, path) = disambiguation
            print("+=+=+=++++ disambiguation example is {}".format(disambiguation_example))
            if disambiguation_example.evaluateFormulaOnTrace(difference_formula) == False:
                raise RuntimeError(
                    "looking for witness of satisfiability, but got a trace {} that is not a model for {}".format(
                        disambiguation_example, difference_formula))

            print("init world is {}".format(init_world))
            w = World(worldDescriptionJson = init_world, json_type=1)
            print("the distinguishing sequence of actions is {}".format(path))
            # emitted_path = w.execute_and_emit_events(path)
            # print("+=+=+=++++ emitted path is is {}".format(emitted_path))

            print("distinguishing between {} and {}".format(f_1, f_2))
            print("the initial world is {}".format(w))


            print("the distinguishing traces are {}".format(disambiguation_example))
            print("\n\n====\n\n")
            return w, path, disambiguation_example
    logging.error("Could not find a path disambiguating between {} and {}".format(f_1, f_2))
    return None, None, None


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--log", dest="loglevel", default="INFO")
    parser.add_argument("--test_file", default="tests/data/test1.txt")
    parser.add_argument("--max_trace_length", default = 5)
    parser.add_argument("--min_trace_length", default=3)


    args, unknown = parser.parse_known_args()

    """
    traces is 
     - list of different recorded values (traces)
     - each trace is a list of recordings at time units (time points)
     - each time point is a list of variable values (x1,..., xk) 
    """

    numeric_level = args.loglevel.upper()
    logging.basicConfig(level=numeric_level)

    max_trace_length = int(args.max_trace_length)
    min_trace_length = int(args.min_trace_length)

    with open(args.test_file) as test_file:
        for line in test_file:
            [f_1_string, f_2_string] = line.split(";")

            f_1 = Formula.convertTextToFormula(f_1_string)
            f_2 = Formula.convertTextToFormula(f_2_string)



            disambiguate(f_1, f_2, min_trace_length, max_trace_length)





if __name__ == "__main__":
    main()

