from z3 import *
import pdb
try:
    from utils.SimpleTree import SimpleTree, Formula
    import encodingConstants
except:
    from encoding.utils.SimpleTree import SimpleTree, Formula
    from encoding import encodingConstants

class DagSATEncoding:
    """
    - D is the depth of the tree
    - lassoStartPosition denotes the position when the trace values start looping
    - traces is 
      - list of different recorded values (trace)
      - each trace is a list of recordings at time units (time point)
      - each time point is a list of variable values (x1,..., xk)
    """
    def __init__(self, D, testTraces, literals):
        
        defaultOperators = [encodingConstants.G, encodingConstants.F, encodingConstants.LNOT, encodingConstants.UNTIL, encodingConstants.LAND,encodingConstants.LOR, encodingConstants.IMPLIES, encodingConstants.X]
        unary = [encodingConstants.G, encodingConstants.F, encodingConstants.LNOT, encodingConstants.X, encodingConstants.ENDS]
        binary = [encodingConstants.LAND, encodingConstants.LOR, encodingConstants.UNTIL, encodingConstants.IMPLIES, encodingConstants.BEFORE, encodingConstants.STRICTLY_BEFORE]
        #except for the operators, the nodes of the "syntax table" are additionally the propositional variables 
        
        if testTraces.operators == None:
            self.listOfOperators = defaultOperators
        else:
            self.listOfOperators = testTraces.operators

        if 'prop' in self.listOfOperators:
            self.listOfOperators.remove('prop')


        self.unaryOperators = [op for op in self.listOfOperators if op in unary]
        self.binaryOperators = [op for op in self.listOfOperators if op in binary]

        
        
        self.solver = Optimize()
        self.formulaDepth = D

        self.literals = literals
        self.listOfVariables = self.literals
        self.variablesToIntMapping = {self.literals[i]: i for i in range(len(self.literals))}


        
        #traces = [t.traceVector for t in testTraces.acceptedTraces + testTraces.rejectedTraces]
        
        self.traces = testTraces



        #self.listOfVariables = [i for i in range(self.traces.numVariables)]
        
        
        
        
        #keeping track of which positions in a tree (and in time) are visited, so that constraints are not generated twice
#        self.visitedPositions = set()


    def getInformativeVariables(self):
        res = []
        res += [v for v in self.x.values()]
        res += [v for v in self.l.values()]
        res += [v for v in self.r.values()]


        return res
    """    
    the working variables are 
        - x[i][o]: i is a subformula (row) identifier, o is an operator or a propositional variable. Meaning is "subformula i is an operator (variable) o"
        - l[i][j]:  "left operand of subformula i is subformula j"
        - r[i][j]: "right operand of subformula i is subformula j"
        - y[i][tr][t]: semantics of formula i in time point t of trace tr
    """
    def encodeFormula(self, unsatCore=True, hintVariablesWithWeights = {'p':2}):
        self.operatorsAndVariables = self.listOfOperators + self.listOfVariables
        #print(self.operatorsAndVariables)
        print("the hints and their weights are {}".format(hintVariablesWithWeights))
        self.x = { (i, o) : Bool('x_'+str(i)+'_'+str(o)) for i in range(self.formulaDepth) for o in self.operatorsAndVariables }

        self.l = {(parentOperator, childOperator) : Bool('l_'+str(parentOperator)+'_'+str(childOperator))\
                                                 for parentOperator in range(1, self.formulaDepth)\
                                                 for childOperator in range(parentOperator)}
        self.r = {(parentOperator, childOperator) : Bool('r_'+str(parentOperator)+'_'+str(childOperator))\
                                                 for parentOperator in range(1, self.formulaDepth)\
                                                 for childOperator in range(parentOperator)}

        self.y = { (i, traceIdx, positionInTrace) : Bool('y_'+str(i)+'_'+str(traceIdx)+'_'+str(positionInTrace))\
                  for i in range(self.formulaDepth)\
                  for traceIdx, trace in enumerate(self.traces.acceptedTraces + self.traces.rejectedTraces)\
                  for positionInTrace in range(trace.lengthOfTrace)}
        
        
        #self.solver.set(unsat_core=unsatCore)

