# Problems with LTL and how to fix them

In the experiments with LTL as a target language, it turned out that many features of LTL are counterproductive
for specifying *what needs to be accomplished*. 
For a trace 
```
   noise; noise;wet,noise; wet; wet; dry
```

LTL descriptions can be meaningful, e.g. `F(dry)`, `F(wet U dry)`, but also completely useless, e.g. 
`F(dry) U wet`. There are a couple of things that turned out to be problematic
 - implications: the tool will come up with all sorts of trivially true implications (with false antecedent)
 - combinations of temporal operators: true-value of temporal operators is often justified later than 
 for what it is used. One example is `F(dry) U wet`
 
More examples of useless formulas, but satisfied by a trace are [here](positiveExamplesAnalysis) and [here](positiveExamplesAnalysis2).
In order to avoid such behaviors, we need a better language. I suggest a restriction of LTL and the introduction
of new operators.


## Domain specific LTL: proposal

The syntax of the language is given by

```
<a> : AP | not AP 
<b> : E <a>  | <a> Ux <a> | F<a>
<c> : <a> | <a> B <c>
<d> : <c> | <b>
<e> : <d> or <d> | <d> and <d> 
```

The semantics of the newly introduced operators E (ends), and B (before) Ux (modified U) is defined
on finite traces, with `last` being the last index of the trace

```
t,i |= E p   iff  t, last |= p
t,i |= p B q iff t,i |= F(p and F(q))
t, i|= f Ux g iff t,last |= g and t,j |= f, for all j
```


This language enables us to reason about the relative ordering of events, about the situation in the end, and about 
safety requirements.
(Optionally, B operator could be defined as *strictly before*. For experiments reasons, in the codebase its marked by `S`)

Kinds of properties that this language can express:

 - sequencing (happens before relation and exact order of events)
 - end conditions
 - safety until reaching the end condition (operator `Ux`)   

When running the tool for that grammar and the trace `safe; safe, goal1;wet,safe; safe, wet; safe, wet; safe,dry, goal2`,
and with hints `safe, goal1, goal2`, we get these results
- `(E goal2)`, 
- `(goal2 B goal2)`, 
- `(E dry)`, 
- `(dry B dry)`, 
- `(safe Ux goal2)`, 
- `(goal2 B safe)`,
 - `(safe B goal2)`, 
 - `(goal1 B goal2)`, 
 - `(dry B safe)`, 
 - `(safe & (E goal2))`, 
 - `(safe & (goal2 B safe))`, 
 - `(safe & (goal2 B goal2))`, 
 - `(safe & (safe Ux goal2))`, 
 - `(safe & (safe B goal2))`, 
 - `(safe & (goal1 B goal2))`
 
 These candidates - while not always correct - are at least all meaningful with respect to what the user
 might want to specify.
 
### Shortcomings of this restriction (and justifications)
 One cannot say things such as
 - `if wet, then eventually dry` : this makes a lot of sense in the RL/IRL/reactive setting, but for us, 
unless the world is very skewed, the user will want to talk about the goals, and not conditions
- `from some point have_hammer until nail` : we can change the available atomic propositions to express
 similar things, for example `take_hammer B nail and never drop_hammer`
 
### Usage in the context of Flipper
 To see what we can describe with this language in Flipper, we have to make a decision what the atomic propositions are.
 
 Propositions used at the moment:
```
picked_[every|single]_<color>_<shape>
picked[every|single]_<color>
picked_[every|single]_<shape>
picked_[every|single]
at_dry
```

Having only these propositions, our language can not differentiate between picking one or two items (if there are more items at the field)
Worse even, it can not distinguish from which field the item is picked up.
Thus, I suggest we change the used propositions to 
```
picked_[<number>|every]_<color>_<shape>_<location>
picked[<number>|every]_<color>_<location>
picked_[<number>|every]_<shape>_<location>
picked_[<number>|every]
at_<location>
at_dry
```

Possible commands one can express using the language:
- *Pick all red items from the location (2,3)*:  `E(picked_every_red_[2,3])`
- *First pick two green items from (3,4) and then all red from (1,1)*: `picked_2_green_[3,4] B picked_every_red_[1,1]`
- *Reach location (2,2) but never step in the water*: `dry Ux at_[2,2]` 
- *Pick one green circle from (2,4) and all items from (3,3), in any order*: `F(picked_1_green_circle_[2,4]) and F(pick_every_[3,3])`
- *Pick an item from (7,5) and take it to (2,2)*: `F(picked_1_[7,6]) & E(at_[2,2])`
- *Pick a green circle from (7,5) and a red triangle*: `E(picked_1_green_circle_[7,5]) & E(picked_1_red_triangle_[7,5])`