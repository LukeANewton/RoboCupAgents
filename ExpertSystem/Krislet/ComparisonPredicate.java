import java.util.ArrayList;

/**
 * This represents a predicate in a rule antecedent that measures some aspect of the agent's environemt
 */
public class ComparisonPredicate extends Predicate{
    // the object in the environment to measure a property of
    private String object;
    // the property of the object to check
    private ObjectAttribute attribute;
    // how we are going to compare the object property to a value
    private ComparisonOperator comparisonOperator;
    // the value to compare to the object property
    private float value;

    /** Constructor */
    public ComparisonPredicate(String object, ObjectAttribute attribute, ComparisonOperator comparisonOperator, float value) {
        this.object = object;
        this.attribute = attribute;
        this.comparisonOperator = comparisonOperator;
        this.value = value;
    }

    /** constuctor */ 
    public ComparisonPredicate(String object, ObjectAttribute attribute) {
        this.object = object;
        this.attribute = attribute;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "ComparisonPredicate{" +
                "object='" + object + '\'' +
                ", attribute='" + attribute + '\'' +
                ", comparisonOperator=" + comparisonOperator +
                ", value=" + value +
                '}';
    }

    @java.lang.Override
    public boolean evaluateTruth(ArrayList<SimplePredicate> facts, ArrayList<Rule> rules, Memory m_memory){
        if(attribute == ObjectAttribute.HEARD)
            return m_memory.getLastMessage().startsWith(this.object);

        ObjectInfo o = m_memory.getObject(this.object.replace("_", " "));
        if(o == null)
            return attribute == ObjectAttribute.NOTVISIBLE;

        float objectAttribute = 0;
        switch(attribute){
            case DIRECTION:
                objectAttribute = o.m_direction;
                break;
            case DISTANCE:
                objectAttribute = o.m_distance;
                break;
            case VISIBLE:
                return true;
            case NOTVISIBLE:
                return false;
        }

        boolean result = false;
        switch(comparisonOperator){
            case EQUAL:
                result = objectAttribute == value;
                break;
            case LESSTHAN:
                result = objectAttribute < value;
                break;
            case GREATERTHAN:
                result = objectAttribute > value;
                break;
            case LESSTHANEQUAL:
                result = objectAttribute <= value;
                break;
            case GREATERTHANEQUAL:
                result = objectAttribute >= value;
                break;
            case NOTEQUAL:
                result = objectAttribute != value;
                break;
        }
        return result;
    }
}