import pdb
from collections import defaultdict

class World:
    def __init__(self, worldDescriptionJson):
        self.width = int(worldDescriptionJson["width"])
        self.height = int(worldDescriptionJson["height"])
        self.wall = []
        self.water = []
        self.items_on_the_floor = defaultdict(lambda : defaultdict(int))
        self.robot_position = (int(worldDescriptionJson["robot"][0]), int(worldDescriptionJson["robot"][1]))
        self.items_on_robot = defaultdict(int)


        for item in worldDescriptionJson["robot"][2]:
            item_description = (item[0], item[1])
            self.items_on_robot[item_description] += 1



        for world_place in worldDescriptionJson["world"]:
            pos = (world_place[0], world_place[1])
            if world_place[2] == "wall":
                self.wall.append(pos)
            elif world_place[2] == "water":
                self.water.append(pos)
            elif world_place[2] == "item":
                found_item = (world_place[3], world_place[4])
                self.items_on_the_floor[pos][found_item] += 1



    def __repr__(self):
        s = ""

        items_on_floor_counter = 0
        dict_of_items_on_floor = {}
        for j in range(self.height,-1,-1):
            for i in range(self.width):
                pos = (i,j)

                if pos in self.items_on_the_floor:
                    if pos == self.robot_position:
                        s += "X"
                        dict_of_items_on_floor["X"] = pos
                    else:
                        s += str(items_on_floor_counter)
                        dict_of_items_on_floor[items_on_floor_counter] = pos
                        items_on_floor_counter += 1
                elif pos == self.robot_position:
                    s += "X"
                elif pos in self.water:
                    s += "w"
                elif pos in self.wall:
                    s += "b"


                else:
                    s += " "
            s +="\n"
        s += "\n"

        s += "items:\n"
        for item_label in dict_of_items_on_floor:
            pos = dict_of_items_on_floor[item_label]
            s += "{} === {}: {}\n".format(item_label, pos, dict(self.items_on_the_floor[pos]))
        s +="\nitems at robot:\n"
        if len(self.items_on_robot) == 0:
            s += "None\n"
        else:
            for item_description in self.items_on_robot:
                s += "{}: {}\n".format(item_description, self.items_on_robot[item_description])

        return s

    def move(self, direction):
        roboX = self.robot_position[0]
        roboY = self.robot_position[1]
        if direction == "left":
            if roboX == 0:
                raise ValueError("Can't move {} when robot is at the position {}".format(direction, self.robot_position))
            else:
                newPosition = (roboX-1, roboY)

        elif direction == "right":
            if roboX == self.width - 1:
                raise ValueError("Can't move {} when robot is at the position {}".format(direction, self.robot_position))
            else:
                newPosition = (roboX+1, roboY)

        elif direction == "up":
            if roboY == self.height - 1:
                raise ValueError("Can't move {} when robot is at the position {}".format(direction, self.robot_position))
            else:
                newPosition = (roboX, roboY+1)

        elif direction == "down":
            if roboY == 0:
                raise ValueError("Can't move {} when robot is at the position {}".format(direction, self.robot_position))
            else:
                newPosition = (roboX, roboY-1)

        if newPosition in self.wall:
            raise RuntimeError("Can't move into the wall")

        self.robot_position = newPosition


        return self.robot_position

    def pick(self, color_shape_pairs):
        new_items_on_robot = self.items_on_robot.copy()
        new_items_at_pos = self.items_on_the_floor[self.robot_position].copy()

        for item_description in color_shape_pairs:
            if new_items_at_pos[item_description] == 0:
                raise RuntimeError("Can not pick {} from {}".format(color_shape_pairs, self.robot_position))
            else:
                new_items_at_pos[item_description] -= 1
                new_items_on_robot[item_description] += 1

        self.items_on_robot = new_items_on_robot
        self.items_on_the_floor[self.robot_position] = new_items_at_pos





