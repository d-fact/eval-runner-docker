package ca.uwaterloo.cs.jgrok;
import org.gnu.readline.ReadlineCompleter;

/**
 * This class is a shell completer. If you press the <code>TAB<code> key
 * at the prompt, you will see possible completions. If <code>null</code>
 * is returned, it signals that no completions are available.
 */
class ShellCompleter implements ReadlineCompleter {
    
    /**
     *  Default constructor.
     */
    ShellCompleter() {}
    
    /**
     * Return possible completion.
     */
    public String completer(String t, int s) {
        if (s == 0) {
            if (t.equals("") || t.equals("L"))
                return "Linux";
            if (t.equals("T"))
                return "Tux";
        } else if (s == 1 && t.equals("")) {
            return "Tux";
        }
        return null;
    }
}
