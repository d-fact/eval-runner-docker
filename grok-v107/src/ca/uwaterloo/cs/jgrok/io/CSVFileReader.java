package ca.uwaterloo.cs.jgrok.io;

import java.io.*;
import java.util.ArrayList;

import ca.uwaterloo.cs.jgrok.fb.*;

public class CSVFileReader implements FactbaseReader {
    
    public CSVFileReader() {}
    
    public Factbase read(String dataFile)
        throws FileNotFoundException {
        TupleSet tSet;
        Factbase factbase;
        
        factbase = new Factbase(dataFile);
        tSet = new TupleSet("CSVDATA");
        factbase.addSet(tSet);
        
        try {
            File file;
            BufferedReader reader;
            
            file = new File(dataFile);
            reader = new BufferedReader(new FileReader(file));
            
            int lineNum = 0;
            String line = null;
            while((line = reader.readLine()) != null) {
                line = line.trim();
                
                if(line.length() > 0) {
                    String[] elems = separate(line);
                    tSet.add(elems);
                }
                lineNum++;
            }
            
            reader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        
        return factbase;
    }
    
    
    private String[] separate(String line) {
        int length;
        char prev, curr;
        
        prev = line.charAt(0);
        length = line.length();
        
        boolean quoted = false;
        ArrayList<String> list = new ArrayList<String>(7);
        
        int i = 0;
        int bgnIndex =-1;
        int endIndex =-1;
        while(i < length) {
            curr = line.charAt(i);
            
            switch(curr) {
            case ' ' :
            case '\t':
                if(!quoted) {
                    if(prev != ',') endIndex = i;
                    
                    i++;
                    while(i < length) {
                        if(line.charAt(i) == ' ' || line.charAt(i) == '\t') i++;
                        else break;
                    }
                    
                    if(line.charAt(i) != ',') i--;
                    else if(bgnIndex < 0) i--;
                    curr = line.charAt(i);
                }
                break;
                
            case '\"':
                if(!quoted) {
                    quoted = true;
                } else {
                    if(prev != '\\') {
                        quoted = false;
                    }
                }
                if(bgnIndex < 0) bgnIndex = i;
                break;
                
            case ',':
                if(!quoted) endIndex = i;
                break;
                
            default:
                if(bgnIndex < 0) bgnIndex = i;
                break;
            }
            
            if (0 <= bgnIndex && bgnIndex <= endIndex) {
                list.add(line.substring(bgnIndex, endIndex));
                bgnIndex = -1;
                endIndex = -1;
            } else if(-1 == bgnIndex && 0 <= endIndex) {
                list.add("");
                bgnIndex = -1;
                endIndex = -1;
            }
            
            prev = curr;
            i++;
        }
        
        if(0 <= bgnIndex && bgnIndex < length)
            list.add(line.substring(bgnIndex, length));
        
        String[] result = new String[list.size()];
        list.toArray(result);
        return result;
    }
}
