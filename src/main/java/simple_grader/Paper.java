package simple_grader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public final class Paper implements Closeable {

    ArrayList<String> studentAnswers = new ArrayList<String>();
    private static final long WAITTIME = 6;
    private static final String ERRORSTR = "ERROR";
    private boolean exitStatus = false;
    private double correct = 0;
    private double length = 0;
    private boolean ADMIN = false;
    private ProcessBuilder builder;
    private Process process;
    private BufferedWriter writer;
    private BufferedReader reader;

    public Paper(String fileName, boolean ADMIN) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("java", fileName);
        this.builder = builder;
        this.ADMIN = ADMIN;
    }

    public void answer(String answer) {
        if (answer == null) {
            return;
        }
        if (ADMIN)
            System.out.println(answer.toLowerCase());
        studentAnswers.add(answer.toLowerCase());
    }

    public void nullAnswer() {
        this.answer(ERRORSTR);
    }

    public String getError() {
        InputStream errorStream = process.getErrorStream();
        try {
            return new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "IOException: Failed to get error";
        }
    }

    public boolean isDead() {
        if (process == null)
            return true;
        return !process.isAlive();
    }

    public boolean isAlive() {
        if (process == null)
            return false;
        return process.isAlive();
    }

    public void kill() {
        if (process != null)
            process.destroyForcibly();
    }

    public void question(String question) throws ProcessEndedException, IOException {
        writer.write(question.toString());
        writer.write("\n");
        writer.flush();
        String answer = reader.readLine();
        this.answer(answer);

        if (this.isDead())
            throw new ProcessEndedException();
    }

    private void setExitStatus(int exitValue) {
        this.exitStatus = exitValue == 0;
    }

    public void start() throws IOException, UnsupportedOperationException, FailedToCompileException {
        process = builder.start();
        OutputStream stdin = process.getOutputStream();
        InputStream stdout = process.getInputStream();
        writer = new BufferedWriter(new OutputStreamWriter(stdin));
        reader = new BufferedReader(new InputStreamReader(stdout));
    }

    @Override
    public void close() throws IOException {
        if (reader != null)
            reader.close();
        if (writer != null && this.isAlive()) {
            writer.write("exit");
            writer.flush();
            writer.close();
        }
    }

    public void restart()
            throws IOException, UnsupportedOperationException, FailedToCompileException, InterruptedException {
        try {
            this.close();
        } catch (IOException e) {
            System.out.println("Warning: failed to close file to restart");
        }
        this.waitFor();
        this.start();
    }

    public void waitFor() throws InterruptedException {
        process.waitFor(WAITTIME, TimeUnit.SECONDS);
        if (this.isAlive()) {
            this.kill();
            System.out.println("File took too long to exit");
        }
        process.waitFor(WAITTIME, TimeUnit.SECONDS);
        if (this.isDead())
            this.setExitStatus(process.exitValue());
    }

    public boolean getExitStatus() {
        return exitStatus;
    }

    public double getScore() {
        return (double) ((int) (10000.0 * correct / length)) / 100.0;
    }

    public void grade(Test test) {

        length = test.getLength();
        if (studentAnswers.size() < length) {
            System.out.println("Warning: You did not answer every question\n");
        } else if (studentAnswers.size() > length) {
            System.out.println("Warning: You gave too many answers\n");
        }

        Object[] Questions = test.getQuestions();
        Object[] Answers = test.getAnswers();
        correct = 0;
        for (int i = 0; i < Questions.length; i++) {

            try {
                Object question = Questions[i];
                Object answer = Answers[i];

                if (ADMIN) {
                    System.out.println("Question : " + question.toString());
                    System.out.println("Answer : " + studentAnswers.get(i).toString());
                    System.out.println("Expected Answer : " + Answers[i].toString());
                    if (answer.toString().toLowerCase().equals(studentAnswers.get(i)))
                        correct++;
                } else {
                    System.out.print("Question " + i + " : " + studentAnswers.get(i).toString());
                    if (answer.toString().toLowerCase().equals(studentAnswers.get(i))) {
                        System.out.println(" | Correct √");
                        correct++;
                    } else {
                        System.out.println(" | Incorrect X");
                    }
                }

                System.out.println();

            } catch (IndexOutOfBoundsException e) {
                if (ADMIN)
                    System.out.println("Missing answer for question " + i + "\n");
            }
        }
    }
}