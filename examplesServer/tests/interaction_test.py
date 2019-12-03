import argparse
import os
import json
import pdb
from world import World
from encoding.utils.SimpleTree import Formula
import requests
from pytictoc import TicToc
from utils import unwind_actions
from encoding.utils.Traces import Trace
import csv
import logging
import constants

FLIPPER_URL = "http://localhost:5000"
TEST_SESSION_ID = "test"

CANDIDATES_GENERATION_TIMEOUT = 60
WAITING_FOR_A_QUESTION_TIMEOUT = 60

INIT_CANDIDATES_HEADER = "init_candidates_time"
NUM_INIT_CANDIDATES_HEADER = "num_initial_candidates"
NL_UTTERANCE_HEADER = "nl_utterance"
FORMULA_HEADER = "target_formula"
IS_FORMULA_FOUND_HEADER = "formula_is_found"
RESULT_FORMULA_HEADER = "result_formula"
NUM_QUESTIONS_ASKED_HEADER = "num_questions_asked"
AVERAGE_WAITING_FOR_QUESTIONS_HEADER = "average_question_waiting"
AVERAGE_DISAMBIGUATION_DURATION_HEADER = "average_disambiguation_duration"
NUM_DISAMBIGUATIONS_HEADER = "num_disambiguations"
NUM_ATTEMPTS_FOR_CANDIDATES_GENERATION_HEADER = "num_attempts_candidates_generation"
TEST_FILENAME_HEADER = "filename"
TEST_ID_HEADER = "test_id"
MAX_NUM_CANDIDATES_HEADER = "max_num_candidates"
STARTING_DEPTH_HEADER = "start_depth"

HEADERS = [
    TEST_ID_HEADER,
    MAX_NUM_CANDIDATES_HEADER,
    STARTING_DEPTH_HEADER,
    NL_UTTERANCE_HEADER,
    FORMULA_HEADER,
    INIT_CANDIDATES_HEADER,
    NUM_INIT_CANDIDATES_HEADER,
    NUM_ATTEMPTS_FOR_CANDIDATES_GENERATION_HEADER,
    NUM_QUESTIONS_ASKED_HEADER,
    AVERAGE_WAITING_FOR_QUESTIONS_HEADER,
    AVERAGE_DISAMBIGUATION_DURATION_HEADER,
    NUM_DISAMBIGUATIONS_HEADER,
    IS_FORMULA_FOUND_HEADER,
    RESULT_FORMULA_HEADER
]

main_log = logging.getLogger('main_logger')
logging.basicConfig(level=logging.INFO, format='%(message)s')


# ch = logging.StreamHandler()
# ch.setLevel(logging.DEBUG)
# main_log.addHandler(ch)


