UP = "up"
DOWN = "down"
LEFT = "left"
RIGHT = "right"
PICK = "pick"
MOVE = "move"

COLORS = ["red", "green", "blue"]
SHAPES = ["square", "circle", "triangle"]

# x is sa symbol for don't care
PICKUP_EVENTS = [ "picked_{}_{}_{}item".format(quantifier, color, shape)
                  for quantifier in ["single", "every"]
                  for color in COLORS+["x"]
                  for shape in SHAPES]+["x"]
STATE_EVENTS = ["dry"]
EVENTS = PICKUP_EVENTS + STATE_EVENTS