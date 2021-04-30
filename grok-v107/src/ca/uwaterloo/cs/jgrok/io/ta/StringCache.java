package ca.uwaterloo.cs.jgrok.io.ta;

/* This class caches strings
 * It avoids use of key which is expensive to construct
 * This won't cache Strings returned by Swing classes
 *
 */

public class StringCache {

	public static void clear() 
	{
	}

    // Returns the String matching this sequence of characters
    
	public static String get(char buf[], int lth)
	{
		String		s  = new String(buf, 0, lth);
		String		s1 = s.intern();

		s = null;
		return s1;
	}

	public static String get(char buf[], int offset, int lth)
	{
		String		s  = new String(buf, offset, lth);
		String		s1 = s.intern();

		s = null;
		return s1;
	}

	public static String get(String s)
	{
		if (s != null) {
			return s.intern();
		}
		return null;
	} 

	public static boolean isCached(String s)
	{
		if (s != null) {
			String s1 = s.intern();
			if (s1 != s) {
				System.out.println("String '" + s + "' not cached");
				return false;
			}
			s1 = null;
		}
		return true;
	}
}

