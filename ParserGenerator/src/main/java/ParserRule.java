import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Roman Golchin (romagolchin@gmail.com)
 */
public class ParserRule {

    private String lhs;

    private List<Alternative> alternatives;

    Set<String> params;

    Set<String> returnValues;

    public ParserRule(String lhs, List<Alternative> alternatives) {
        this.lhs = lhs;
        this.alternatives = alternatives;
    }

    public String getLhs() {
        return lhs;
    }

    public void setParams(Set<String> params) {
        this.params = params;
    }

    public void setReturnValues(Set<String> returnValues) {
        this.returnValues = returnValues;
    }

    public List<Alternative> getAlternatives() {
        return alternatives;
    }

    static class Alternative {
        public Alternative(List<Application> rhs, Map<String, Integer> labels, String code) {
            this.rhs = rhs;
            this.labels = labels;
            this.code = code;
        }

        public List<Application> getRhs() {
            return rhs;
        }

        public String getCode() {
            return code;
        }

        private List<Application> rhs;
        // maps label l to index of element in the `rhs` list
        Map<String, Integer> labels;
        private String code;

        @Override
        public String toString() {
            return "Alternative{" +
                    "rhs=" + rhs +
                    ", labels=" + labels +
                    ", code='" + code + '\'' +
                    '}';
        }
    }

    // element applied to args
    static class Application {
        public Application(int tokenId, String elem, String args, String label) {
            this.tokenId = tokenId;
            this.elem = elem;
            this.args = args;
            this.label = label;
        }
        int tokenId;
        String elem;
        String args;
        String label;

        public boolean isToken() { return  tokenId != Util.Constants.NONE;}

        @Override
        public String toString() {
            return "Application{" +
                    "tokenId=" + tokenId +
                    ", elem='" + elem + '\'' +
                    ", args='" + args + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ParserRule{" +
                "lhs='" + lhs + '\'' +
                ", alternatives=" + alternatives +
                ", params=" + params +
                ", returnValues=" + returnValues +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParserRule rule = (ParserRule) o;
        return Objects.equals(lhs, rule.lhs) &&
                Objects.equals(alternatives, rule.alternatives) &&
                Objects.equals(params, rule.params) &&
                Objects.equals(returnValues, rule.returnValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lhs, alternatives, params, returnValues);
    }
}
