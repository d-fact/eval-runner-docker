package ca.uwaterloo.cs.jgrok.lib;

import java.util.Enumeration;
import java.io.FileNotFoundException;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.io.*;
import ca.uwaterloo.cs.jgrok.io.ta.MysqlReader;

/**
 * <pre>
 *    void read (string dataFile)
 *    void getta(string dataFile)
 *    void getdb(string dataFile)
 * </pre>
 */
public class Getmysql extends Reader {
    
    public Getmysql() {
        name = "getmysql";
    }
    
    public Value invoke(Env env, Value[] vals) throws InvocationException 
    {
		String fileName = "";

		switch (vals.length) {
		case 1:
			fileName = vals[0].stringValue();
		case 0:
			FactbaseReader	fbReader = new MysqlReader();
        
			return load(env, fbReader, fileName);
		}
		return illegalUsage();
    }
}
