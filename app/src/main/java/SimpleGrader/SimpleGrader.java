package SimpleGrader;

import java.io.IOException;
import java.util.HashMap;

import SimpleGrader.resource_converter.TestCases;

public class SimpleGrader {
    private static boolean ADMIN = false;
    private static HashMap<String, Test> tests;
    private static int[] bender = { 1003700, 810111, 361667, 99027, 14666, 861, 650000 };

    private static void test(Paper paper, Test test)// TODO: check for timeout
            throws IOException, InterruptedException, FailedToCompileException {
        paper.start();

        Object[] questions = test.getQuestions();

        for (int i = 0; i < questions.length; i++) {
            Object question = questions[i];
            try {
                try {
                    paper.question(question.toString());
                } catch (Exception e) {
                    String err = paper.getError();
                    if (err.length() != 0)
                        System.out.println("\n" + err);
                    paper.nullAnswer();
                    paper.restart();
                    throw e;
                }
            } catch (ProcessEndedException e) {
                System.out.println("Warning: file has unexpectedly closed, moving to next question");
            } catch (IOException e) {
                System.out.println("Warning: Issue communicating with file, moving to next question");
            }
        }

        try {
            paper.close();
        } catch (IOException e) {
            System.out.println("Warning: Issue sending exit command");
        }

        System.out.println("\n-----< Waiting to exit >-----");
        paper.waitFor();

    }

    private static void vocalizeStart(String test, String file) {
        System.out.println("\n" + test + " TEST | " + file + ".java");
    }

    private static void runTest(String testName, String fileName)
            throws TestNotFoundException, IOException, InterruptedException, FailedToCompileException {
        if (!tests.containsKey(testName))
            throw new TestNotFoundException();

        Test test = tests.get(testName);
        Paper studentPaper = new Paper(fileName, ADMIN);

        vocalizeStart(testName, fileName);

        test(studentPaper, test);

        System.out.println("\n---------[ Grading ]---------\n");
        studentPaper.grade(test);
        System.out.println("-----------------------------\n");
        System.out.println("Grade: " + studentPaper.getScore() + "%");
        System.out.println("\n-----------------------------\n");
    }

    private static void help() {
        String str = "\n-----[ Avaliable Tests ]-----\n\n";
        for (HashMap.Entry<String, Test> test : tests.entrySet()) {
            str += test.getKey().toString() + "\n";
        }
        str += "\n-----------[ Help ]----------\n";
        str += "\nWhen making a homework file make sure to\nuse the scanner class to take in input.";
        str += "\nAny new line that is output by your file\nwill be considered an answer.";
        str += "\nAnswers are case insensitive.\n";
        str += "\nIn anycase, a template for any specific\nhomework should have been given to you.\n";
        str += "\n----------[ Usage ]----------\n";
        str += "\nUse this jar to test your homework .java files\n";
        str += "\nIf testing freezes, pressing Ctrl + C ,when\nfocused on the running terminal, will stop the process\n";
        str += "\nGive test name as the first argument (case insensitive)\nand fileName second\n";
        str += "\nEg. usage:\n";
        str += "\n\tjava -jar SimpleGrader.jar TestName MyRecursionFile.java";
        System.out.println(str);
    }

    private static String key() {
        char[] str = new char[7];
        for (int x = 0; x < str.length; x++) {
            int a = (bender[0] * x - bender[1] * x * x + bender[2] * x * x * x - bender[3] * x * x * x * x
                    + bender[4] * x * x * x * x * x - bender[5] * x * x * x * x * x * x + bender[6]) / 10000;
            str[x] = (char) a;
        }
        return new String(str);
    }

    public static void main(String[] args) throws IOException, InconsistentArrayLengthException, InterruptedException {
        HashMap<String, Test> testMap = TestCases.getMap(key());
        tests = testMap;

        String testName;
        String fileName;

        try {
            testName = args[0];
            fileName = args[1];
        } catch (IndexOutOfBoundsException e) {
            help();
            return;
        }

        if (args.length == 3 && args[2] != null && args[2].equals(key())) {
            ADMIN = true;
            System.out.println("\n----------< ADMIN >----------");
        }

        fileName = fileName.replace(".java", "");
        testName = testName.toUpperCase();
        try {
            try {
                runTest(testName, fileName + ".java");
            } catch (Exception e) {
                System.out.println("\n------[ ERROR MESSAGE ]------");
                if (ADMIN)
                    System.out.println("\n" + e);
                throw e;
            }
        } catch (FailedToCompileException e) {
            System.out.println("\n" + e.getMessage());
        } catch (TestNotFoundException e) {
            System.out.println("\nTest not found!");
            help();
        }
    }
}