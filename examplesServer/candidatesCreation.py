import random
from utils import create_json_spec
import nlp_helpers
from samples2LTL.experiment import start_experiment
from world import World
import pdb
import json

def create_candidates(nl_utterance, context, example):

    test_world = World(context)
    (emitted_events, pickup_locations, collection_of_negative, all_locations) = test_world.execute_and_emit_events(
        example)

    hints = nlp_helpers.get_hints_from_utterance(nl_utterance)
    relevant_locations = nlp_helpers.get_locations_from_utterance(nl_utterance)

    hintsWithLocations = {hint + "_" + str(l): hints[hint] for hint in hints for l in pickup_locations}
    maxHintsWithLocations = max(hintsWithLocations.values())
    minHintsWithLocations = min(hintsWithLocations.values())
    middleValue = (maxHintsWithLocations + minHintsWithLocations) / 2

    atLocationsHints = {"at_{}".format(str(loc)): middleValue for loc in relevant_locations}
    hintsWithLocations.update(atLocationsHints)
    print(hintsWithLocations)
    create_json_spec(file_name="data/exampleWithHints.json", emitted_events=emitted_events, hints=hintsWithLocations,
                     pickup_locations=pickup_locations, all_locations=all_locations,
                     negative_sequences=collection_of_negative)

    create_json_spec(file_name="data/exampleWithHints.json", emitted_events=emitted_events, hints=hintsWithLocations,
                     pickup_locations=pickup_locations, all_locations=all_locations,
                     negative_sequences=collection_of_negative)

    collection_of_candidates = start_experiment(experiment_specification="data/exampleWithHints.json")


    return collection_of_candidates

def update_candidates(old_candidates, path, decision, world):
    coin = random.randint(0, 2)
    collection_of_candidates = []
    if coin == 0:
        collection_of_candidates = []
    elif coin == 1:
        collection_of_candidates = old_candidates[0]
    else:
        collection_of_candidates = old_candidates

    return collection_of_candidates

# dummy implementation
def create_disaumbiguation_example(candidates):
    with open("temp.json") as world:
        w_json = json.load(world)
        path = [["move", "left"], ["move", "right"], ["move", "down"], ["pick", ["yellow", "square"], ["green", "circle"]]]
        return (w_json, path)


