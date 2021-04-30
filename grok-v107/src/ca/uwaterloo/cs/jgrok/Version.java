package ca.uwaterloo.cs.jgrok;

public class Version {
	public final static int MAJOR = 1;
	public final static int MINOR = 0;
	public final static int BUILD = 7;
	public final static String COMPILED = "14th February, 2016";

	public static String Number()
	{
		return(MAJOR + "." + MINOR + "." + BUILD);
	}

	public static String formatNumber(long l)
	{
		String	val  = Long.toString(l);
		String  ret  = "";
		int		lth  = val.length();

		for (int i = 0;; ) {
			ret += val.charAt(i);
			if (++i >= lth) {
				break;
			}
			if (((lth - i) % 3) == 0) {
				ret += ',';
		}	}
		return ret;
	}

	public static int	InternalNumber()
	{
		return (((MAJOR * 1000) + MINOR) * 1000) + BUILD;
	}

	public static String CompileDate()
	{
		return("Compiled: " + COMPILED);
	}

	public static String Detail(String property)
	{
		String result;

		try {
			// This will fail if an applet
			result = System.getProperty(property);
		} catch (Exception e) {
			result = "**Denied**";
			System.out.println("System.getProperty(\"" + property + "\"): " + e.getMessage());
		};
		return(result);
	}

	public static String authorsAndCopyright()
	{
		String  result;

		result	= "JGrok " + Version.Number() + " " + Version.CompileDate() + "\n"
		        + "Developed at the University of Waterloo under the supervision of Prof. Ric Holt\n"
		        + "Original Author: Jingwe (Currently supported by Ian Davis)\n";

		return result;
	}

	public static String Details()
	{
		String  result;
		Runtime	r;

		// System.getProperties().list(System.out);

/*
		for (int i = 0x7FFFFFFF; i != 0; i >>= 1) {
			System.out.println("" + i + "'" + formatNumber(i) + "'");
		}
*/

		result  = authorsAndCopyright()
				+ "\n\n";

		try		{ //set if possible
				result += "Run Time Engine: " + Detail("java.vendor") + " " + Detail("java.version") + "\n"
				        + "Virtual Machine: " + Detail("java.vm.name") + " " + Detail("java.vm.version") + "\n"
				        + "V/M Vendor: " + Detail("java.vm.vendor") + "\n" 
				        + "Operating System: '" + Detail("os.name") + "' " + Detail("os.arch") + " " + Detail("os.version") + "\n"
				        + "Patch level: " + Detail("sun.os.patch.level") + "\n"
				        + "User id: " + Detail("user.name") + "\n"
				        + "Directory: " + Detail("user.dir") + "\n";

				r       = Runtime.getRuntime();
				result += "Memory: " + formatNumber(r.totalMemory()) + " - Free: " + formatNumber(r.freeMemory()) + " = " + formatNumber(r.totalMemory() - r.freeMemory()) + " Max: " + formatNumber(r.maxMemory()) + "\n";
				r.gc();
				result += "Memory: " + formatNumber(r.totalMemory()) + " - Free: " + formatNumber(r.freeMemory()) + " = " + formatNumber(r.totalMemory() - r.freeMemory()) + " Max: " + formatNumber(r.maxMemory()) + "\n";
				

		} catch (Exception e) {}

		return(result);
	}
}

/* History:
   1.0.2 -- Changes by Ian Davis
 * Changed how parsing works to conform with LSEdit parser
 * Permit reading of schema
 * Capture of schema information
 * Simplified attribute structures
 * 1.0.3
 * Changed how JGrok reads very long strings
 * 1.0.4
 * Added DateTime and DateTimeAs functions
 * Added ability to cast to a long
 * 1.0.5
 * Reworked all functions to check their own arguments
 * Reworked to automatically load all builtin functions
 * Added trace() function
 * Added use() function
 * Removed appendta() function -- it makes no sense to append to TA
 * Reworked putta() to generate valid ta
 * Reworked putdb() to not use the putta() stuff
 * 1.0.6
 * Corrected some typing issues with operators
 * Added type() function
 * 1.0.7
 * Fixed fact that classes not mentioned in $INHERIT inherit from $ENTITY/$RELATION
 * Made naming consistent
 *  All relation classes are identified by $_
 *  All entity   classes are identified by $ not followed by _
 * Fixed some minor bugs
 * Added getmysql()
 */
