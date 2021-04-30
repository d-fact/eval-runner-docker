package ca.uwaterloo.cs.jgrok.lib;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.io.FileNotFoundException;
import java.nio.file.Files;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.fb.*;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.io.*;
import ca.uwaterloo.cs.jgrok.io.ta.TAFileReader;

/**
 * <pre>
 *    void read (string dataFile)
 *    void getta(string dataFile)
 *    void getdb(string dataFile)
 * </pre>
 */
public class Getta extends Reader {

    public Getta() {
        name = "getta";
    }

    public Value invoke(Env env, Value[] vals) throws InvocationException {
        switch (vals.length) {
            case 1:
                String gettaPath = vals[0].stringValue();
                File file = new File(gettaPath);
                if (file.isDirectory()) {
                    try {
                        Files.find(java.nio.file.Paths.get(gettaPath), Integer.MAX_VALUE, (filePath, fileAttr) -> fileAttr.isRegularFile()).
                                forEach(name -> {
                                    try {
                                        String nameStr = name.toString();
                                        if (nameStr.endsWith(".ta")) {
                                            System.out.println(String.format("read ta file @ %s", name));
                                            FactbaseReader fbReader = new TAFileReader();
                                            load(env, fbReader, nameStr);
                                        }
                                    } catch (InvocationException e) {
                                        e.printStackTrace();
                                    }
                                });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return Value.VOID;
                } else if (file.isFile()) {
                    FactbaseReader fbReader = new TAFileReader();
                    return load(env, fbReader, gettaPath);
                }
        }
        return illegalUsage();
    }
}
