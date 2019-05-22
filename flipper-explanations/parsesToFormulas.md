# Converting a parse tree to a formula

Once a sequence of tokens is parsed into a parse tree, the question is: how is the formula created?

In the classes that extend SemanticFn, one needs to look at function `call`.
The most interesting things happen at ApplyFn --> there, formula `f` is created from list tree (function `Formulas.fromLispTree`).

In `Formulas.fromLispTree`, it is checked if explicit function is given ('var', 'limit', 'call').
If not, for one by one class the mode of formula is parsed.
If it does not belong to the class under consideration, the next class is tried.

Therefore, one needs to be careful not to have two modes for different classes.
