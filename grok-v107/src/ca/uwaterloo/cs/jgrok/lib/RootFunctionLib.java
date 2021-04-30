package ca.uwaterloo.cs.jgrok.lib;

import ca.uwaterloo.cs.jgrok.lib.math.*;
import ca.uwaterloo.cs.jgrok.lib.time.*;

/* This provides automatic loading of the built in functions */

public class RootFunctionLib extends FunctionLib {
    
    public RootFunctionLib(String name) 
    {
		super(name);
		
		register(new Aliases());
		register(new Appenddb());
		register(new Basket());
		register(new CSVReader());
		register(new Closure());
		register(new Concept());
		register(new Degree());
		register(new DeleteSet());
		register(new DirContain());
		register(new Double1());
		register(new EDistance());
		register(new ENT());
		register(new ETree());
		register(new Echo());
		register(new Entropy());
		register(new Eset());
		register(new Exec());
		register(new Flist());
		register(new Float1());
		register(new Form());
		register(new Getdb());
		register(new Getmysql());
		register(new Getta());
		register(new GraphPartition());
		register(new GraphPartitionCount());
		register(new Grep());
		register(new Head());
		register(new ID());
		register(new InDegree());
		register(new Int1());
		register(new Lattice());
		register(new Level());
		register(new List1());
		register(new LocalOf());
		register(new Long1());
		register(new Name());
		register(new Normalize());
		register(new Numbering());
		register(new OutDegree());
		register(new Paths());
		register(new Pause());
		register(new Pick());
		register(new Putdb());
		register(new Putta());
		register(new Rand());
		register(new Range());
		register(new Reach());
		register(new Read());
		register(new Relnames());
		register(new Replace());
		register(new ReplaceIdentifier());
		register(new Reset());
		register(new Reverse());
		register(new Run());
		register(new ScriptParser());
		register(new ScriptSource());
		register(new Set1());
		register(new Setnames());
		register(new Show());
		register(new ShowEdge());
		register(new ShowPath());
		register(new ShowTree());
		register(new SimOut("sim"));
		register(new SimIn());
		register(new SimOut("simout"));
		register(new SimRankBi());
		register(new Sort());
		register(new Tail());
		register(new Timing());
		register(new Trace());
		register(new Type());
		register(new Unbasket());
		register(new Unclosure());
		register(new Use());
		register(new WriteDotty());
		
		/* Math */
		
		register(new Avg());
		register(new Ln());
		register(new Log());
		register(new Max());
		register(new Maxf());
		register(new Maxi());
		register(new Median());
		register(new Min());
		register(new Minf());
		register(new Mini());
		register(new Pow());
		register(new Sqrt());
		register(new Stdev());
		register(new Sum());
		
		/* Time */
		
		register(new DateTime());
		register(new DateTimeAs());
	}
}
