import pdb
from flask import Flask, request
from flask_cors import CORS
import json
from world import World
from candidatesCreation import create_candidates, update_candidates, create_disambiguation_example
from utils import convert_path_to_formatted_path

try:
    from utils.SimpleTree import Formula
except:
    from samples2LTL.utils.SimpleTree import Formula


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
    wall_locations = world.get_wall_locations()
    print(nl_utterance, world, example)

    candidates = create_candidates(nl_utterance, context, example)

    answer = {}
    answer["sessionId"] = sessionId
    if len(candidates) == 0:
        answer["status"] = "failed"
    elif len(candidates) == 1:
        answer["status"] = "ok"
    elif len(candidates) > 1:
        print(candidates)
        answer["status"] = "indoubt"
        status, disambiguation_world, disambiguation_path, candidate_1, candidate_2 = create_disambiguation_example(candidates, wall_locations)
        if not status == "indoubt":
            answer[status] = status
            return answer
        print("disambiguation world is {}, disambiguation path is {} for candidate1 = {} and candidate2 = {}".format(disambiguation_world, disambiguation_path, candidate_1, candidate_2))
        answer["world"] = disambiguation_world.export_as_json()
        formatted_path = convert_path_to_formatted_path(disambiguation_path, disambiguation_world)
        print("formatted path is {}".format(formatted_path))
        answer["path"] = formatted_path





        answer["candidates"] = [str(c) for c in candidates]
        answer["disambiguation-candidate-1"] = str(candidate_1)
        answer["disambiguation-candidate-2"] = str(candidate_2)

    return answer




@app.route('/user-decision-update')
def user_decision_update():
    decision = request.args.get("decision")
    sessionId = request.args.get("sessionId")
    candidates = json.loads(request.args.get("candidates"))
    path = json.loads(request.args.get("path"))

    context = json.loads(request.args.get("context"))
    world = World(context, json_type=2)
    wall_locations = world.get_wall_locations()
    updated_candidates = update_candidates(candidates, path, decision, world)


    answer = {}
    answer["sessionId"] = sessionId
    answer["candidates"] = updated_candidates
    if len(updated_candidates) == 0:
        answer["status"] = "failed"
        return answer
    elif len(updated_candidates) == 1:
        answer["status"] = "ok"
        return answer
    elif len(updated_candidates) > 1:
        answer["status"] = "indoubt"

        converted_candidates = [Formula.convertTextToFormula(c) for c in updated_candidates]


        status, disambiguation_world, disambiguation_path, candidate_1, candidate_2 = create_disambiguation_example(
            converted_candidates, wall_locations=wall_locations)

        if not status == "indoubt":
            answer["status"] = status
            return answer
        else:

            answer["world"] = disambiguation_world.export_as_json()
            formatted_path = convert_path_to_formatted_path(disambiguation_path, disambiguation_world)
            answer["path"] = formatted_path
            answer["disambiguation-candidate-1"] = str(candidate_1)
            answer["disambiguation-candidate-2"] = str(candidate_2)
            return answer
