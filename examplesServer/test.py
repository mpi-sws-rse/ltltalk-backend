"""
a dummy file to test world functionality before it can be completely integrated
"""

from world import World
import json
import pdb

def main():
    with open("temp.json") as world_file:
        w = json.load(world_file)
        test_world = World(w)
        print(test_world)




        sequence_of_actions = []
        # sequence_of_actions += [("move", "right") for _ in range(5)]
        # sequence_of_actions += [("pick", [("red", "circle")])]

        sequence_of_actions += [("move", "right") for _ in range(5)]
        sequence_of_actions += [("pick", [("red", "circle"),("red", "circle"),("blue", "circle"), ("green", "square"), ("green", "circle")])]


        emitted_events = test_world.execute_and_emit_events(sequence_of_actions)
        print(emitted_events)
        print(test_world)

if __name__ == '__main__':
    main()