def flipper_session(test_def, max_num_init_candidates, starting_depth, questions_timeout, candidates_timeout):
    stats = {}
    server_num_disambiguations = 0
    server_disambiguations_stats = []

    nl_utterance = test_def["description"]
    world_context = test_def["context"]
    world = World(world_context, json_type=2)
    init_path = test_def["init-path"]
    target_formula = Formula.convertTextToFormula(test_def["target-formula"])

    candidate_spec_payload = {}
    candidate_spec_payload["context"] = json.dumps(world_context)
    candidate_spec_payload["query"] = json.dumps(nl_utterance)
    candidate_spec_payload["path"] = json.dumps(init_path)
    candidate_spec_payload["sessionId"] = TEST_SESSION_ID
    candidate_spec_payload["num-formulas"] = max_num_init_candidates
    candidate_spec_payload["starting-depth"] = starting_depth

    # try:
    #     r = requests.get(FLIPPER_URL + "/get-candidate-spec", params=candidate_spec_payload, timeout=candidates_timeout)
    # except requests.exceptions.Timeout:
    #     stats[INIT_CANDIDATES_HEADER] = "timeout"
    #     for h in HEADERS:
    #         if h not in stats:
    #             stats[h] = "/"
    #     return stats
    r = requests.get(FLIPPER_URL + "/get-candidate-spec", params=candidate_spec_payload)
    init_candidates_time = r.elapsed.total_seconds()
    json_response = r.json()

    if json_response["status"] == constants.UNKNOWN_SOLVER_RES:
        stats[INIT_CANDIDATES_HEADER] = "timeout"
        for h in HEADERS:
            if h not in stats:
                stats[h] = "/"
        return stats


    candidates = json_response["candidates"]
    main_log.info("init candidates are {}\n\n".format(candidates))

    num_initial_candidates = len(candidates)
    world_context = json_response["world"]
    world = World(world_context, json_type=2)
    disambiguation_path = json_response["path"]

    server_num_disambiguations += json_response["num_disambiguations"]
    server_disambiguations_stats += json_response["disambiguation_stats"]

    stats[STARTING_DEPTH_HEADER] = starting_depth
    stats[MAX_NUM_CANDIDATES_HEADER] = max_num_init_candidates
    stats[INIT_CANDIDATES_HEADER] = init_candidates_time
    stats[NUM_INIT_CANDIDATES_HEADER] = num_initial_candidates
    stats[NL_UTTERANCE_HEADER] = nl_utterance
    stats[NUM_ATTEMPTS_FOR_CANDIDATES_GENERATION_HEADER] = json_response["num_attempts"]
    stats[FORMULA_HEADER] = test_def["target-formula"]

    interaction_status = json_response["status"]

    actions = json_response["actions"]

    num_questions_asked = 0
    decision_update_durations = []
    while num_questions_asked < num_initial_candidates:

        converted_path = unwind_actions(actions)

        (emitted_events, _, _, _) = world.execute_and_emit_events(converted_path)

        all_relevant_literals = target_formula.getAllVariables()
        trace = Trace.create_trace_from_events_list(emitted_events, literals_to_consider=all_relevant_literals)
        user_decision = Trace.evaluateFormulaOnTrace(trace, target_formula)

        decision_update_payload = {"session-id": json.dumps(TEST_SESSION_ID), "sessionId": json.dumps(TEST_SESSION_ID),
                                   "decision": json.dumps(int(user_decision)), "context": json.dumps(world_context),
                                   "path": json.dumps(converted_path), "candidates": json.dumps(candidates),
                                   "actions": json.dumps(actions)}

        # try:
        #     decision_update_request = requests.get(FLIPPER_URL + "/user-decision-update",
        #                                            params=decision_update_payload, timeout=questions_timeout)
        # except requests.exceptions.Timeout:
        #     stats[AVERAGE_WAITING_FOR_QUESTIONS_HEADER] = "timeout"
        #     for h in HEADERS:
        #         if not h in stats:
        #             stats[h] = "/"
        #     return stats
        decision_update_request = requests.get(FLIPPER_URL + "/user-decision-update", params=decision_update_payload)
        decision_time = decision_update_request.elapsed.total_seconds()
        decision_update_durations.append(decision_time)

        result_decision_update = decision_update_request.json()



        num_questions_asked += 1
        status = result_decision_update["status"]

        if status == constants.UNKNOWN_SOLVER_RES:
            stats[AVERAGE_WAITING_FOR_QUESTIONS_HEADER] = "timeout"
            for h in HEADERS:
                if not h in stats:
                    stats[h] = "/"
            return stats

        candidates = result_decision_update["candidates"]
        server_num_disambiguations += result_decision_update["num_disambiguations"]
        server_disambiguations_stats += result_decision_update["disambiguation_stats"]
        main_log.info("decision update. remaining candidates are {}".format(candidates))

        if status == "ok":

            foundFormula = Formula.convertTextToFormula(candidates[0])
            stats[RESULT_FORMULA_HEADER] = str(foundFormula)
            if foundFormula == target_formula:
                stats[IS_FORMULA_FOUND_HEADER] = True
            else:
                stats[IS_FORMULA_FOUND_HEADER] = False

            stats[NUM_QUESTIONS_ASKED_HEADER] = num_questions_asked
            stats[NUM_DISAMBIGUATIONS_HEADER] = server_num_disambiguations

            try:
                stats[AVERAGE_DISAMBIGUATION_DURATION_HEADER] = sum(server_disambiguations_stats) / len(
                    server_disambiguations_stats)
            except ZeroDivisionError:
                stats[AVERAGE_DISAMBIGUATION_DURATION_HEADER] = "/"

            try:
                stats[AVERAGE_WAITING_FOR_QUESTIONS_HEADER] = sum(decision_update_durations) / len(
                    decision_update_durations)
            except ZeroDivisionError:
                stats[AVERAGE_WAITING_FOR_QUESTIONS_HEADER] = "/"
            break
        if status == constants.FAILED_CANDIDATES_GENERATION_STATUS:
            stats[AVERAGE_WAITING_FOR_QUESTIONS_HEADER] = "candidates generation failure"
            for h in HEADERS:
                if not h in stats:
                    stats[h] = "/"
            return stats

        world_context = result_decision_update["world"]
        world = World(world_context, json_type=2)

        actions = result_decision_update["actions"]

    return stats


