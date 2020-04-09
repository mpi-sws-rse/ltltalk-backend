package edu.stanford.nlp.sempre;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import fig.basic.LispTree;
import fig.basic.LogInfo;
import fig.basic.Option;


public class DSLTLFormula extends Formula {
  public enum Mode {

    prop_var(":"),
      eventually(":eventually"),
      before(":before"),
    strictly_before(":strictly_before"),
    next(":next"),
    globally(":globally"),
      until(":until"),
    lnot(":neg"),
    lor(":or"),
    land(":and");


    private final String value;

    Mode(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return this.value;
    }
  };

  public static class Options {
    @Option(gloss = "symbol for operator eventually (F)")
    public String eventually;
    @Option(gloss = "symbol for operator globally (G)")
    public  String globally;
    @Option(gloss = "symbol for operator until (U)")
    public  String until;
    @Option(gloss = "symbol for operator next (X)")
    public  String next;
    @Option(gloss = "symbol for operator strictly before (S)")
    public  String strictly_before;
    @Option(gloss = "symbol for operator before")
    public  String before;
    @Option(gloss = "symbol for operator logical and")
    public  String land;
    @Option(gloss = "symbol for operator logical or")
    public  String lor;
    @Option(gloss = "symbol for operator logical negation")
    public  String lnot;

  }

  public final Mode mode;
  public final List<Formula> args;

  public DSLTLFormula(Mode mode, List<Formula> args) {
    this.mode = mode;
    this.args = args;
  }

  public static Options opts = new Options();

  @Override
  public String prettyString(){
      String s;

      if (this.mode.equals(Mode.prop_var)){

        List<String> ss = args.stream().map(arg -> arg.prettyString()).collect(Collectors.toList());
        s = String.join("_", ss);

      }
      else if (this.mode.equals(Mode.eventually)){
        s = opts.eventually+"("+this.getChildren().get(0).prettyString()+")";
      }
      else if (this.mode.equals(Mode.globally)){
        s = opts.globally+"("+this.getChildren().get(0).prettyString()+")";
      }
      else if (this.mode.equals(Mode.before)){
        s = opts.before + "(" + this.getChildren().get(0).prettyString()+", "+this.getChildren().get(1).prettyString()+")";
      }
      else if (this.mode.equals(Mode.strictly_before)){
        s = opts.strictly_before + "(" + this.getChildren().get(0).prettyString()+", "+this.getChildren().get(1).prettyString()+")";
      }
      else if (this.mode.equals(Mode.until)){
        s = opts.until + "(" + this.getChildren().get(0).prettyString()+", "+this.getChildren().get(1).prettyString()+")";
      }
      else if (this.mode.equals(Mode.land)){
        s = opts.land + "(" + this.getChildren().get(0).prettyString()+", "+this.getChildren().get(1).prettyString()+")";
      }
      else if (this.mode.equals(Mode.lor)){
        s = opts.lor + "(" + this.getChildren().get(0).prettyString()+", "+this.getChildren().get(1).prettyString()+")";
      }
      else if (this.mode.equals(Mode.lnot)){
        s = opts.lnot+"("+this.getChildren().get(0).prettyString()+")";
      }
      else
      {
        s = this.toString();
      }



      return s;

  }

  @Override
  public List<Formula>getChildren(){

	  return this.args;

  }

  public static Mode parseMode(String mode) {
    if (mode == null)
      return null;
    for (Mode m : Mode.values()) {
      // LogInfo.logs("mode string %s \t== %s \t!= %s", m.toString(), mode,
      // m.name());
      if (m.toString().equals(mode))
        return m;
    }
    if (mode.startsWith(":")) {
      LogInfo.logs("mode is: %s", mode);
      throw new RuntimeException("Unsupported DSLTLFormula mode.");
    }
    return null;
  }

  @Override
  public LispTree toLispTree() {
    LispTree tree = LispTree.proto.newList();
    tree.addChild(this.mode.toString());
    for (Formula arg : args)
      tree.addChild(arg.toLispTree());
    return tree;
  }

  @Override
  public void forEach(Function<Formula, Boolean> func) {
    if (!func.apply(this)) {
      for (Formula arg : args)
        arg.forEach(func);
    }
  }

  @Override
  public Formula map(Function<Formula, Formula> transform) {
    Formula result = transform.apply(this);
    if (result != null)
      return result;
    List<Formula> newArgs = Lists.newArrayList();
    for (Formula arg : args)
      newArgs.add(arg.map(transform));
    return new DSLTLFormula(this.mode, newArgs);
  }

  @Override
  public List<Formula> mapToList(Function<Formula, List<Formula>> transform, boolean alwaysRecurse) {
    List<Formula> res = transform.apply(this);
    if (res.isEmpty() || alwaysRecurse) {
      for (Formula arg : args)
        res.addAll(arg.mapToList(transform, alwaysRecurse));
    }
    return res;
  }

  @SuppressWarnings({ "equalshashcode" })
  @Override
  public boolean equals(Object thatObj) {
    if (!(thatObj instanceof DSLTLFormula))
      return false;
    DSLTLFormula that = (DSLTLFormula) thatObj;
    if (!this.mode.equals(that.mode))
      return false;
    if (!this.args.equals(that.args))
      return false;
    return true;
  }

  @Override
  public int computeHashCode() {
    int hash = 0x7ed55d16;
    hash = hash * 0xd3a2646c + mode.hashCode();
    hash = hash * 0xd3a2646c + args.hashCode();
    return hash;
  }
}
