import java.util.ArrayList;

public abstract class Predicate{
    public abstract boolean evaluateTruth(ArrayList<SimplePredicate> facts, ArrayList<Rule> rules, Memory m_memory);
}