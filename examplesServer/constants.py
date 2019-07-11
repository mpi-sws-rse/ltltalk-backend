UP = "up"
DOWN = "down"
LEFT = "left"
RIGHT = "right"
PICK = "pick"
MOVE = "move"

WIDTH = 10
HEIGHT = 10

COLORS = ["red", "green", "blue"]
SHAPES = ["square", "circle", "triangle"]
QUANTIFIERS = ["one","two", "every"]
numbersToWords = {1: "one", 2: "two", 3: "three"}



# x is sa symbol for don't care
PICKUP_EVENTS = ["{}_{}_{}_{}_item".format(PICK, quantifier, color, shape)
                for quantifier in QUANTIFIERS
                for color in COLORS+["x"]
                for shape in SHAPES+["x"]]

STATE_EVENTS = ["dry"]

AT_EVENTS_PER_LOCATION ={(i,j): "at_{}_{}".format(i,j) for i in range(WIDTH) for j in range(HEIGHT)}

PICKUP_EVENTS_PER_LOCATION = {}
for x in range(WIDTH):
    for y in range(HEIGHT):
        for p in PICKUP_EVENTS:
            if (x,y) in PICKUP_EVENTS_PER_LOCATION:
                PICKUP_EVENTS_PER_LOCATION[(x,y)].append(p+"_"+str((x,y)))
            else:
                PICKUP_EVENTS_PER_LOCATION[(x, y)] = [(p + "_" + str((x, y)))]



EVENTS = PICKUP_EVENTS + STATE_EVENTS

SYNONYMS = {"every": ["all"], PICK: ["grab", "collect", "take"],
            "one": ["1", "single", "individual", "the"], "two": ["2"], "three": ["3"]}

ALL_SIGNIFICANT_WORDS = COLORS + SHAPES + ["dry"] + QUANTIFIERS + [syn for syns in SYNONYMS.values() for syn in syns ]
OPERATORS = [
    "E",
    "G",
    "&",
    "U",
    "|",
    "S"
  ]
