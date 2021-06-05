package org.clyze.doop.extractor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class Annotations {
    private HashMap<String, ClassAnnotations> table;

    public Annotations(String annotsFile) {
        table = new HashMap<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(annotsFile));
            System.out.println("Using annotations file: " + annotsFile);
            String line = reader.readLine();
            while (line != null) {
                StringTokenizer st = new StringTokenizer(line, "\t");
                String className = st.nextElement().toString(); //.replace(".java", "");
                Integer l = Integer.decode(st.nextElement().toString());

                if (st.hasMoreElements()) {

                    String pc = st.nextToken();

                    ClassAnnotations clsAnnots = table.get(className);
                    if (clsAnnots == null) {
                        clsAnnots = new ClassAnnotations();
                        table.put(className, clsAnnots);
                    }

                    clsAnnots.addAnnotation(l, pc);
                }

                line = reader.readLine();
            }
            reader.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public PresenceCondition getPC(String className, int line) {
        ClassAnnotations cls = table.get(className);
        PresenceCondition pc = null;
        if (cls != null) {
            pc = cls.getPC(line);
        }

        if (pc == null) {
            return PresenceCondition.getDefault(); 
        } else {
            return pc;
        }
    }
}

class ClassAnnotations {
    private HashMap<Integer, PresenceCondition> table;

    public ClassAnnotations() {
        table = new HashMap<>();
    }

    public void addAnnotation(int line, String pc)
    {
        table.put(line, new PresenceCondition(pc));
    }

    public PresenceCondition getPC(int pos) {
        return table.get(pos);
    }
}
