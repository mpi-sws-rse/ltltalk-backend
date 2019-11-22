from utils.Traces import Trace, ExperimentTraces
import glob

testTracesFolder ='traces/tests/'



def test_next_and_future():
    allFiles =glob.glob(testTracesFolder+'*') 
    for testFileName in allFiles:
        logging.debug(testFileName)
        if 'And' not in testFileName or '~' in testFileName:
            continue
        
        #acceptedTraces, rejectedTraces, availableOperators, expectedResult, depth = readTestTraceFile(testFileName, maxDepth)
        traces = ExperimentTraces()
        traces.readTracesFromFile(testFileName)
        
        
        for trace in traces.acceptedTraces + traces.rejectedTraces:
            logging.debug("trace: \n%s" % repr(trace))
            for currentPos in range(trace.lengthOfTrace):
                
                logging.debug("current position %d"%currentPos)
                logging.debug("next: "+str(trace.nextPos(currentPos)))
                logging.debug("future: %s\n"%str(trace.futurePos(currentPos)))
            logging.debug("=========\n\n")
        
if __name__ == "__main__":
    test_next_and_future()