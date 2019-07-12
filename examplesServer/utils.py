import constants
import json
import pdb


DEFAULT_NUM_FORMULAS = 10
DEFAULT_START_DEPTH = 2
DEFAULT_MAX_DEPTH = 6

def create_json_spec(file_name, emitted_events, hints, pickup_locations, all_locations, negative_sequences, num_formulas = DEFAULT_NUM_FORMULAS,
                     start_depth = DEFAULT_START_DEPTH, max_depth = DEFAULT_MAX_DEPTH):
    with open(file_name, "w") as exampleJsonFile:
        example_info = {}
        literals = constants.STATE_EVENTS
        for loc in pickup_locations:
            literals += constants.PICKUP_EVENTS_PER_LOCATION[loc]
        for loc in all_locations:
            literals.append(constants.AT_EVENTS_PER_LOCATION[loc])
        example_info["literals"] = literals
        example_info["number-of-formulas"] = num_formulas
        example_info["start-depth"] = start_depth
        example_info["max-depth-of-formula"] = max_depth
        example_info["operators"] = constants.OPERATORS
        example_info["hints"] = [[h, hints[h]] for h in hints]
        positive = [";".join( [ ",".join([ e for e in timestep_events ]) for timestep_events in emitted_events] )]
        example_info["positive"] = positive
        negative = [";".join( [ ",".join([ e for e in timestep_events ])
                                for timestep_events in neg_emitted_events] )
                    for neg_emitted_events in negative_sequences]
        example_info["negative"] = negative


        json.dump(example_info, exampleJsonFile)
