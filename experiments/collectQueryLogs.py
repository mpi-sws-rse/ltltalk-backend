import pdb
import argparse
import json
import csv


"""
the whole "special condition" part is becuase there were few mistakes in the experiment
 1. I didn't reset the log file after the tutorial. therefore, it contained their tutorial attempts. that's why we have startingPoints that describe where for each participant the tutorial stops
 2. we were short on time and then two participants had a break before continuing. That assigned them different session ids. THat's why I have artificial session ids now (P1, P2, P3)
 
 ---
 
 also, for it is changed for the second group:
 1. one participant continued experimenting even after he finished all the tasks (that's why now it is eliminated if it exceeds the count of his last query)
"""
#sessionIds = {"kamqupluqc":"P1", "4whvuab45y":"P2", "c70jstrl5l":"P2", "mrb7cylagj":"P3", "g8o4jp76a":"P3"}
#startingPoints = {"P1" : 334, "P3" : 210, "P2":345}

sessionIds = {"vmdwsrp3a4":"B1", "brv6imuw4t":"B2", "x1z4imvyyc":"B3", "70wnvrj0uk":"B4" }
startingPoints = {"B1" : 679, "B3" : 2000, "B2":2000, "B4":2000}

def transformIncomingJson(jsonInput, specialConditions = False):
    if jsonInput['q'].startswith("(:context") or jsonInput['q'].startswith("(:def"):
        return None
    if specialConditions == True:
        try:
            id = sessionIds[jsonInput["sessionId"]]
        except:
            print(jsonInput["sessionId"])
            print("unkonwn id")
            return None
        if jsonInput["count"] >= startingPoints[id]:
            return None
        else:
            jsonInput["sessionId"] = id
            return jsonInput
    else:
        return jsonInput
        
        
        
        
    

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--special_conditions", dest="specialConditions", action='store_true', default = False)
    parser.add_argument("--query_log_file", dest="queryLogFile", default= "groupA.log")
    parser.add_argument("--cleaned_output_file", dest = "outputFile")
    parser.add_argument("--statistics_file", dest = "statisticsFile", default = "statistics.csv")
    parser.add_argument("--group", dest = "group")
    
    args, unknown = parser.parse_known_args()
    
    specialConditions = args.specialConditions
    queryLogFile = args.queryLogFile
    outputFile = args.outputFile
    statisticsFile = args.statisticsFile
    group = args.group
    pdb.set_trace()
    
    if group == None:
        raise Exception("no group was given")
    
    lengthOfQueries = {}
    numberOfQueries = {}
    numberOfTokens = {}
    
    
    if outputFile == None:
        outputFile = "cleaned_"+queryLogFile
    with open(queryLogFile) as inputFile:
        with open(outputFile, "w") as outputFile:
            for line in inputFile:
                transformedJson = transformIncomingJson(json.loads(line), specialConditions) 
                if not  transformedJson == None:
                    
                    key = transformedJson["sessionId"]
                    
                    characterCount = len(transformedJson["q"]) - 7
                    tokenCount = len( transformedJson["q"].split()) - 1 
                    try:
                        
                        lengthOfQueries[key] += characterCount
                        numberOfTokens[key] += tokenCount
                        numberOfQueries[key] += 1
                    except: 
                        print("initializing "+str(key))
                        lengthOfQueries[key] = characterCount
                        numberOfQueries[key] = 1
                        numberOfTokens[key] = tokenCount
                    outputFile.write(json.dumps(transformedJson)+"\n")
    with open(statisticsFile, "a") as csvfile:
        writer = csv.writer(csvfile)
        for keyId in lengthOfQueries.keys():
            writer.writerow([keyId, group, numberOfQueries[keyId], lengthOfQueries[keyId], numberOfTokens[keyId] ])
                
    

if __name__ == '__main__':
    main()