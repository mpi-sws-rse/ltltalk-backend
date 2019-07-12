import constants
import nltk
nltk.download('punkt')
nltk.download('wordnet')
import re


from nltk.tokenize import word_tokenize
from nltk.stem import WordNetLemmatizer


def get_locations_from_utterance(nl_utterance):
    locations_strings = re.findall(r"(\d,[\n\t ]*\d)", nl_utterance)

    locations = []
    for loc in locations_strings:
        print(loc)
        try:
            location_pair = loc.split(',')
            locations.append((int(location_pair[0]), int(location_pair[1])))
        except:
            continue

    return locations

def get_hints_from_utterance(nl_utterance):


    """
    the function takes the natural language utterance as an input and tries to distill relevant propositional variables
(a subset of constants.EVENTS) with the weights attached to them. This is a crude implementation based on the appearences
of individual subwords.

    :param nl_utterance: string
    :return: dictionary {prop_variable: weight}
    """





    lemmatizer = WordNetLemmatizer()

    utterance_tokens = [lemmatizer.lemmatize(token) for token in word_tokenize(nl_utterance) if token in constants.ALL_SIGNIFICANT_WORDS]
    scores = {}
    for prop_variable in constants.EVENTS:
        score = 0
        list_of_descriptors = [lemmatizer.lemmatize(el) for el in prop_variable.split("_")[:-1] if not (el == "x" or el == "item")]

        for desc in list_of_descriptors:
            candidates = [desc]
            if desc in constants.SYNONYMS:
                candidates  += constants.SYNONYMS[desc]
            for candidate in candidates:
                if candidate in utterance_tokens:
                    score += 1
                    continue
        # denominator consists of number of tokens in the descriptor of prop_variable (to account for things not
        # mentioned in the utterance) and number of tokens in the utterance (to account for things mentioned in the
        # utterance but not in the propositional variable)
        score = score / (len(list_of_descriptors) + len(utterance_tokens))
        scores[prop_variable] = score



    max_dict_value = max(scores.values())
    second_max_value = max( [value for value in scores.values() if value < max_dict_value] )
    hints = {k : (1 + scores[k]) for k in scores if (scores[k] == max_dict_value or scores[k] == second_max_value)}

    return hints


