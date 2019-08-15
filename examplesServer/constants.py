UP = "up"
DOWN = "down"
LEFT = "left"
RIGHT = "right"
PICK = "pick"
MOVE = "move"
DRY = "at_dry"

DIRECTIONS = [UP, DOWN, LEFT, RIGHT]

WIDTH = 12
HEIGHT = 9

COLORS = ["red", "green", "blue", "yellow"]
COLOR_CODES = {"red": 1, "green": 2, "blue": 3, "yellow":4, "x": 0}
SHAPES = ["square", "circle", "triangle"]
SHAPE_CODES = {"square": 1, "circle": 2, "triangle": 3, "x": 0}
QUANTIFIERS = ["one","two", "every"]
ACTION_CODES = {MOVE: 0, PICK: 1}
DIRECTION_CODES = {LEFT: 0, RIGHT: 1, UP: 2, DOWN: 3}
numbersToWords = {1: "one", 2: "two", 3: "three", -1: "every"}

HINTS_CUTOFF_VALUE = 0.15



# x is sa symbol for don't care
PICKUP_EVENTS = ["{}_{}_{}_{}_item".format(PICK, quantifier, color, shape)
                for quantifier in QUANTIFIERS
                for color in COLORS+["x"]
                for shape in SHAPES+["x"]]

STATE_EVENTS = [DRY]

AT_EVENTS_PER_LOCATION ={(i,j): "at_{}_{}".format(i,j) for i in range(WIDTH) for j in range(HEIGHT)}

PICKUP_EVENTS_PER_LOCATION = {}
for x in range(WIDTH):
    for y in range(HEIGHT):
        for p in PICKUP_EVENTS:
            if (x,y) in PICKUP_EVENTS_PER_LOCATION:
                PICKUP_EVENTS_PER_LOCATION[(x,y)].append(p+"_"+str(x)+"_"+str(y))
            else:
                PICKUP_EVENTS_PER_LOCATION[(x, y)] = [(p + "_" + str(x)+"_"+str(y))]



EVENTS = PICKUP_EVENTS + STATE_EVENTS

SYNONYMS = {"every": ["all"], PICK: ["grab", "collect", "take"],
            "one": ["1", "single", "individual", "the"], "two": ["2"], "three": ["3"]}
CONNECTED_WORDS = {"dry": ["water"], "water": ["dry"]}

ALL_SIGNIFICANT_WORDS = COLORS + SHAPES + ["dry"] + ["water"] + QUANTIFIERS + [syn for syns in SYNONYMS.values() for syn in syns ] + DIRECTIONS + [MOVE]
OPERATORS = [
    "E",
    "G",
    "&",
    "U",
    "S",
    "!"
  ]
