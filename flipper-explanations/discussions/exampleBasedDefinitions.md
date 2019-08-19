# Definitions based on examples

The setup differs from classical Flipper/sempre-interactive: instead of providing 
a definition, users give an example when Flipper can't parse their utterance.
Having only one example makes generalization much more difficult. Therefore,
we want recover the formula that the user had in mind through interaction with the user.

## Pseudocode

```
 hyperparameters: number of candidate specifications n
 input: natural language utterance u, example trace t
 
1: h = createHints(u)
2: C = getCandidateSpecifications(h, t, n)
3: w = getDistinguishingWorld(C)
4: t_d = USER_INPUT(w)
5: C = updateCandidates(C, t_d)
6: while |C| > 1:
7:     w', t' = distinguishingTrace(C[0], C[1])
8:     decision = USER_INPUT(t')
9:     C = updateCandidates(decision, C)
```
- Line 1 takes the utterance and creates a set of hints from that utterance.
These hints are propositional variables recognized in the utterance. NOT IMPLEMENTED
- Line 2 comes up with n candidate specifications based on the examples t
and hints h. Parameter n should be large enought to ensure the correct specification
is among the candidates, yet small enough to make further disambiguation between the 
candidates quick. Implemented in [this repository](https://gitlab.mpi-sws.org/gavran/flipper-ltl-language/tree/hintsLearning).  
(The implementation is still missing a check for semantic equivalence: we don't want to have two candidates that are exactly the same)
- Line 3 uses a SAT solver to create the world for which there is a trace modelling each
of the candidate specifications and *it is likely* that the trace user would show would only
model one candidate specification (and the ones implied by it). NOT IMPLEMENTED
- Line 4 receives a new trace, user's input for the world w and line 5 updates
the set of candidate specifications according to t_d.
- Even if Line 3 did a good job creating such a world for which user's new example
t_d would distinguish between as many specifications as possible, that is not enough.
Assume `f` is the specification we are looking for and `g` is another candidate 
specification. Furthermore, it holds that `f => g`. The only way to distinguish
between those two candidates is by means of a *negative example*, a trace
that models `g`, but does not model `f`. Lines 6-9 are doing that. A trace
is shown to the user and the user has to pick whether it is modelling his intuition or not.
Thus, in the worst case, one candidate specification is eliminated per each step. NOT IMPLEMENTED 

## getCandidateSpecification 
... 

## getDistinguishingWorld

The goal is to find a world for which the user would likely show us an example
(trace) that would be consistent with only one candidate specifications. 
This is not always possible (a problem with implications, as described in the previous paragraph).
Furthermore, we can't assume which traces the user will pick (will it be the shortest? or some other?). 
The final problem is that we don't really want to calculate *for all traces modelling f_i, they are not modelling f_j*, 
but an approximation of that. In this case, we want the trace found by a SAT solver as 
the one modelling `f_i` not to be a model for `f_j`.  

```
parameters: increase in length of candidate traces inc
input: candidate specifications f_1, f_2,..., f_n
    l = 1
    repeat
        SAT: find world w and witness traces t_1,..., t_n of length up to l such that
            hard constraints: t_i models f_i
            soft constraints: t_i does not model f_j, for j != i
        if such world does not exist
            l +=  inc
    until world is found
``` 

The choice of parameter `inc` is important: if it is too small, due to hard constraints,
the system might create such a world in which every spec could be accomplished as soon as possible,
but at a cost of non-distiguishability between them. On the other hand, if `inc` is too large,
the witness traces will not be the ones the user would choose (following the intuition that the user
will likely show the shortest traces).


## distinguishingTrace

Given two candidate specifications (`f_i` and `f_j`) we want to find a world and a candidate 
trace that models one, but not both.
A SAT solver needs to find world `w` and a trace `t` such that either
`t` models `f_i` and does not model `f_j` or vice versa.