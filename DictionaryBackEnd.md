#Implementing the Dictionary
Relevant files are `Dictionary.java`, `DictionaryEntry.java`, and relevant functions are `InteractiveMaster.handleCommand` and `Grammar.readInducedGrammar`.
`Json.java` was used to handle converting objects into JSON strings and vice versa, particularly the functions `readValueHard` and `writeValueAsStringHard`.

##Generating the Dictionary. 
The list of induced rules is stored in `int-output/grammar.log.json`. The file is read by `Grammar.readInducedGrammar` and a list of JSON lines is returned by the function. 
The function is called from `Dictionary.compileDictionary`, and each JSON string is parsed into a rule, and the necessary information is filtered to create a DictionaryEntry. 
A list of DictionaryEntries is generated and converted into a JSON string by the function `Dictionary.jsonDictionary`.

##Communicating with front end
The communication with the front end is handled by `InteractiveMaster.java`. 
In `handleCommand`, the request for a dictionary by the front end is handled by the if clause `if (command.equals(":dictionary"))`. 
`Dictionary.jsonDictionary` is called and the returned String is put in the response that is sent back to front end (using `stats.put` defined in `QueryStats.java`). 

Read more about implementing Dictionary in the front end [here](https://github.com/akshalaniche/flipper/blob/master/documentation/explanations/DictionaryFrontEnd.md) 