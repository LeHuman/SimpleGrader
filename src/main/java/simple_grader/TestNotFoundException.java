package simple_grader;

public class TestNotFoundException extends Exception {
    private static final long serialVersionUID = 75958540576736564L;

    public TestNotFoundException() {
        super("Test not found");
    }
}
