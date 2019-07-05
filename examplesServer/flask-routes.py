import pdb
from flask import Flask, request
import json
from world import World
app = Flask(__name__)


@app.route('/')
def hello_world():
    print(request.args)
    return "Hello world"

@app.route('/get-candidate-spec')
def candidate_spec():
    nl_utterance = request.args.get("utterance")
    example = request.args.get("example").split(";")
    context = json.loads(request.args.get("context"))
    world = World(context)
    print(nl_utterance, world, example)
    return "dummy"
