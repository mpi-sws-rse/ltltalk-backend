import random
def create_candidates(nl_utterance, context, example):
    coin = random.randint(0,2)
    collection_of_candidates = []
    if  coin == 0:
        collection_of_candidates = []
    elif coin == 1:
        collection_of_candidates = ["E(at_(7,4))"]
    else:
        collection_of_candidates = ["E(at_(7,4))", "S(at_(7,4), picked_red_triangle_item_(8,5)"]

    return collection_of_candidates

def update_candidates(old_candidates, path, decision, world):
    coin = random.randint(0, 2)
    collection_of_candidates = []
    if coin == 0:
        collection_of_candidates = []
    elif coin == 1:
        collection_of_candidates = old_candidates[0]
    else:
        collection_of_candidates = old_candidates

    return collection_of_candidates


