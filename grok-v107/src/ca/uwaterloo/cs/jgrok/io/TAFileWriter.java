package ca.uwaterloo.cs.jgrok.io;

import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;

import ca.uwaterloo.cs.jgrok.fb.Factbase;
import ca.uwaterloo.cs.jgrok.fb.EdgeSet;
import ca.uwaterloo.cs.jgrok.fb.TupleSet;
import ca.uwaterloo.cs.jgrok.fb.Tuple;
import ca.uwaterloo.cs.jgrok.fb.IDManager;

public class TAFileWriter implements FactbaseWriter {
    
    public TAFileWriter() {}
    
    public void write(String fileName, Factbase factbase) throws IOException 
    {
        File file = new File(fileName);
        if(!file.exists()) {
            file.createNewFile();
        } else {
            if(file.isDirectory()) {
                throw new IOException("File " + fileName + " is a directory");
            }
        }
        
        FileOutputStream		fileOut = new FileOutputStream(file);
        PrintWriter				writer  = new PrintWriter(fileOut);
        Hashtable<String,EdgeSet> attributes = new Hashtable<String,EdgeSet>();

        Enumeration<TupleSet>	en1;
        Enumeration<EdgeSet>	en2;
        TupleSet				inherit;
        TupleSet				instance;
        TupleSet				set;
        EdgeSet					eset;
        String					name;
                 
        writer.println("SCHEME TUPLE :");
        writer.println();
        
        name = "$INHERIT";
        inherit = factbase.getSet(name);
        if (inherit != null) {
			inherit.printTA(name, writer);
			writer.println();
		}
		instance = factbase.getSet("$INSTANCE");
           
        for (en1 = factbase.allSets(); en1.hasMoreElements(); ) {
            set  = en1.nextElement();
            if (set != inherit && set != instance) {
				name = set.getName();
				if (name.length() > 0 && name.charAt(0) == '$') {
					name = name.substring(1);
					if (name.charAt(0) == '_') {
						name = name.substring(1);
					}
					set.printTA(name, writer);
		}	}	}
        
        writer.println();
        writer.println("SCHEME ATTRIBUTE :");
        writer.println();

        for (en2 = factbase.allEdgeSets(); en2.hasMoreElements(); ) {
            eset = en2.nextElement();
            name = eset.getName();
			if(name.length() > 0 && name.charAt(0) == '@') {
				putAttributes(name, eset, attributes);
		}	}
		
		printAttributes(attributes, true, writer);
		
        writer.println();
		writer.println("FACT TUPLE :");
         writer.println();
       
        if (instance != null) {
 			instance.printTA("$INSTANCE", writer);
	        writer.println();
 		}
      
        for (en1 = factbase.allSets(); en1.hasMoreElements(); ) {
            set  = en1.nextElement();
            name = set.getName();
            if(name.length() == 0 || (name.charAt(0) != '$' && name.charAt(0) != '@')) {
                set.printTA(name, writer);
        }	}
        
        writer.println();
        writer.println("FACT ATTRIBUTE :");
        writer.println();

        printAttributes(attributes, false, writer);
		
		attributes.clear();
		        
        writer.close();
    }
    
    private void putAttributes(String name, EdgeSet eset, Hashtable attributes)
    {
		int			size = eset.size();
		int			i;
		Tuple		tuple;
		String		id, value;
		EdgeSet		values;
		
		for(i = 0; i < size; i++) {
			tuple = (Tuple) eset.get(i);
			id    = IDManager.get(tuple.getDom());
			value = IDManager.get(tuple.getRng());

			values = (EdgeSet) attributes.get(id);
			if (values == null) {
				values = new EdgeSet(id);
				attributes.put(id, values);
			}
			values.add(name, value);
	}	}
	
	private void printAttributes(Hashtable attributes, boolean schema, PrintWriter writer)
	{
		Enumeration<EdgeSet>	en;
		EdgeSet					values;
		String					name;
		
		if (!schema) {
			values = (EdgeSet) attributes.get("$ROOT");
			if (values != null) {
				printAttributes(false, "$ROOT", values, writer);
		}	}
	
		for (en = attributes.elements(); en.hasMoreElements(); ) {
			values = en.nextElement();
			name   = values.getName();
			if (name.charAt(0) == '$') {
				if (name.equals("$ROOT")) {
					continue;
				}
				if (!schema) {
					continue;
				}
				name = name.substring(1);
			} else {
				if (schema) {
					continue;
			}	}
			printAttributes(schema, name, values, writer);
	}	}
	
	private void printAttributes(boolean schema, String name, EdgeSet values, PrintWriter writer)
	{
		int						i, size;
		Tuple					tuple;
		int						rng;
		String					value;
		
		if (name.charAt(0) == '_') {
			writer.print('(');
			writer.print(name.substring(1));
			writer.print(')');
		} else {
			writer.print(name);
		}
		writer.println(" {");
		size = values.size();
		for (i = 0; i < size; ++i) {
			tuple = values.get(i);
			writer.print("  ");
			name = IDManager.get(tuple.getDom());
			writer.print(name.substring(1));	// Drop the @
			rng = tuple.getRng();
			if (!schema || rng != 0) {
				value = IDManager.get(rng);
				writer.print("=");
				writer.print(value);
			}
			writer.println();
		}
		writer.println("}");
	}	
}
