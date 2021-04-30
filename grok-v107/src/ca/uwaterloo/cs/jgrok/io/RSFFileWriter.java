package ca.uwaterloo.cs.jgrok.io;

import java.io.*;
import java.util.Enumeration;

import ca.uwaterloo.cs.jgrok.fb.*;

public class RSFFileWriter implements FactbaseWriter {
    
    public RSFFileWriter() {}
    
    public void write(String fileName, Factbase factbase) throws IOException 
    {
        File	file = new File(fileName);
        
        if(!file.exists()) {
            file.createNewFile();
        } else {
            if(file.isDirectory()) {
                throw new IOException("File " + fileName + " is a directory");
            }
        }
        
        FileOutputStream fileOut;
        fileOut = new FileOutputStream(file);
        PrintWriter	writer  = new PrintWriter(fileOut);

        Enumeration<TupleSet> en1;
        Enumeration<EdgeSet>  en2;
        TupleSet set;
        EdgeSet eset;
        String	name;

        for (en1 = factbase.allSets(); en1.hasMoreElements(); ) {
            set  = en1.nextElement();
            name = set.getName();
            if(!name.startsWith("@")) {
                set.printRSF(name, writer);
        }	}
        
        for (en2 = factbase.allEdgeSets(); en2.hasMoreElements(); ) {
            eset = en2.nextElement();
            name = eset.getName();
            if(name.startsWith("@")) {
                eset.printRSF(writer);
        }	}
    }

}
