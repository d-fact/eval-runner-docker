package ca.uwaterloo.cs.jgrok.io;

import java.io.*;
import java.util.StringTokenizer;

import ca.uwaterloo.cs.jgrok.fb.*;

public class RSFFileReader implements FactbaseReader {
    
    public RSFFileReader() {}
    
    public Factbase read(String dataFile)
        throws FileNotFoundException {
        NodeSet nSet;
        EdgeSet eSet;
        TupleSet tSet;
        Factbase factbase;
        
        factbase = new Factbase(dataFile);
        
        try {
            int lineNum;
            int tokCount;
            
            File file;
            String line;
            String name;
            
            StringBuffer buffer;
            BufferedReader reader;
            StringTokenizer tokenizer;
            
            file = new File(dataFile);
            buffer = new StringBuffer();
            reader = new BufferedReader(new FileReader(file));
            
            lineNum = 1;
            while((line = reader.readLine()) != null) {
                if(line.length() == 0) {
                    lineNum++;
                    continue;
                }
                
                tokenizer = new StringTokenizer(line);
                tokCount = tokenizer.countTokens();
                
                if(tokCount == 3) {
                    name = tokenizer.nextToken();        
                    
                    eSet = factbase.getEdgeSet(name);
                    if(eSet == null) {
                        eSet = new EdgeSet(name);
                        factbase.addSet(eSet);
                    }
                    eSet.add(tokenizer.nextToken(),
                             tokenizer.nextToken());
                } else if(tokCount == 2) {
                    name = tokenizer.nextToken();
                    
                    nSet = factbase.getNodeSet(name);
                    if(nSet == null) {
                        nSet = new NodeSet(name);
                        factbase.addSet(nSet);
                    }
                    nSet.add(tokenizer.nextToken());
                } else if(tokCount > 3) {
                    buffer.delete(0, buffer.length());
                    name = tokenizer.nextToken();
                    
                    tSet = factbase.getSet(name);
                    if(tSet == null) {
                        tSet = new TupleSet(name);
                        factbase.addSet(tSet);
                    }
                    
                    String[] elems = new String[tokCount - 1];
                    for(int i = 0; i < elems.length; i++) {
                        elems[i] = tokenizer.nextToken();
                    }
                    tSet.add(elems);
                } else {
                    System.err.println(">> File: " + dataFile +
                                       " line: " + lineNum +
                                       " not processed");
                    System.err.println(line);
                }
                
                lineNum++;
            }
            
            reader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        
        return factbase;
    }
}
