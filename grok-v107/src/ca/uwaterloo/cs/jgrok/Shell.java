package ca.uwaterloo.cs.jgrok;

import java.io.*;
import org.gnu.readline.*;

import ca.uwaterloo.cs.jgrok.env.Env;
import ca.uwaterloo.cs.jgrok.interp.*;
import ca.uwaterloo.cs.jgrok.util.Timing;

/**
 * 
 * This class shows the usage of the readline wrapper. It will read lines 
 * from standard input using the GNU-Readline library. You can use the
 * standard line editing keys. You can also define application specific 
 * keys. Put this into your ~/.inputrc (or into whatever file $INPUTRC
 * points to) and see what happens if you press function keys F1 to F3:
 * <pre>
 *$if ReadlineTest
 *"\e[11~":        "linux is great"
 *"\e[12~":        "jikes is cool"
 *"\e[13~":        "javac is slow"
 *$endif
 *</pre>
 *
 * If one argument is given to ReadlineTest, a private initialization file
 * is read. If a second argument is given, the appropriate library is
 * loaded.
 */
public class Shell {
    private Env env;
    private Timing timing;
    private Interp interp;
    private StringBuffer buffer;
    private ByteArrayInputStream byteStream;
    
    private Value value;
    private ScriptUnitNode unit;
    private SyntaxTreeNode node;
    
    Shell() {}
    
    void debugEvaluate(String[] args) {
        String line;
        initEnv(args);
        
        try {
            unit.debugEvaluate(env, QdbCode.c);
        } catch(Exception e) {
            System.err.println(e.getMessage());
            System.exit(0);
        }
        
        if(initReadline()) {
            while(true) {
                try {
                    line = Readline.readline(Env.promptText);
                    
                    if(line != null) {
                        if(isKeyword(line)) {
                            qdbHelp(line);
                            continue;
                        } else {
                            int code = QdbCode.get(line);
                            if(code >= 0) {
                                try {
                                    unit.debugEvaluate(env, code);
                                } catch(Exception e) {
                                    System.err.println(e.getMessage());
                                    System.exit(0);
                                }
                                continue;
                            }
                        }
                        evaluate(line);
                    }
                } catch(UnsupportedEncodingException e) {
                    env.out.println("Exception: " + e.getMessage());
                } catch(IOException eof) {
                    break;
                }
            }
        } else {
            InputStreamReader reader = new InputStreamReader(env.in);
            BufferedReader bufReader = new BufferedReader(reader);
            while(true) {
                env.out.print(Env.promptText);
                try {
                    line = bufReader.readLine();
                    
                    if(line == null) {
                        env.out.println();
                        continue;
                    } else {
                        if(isKeyword(line)) {
                            qdbHelp(line);  
                            continue;
                        } else {
                            int code = QdbCode.get(line);
                            if(code >= 0) {
                                try {
                                    unit.debugEvaluate(env, code);
                                } catch(Exception e) {
                                    System.err.println(e.getMessage());
                                    System.exit(0);
                                }
                                continue;
                            }
                        }
                        evaluate(line);
                    }
                } catch(IOException e) {
                    continue;
                }
            }
        }
        
        env.out.println();
        Readline.cleanup();
        freeEnv();
    }
    
    void shellEvaluate() {
        String line;
        initEnv();
        
        if(initReadline()) {
            while(true) {
                try {
                    line = Readline.readline(Env.promptText);
                    
                    if(line != null) {
                        if(isKeyword(line)) continue;
                        evaluate(line);
                    }
                } catch(UnsupportedEncodingException e) {
                    env.out.println("Exception: " + e.getMessage());
                } catch(IOException eof) {
                    break;
                }
            }
        } else {
            InputStreamReader reader = new InputStreamReader(env.in);
            BufferedReader bufReader = new BufferedReader(reader);
            while(true) {
                env.out.print(Env.promptText);
                try {
                    line = bufReader.readLine();
                    
                    if(line == null) {
                        env.out.println();
                        continue;
                    } else {
                        if(isKeyword(line)) continue;
                        evaluate(line);
                    }
                } catch(IOException e) {
                    continue;
                }
            }
        }
        
        env.out.println();
        Readline.cleanup();
        freeEnv();
    }
    
    private void initEnv(String[] args) {
        env = new Env();
        
        File file = new File(args[0]);
        try {
            interp = new Interp(file);
            unit = interp.parse();
            if(unit == null) System.exit(0);
            env.setMainUnit(unit);
            env.pushScope(unit);
            
            Variable var;
            // Add $# (number of script arguments).
            var = new Variable(unit, "$#", new Value(args.length-1));
            unit.addVariable(var);
            
            // Add script arguments: $0, $1, $2...
            // The argument $0 is the script file.
            for(int i = 0; i < args.length; i++) {
                var = new Variable(unit, "$"+i, new Value(args[i]));
                unit.addVariable(var);
            }
            
            timing = null;
            buffer = new StringBuffer();
        } catch(FileNotFoundException ex) {
            System.err.println("No such a file " + file + " exists");
            System.exit(0);
        }
    }
    
