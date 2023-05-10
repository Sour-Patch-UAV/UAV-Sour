package STATICS;

// static method for all classes to return their respective class name, instead of rewriting each time
// an effort to make console log messages easier to understand where they were called from
public class GetClassName {
    public static String THIS_CLASSNAME(Object obj, String msg) {
        return "Called from: " + obj.getClass().getName() + " | " + msg;
    }
}