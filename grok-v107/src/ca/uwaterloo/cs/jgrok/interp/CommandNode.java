package ca.uwaterloo.cs.jgrok.interp;

public abstract class CommandNode extends StatementNode {
    protected String command;
    
    public CommandNode(String cmd) {
        this.command = cmd;
    }
}
