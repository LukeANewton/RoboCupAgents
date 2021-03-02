import java.util.ArrayList;

/**
 * This represents a predicate that can be placed in a rule's consequent or antecent, or as  afact in the knowledge base
 */
public class SimplePredicate extends Predicate{
    // the contents of the predicate
    private String predicate;

    /** Constructor */
    public SimplePredicate(String predicate) {
        this.predicate = predicate;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "SimplePredicate{" +
                "predicate='" + predicate + '\'' +
                '}';
    }

    public String getPredicate() {
        return predicate;
    }

    @java.lang.Override
    public boolean evaluateTruth(ArrayList<SimplePredicate> facts, ArrayList<Rule> rules, Memory m_memory){
        for (SimplePredicate fact: facts) {
            if (fact.getPredicate().equals(this.predicate))
                return true;
        }
        return false;
    }

    public boolean equals(Object object) {
        return this.getPredicate().equals(((SimplePredicate) object).getPredicate());
    }
}