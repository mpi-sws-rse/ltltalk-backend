package edu.stanford.nlp.sempre;

import fig.basic.LispTree;
import fig.basic.LogInfo;

/**
 * A ValueFormula represents an atomic value which is cannot be decomposed
 * into further symbols.  Simply a wrapper around Value.
 *
 * @author Percy Liang
 */
public class ValueFormula<T extends Value> extends PrimitiveFormula {
  public final T value;

  public ValueFormula(T value) { this.value = value; }
  public LispTree toLispTree() {
    if (value instanceof NameValue) return LispTree.proto.newLeaf(((NameValue) value).id);
    return value.toLispTree();
  }

  @Override
  public String prettyString(){
	  //LogInfo.logs("value is of class %s", this.value.getClass());
	  if (value.sortString().equals("items?color")){
		  return "has color";
	  }
	  else if (value.sortString().equals("all_rooms")){
		  return "rooms";
	  }
	  else
	  {
		  return this.value.contentString();
	  }
  }
  
  @SuppressWarnings({"equalshashcode"})
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ValueFormula<?> that = (ValueFormula<?>) o;
    if (!value.equals(that.value)) return false;
    return true;
  }

  public int computeHashCode() {
    return value.hashCode();
  }
}
