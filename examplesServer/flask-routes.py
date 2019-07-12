import pdb
from flask import Flask, request
import json
from world import World
from candidatesCreation import create_candidates, update_candidates

app = Flask(__name__)



@app.route('/')
def hello_world():
    print(request.args)
    return "Hello world"

@app.route('/get-candidate-spec')
def candidate_spec():
    nl_utterance = request.args.get("query")
    example = json.loads(request.args.get("path"))
    context = json.loads(request.args.get("context"))
    sessionId = request.args.get("session-id")
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
        answer["status"] = "indoubt"
        answer["world"] = context
        answer["path"] = [["move", "left"], ["move", "right"], ["move", "down"], ["pick", ["yellow", "square"], ["green", "circle"]]]
    answer["session-id"] = sessionId
    answer["candidates"] = candidates
    return answer




@app.route('/user-decision-update')
def user_decision_update():
    decision = request.args.get("decision")
    sessionId = request.args.get("session-id")
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
    answer["session-id"] = sessionId
    answer["candidates"] = updated_candidates
    return answer
