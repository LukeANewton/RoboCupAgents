import java.util.ArrayList;

public class SimplePredicate extends Predicate{
    private String predicate;

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