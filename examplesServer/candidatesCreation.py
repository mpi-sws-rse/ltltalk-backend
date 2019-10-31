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

def create_candidates(nl_utterance, context, example):

    test_world = World(context, json_type=2)
    (emitted_events, pickup_locations, collection_of_negative, all_locations) = test_world.execute_and_emit_events(
        example)

    hints = nlp_helpers.get_hints_from_utterance(nl_utterance)
    relevant_locations = nlp_helpers.get_locations_from_utterance(nl_utterance)


    hintsWithLocations = {}
    for hint in hints:
        if hint == constants.DRY:
            hintsWithLocations[hint] = hints[hint]
            continue
        for l in pickup_locations:
            hintsWithLocations["{}_at_{}_{}".format(hint, l[0], l[1])] = hints[hint]
    #hintsWithLocations = {"{}_{}_{}".format(hint, l[0], l[1]) : hints[hint] for hint in hints for l in pickup_locations}

    if len(hintsWithLocations) > 0:
        maxHintsWithLocations = max(hintsWithLocations.values())
        minHintsWithLocations = min(hintsWithLocations.values())
    else:
        maxHintsWithLocations = 0
        minHintsWithLocations = 0
    middleValue = (maxHintsWithLocations + minHintsWithLocations) / 2

    atLocationsHints = {"at_{}_{}".format(loc[0],loc[1]): max(middleValue,1) for loc in relevant_locations}
    hintsWithLocations.update(atLocationsHints)

    os.makedirs("data", exist_ok=True)
    create_json_spec(file_name="data/exampleWithHints.json", emitted_events=emitted_events, hints=hintsWithLocations,
                     pickup_locations=pickup_locations, all_locations=all_locations,
                     negative_sequences=collection_of_negative)


    collection_of_candidates = start_experiment(experiment_specification="data/exampleWithHints.json")


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


    print(converted_path)



    (emitted_events, _,_,_) = world.execute_and_emit_events(converted_path)

    all_relevant_literals = []
    old_candidate_formulas = []
    for c in old_candidates:
        f = Formula.convertTextToFormula(c)
        old_candidate_formulas.append(f)
        all_relevant_literals += f.getAllVariables()
    all_relevant_literals = list(set(all_relevant_literals))
    # print("emitted events are {}".format(emitted_events))
    # print("+-+-------------- all relevant literals are {}".format(all_relevant_literals))


    trace = Trace.create_trace_from_events_list(emitted_events, literals_to_consider=all_relevant_literals)



    # print("+++++++++++++++++=======================\n elimination trace is {}".format(trace))
    # print("desired formula value is {}".format(formula_value))

    for f in old_candidate_formulas:

        if trace.evaluateFormulaOnTrace(f) == formula_value:
            collection_of_candidates.append(str(f))
            collection_of_formulas.append(f)
            print("candidate {} was retained".format(f))
        else:
            print("candidate {} was eliminated".format(f))




    return collection_of_candidates, collection_of_formulas


def create_path_from_formula(f, wall_locations, water_locations, robot_position, items_locations = None):
    return get_path(f,wall_locations, water_locations, robot_position, items_locations)




def create_disambiguation_example(candidates, wall_locations = []):
    print("creating disambiguation examples for candidates {}".format(candidates))
    w = None
    path = None
    candidate_1 = None
    candidate_2 = None
    if len(candidates) == 0:
        status = "failure"
        return (status, w, path, None, None, None, None)
    elif len(candidates) == 1:
        status = "ok"
        return (status, w, path, candidates[0], None, None, None)

    else:

        candidate_1 = candidates[0]
        candidate_2 = candidates[1]


        w, path, disambiguation_trace = disambiguate(candidate_1, candidate_2, 4, 10, wall_locations)
        if w is None and path is None:

            if candidate_1 < candidate_2:
                candidates.remove(candidate_2)
            else:
                candidates.remove(candidate_1)
            print("will call now disambiguation for candidates {}".format(candidates))
            return create_disambiguation_example(candidates, wall_locations)
        else:
            status = "indoubt"



        return (status, w, path, candidate_1, candidate_2, candidates, disambiguation_trace)



