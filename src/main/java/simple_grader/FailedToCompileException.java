package simple_grader;

public class FailedToCompileException extends Exception {
    private static final long serialVersionUID = 5651126580803066672L;

    public FailedToCompileException() {
        super("Failed to compile file");
    }
}
