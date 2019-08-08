import random
from utils import create_json_spec, convert_json_actions_to_world_format
import nlp_helpers
from samples2LTL.experiment import start_experiment
from samples2LTL.run_solver_tests import disambiguate
from samples2LTL.utils.SimpleTree import Formula
from samples2LTL.utils.Traces import Trace
from world import World
import pdb
import json
import os
import logging
from copy import deepcopy

def create_candidates(nl_utterance, context, example):

    test_world = World(context, json_type=2)
    (emitted_events, pickup_locations, collection_of_negative, all_locations) = test_world.execute_and_emit_events(
        example)

    hints = nlp_helpers.get_hints_from_utterance(nl_utterance)
    relevant_locations = nlp_helpers.get_locations_from_utterance(nl_utterance)

    hintsWithLocations = {}
    for hint in hints:
        if hint == "dry":
            hintsWithLocations[hint] = hints[hint]
            continue
        for l in pickup_locations:
            hintsWithLocations["{}_{}_{}".format(hint, l[0], l[1])] = hints[hint]
    #hintsWithLocations = {"{}_{}_{}".format(hint, l[0], l[1]) : hints[hint] for hint in hints for l in pickup_locations}
    if len(hintsWithLocations) > 0:
        maxHintsWithLocations = max(hintsWithLocations.values())
        minHintsWithLocations = min(hintsWithLocations.values())
    else:
        maxHintsWithLocations = 0
        minHintsWithLocations = 0
    middleValue = (maxHintsWithLocations + minHintsWithLocations) / 2

    atLocationsHints = {"at_{}_{}".format(loc[0],loc[1]): middleValue for loc in relevant_locations}
    hintsWithLocations.update(atLocationsHints)
    os.makedirs("data", exist_ok=True)
    create_json_spec(file_name="data/exampleWithHints.json", emitted_events=emitted_events, hints=hintsWithLocations,
                     pickup_locations=pickup_locations, all_locations=all_locations,
                     negative_sequences=collection_of_negative)


    collection_of_candidates = start_experiment(experiment_specification="data/exampleWithHints.json")


    return collection_of_candidates

def update_candidates(old_candidates, path, decision, world):
    print("--+--+ old candidates are {}".format(old_candidates))
    collection_of_candidates = []
    if int(decision) == 0:
        formula_value = True
    elif int(decision) == 1:
        formula_value = False
    else:
        raise ValueError("got user decision different from 0 or 1, the value was {}".format(decision))

    print("-- ++ -- path is {}".format(path))
    print(world)
    converted_path = convert_json_actions_to_world_format(deepcopy(world), path)
    print(world)

    print("converted path is {}".format(converted_path))
    print("formula value should be {}".format(formula_value))
    (emitted_events, _,_,_) = world.execute_and_emit_events(converted_path)
    print("-- -- -- -- emitted_events are {}".format(emitted_events))
    trace = Trace.create_trace_from_events_list(emitted_events)
    print("trace is {}".format(trace))
    for candidate_formula in old_candidates:
        f = Formula.convertTextToFormula(candidate_formula)

        if trace.evaluateFormulaOnTrace(f) == formula_value:
            collection_of_candidates.append(candidate_formula)


    return collection_of_candidates


def create_disaumbiguation_example(candidates):

    candidate_1 = candidates[0]
    candidate_2 = candidates[1]
    print("================\n{}, {}, {}, {}".format(candidate_1, type(candidate_1), candidate_2, type(candidate_2)))
    w, path = disambiguate(candidate_1, candidate_2, 3, 6)


    return (w, path, candidate_1, candidate_2)



