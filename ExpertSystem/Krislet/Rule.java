import java.util.ArrayList;

public class Rule{
    ArrayList<Predicate> antecedent;
    SimplePredicate consequent;

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

    public boolean holds(ArrayList<SimplePredicate> facts, ArrayList<Rule> rules, Memory m_memory){
        for (Predicate p: antecedent) {
            if(!p.evaluateTruth(facts, rules, m_memory))
                return false;
        }
        return true;
    }
}