    private void initEnv() {
        env = new Env();
        unit = new ScriptUnitNode();
        env.setMainUnit(unit);
        env.pushScope(unit);
        
        timing = null;
        interp = new Interp();
        buffer = new StringBuffer();
    }
    
    private void freeEnv() {
        env.popScope();
    }
    
    private boolean initReadline() {
        try {
            Readline.load(ReadlineLibrary.GnuReadline);
        } catch(UnsatisfiedLinkError ignore_readline) {
            try {
                Readline.load(ReadlineLibrary.Editline);
            } catch(UnsatisfiedLinkError ignore_editline) {
                System.err.println("Unable to load readline lib. Using stdin.");
            }
        }
        
        // Initialize
        Readline.initReadline("jGrok Shell");
        
//          try {
//              if(args.length > 0)
//                  Readline.readInitFile(args[0]);    // read private inputrc
//          } catch(IOException e) {                   // this deletes any initialization
//              System.out.println(e.toString());      // from /etc/inputrc and ~/.inputrc
//              System.exit(0);
//          }
        
        // Define some additional function keys
        Readline.parseAndBind("\"\\e[18~\":        \"Function key F7\"");
        Readline.parseAndBind("\"\\e[19~\":        \"Function key F8\"");
        
        // Set word break characters
        try {
            Readline.setWordBreakCharacters(" \t;");
        } catch(UnsupportedEncodingException enc) {
            System.err.println("Unable to set word break characters");
            return false;
        }
        
        // Set shell completer
        Readline.setCompleter(new ShellCompleter());
        return true;
    }
    
    private boolean isKeyword(String line) {
        if(line.equals("?") ||
           line.equals("help") ||
           line.equals("print") ||
           line.equals("delete") ) {
            return true;
        } else {
            return false;
        }
    }
    
    private void qdbHelp(String line) {
        if(line.equals("?") ||
           line.equals("help")) {
            System.out.println("qdb:");
            System.out.println("    x - Continue to finish");
            System.out.println("    c - Continue to next pause");
            System.out.println("    l - Display next statement");
            System.out.println("    n - Execute next statement");
        }
    }
    
    private void evaluate(String line) {
        try {
            line = line.trim();
            
            // Ignore comments
            if(line.startsWith("%") ||
               line.startsWith("//") ) {
                line = "";
            }
            
            if(line.endsWith("\\")) {
                while(line.endsWith("\\")) {
                    line = line.substring(0, line.length()-1);
                    line = line.trim();
                }
                
                if(line.length() > 0) {
                    buffer.append(" ");
                    buffer.append(line);
                }
                return;
            } else {
                if(line.length() > 0) {
                    buffer.append(" ");
                    buffer.append(line);
                }
            }
            
            // No need to parse/evaluate.
            if(buffer.length() == 0) return;
            
            // Start to parse/evaluate it.
            byteStream = new ByteArrayInputStream(buffer.toString().getBytes());
            Interp.ReInit(byteStream);
            try {
                node = Interp.Statement();
            } catch(ParseException e) {
                byteStream.close();
                byteStream = new ByteArrayInputStream(buffer.toString().getBytes());
                Interp.ReInit(byteStream);
                node = Interp.Expression();
            }
            
            if(unit.isTimeOn()) {
                if(timing == null) timing = new Timing();
                timing.start();
            }
            
            if(unit.isEchoOn()) {
                env.out.println(Env.promptText + node.toString());
            }
            
            // Do evaluation.
            value = node.evaluate(env);
            
            if(node instanceof ExpressionNode) value.print(env.out);
            
            if(unit.isTimeOn()) {
                if(timing == null) timing = new Timing();
                else {
                    timing.stop();
                    env.out.println("time:");
                    env.out.println("\t" + timing.getTime());
                }
            }
            
            byteStream.close();
            byteStream = null;
            buffer.delete(0, buffer.length());
            
        } catch(IOException e) {
            env.out.println("Exception: " + e.getMessage());
        } catch(TokenMgrError e) {
            env.out.println("Exception: " + e.getMessage());
        } catch(ParseException e) {
            env.out.println("Exception: unable to parse " + buffer);
        } catch(EvaluationException e) {
            env.out.println("Exception: " + e.getMessage());
        } finally {
            buffer.delete(0, buffer.length());
        }
    }
}
