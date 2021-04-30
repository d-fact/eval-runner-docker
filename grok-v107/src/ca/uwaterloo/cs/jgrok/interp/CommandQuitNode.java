package ca.uwaterloo.cs.jgrok.interp;

import ca.uwaterloo.cs.jgrok.env.Env;

public class CommandQuitNode extends CommandNode {
    
    public CommandQuitNode(String cmd) {
        super(cmd);
    }
    
    public Value evaluate(Env env) throws EvaluationException {
        if(command.equals("quit") || command.equals("exit")) {
            if(env.out == System.out) System.exit(0);
            else env.out.println("[Warning] Command '" + command + "' disabled!");
        } else {
            throw new EvaluationException(this, "unrecognized command");
        }
        
        return Value.EVAL;
    }

    public String toString() {
        StringBuffer buffer;
        buffer = new StringBuffer();
        
        buffer.append(command);
        buffer.append(';');
        
        return buffer.toString();
    }
}
