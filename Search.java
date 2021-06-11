import java.util.Scanner;

public class Search {
    public static int ternarySearch(int[] A, int x) {
        int left = 0;
        int right = A.length - 1;

        while (left <= right) {
            int leftMid = left + (right - left) / 3;
            int rightMid = right - (right - left) / 3;

            if (A[leftMid] == x) {
                return leftMid;

            } else if (A[rightMid] == x) {
                return rightMid;
            } else if (A[leftMid] > x) {
                right = leftMid - 1;
            } else if (A[rightMid] < x) {
                left = rightMid + 1;
            } else {
                left = leftMid + 1;
                right = rightMid - 1;
            }
        }
        return -1;
    }

    public static int[] arrayString(String str) {
        String[] arr = str.split(",");
        int[] nums = new int[arr.length];
        for (int i = 0; i < nums.length; i++) {
            nums[i] = Integer.parseInt(arr[i]);
        }
        return nums;
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        String voice = "";
        while (!voice.equals("exit")) {
            voice = scan.nextLine();
            int[] searchMe = arrayString(voice);
            voice = scan.nextLine();
            int key = Integer.parseInt(voice);
            System.out.println(ternarySearch(searchMe, key));
            voice = scan.nextLine();
        }
        scan.close();
    }
}