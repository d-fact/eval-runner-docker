package ca.uwaterloo.cs.jgrok.env;

import java.util.Scanner;
import java.io.StringReader;

public class Indent {
    public static String defaultIndent = "    ";
    
    public static String addIndent(String input) {
        return addIndent(input, defaultIndent);
    }
    
    public static String addIndent(String input, String indent) {
        if(indent == null) {
            return input;
        }
        
        StringBuffer buf = new StringBuffer();
        Scanner s = new Scanner(new StringReader(input)).useDelimiter("\\n");
        while(s.hasNextLine()) {
            buf.append(indent);
            buf.append(s.nextLine());
            if(s.hasNextLine()) {
                buf.append("\n");
            }
        }
        
        return buf.toString();
    }
}
