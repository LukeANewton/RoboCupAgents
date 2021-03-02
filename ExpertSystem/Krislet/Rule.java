import java.util.ArrayList;

/**
 * This repesents a rule in the agent's knowledge base to be evaluated
 */
public class Rule{
    // the list of predicates in the rule's antecedent to evaluate
    ArrayList<Predicate> antecedent;
    // the consequent to add to the knowledge base if the antecent is true
    SimplePredicate consequent;

    /** Constructor */
    public Rule(ArrayList<Predicate> antecedent, SimplePredicate consequent) {
        this.antecedent = antecedent;
        this.consequent = consequent;
    }

    public ArrayList<Predicate> getAntecedent() {
        return antecedent;
    }

    public SimplePredicate getConsequent() {
        return consequent;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "Rule{" +
                "antecedent=" + antecedent +
                ", consequent=" + consequent +
                '}';
    }

    /**
     *
     * @param facts the list of facts in the agents knowledge base
     * @param rules the list of rules in the agents knowledge base
     * @param m_memory the memory of the agent
     * @return true if the entire antecedent evaluates to true, otherwise false
     */
    public boolean holds(ArrayList<SimplePredicate> facts, ArrayList<Rule> rules, Memory m_memory){
        for (Predicate p: antecedent) {
            if(!p.evaluateTruth(facts, rules, m_memory))
                return false;
        }
        return true;
    }
}