def main():
    parser = argparse.ArgumentParser()

    parser.add_argument("--tests_definition_folder", dest="testsFolder")
    parser.add_argument("--output", dest="statsOutput", default="stats.csv")
    parser.add_argument("--num_repetitions", dest="numRepetitions", type=int, default=1)
    parser.add_argument("--num_init_candidates", dest="numInitCandidates", nargs='+', type=int, default=[3, 6, 10])
    parser.add_argument("--starting_depth", dest="startingDepth", type=int, nargs='+', default=[2, 3])
    parser.add_argument("--continue_test", dest="continueTest", action='store_true', default=False)
    parser.add_argument("--candidates_timeout", dest="candidatesTimeout", type=int,
                        default=CANDIDATES_GENERATION_TIMEOUT)
    parser.add_argument("--questions_timeout", dest="questionsTimeout", type=int,
                        default=WAITING_FOR_A_QUESTION_TIMEOUT)

    args, unknown = parser.parse_known_args()
    directory = args.testsFolder

    if args.continueTest:
        statsOpeningMode = "a"
    else:
        statsOpeningMode = "w"

    with open(args.statsOutput, statsOpeningMode) as csv_stats:
        headers = HEADERS
        writer = csv.DictWriter(csv_stats, fieldnames=headers)
        tests_already_covered = []

        if args.continueTest:
            with open(args.statsOutput) as csv_read_stats:
                reader = csv.DictReader(csv_read_stats)

                tests_already_covered = [row[TEST_ID_HEADER] for row in reader]
        else:

            writer.writeheader()

        for test_filename in os.scandir(directory):
            main_log.info("testing {}".format(test_filename.name))
            with open(test_filename) as test_file:
                test_def = json.load(test_file)
                for num_init_candidates in args.numInitCandidates:
                    main_log.info("\tnum candidates: {}".format(num_init_candidates))
                    for starting_depth in args.startingDepth:
                        main_log.info("\t\tstarting depth: {}".format(starting_depth))
                        for rep in range(args.numRepetitions):
                            main_log.info("\t\t\trepetition {}".format(rep))
                            test_id = test_filename.name + str(num_init_candidates) + str(starting_depth) + str(rep)
                            if not test_id in tests_already_covered:
                                stats = flipper_session(test_def, max_num_init_candidates=num_init_candidates,
                                                        starting_depth=starting_depth,
                                                        questions_timeout=args.questionsTimeout,
                                                        candidates_timeout=args.candidatesTimeout)
                                stats[TEST_ID_HEADER] = test_id
                                writer.writerow(stats)
                                main_log.info("\n")
            main_log.info("\n\n===\n\n")


if __name__ == '__main__':
    main()
