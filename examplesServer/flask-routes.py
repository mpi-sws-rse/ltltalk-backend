import pdb
from flask import Flask, request
from flask_cors import CORS
import json
from world import World
from candidatesCreation import create_candidates, update_candidates, create_disaumbiguation_example

app = Flask(__name__)
CORS(app)


@app.route('/')
def hello_world():
    print(request.args)
    return "Hello world"

@app.route('/get-candidate-spec')
def candidate_spec():
    nl_utterance = request.args.get("query")
    example = json.loads(request.args.get("path"))

    context = json.loads(request.args.get("context"))
    sessionId = request.args.get("sessionId")
    world = World(context, json_type=2)
    print(nl_utterance, world, example)

    candidates = create_candidates(nl_utterance, context, example)
    answer = {}
    if len(candidates) == 0:
        answer["status"] = "failed"
    elif len(candidates) == 1:
        answer["status"] = "ok"
    elif len(candidates) > 1:
        print(candidates)
        answer["status"] = "indoubt"
        disaumbiguation_world, disaumbiguation_path, candidate_1, candidate_2 = create_disaumbiguation_example(candidates)
        answer["world"] = disaumbiguation_world.export_as_json()
        formatted_path = []
        # not sure if it is necessary, but probably does not hurt: setting the first step to be the move to the init
        # position
        formatted_path.append({"action": "path",
                               "x": str(disaumbiguation_world.robot_position[0]),
                               "y": str(disaumbiguation_world.robot_position[1]),
                               "color": "null",
                               "shape": "null",
                               "possible": "true"
                               })

        for step in disaumbiguation_path:



            if step[0] == "move":
                disaumbiguation_world.move(step[1])
                formatted_path.append({"action":"path",
                                       "x":str(disaumbiguation_world.robot_position[0]),
                                       "y": str(disaumbiguation_world.robot_position[1]),
                                       "color": "null",
                                       "shape": "null",
                                       "possible": "true"
                                       })
            elif step[0] == "pick":
                for item_desc in step[1:]:
                    for _ in range(item_desc[0]):
                        formatted_path.append({
                            "action":"pickitem",
                            "x": str(disaumbiguation_world.robot_position[0]),
                            "y": str(disaumbiguation_world.robot_position[1]),
                            "color": item_desc[1],
                            "shape": item_desc[2],
                            "possible":"true"
                        })
                        disaumbiguation_world.pick([(item_desc[1], item_desc[2])])

        answer["path"] = formatted_path





        answer["candidates"] = [str(c) for c in candidates]
        answer["disambiguation-candidate-1"] = str(candidate_1)
        answer["disambiguation-candidate-2"] = str(candidate_2)
    answer["sessionId"] = sessionId
    return answer




@app.route('/user-decision-update')
def user_decision_update():
    decision = request.args.get("decision")
    sessionId = request.args.get("sessionId")
    candidates = json.loads(request.args.get("candidates"))
    path = json.loads(request.args.get("path"))

    context = json.loads(request.args.get("context"))
    world = World(context)
    updated_candidates = update_candidates(candidates, path, decision, world)

    ## dummy response
    answer = {}
    if len(updated_candidates) == 0:
        answer["status"] = "failed"
    elif len(updated_candidates) == 1:
        answer["status"] = "ok"
    elif len(updated_candidates) > 1:
        answer["status"] = "indoubt"
        answer["world"] = context
        answer["path"] = [["move", "left"], ["move", "right"], ["move", "down"], ["pick", ["yellow", "square"], ["green", "circle"]]]
    answer["sessionId"] = sessionId
    answer["candidates"] = updated_candidates
    return answer
