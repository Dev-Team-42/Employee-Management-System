public class MakeEmployee {
    public static void main(String[] args) {
        String password = "employee";  // change if you want
        java.util.ArrayList<String> result = HashGenerator.eryption(password);
        System.out.println("Hash: " + result.get(0));
        System.out.println("Salt: " + result.get(1));
    }
}