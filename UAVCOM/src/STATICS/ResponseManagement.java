package STATICS;

public class ResponseManagement {
    public static boolean CheckPeripheralResponse(String a, String b) {

        a = subString(a,6);
        b = subString(b, 6);

        a = removeAll(a,"0", "");
        b = removeAll(b,",", "");

        // Compare the two byte arrays
        return a.equals(b);
    };
    
    // helper for reader supervisor, why wouldn't their job be easy?!
    public static boolean CheckResponse_STRICT(String a, String b) {
        if(a.trim().equals(b)) return true;
        return false;
    };

    // loose checks
    public static boolean CheckResponse_LOOSE(String a, String b) {
        if(a.contains(b)) return true;
        return false;
    };

    private static String subString(String str, int index) {
        return str.substring(index);
    };

    private static String removeAll(String str, String chr, String repl) {
        return str.replaceAll(chr, repl);
    }
};