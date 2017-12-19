import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Roman Golchin (romagolchin@gmail.com)
 */
public class ParserRule {

    public ParserRule(String lhs, List<Alternative> alternatives) {
        this.lhs = lhs;
        this.alternatives = alternatives;
    }

    public void setParams(Set<String> params) {
        this.params = params;
    }

    public void setReturnValues(Set<String> returnValues) {
        this.returnValues = returnValues;
    }

    private String lhs;

    private List<Alternative> alternatives;

    Set<String> params;

    Set<String> returnValues;

    static class Alternative {
        public Alternative(List<Application> rhs, Map<String, Integer> labels, String code) {
            this.rhs = rhs;
            this.labels = labels;
            this.code = code;
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
        public Application(int tokenId, String elem, String args) {
            this.tokenId = tokenId;
            this.elem = elem;
            this.args = args;
        }
        int tokenId;
        String elem;
        String args;

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
}
