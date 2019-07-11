"""
a dummy file to test world functionality before it can be completely integrated
"""

from world import World
import json
import pdb
import nlp_helpers
from utils import create_json_spec
from samples2LTL.experiment import start_experiment

"""
problems: if the actions is a more complicated one, the system will find "underapproximation explanation", that specify
that parts of the picked items really are picked, but treats the rest as noise. How to fight that? Add all the picking 
subsets as additional negative examples?


"""


def main():


    with open("temp.json") as world_file:
        w = json.load(world_file)
        test_world = World(w)
        print(test_world)




        sequence_of_actions = []
        # sequence_of_actions += [("move", "right") for _ in range(5)]
        # sequence_of_actions += [("pick", [("red", "circle")])]

        sequence_of_actions += [("move", "right") for _ in range(5)]
        #sequence_of_actions += [("pick", [("red", "circle"),("red", "circle"),("blue", "circle"), ("green", "square"), ("green", "circle")])]
        sequence_of_actions += [("pick", [("red", "circle"), ("red", "circle"), ("blue", "circle"), ("green", "circle")])]


        (emitted_events, locations) = test_world.execute_and_emit_events(sequence_of_actions)
        hints = nlp_helpers.get_hints_from_utterance("pick me all circle items from [7,4]")
        hintsWithLocations = {hint+"_"+str(l) : hints[hint] for hint in hints for l in locations}
        print(hintsWithLocations)
        create_json_spec(file_name="data/exampleWithHints.json", emitted_events=emitted_events, hints = hintsWithLocations, locations=locations)

        start_experiment(experiment_specification = "data/exampleWithHints.json")


if __name__ == '__main__':
    main()