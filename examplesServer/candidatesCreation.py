import random
from utils import create_json_spec, convert_json_actions_to_world_format, convert_path_to_formatted_path, unwind_actions
import nlp_helpers
from encoding.experiment import start_experiment
from encoding.run_solver_tests import disambiguate, get_path
from encoding.utils.SimpleTree import Formula
from encoding.utils.Traces import Trace
from world import World
import pdb
import json
import os
import logging
from copy import deepcopy
import constants
from logger_initialization import stats_log
from pytictoc import TicToc


def create_candidates(nl_utterance, examples, testing=False, num_formulas=None, id=None, max_depth=None, criterion=None,
                      use_hints = True):

    t = TicToc()
    emitted_events_seq = []
    collection_of_negative = []
    pickup_locations = []
    all_locations = []

    if max_depth is None:
        max_depth = constants.CANDIDATE_MAX_DEPTH

    for ex in examples:
        context = ex["context"]
        path = ex["init-path"]
        test_world = World(context, json_type=2)
        (emitted_events, pickup_locations_ex, collection_of_negative_ex, all_locations_ex) = test_world.execute_and_emit_events(
        path)
        emitted_events_seq.append(emitted_events)
        collection_of_negative += collection_of_negative_ex
        pickup_locations += pickup_locations_ex
        all_locations += all_locations_ex

    pickup_locations = list(set(pickup_locations))
    all_locations = list(set(all_locations))



    t.tic()

    if use_hints is True:
        hints = nlp_helpers.get_hints_from_utterance(nl_utterance)
    else:
        hints = {}

    stats_log.debug("nlp hints creation time: {}".format(t.tocvalue()))

    relevant_locations = nlp_helpers.get_locations_from_utterance(nl_utterance)


    hintsWithLocations = {}

    for hint in hints:

        if hint == constants.DRY:
            hintsWithLocations[hint] = hints[hint]

            continue
        if hint in constants.OPERATORS or hint in constants.AT_SPECIAL_LOCATION_EVENTS:
            hintsWithLocations[hint] = hints[hint]

        for l in pickup_locations:
            hintsWithLocations["{}_at_{}_{}".format(hint, l[0], l[1])] = hints[hint]
    #hintsWithLocations = {"{}_{}_{}".format(hint, l[0], l[1]) : hints[hint] for hint in hints for l in pickup_locations}

    if len(hintsWithLocations) > 0:
        maxHintsWithLocations = max(hintsWithLocations.values())
        minHintsWithLocations = min(hintsWithLocations.values())
    else:
        maxHintsWithLocations = 0
        minHintsWithLocations = 0


    stats_log.debug("hints: {}".format("\n\t".join(hints)))
    middleValue = (maxHintsWithLocations + minHintsWithLocations) / 2

    atLocationsHints = {"at_{}_{}".format(loc[0],loc[1]): max(minHintsWithLocations,1) for loc in relevant_locations}


    hintsWithLocations = nlp_helpers.filter_hints_with_emitted_events(hintsWithLocations, emitted_events_seq)
    hintsWithLocations.update(atLocationsHints)

    # DEBUG: at_dry hint is disadvantaged. want to give it back some weight
    if constants.DRY in hints:
        hintsWithLocations[constants.DRY] = hints[constants.DRY] + 1


    os.makedirs("data", exist_ok=True)
    hints_report = [ "{} --> {}".format(k, hintsWithLocations[k]) for k in hintsWithLocations ]
    stats_log.debug("hints: \n\t{}".format("\n\t".join(hints_report)))

    json_name = "data/"+id+".json"
    txt_name = "data/"+id+".txt"


    create_json_spec(file_name=json_name, emitted_events_sequences=emitted_events_seq, hints=hintsWithLocations,
                     pickup_locations=pickup_locations, all_locations=all_locations,
                     negative_sequences=collection_of_negative, num_formulas=num_formulas, max_depth=max_depth)



    if testing:

        collection_of_candidates, num_attempts, time_passed, solver_solving_times = start_experiment(experiment_specification=json_name, testing=testing, trace_out=txt_name, criterion=criterion)
        return collection_of_candidates, num_attempts, time_passed, solver_solving_times
    else:
        collection_of_candidates = start_experiment(experiment_specification=json_name,
                                                                  testing=testing)
        return collection_of_candidates

def update_candidates(old_candidates, path, decision, world, actions):


    collection_of_candidates = []
    collection_of_formulas = []

    if int(decision) == 1:
        formula_value = True
    elif int(decision) == 0:
        formula_value = False
    else:
        raise ValueError("got user decision different from 0 or 1, the value was {}".format(decision))



    #converted_path = convert_json_actions_to_world_format(deepcopy(world), path)
    converted_path = unwind_actions(actions)


    logging.debug(converted_path)



    (emitted_events, _,_,_) = world.execute_and_emit_events(converted_path)

    all_relevant_literals = []
    old_candidate_formulas = []
    for c in old_candidates:
        f = Formula.convertTextToFormula(c)
        old_candidate_formulas.append(f)
        all_relevant_literals += f.getAllVariables()
    all_relevant_literals = list(set(all_relevant_literals))
    # logging.debug("emitted events are {}".format(emitted_events))
    # logging.debug("+-+-------------- all relevant literals are {}".format(all_relevant_literals))


    trace = Trace.create_trace_from_events_list(emitted_events, literals_to_consider=all_relevant_literals)



    # logging.debug("+++++++++++++++++=======================\n elimination trace is {}".format(trace))
    # logging.debug("desired formula value is {}".format(formula_value))

    for f in old_candidate_formulas:

        if trace.evaluateFormulaOnTrace(f) == formula_value:
            collection_of_candidates.append(str(f))
            collection_of_formulas.append(f)
            stats_log.debug("candidate {} was retained".format(f))
        else:
            stats_log.debug("candidate {} was eliminated".format(f))




    return collection_of_candidates, collection_of_formulas


def create_path_from_formula(f, wall_locations, water_locations, robot_position, items_locations = None):
    return get_path(f,wall_locations, water_locations, robot_position, items_locations)




def create_disambiguation_example(candidates, wall_locations = [], testing=False):

    logging.debug("creating disambiguation examples for candidates {}".format(candidates))
    w = None
    path = None
    candidate_1 = None
    candidate_2 = None
    if len(candidates) == 0 or str(candidates[0]) == constants.UNKNOWN_SOLVER_RES:
        status = constants.FAILED_CANDIDATES_GENERATION_STATUS
        return (status, w, path, None, None, None, None)
    elif len(candidates) == 1:
        status = "ok"
        return (status, w, path, candidates[0], None, None, None)

    else:

        candidate_1 = candidates[0]
        candidate_2 = candidates[1]


        w, path, disambiguation_trace = disambiguate(candidate_1, candidate_2, wall_locations, testing=testing)
        if w == constants.UNKNOWN_SOLVER_RES:
            return (w, None, None, None, None, None, None)
        if w is None and path is None:

            if candidate_1 < candidate_2:
                longer_candidate = candidate_2

            else:
                longer_candidate = candidate_1

            candidates.remove(longer_candidate)
            stats_log.warning("was not able to disambiguate between {} and {}. Removing {}".format(candidate_1, candidate_2, longer_candidate))

            return create_disambiguation_example(candidates, wall_locations)
        else:
            status = "indoubt"



        return (status, w, path, candidate_1, candidate_2, candidates, disambiguation_trace)



