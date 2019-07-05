import pdb
class World:
    def __init__(self, worldDescriptionJson):
        self.width = int(worldDescriptionJson["width"])
        self.height = int(worldDescriptionJson["height"])
        self.wall = []
        self.water = []
        self.items_on_the_floor = {}
        self.robot_position = (int(worldDescriptionJson["robot"][0]), int(worldDescriptionJson["robot"][1]))
        self.items_on_robot = []

        for item in worldDescriptionJson["robot"][2]:
            self.items_on_robot.append((item[0], item[1]))

        for world_place in worldDescriptionJson["world"]:
            pos = (world_place[0], world_place[1])
            if world_place[2] == "wall":
                self.wall.append(pos)
            elif world_place[2] == "water":
                self.water.append(pos)
            elif world_place[2] == "item":
                found_item = (world_place[3], world_place[4])
                if pos in self.items_on_the_floor:
                    self.items_on_the_floor[pos].append(found_item)
                else:
                    self.items_on_the_floor[pos] = [found_item]


    def __repr__(self):
        s = ""

        for i in range(self.height,-1,-1):
            for j in range(self.width):
                pos = (j,i)
                if pos in self.water:
                    s += "w"
                elif pos in self.wall:
                    s += "b"
                elif pos in self.items_on_the_floor:
                    s += "i"
                else:
                    s += " "
            s +="\n"
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


