package edu.stanford.nlp.sempre.interactive;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.stanford.nlp.sempre.Example;
import edu.stanford.nlp.sempre.Params;
import edu.stanford.nlp.sempre.ParserState;
import edu.stanford.nlp.sempre.Rule;
import edu.stanford.nlp.sempre.Trie;

public class SessionBeamParser extends InteractiveBeamParser {

  public SessionBeamParser(Spec spec) {
    super(spec);
  }

  @Override
  public synchronized void addRule(Rule rule) {
    //if (allRules.contains(rule))
      //return;
    
    SessionRule sRule = new SessionRule(rule);
    
    //allRules.add(rule);
    allRules.add(sRule);

    if (!rule.isCatUnary()) {
      trie.add(sRule);
    } else {
      interactiveCatUnaryRules.add(sRule);
    }
    System.out.printf("~~~ %s\n", allRules.stream().filter(r -> r.source != null).map(r -> r.source.uid + " : " + r).collect(Collectors.toList()));
  }
  
  @Override
  public ParserState parse(Params params, Example ex, boolean computeExpectedCounts) {
    String sessionId = ex.id;
    List<Rule> oldInteractiveCatUnaryRules = this.catUnaryRules;
    Trie oldTrie = this.trie;
    Set<Rule> oldAllRules = this.allRules;
    this.interactiveCatUnaryRules = new ArrayList<>();
    this.trie = new Trie();
    this.allRules = new LinkedHashSet<>();
    System.out.println("~~~ " + sessionId);
    for (Rule r : oldAllRules) {
      if (r.source == null || sessionId.equals(r.source.uid)) {
        if (r.source != null)
          System.out.printf("~~~ %s : %s", r.source.uid, r);
        this.addRule(r);
      } else
        System.out.printf("~~~ %s : %s", r.source.uid, r);
    }

    ParserState state = super.parse(params, ex, computeExpectedCounts);

    this.interactiveCatUnaryRules = oldInteractiveCatUnaryRules;
    this.trie = oldTrie;
    this.allRules = oldAllRules;

    return state;
  }

}
