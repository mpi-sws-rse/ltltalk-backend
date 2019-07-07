UP = "up"
DOWN = "down"
LEFT = "left"
RIGHT = "right"
PICK = "pick"
MOVE = "move"

COLORS = ["red", "green", "blue"]
SHAPES = ["square", "circle", "triangle"]
QUANTIFIERS = ["single", "every"]

# x is sa symbol for don't care
PICKUP_EVENTS = ["{}_{}_{}_{}_item".format(PICK, quantifier, color, shape)
                for quantifier in QUANTIFIERS
                for color in COLORS+["x"]
                for shape in SHAPES+["x"]]
STATE_EVENTS = ["dry"]
EVENTS = PICKUP_EVENTS + STATE_EVENTS

SYNONYMS = {"every": ["all"], "single": ["a", "one", "individual", "the"], PICK:["grab", "collect", "take"]}
ALL_SIGNIFICANT_WORDS = COLORS + SHAPES + ["dry"] + QUANTIFIERS + [syn for syns in SYNONYMS.values() for syn in syns ]
OPERATORS = [
    "E",
    "G",
    "&",
    "U",
    "|",
    "S"
  ]
