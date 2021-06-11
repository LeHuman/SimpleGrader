import java.util.Scanner;

public class Recursion {

    /**
     * This method recursivly counts the number of times the pattern {@code "JAC"}
     * appears in a given string
     * <p>
     * Allowed String methods are: subString length equals
     * 
     * @param str the string to check for a pattern
     * @return the number of times the pattern was found
     */
    public static int findJAC(String str) {
        String jac = "JAC";
        int stringJAC = jac.length();
        int amountJAC = 0;
        if (str.length() == 0) {
            return 0;
        }
        if (str.substring(0, stringJAC).equals(jac)) {
            amountJAC++;
        }
        return amountJAC;
    }

    /**
     * This main method is already setup to use a scanner to read user input
     * <p>
     * Only edit where your code should go
     * <p>
     * User Input:
     * <p>
     * Any line entered is string
     * <p>
     * Enter exit at anytime to exit the program
     * 
     * @param args String array passed when calling main
     */
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        String voice = "";
        while (!(voice = scan.nextLine()).equals("exit")) {
            System.out.println(findJAC(voice));
        }
        scan.close();
    }
}