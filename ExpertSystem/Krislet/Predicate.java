import java.util.ArrayList;

/**
 * This represents a statement that evaluates to true or false, either in the consequent or antecedent of a rule
 */
public abstract class Predicate{
    /**
     * determine whether a predicate is true
     *
     * @param facts the facts in the agents knowledge base
     * @param rules the rules in the agents knowledge base
     * @param m_memory the agent's memory object
     * @return true if the predicate holds, otherwise false
     */
    public abstract boolean evaluateTruth(ArrayList<SimplePredicate> facts, ArrayList<Rule> rules, Memory m_memory);
}