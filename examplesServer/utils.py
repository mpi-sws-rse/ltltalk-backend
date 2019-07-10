import constants
import json
import pdb


DEFAULT_NUM_FORMULAS = 10
DEFAULT_START_DEPTH = 2
DEFAULT_MAX_DEPTH = 4

def create_json_spec(file_name, emitted_events, hints, locations, num_formulas = DEFAULT_NUM_FORMULAS, start_depth = DEFAULT_START_DEPTH,
                     max_depth = DEFAULT_MAX_DEPTH):
    with open(file_name, "w") as exampleJsonFile:
        example_info = {}
        literals = constants.STATE_EVENTS
        for loc in locations:
            pdb.set_trace()
            literals += constants.PICKUP_EVENTS_PER_LOCATION[loc]
        example_info["literals"] = literals
        example_info["number-of-formulas"] = num_formulas
        example_info["start-depth"] = start_depth
        example_info["max-depth-of-formula"] = max_depth
        example_info["operators"] = constants.OPERATORS
        example_info["hints"] = [[h, hints[h]] for h in hints]
        example_info["positive"] = [";".join( [ ",".join([ e for e in timestep_events ]) for timestep_events in emitted_events] )]
        json.dump(example_info, exampleJsonFile)