#        pdb.set_trace()

        self.exactlyOneOperator()       
        self.firstOperatorVariable()

        #self.FandGOnlyTopLevel()

        self.propVariablesSemantics()
         
        self.operatorsSemantics()
        self.noDanglingVariables()

        #positive traces should be accepted
        self.solver.add(And( [ self.y[(self.formulaDepth - 1, traceIdx, 0)] for traceIdx in range(len(self.traces.acceptedTraces))] ))

        #negative traces should be rejected
        self.solver.add(And( [ Not(self.y[(self.formulaDepth - 1, traceIdx, 0)]) for traceIdx in range(len(self.traces.acceptedTraces), len(self.traces.acceptedTraces+self.traces.rejectedTraces))] ))

        self.roughGrammarRestrictions()

        self.include_hint_variables_softly(hint_variables_with_weights=hintVariablesWithWeights)
       # pdb.set_trace()

        # self.solver.add_soft(self.x[(0,0)], weight=1)
        # self.solver.add_soft(self.x[(0, 1)], weight=5)
        
        
    
    def include_hint_variables_softly(self, hint_variables_with_weights):
        for prop_var in hint_variables_with_weights:
            self.solver.add_soft(
                Or([self.x[(i, prop_var)] for i in range(self.formulaDepth)]),
                weight=hint_variables_with_weights[prop_var])

            # debug: adding weights to operators
            # self.solver.add_soft(
            #     Or([self.x[(i, encodingConstants.UNTIL)] for i in range(self.formulaDepth)]),
            #     weight=10
            # )





    def propVariablesSemantics(self):
        for i in range(self.formulaDepth):
            for p in self.listOfVariables:
                for traceIdx, tr in enumerate(self.traces.acceptedTraces + self.traces.rejectedTraces):
                    self.solver.add(Implies(self.x[(i, p)],\
                                                          And([ self.y[(i,traceIdx, timestep)] if tr.traceVector[timestep][self.variablesToIntMapping[p]] == True else Not(self.y[(i, traceIdx, timestep)])\
                                                               for timestep in range(tr.lengthOfTrace)])))
                    
            

        
    
    def firstOperatorVariable(self):
        self.solver.add(Or([self.x[k] for k in self.x if k[0] == 0 and k[1] in self.listOfVariables]))


    def noDanglingVariables(self):
        if self.formulaDepth > 0:
            self.solver.add(
                And([
                    Or(
                        AtLeast([self.l[(rowId, i)] for rowId in range(i+1, self.formulaDepth)]+ [1]),
                        AtLeast([self.r[(rowId, i)] for rowId in range(i+1, self.formulaDepth)] + [1])
                    )
                    for i in range(self.formulaDepth - 1)]
                )
            )

    def FandGOnlyTopLevel(self):
        self.solver.add( And([
            Not(self.x[(i,o)]) for i in range(self.formulaDepth-1) for o in [encodingConstants.G]
        ]))
    
    def exactlyOneOperator(self):
            
            
            self.solver.add(And([\
                                              AtMost( [self.x[k] for k in self.x if k[0] == i] +[1])\
                                              for i in range(self.formulaDepth)\
                                              ])
            )
            
            self.solver.add(And([\
                                              AtLeast( [self.x[k] for k in self.x if k[0] == i] +[1])\
                                              for i in range(self.formulaDepth)\
                                              ])
            )
            
            if (self.formulaDepth > 0):
                self.solver.add(And([\
                                                Implies(
                                                    Or(
                                                        [self.x[(i, op)] for op in self.binaryOperators+self.unaryOperators]
                                                    ),
                                                    AtMost( [self.l[k] for k in self.l if k[0] == i] +[1])\
                    )
                                              for i in range(1,self.formulaDepth)\
                                              ])
            )
            
            if (self.formulaDepth > 0):
                self.solver.add(And([\
                                                Implies(
                                                    Or(
                                                        [self.x[(i, op)] for op in
                                                         self.binaryOperators + self.unaryOperators]
                                                    ),
                                                    AtLeast( [self.l[k] for k in self.l if k[0] == i] +[1])\
                                                    )
                                              for i in range(1,self.formulaDepth)\
                                              ])
            )

            if (self.formulaDepth > 0):
                self.solver.add(And([ \
                    Implies(
                        Or(
                            [self.x[(i, op)] for op in self.binaryOperators]
                        ),
                        AtMost([self.r[k] for k in self.r if k[0] == i] + [1]) \
                        )
                    for i in range(1, self.formulaDepth) \
                    ])
                    )

            if (self.formulaDepth > 0):
                self.solver.add(And([ \
                    Implies(
                        Or(
                            [self.x[(i, op)] for op in
                             self.binaryOperators]
                        ),
                        AtLeast([self.r[k] for k in self.r if k[0] == i] + [1]) \
                        )
                    for i in range(1, self.formulaDepth) \
                    ])
                    )

            if (self.formulaDepth > 0):
                self.solver.add(And([ \
                    Implies(
                        Or(
                            [self.x[(i, op)] for op in
                             self.unaryOperators]
                        ),
                        Not(
                            Or([self.r[k] for k in self.r if k[0] == i]) \
                        )
                    )
                    for i in range(1, self.formulaDepth) \
                    ])
                    )

            if (self.formulaDepth > 0):
                self.solver.add(And([ \
                    Implies(
                        Or(
                            [self.x[(i, op)] for op in
                             self.listOfVariables]
                        ),
                        Not(
                            Or(
                                Or([self.r[k] for k in self.r if k[0] == i]), \
                                Or([self.l[k] for k in self.l if k[0] == i])
                            )

                        )
                    )
                    for i in range(1, self.formulaDepth) \
                    ])
                    )

    def roughGrammarRestrictions(self):

        for i in range(self.formulaDepth):
            # left operator of U, E, G, F, B may only be a propositional variable
            allowedLeft =  self.listOfVariables + [k for k in [encodingConstants.LNOT] if k in self.listOfOperators]



            for op in [encodingConstants.UNTIL, encodingConstants.ENDS, encodingConstants.G, encodingConstants.F, encodingConstants.BEFORE, encodingConstants.STRICTLY_BEFORE]:
                if op in self.listOfOperators:
                    self.solver.add(
                        And([
                            Implies(
                                And(
                                    self.x[(i,op)],
                                    self.l[(i, left)]
                                ),
                                Or([
                                    self.x[(left, vv)] for vv in allowedLeft
                                ])
                            )
                            for left in range(i)
                        ])
                    )

            # right operator of U, G, F, B may only be a propositional variable
            allowedRight = self.listOfVariables + [k for k in [encodingConstants.LNOT] if k in self.listOfOperators]
            for op in [encodingConstants.UNTIL, encodingConstants.G, encodingConstants.F]:
                if op in self.listOfOperators:
                    self.solver.add(
                        And([
                            Implies(
                                And(
                                    self.x[(i,op)],
                                    self.r[(i, right)]
                                ),
                                Or([
                                    self.x[(right, vv)] for vv in allowedRight
                                ])
                            )
                            for right in range(i)
                        ])
                    )
                    # right operator of B may be a propositional variable, or another formula labeled by B
                    allowedRight = self.listOfVariables + [k for k in [encodingConstants.LNOT, encodingConstants.BEFORE, ] if k in self.listOfOperators]

                    if encodingConstants.BEFORE in self.listOfOperators:
                        special_restriction_B = And([
                            Implies(
                                And(
                                    self.x[(i, encodingConstants.BEFORE)],
                                    self.r[(i, right)]
                                ),
                                Or([
                                    self.x[(right, vv)] for vv in allowedRight
                                ])
                            )
                            for right in range(i)
                        ])
                        self.solver.add(
                            special_restriction_B
                        )

                    allowedRight = self.listOfVariables + [k for k in [encodingConstants.LNOT, encodingConstants.STRICTLY_BEFORE, ] if k in self.listOfOperators]

                    if encodingConstants.STRICTLY_BEFORE in self.listOfOperators:
                        special_restriction_S = And([
                            Implies(
                                And(
                                    self.x[(i, encodingConstants.STRICTLY_BEFORE)],
                                    self.r[(i, right)]
                                ),
                                Or([
                                    self.x[(right, vv)] for vv in allowedRight
                                ])
                            )
                            for right in range(i)
                        ])
                        self.solver.add(
                            special_restriction_S
                        )
             # boolean operators can't have prop variables as their left argument
            for op in [encodingConstants.LOR, encodingConstants.LAND]:
                if op in self.listOfOperators:
                    no_prop_vars_left_restriction = And([
                        Implies(
                            And(
                                self.x[(i, op)],
                                self.l[(i, left)]
                            ),
                            And([
                                Not(self.x[(left, vv)]) for vv in self.listOfVariables
                            ])
                        )
                        for left in range(i)
                    ])
                    self.solver.add(no_prop_vars_left_restriction)
            for op in [encodingConstants.LAND, encodingConstants.LOR]:
                if op in self.listOfOperators:
                    no_prop_vars_right_restriction = And([
                        Implies(
                            And(
                                self.x[(i, op)],
                                self.r[(i, right)]
                            ),
                            And([
                                Not(self.x[(right, vv)]) for vv in self.listOfVariables
                            ])
                        )
                        for right in range(i)
                    ])


                    self.solver.add(no_prop_vars_right_restriction)

            # the only argument allowed for negation operator (!) is a prop variable
            for op in [encodingConstants.LNOT]:
                if op in self.listOfOperators:
                    neg_only_with_prop_restriction = And([
                        Implies(
                            And(
                                self.x[(i, op)],
                                self.l[(i, left)]
                            ),
                            Or([
                                self.x[(left, vv)] for vv in self.listOfVariables
                            ])
                        )
                        for left in range(i)
                    ])

                    self.solver.add(neg_only_with_prop_restriction)

                    # the fact that a single proposition can not be a valid formula can be controled by setting a depth
                    # of a formula to value greater than 1







    def operatorsSemantics(self):


        for traceIdx, tr in enumerate(self.traces.acceptedTraces + self.traces.rejectedTraces):
            for i in range(1, self.formulaDepth):
                
                if encodingConstants.LOR in self.listOfOperators:
                    #disjunction
                     self.solver.add(Implies(self.x[(i, encodingConstants.LOR)],\
                                                            And([ Implies(\
                                                                           And(\
                                                                               [self.l[i, leftArg], self.r[i, rightArg]]\
                                                                               ),\
                                                                           And(\
                                                                               [ self.y[(i, traceIdx, timestep)]\
                                                                                ==\
                                                                                Or(\
                                                                                   [ self.y[(leftArg, traceIdx, timestep)],\
                                                                                    self.y[(rightArg, traceIdx, timestep)]]\
                                                                                   )\
                                                                                 for timestep in range(tr.lengthOfTrace)]\
                                                                               )\
                                                                           )\
                                                                          for leftArg in range(i) for rightArg in range(i) ]))
                                     )

                if encodingConstants.LAND in self.listOfOperators:
                      #conjunction
                     self.solver.add(Implies(self.x[(i, encodingConstants.LAND)],\
                                                            And([ Implies(\
                                                                           And(\
                                                                               [self.l[i, leftArg], self.r[i, rightArg]]\
                                                                               ),\
                                                                           And(\
                                                                               [ self.y[(i, traceIdx, timestep)]\
                                                                                ==\
                                                                                And(\
                                                                                   [ self.y[(leftArg, traceIdx, timestep)],\
                                                                                    self.y[(rightArg, traceIdx, timestep)]]\
                                                                                   )\
                                                                                 for timestep in range(tr.lengthOfTrace)]\
                                                                               )\
                                                                           )\
                                                                          for leftArg in range(i) for rightArg in range(i) ]))
                                     )
                     
                if encodingConstants.IMPLIES in self.listOfOperators:
                       
                      #implication
                     self.solver.add(Implies(self.x[(i, encodingConstants.IMPLIES)],\
                                                            And([ Implies(\
                                                                           And(\
                                                                               [self.l[i, leftArg], self.r[i, rightArg]]\
                                                                               ),\
                                                                           And(\
                                                                               [ self.y[(i, traceIdx, timestep)]\
                                                                                ==\
                                                                                Implies(\
                                                                                  self.y[(leftArg, traceIdx, timestep)],\
                                                                                  self.y[(rightArg, traceIdx, timestep)]\
                                                                                   )\
                                                                                 for timestep in range(tr.lengthOfTrace)]\
                                                                               )\
                                                                           )\
                                                                          for leftArg in range(i) for rightArg in range(i) ]))
                                     )
                if encodingConstants.LNOT in self.listOfOperators:
                      #negation
                     self.solver.add(Implies(self.x[(i, encodingConstants.LNOT)],\
                                                           And([\
                                                               Implies(\
                                                                         self.l[(i,onlyArg)],\
                                                                         And([\
                                                                              self.y[(i, traceIdx, timestep)] == Not(self.y[(onlyArg, traceIdx, timestep)])\
                                                                              for timestep in range(tr.lengthOfTrace)\
                                                                              ])\
                                                                          )\
                                                               for onlyArg in range(i)\
                                                               ])\
                                                           )
                                                   )
                if encodingConstants.G in self.listOfOperators:
                      #globally                
                     self.solver.add(Implies(self.x[(i, encodingConstants.G)],\
                                                           And([\
                                                               Implies(\
                                                                         self.l[(i,onlyArg)],\
                                                                         And([\
                                                                              self.y[(i, traceIdx, timestep)] ==\
                                                                              And([self.y[(onlyArg, traceIdx, futureTimestep)] for futureTimestep in tr.futurePos(timestep) ])\
                                                                              for timestep in range(tr.lengthOfTrace)\
                                                                              ])\
                                                                          )\
                                                               for onlyArg in range(i)\
                                                               ])\
                                                           )
                                                   )

                if encodingConstants.F in self.listOfOperators:
                      #finally                
                     self.solver.add(Implies(self.x[(i, encodingConstants.F)],\
                                                           And([\
                                                               Implies(\
                                                                         self.l[(i,onlyArg)],\
                                                                         And([\
                                                                              self.y[(i, traceIdx, timestep)] ==\
                                                                              Or([self.y[(onlyArg, traceIdx, futureTimestep)] for futureTimestep in tr.futurePos(timestep) ])\
                                                                              for timestep in range(tr.lengthOfTrace)\
                                                                              ])\
                                                                          )\
                                                               for onlyArg in range(i)\
                                                               ])\
                                                           )
                                                   )

                if encodingConstants.ENDS in self.listOfOperators:
                    # end in
                    self.solver.add(Implies(self.x[(i,encodingConstants.ENDS)],
                                    And([
                                        Implies(self.l[(i, onlyArg)],
                                                And([
                                                    self.y[(i,traceIdx, timestep)] == self.y[(onlyArg, traceIdx, tr.lengthOfTrace-1)]
                                                for timestep in range(tr.lengthOfTrace)]))
                                        for onlyArg in range(i)]
                                    )
                                            )
                                    )

                if encodingConstants.BEFORE in self.listOfOperators:
                    # p before q
                    self.solver.add(Implies(self.x[(i, encodingConstants.BEFORE)],
                                            And([
                                                Implies(
                                                    And([self.l[(i, leftArg)], self.r[(i, rightArg)]]),
                                                    #for all positions
                                                    And([
                                                        self.y[(i, traceIdx, timestep)] ==
                                                        Or([
                                                            And([self.y[(leftArg, traceIdx, nearFuture)], self.y[(rightArg, traceIdx, distantFuture)]])
                                                            for nearFuture in tr.futurePos(timestep) for distantFuture in tr.futurePos(nearFuture)
                                                        ])
                                                    for timestep in range(tr.lengthOfTrace)
                                                    ])
                                                )
                                            for leftArg in range(i) for rightArg in range(i)
                                            ])
                                            ))
                if encodingConstants.STRICTLY_BEFORE in self.listOfOperators:
                    # p strictly before q
                    self.solver.add(Implies(self.x[(i, encodingConstants.STRICTLY_BEFORE)],
                                            And([
                                                Implies(
                                                    And([self.l[(i, leftArg)], self.r[(i, rightArg)]]),
                                                    #for all positions
                                                    And([
                                                        self.y[(i, traceIdx, timestep)] ==
                                                        Or([
                                                            And([self.y[(leftArg, traceIdx, nearFuture)], self.y[(rightArg, traceIdx, distantFuture)]])
                                                            for nearFuture in tr.futurePos(timestep) for distantFuture in tr.futurePos(nearFuture)[1:]
                                                        ])
                                                    for timestep in range(tr.lengthOfTrace)
                                                    ])
                                                )
                                            for leftArg in range(i) for rightArg in range(i)
                                            ])
                                            ))
                  
                if encodingConstants.X in self.listOfOperators:
                      #next                
                     self.solver.add(Implies(self.x[(i, encodingConstants.X)],\
                                                           And([\
                                                               Implies(\
                                                                         self.l[(i,onlyArg)],\
                                                                         And([\
                                                                              self.y[(i, traceIdx, timestep)] ==\
                                                                              self.y[(onlyArg, traceIdx, tr.nextPos(timestep))]\
                                                                              for timestep in range(tr.lengthOfTrace-1)\
                                                                              ])\
                                                                          )\
                                                               for onlyArg in range(i)\
                                                               ])
                                             )
                                     )\

                     self.solver.add(
                        Implies(self.x[(i,encodingConstants.X)],\
                                            And([
                                                Implies(
                                                    self.l[(i, onlyArg)],
                                                    Not(self.y[(onlyArg, traceIdx, tr.lengthOfTrace-1)])
                                                )
                                            for onlyArg in range(i)])
                                            )
                                    )

                if encodingConstants.UNTIL in self.listOfOperators:
                    #until
                     self.solver.add(Implies(self.x[(i, encodingConstants.UNTIL)],\
                                                          And([ Implies(\
                                                                         And(\
                                                                             [self.l[i, leftArg], self.r[i, rightArg]]\
                                                                             ),\
                                                                         And([\
                                                                            self.y[(i, traceIdx, timestep)] ==\
                                                                            Or([\
                                                                                And(\
                                                                                    [self.y[(leftArg, traceIdx, futurePos)] for futurePos in tr.futurePos(timestep)[0:qIndex]]+\
                                                                                    [self.y[(rightArg, traceIdx, tr.futurePos(timestep)[qIndex])]]\
                                                                                    )\
                                                                                for qIndex in range(len(tr.futurePos(timestep)))\
                                                                                ])\
                                                                            for timestep in range(tr.lengthOfTrace)]\
                                                                             )\
                                                                         )\
                                                                for leftArg in range(i) for rightArg in range(i) ]))
                                     )
    def reconstructWholeFormula(self, model):
        return self.reconstructFormula(self.formulaDepth-1, model)

    def reconstructFormula(self, rowId, model):


        def getValue(row, vars):
            tt = [k[1] for k in vars if k[0] == row and model[vars[k]] == True]
            if len(tt) > 1:
                raise Exception("more than one true value")
            else:
                return tt[0]

        operator = getValue(rowId, self.x)
        
        if operator in self.listOfVariables:
            return Formula(operator)

        elif operator in self.unaryOperators:
            leftChild = getValue(rowId, self.l)
            return Formula([operator, self.reconstructFormula(leftChild, model)])
        elif operator in self.binaryOperators:
            leftChild = getValue(rowId, self.l)
            rightChild = getValue(rowId, self.r)
            return Formula([operator, self.reconstructFormula(leftChild, model), self.reconstructFormula(rightChild, model)])
        
    
        
      
