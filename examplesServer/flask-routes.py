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
    world = World(context)
    print(nl_utterance, world, example)

    ## dummy responses
    candidates = create_candidates(nl_utterance, context, example)
    answer = {}
    if len(candidates) == 0:
        answer["status"] = "failed"
    elif len(candidates) == 1:
        answer["status"] = "ok"
    elif len(candidates) > 1:
        print(candidates)
        answer["status"] = "indoubt"
        disaumbiguation_world, disaumbiguation_path = create_disaumbiguation_example(candidates)
        answer["world"] = disaumbiguation_world
        answer["path"] = disaumbiguation_path
        answer["candidates"] = [str(c) for c in candidates]
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
