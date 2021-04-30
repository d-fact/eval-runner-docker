package ca.uwaterloo.cs.jgrok.io.ta;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.ZipInputStream;
import java.util.zip.GZIPInputStream;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import java.io.*;
import java.net.URL;

import ca.uwaterloo.cs.jgrok.io.ta.*;
import ca.uwaterloo.cs.jgrok.fb.Factbase;
import ca.uwaterloo.cs.jgrok.fb.EdgeSet;
import ca.uwaterloo.cs.jgrok.io.FactbaseReader;

public class TAFileReader implements FactbaseReader {

	// Stream tokenizer stuff
	
	private static final byte	CT_DELIMIT	= 2;
	private static final byte	CT_QUOTE    = 3;
	public  static final int	TT_EOF      = -1;				// The End-of-file token.
	public	static final int	TT_WORD     = -2;				// The word token.	This value is in sval.

	private static char			m_ctype[];

	private LineNumberReader	m_reader;
	private String				m_filename;
	private int					m_peekc = ' ';	// Will appear to be first character but ignored so ok
	private boolean				m_pushedBack;	// True if last token pushed back to be reconsumed on next call
	private boolean				m_escaped;


	/* TODO: Thinks about a more memory efficient -- extensible method perhaps by using StringBuffer 
	         Also try to make this static by changing how include is implemented
	 */

	private int		m_buf_size = 20 * 1024;
	private char[]	m_buf      = new char[m_buf_size];		// The area that captures a token or attribute value

	/**
	 * The type of the last token returned when m_pushBack is true.	 It's value will either
	 * be one of the following TT_* constants, or a single character.  For example, if '+' is
	 * encountered and is not a valid word character, ttype will be '+'
	 */

	private int m_ttype = ' ';


	/**
	 * The Stream value.
	 */

	private String m_sval;

	private final String filename()
	{
		return m_filename;
	}

	/** Return the current line number. */

	private final int lineno() 
	{
		return m_reader.getLineNumber();
	}

	/* We presume that the reader is sensibly buffered externally */

	private final int charToken() throws IOException 
	{
		int	c;

		c         = m_peekc;
		m_peekc   = m_reader.read();
		switch (c) {
		case '\\':
			switch (m_peekc) {
			case 'n':
				c = '\n';
				break;
			case 't':
				c = '\t';
				break;
			case 'f':
				c = '\f';
				break;
			case 'r':
				c = '\r';
				break;
			case 'e':
				c = 27;
				break;
			case 'd':
				c = 127;
			case '\\':
				m_peekc = ' ';
				break;
			case '"':
				c = '"';
				m_escaped = true;
				break;
			case '\'':
				c = '\'';
				m_escaped = true;
				break;
			default:
				c = m_peekc;
			}
			charToken();	// Consume and discard m_peekc
			break;
		case '\'':
		case '"':
			m_escaped = false;
		}
//		System.err.print("" + ((char) c));
		return c;
	}

	/** Does an unget of the last token */

	protected final void pushBack(int ttype) 
	{
		m_ttype      = ttype;
		m_pushedBack = true;
	} 

	/**
	 * Parses a token from the input stream.  The return value is
	 * the same as the value of ttype.	Typical clients of this
	 * class first set up the syntax tables and then sit in a loop
	 * calling nextToken to parse successive tokens until TT_EOF
	 * is returned.
	 */

	public final int nextToken() throws IOException 
	{
		if (m_pushedBack) {
			m_pushedBack = false;
			return m_ttype;
		}

		int		pos = 0;
		int		c;
		
		for (m_sval = null; ; ) {
			// This loop is repeated while initial whitespace
			c = charToken();

			switch(c) {
			case TT_EOF:
				return TT_EOF;
			case '\f':
			case '\r':
			case '\n':
			case '\t':
			case ' ':
				continue;
			case '"':
				for (;;) {
					c = charToken();
					switch (c) {
					case TT_EOF:
						System.out.println("Unterminated \" in input");
					case '"':
						if (!m_escaped) {
							break;
						}
					default:
						if (pos >= m_buf_size) {
							int		new_buf_size = m_buf_size * 2;
							char[]	new_buf      = new char[new_buf_size];
							
							System.arraycopy(m_buf, 0, new_buf, 0, m_buf_size); 
							m_buf_size = new_buf_size;
							m_buf      = new_buf;
						}							
						m_buf[pos++] = (char) c;
						continue;
					}
					break;
				} 
				m_sval = StringCache.get(m_buf, pos);
				return TT_WORD;
			case '\'':
				for (;;) {
					c = charToken();
					switch (c) {
					case TT_EOF:
						System.out.println("Unterminated ' in input");
						break;
					case '\'':
						if (!m_escaped) {
							break;
						}
					default:
						if (pos >= m_buf_size) {
							int		new_buf_size = m_buf_size * 2;
							char[]	new_buf      = new char[new_buf_size];
							
							System.arraycopy(m_buf, 0, new_buf, 0, m_buf_size); 
							m_buf_size = new_buf_size;
							m_buf      = new_buf;
						}		
						m_buf[pos++] = (char) c;
						continue;
					}
					break;
				} 
				m_sval = StringCache.get(m_buf, pos);
				return TT_WORD;
			case '/':
				switch (m_peekc) {
					case '/':
					{
						// Toss the remainder of the line
						for (;;) {
							c = charToken();
							switch (c) {
							case '\r':
								if (m_peekc == '\n') {
									continue;
								}
							case '\n':
							case TT_EOF:
								break;
							default:
								continue;
							}
							break;
						}
						continue;
					}
					case '*':
					{
						for (;;) {
							c = charToken();
							switch (c) {
							case '*':
								if (m_peekc != '/') {
									continue;
								}
								c = charToken();
							case TT_EOF:
								break;
							default:
								continue;
							}
							break;
						}
						continue;
				}	}
			default:
				if (c < m_ctype.length && m_ctype[c] == CT_DELIMIT) {
					m_sval = String.valueOf((char) c);
					return c;
				}

				// Read characters until delimitter

				for (;;) {
					m_buf[pos++] = (char) c;
					if (m_peekc == TT_EOF) {
						break;
					}
					if (c == '\\') {
						m_buf[pos++] = (char) charToken();
					} 
					c = m_peekc;
					if (c < m_ctype.length && m_ctype[c] != 0) {
						break;
					}
					c = charToken();
				}
				m_sval = StringCache.get(m_buf, pos);
				if (pos == 1 && m_buf[0] == ':') {
					return ':';
				}
				return TT_WORD;
			}
		}
	}

	public final static int SCHEME_TUPLE		= 0;
	public final static int SCHEME_ATTRIBUTE	= 1;
	public final static int FACT_TUPLE			= 2;
	public final static int FACT_ATTRIBUTE		= 3;

	public final static int ERROR				= -1;
	public final static int NONE				= -2; 
	public final static int EOF					= 99;	
	
	public final static String INSTANCE_ID				= "$INSTANCE";	
	public final static String INHERIT_RELN				= "$INHERIT";
	public final static String ENTITY_ID                = "$ENTITY";
	public final static String RELATION_ID              = "$RELATION";
	public final static String CONTAIN_ID				= "contain"; 
	public final static String ROOT_ID                  = "$ROOT";
	public final static String SCHEME_ID                = "SCHEME";
	public final static String FACT_ID	                = "FACT";
	public final static String TUPLE_ID	                = "TUPLE";
	public final static String ATTRIBUTE_ID             = "ATTRIBUTE";

	public static boolean		m_strict_TA			    = false;		// Set by -s option

	private   Object			m_context;
	private	  String			m_zipEntry;

	protected Factbase			m_factbase          = null;
	protected String			m_resString         = null;

	protected boolean m_fatalError = false;

	// push backed tokens 

	protected String m_token1, m_token2; 
	protected int	 m_ttype3;
	
	// triple values
	
	public String	m_verb;
	public String	m_object;
	public String	m_subject; 
	public int		m_relations;
	
	// Start line no
	
	protected int	m_startLineno = -1;

	protected void skipRecord() throws IOException 
	{
		int ttype;

		do {
			ttype = nextToken();
		} while (ttype != TT_EOF && ttype != '}');
	}


	// --------------

	// Public methods

	// --------------

	public int getStartLineno()
	{
		return m_startLineno;
	}
	
	public void errorNS(String msg) 
	{
		System.out.println("*** Error (" + filename() + ":" + lineno() + "): " + msg);
	}

	public void error(String msg) 
	{
		System.out.println("*** Error (" + filename() + ":" + lineno() + "): " + msg + ". Found " + m_sval);
	}

	public void warning(String msg) 
	{
		System.out.println(">>> Warning (" + filename() + ":" + lineno() + "): " + msg);
	}

	public int nextSection() throws IOException 
	{
		// Called when a section id is expected
		// Returns section id if new section found, or 
		// EOF when at stream end.

		int ttype;

		String	graph;
		String	type;

		if (m_fatalError) {
			return EOF;
		}

		if (m_token1 != null) {
			if (m_ttype3 == ':') {
				graph  = m_token1; 
				type   = m_token2;
				m_token1 = null;
				m_token2 = null;
			} else {
				error("Expecting section header ':'"); 
				return ERROR; 
			}
		} else { 
			ttype = nextToken();
			switch (ttype) {
			case TT_EOF:
				return EOF;
			case TT_WORD:
				graph = m_sval;

				if (nextToken() != TT_WORD) { 
					error("Expecting section type id");
					return ERROR;
				}
				type = m_sval; 
				if (nextToken() != ':') {
					error("Expecting ':'");
					return ERROR;
				}
				break;
			default:
				error("Expecting graph id");
				return ERROR;
		}	} 

		if (graph.equals(SCHEME_ID)) {
			if (type.equals(TUPLE_ID)) {
				return SCHEME_TUPLE;
			}
			if (type.equals(ATTRIBUTE_ID)) {
				return SCHEME_ATTRIBUTE;
			}
			error("Bad section type");
			return ERROR; 
		}

		if (graph.equals(FACT_ID)) {
			if (type.equals(TUPLE_ID)) {
				return FACT_TUPLE;
			}
			if (type.equals(ATTRIBUTE_ID)) {
				return FACT_ATTRIBUTE;
			}
			error("Bad section type");
			return ERROR;
		}

		error("Bad section id");
		return ERROR;
	}

	public boolean nextSchemaTriple() throws IOException 
	{
		if (!m_fatalError) {

			m_startLineno = lineno();
			
			switch (nextToken()) {
			case TT_EOF:
				// End of section 
				return false;
			case TT_WORD: 
				break;
			default:
				error("Expecting word verb token");
				m_fatalError = true;
				return false;
			}

			m_relations = 0;
			m_verb      = m_sval;

			switch (nextToken()) {
			case TT_WORD:
				m_object = m_sval;
				break;
			case '(':
				m_relations |= 1;
				if (nextToken() == TT_WORD) {
					m_object = m_sval;
					if (nextToken() == ')') {
						break;
				}	}
			default:
				error("Expecting word object token"); 
				m_fatalError = true;
				return false;
			}

			switch (nextToken()) {
			case ':':
				m_token1 = m_verb;
				m_token2 = m_object; 
				m_ttype3 = ':'; 
				return false; 
			case TT_WORD:
				m_subject = m_sval;
				return true;
			case '(':
				m_relations |= 2;
				if (nextToken() == TT_WORD) {
					m_subject = m_sval;
					if (nextToken() == ')') {
						return true;
				}	}
			default:
				error("Expecting word subject token");
				m_fatalError = true;
			}
			return true;
		}
		return false;
	}

	public boolean nextFactTriple() throws IOException 
	{
		if (!m_fatalError) {

			m_startLineno = lineno();

			int ttype = nextToken();

			switch (ttype) {
			case TT_EOF:
				// End of section 
				return false;
			case TT_WORD: 
				m_verb = m_sval;

				if (nextToken() != TT_WORD) {
					error("Expecting word token"); 
					break;
				}

				m_object = m_sval;

				ttype = nextToken();

				if (ttype == ':') {
					m_token1 = m_verb;
					m_token2 = m_object; 
					m_ttype3 = ttype; 
					return false; 
				} 

				if (ttype != TT_WORD) {
					error("Error in tuple parse");
					break;
				}

				m_subject = m_sval;

				return true;
			default:		
				error("Expecting word token");
			}
			m_fatalError = true;
		}
		return false;
	}

	// ------------------
	// TA Reading methods
	// ------------------

	private final int charAVI() throws IOException 
	{
		int	c;

		c         = m_peekc;
		m_peekc   = m_reader.read();
		return c;
	}

	public final void nextAVI(AttributeSettingNode target) throws IOException 
	{
		int					depth		 = 0;
		int					pos			 = 0;
		int					tokens		 = 0;
		int					startbracket = -1;
		int					i, c, start;
		boolean				escaped, simple;
		String				string;

		for (; ; ) {
			// This loop is repeated while initial whitespace
			c = charAVI();

			switch(c) {
			case TT_EOF:
				return;
			case '\f':
			case '\r':
			case '\n':
			case '\t':
			case ' ':
				continue;
			case '"':
				++tokens;
				start        = pos;
				m_buf[pos++] = '"';
				escaped      = false;
				simple       = true;
				for (;;) {
					c = charAVI();
					switch (c) {
					case TT_EOF:
						System.out.println("Unterminated \" in input");
					case '\\':
						escaped      = !escaped;
						m_buf[pos++] = '\\';
						simple       = false;
						continue;
					case '"':
						if (!escaped) {
							break;
						}
					case '\'':
						simple = false;
					default:
						if (c < m_ctype.length && m_ctype[c] == CT_DELIMIT) {
							simple = false;
						}
						m_buf[pos++] = (char) c;
						escaped      = false;
						continue;
					}
					break;
				} 
				if (!simple) {
					// m_buf[pos++] = '"';
					for (--pos; ++start <= pos; m_buf[start-1] = m_buf[start]);
				} else {
					// Remove the double quotes so that integers etc parse easily
					for (--pos; ++start <= pos; m_buf[start-1] = m_buf[start]);
				}
				break;
			case '\'':
				++tokens;
				start        = pos;
				m_buf[pos++] = '\'';
				escaped      = false;
				simple       = true;
				for (;;) {
					c = charAVI();
					switch (c) {
					case TT_EOF:
						System.out.println("Unterminated ' in input");
						break;
					case '\\':
						escaped      = !escaped;
						m_buf[pos++] = '\\';
						simple       = false;
						continue;
					case '\'':
						if (!escaped) {
							break;
						}
					case '"':
						simple = false;
					default:	
						if (c < m_ctype.length && m_ctype[c] == CT_DELIMIT) {
							simple = false;
						}
						m_buf[pos++] = (char) c;
						escaped      = false;
						continue;
					}
					break;
				} 
				if (!simple) {
					m_buf[pos++] = '\'';
				} else {
					// Remove the single quotes so that integers etc parse easily
					for (--pos; ++start <= pos; m_buf[start-1] = m_buf[start]);
				}
				break;
			case '(':
				startbracket = pos;
				m_buf[pos++] = '(';
				if (depth != 0) {
					tokens = 2;	// Force later test to fail -- don't want to treat ((1)) as 1
				}
				++depth;
				continue;
			case ')':
				if (depth <= 0) {
					// Ignore the ')' -- treat as parse error
					System.out.println("Bad nesting of () at " + lineno());
					return;
				} 
				switch (m_buf[pos-1]) {
				case '(':
					// Omit () altogether
					System.out.println("Empty nesting of () at " + lineno());
					--pos;
					break;
				case ' ':
					// Omit ' ' before ')'
					--pos;
				default:
					if (tokens != 1) {
						m_buf[pos++] = ')';
					} else {
						// Drop the brackets
						--pos;
						for (i = startbracket; i < pos; ++i) {
							m_buf[i] = m_buf[i+1];
				}	}	}
				--depth;
				break;
			case '/':
				switch (m_peekc) {
					case '/':
					{
						// Toss the remainder of the line
						for (;;) {
							c = charAVI();
							switch (c) {
							case '\r':
								if (m_peekc == '\n') {
									continue;
								}
							case '\n':
							case TT_EOF:
								break;
							default:
								continue;
							}
							break;
						}
						continue;
					}
					case '*':
					{
						// Toss the remainder of the comment
						for (;;) {
							c = charAVI();
							switch (c) {
							case '*':
								if (m_peekc != '/') {
									continue;
								}
								c = charAVI();
							case TT_EOF:
								break;
							default:
								continue;
							}
							break;
						}
						continue;
				}	}
			default:
				if (c < m_ctype.length && m_ctype[c] == CT_DELIMIT) {
					pushBack(c);
					return;
				}

				// Read characters until delimitter

				++tokens;
				start    = pos;
				simple   = true;
				for (;;) {
					if (c == '\\') {
						simple = false;
					}
					m_buf[pos++] = (char) c;
					if (m_peekc == TT_EOF) {
						break;
					}
					c = m_peekc;
					if (c < m_ctype.length && m_ctype[c] != 0) {
						break;
					}
					c = charAVI();
				}
				if (!simple) {
					// If the string contains \ then quote it so that parseString kept simple
					for (i = pos; i >= start; --i) {
						m_buf[i+1] = m_buf[i];
					}
					m_buf[start] = '"';
					m_buf[pos++] = '"';
				}
				break;
			}
			if (depth > 0) {
				m_buf[pos++] = ' ';
				continue;
			}
			if (tokens == 0) {
				return;
			}

			if (target != null) {
				target.setValue(StringCache.get(m_buf, pos));
			}
			return;
		}
	}
	
	private void parseAttributes(AttributeNode target) throws IOException 
	{
		int						ttype;
		AttributeSettingNode	attribute = null;

		for (;;) {
			ttype = nextToken();

			switch (ttype) {
			case '}':
				return;
			case TT_WORD:
				if (target != null) {
					attribute = new AttributeSettingNode(m_sval);
					target.add(attribute);
				}
				ttype = nextToken();
				if (ttype != '=') {
					// Attribute declaration with no value 
					pushBack(ttype);
				} else {
					nextAVI(attribute);
					// attribute.dump(0);
				}
				break;
			default:
				error("Expecting attribute id for " + target);
				return;
			}
		}
	}

	private void 
	processSchemeTuples() throws IOException 
	{
		String			verb, object, subject;
		String			msg;
		FactNode		factnode;

		while (nextSchemaTriple()) {

			verb    = m_verb;
			object  = m_object;
			subject = m_subject;
			if (subject.charAt(0) != '$') {
				subject = '$' + subject;
			}
			if (verb.equals(INHERIT_RELN)) {							// $INHERIT
				switch (m_relations) {
				case 0:	/* Neither term is a relation */
					if (object.equals(ENTITY_ID)) {	// $ENTITY
						errorNS("Improper use of $ENTITY with $INHERIT");
						break;
					}
/*					
					ec1 = addEntityClass(object); 
					ec2 = addEntityClass(m_subject);
					msg = ec1.addParentClass(ec2);
					if (msg != null) {
						errorNS(msg);
					}
 */
					if (subject.charAt(0) != '$') {
						subject = '$' + subject;
					}
					// $INHERIT object subject (Object subclass of Subject)
 					factnode = new FactNode(verb, "$" + object, subject);
					// Add to edgeSet $INHERIT relation $object->$subject
					// Object and subject begin $ to indicate node class
					factnode.putInto(m_factbase);
					break;
				case 1:	/* First  is relation		*/
				case 2:	/* Second is relation		*/
					errorNS("Mismatched entity/relation with $INHERIT -- presuming both relations");
				case 3:	/* Both term is a relation	*/
					if (object.equals(RELATION_ID)) {	// $ENTITY
						errorNS("Improper use of $RELATION with $INHERIT");
						break;
					}
/*
					rc1 = addRelationClass(object);
					rc2 = addRelationClass(m_subject);
					msg = rc1.addParentClass(rc2);
					if (msg != null) {
						errorNS(msg);
					}
 */
					if (subject.charAt(0) != '$') {
						subject = "$_" + subject;
					} else {
						subject = "$_" + subject.substring(1);
					}
					// $INHERIT object subject (Object subclass of Subject)
 					factnode = new FactNode(verb, "$_" + object, subject);
					// Add to edgeSet $INHERIT relation $_object->$_subject
					// Object and subject begin $_ to indicate relation class
					factnode.putInto(m_factbase);
					break;
				}

			} else { 
				if (m_relations != 0) {
					errorNS("Cannot create relationships between relationships");
				} else {
/*
					ec1 = addEntityClass(object); 
					ec2 = addEntityClass(m_subject);
					rc1 = addRelationClass(verb);
					rc1.addRelationConstraint(ec1, ec2);	// If not already present
 */
					if (verb.charAt(0) == '$') {
						verb = "$_" + verb.substring(1);
					} else {
						verb = "$_" + verb;
					}
					// Add to edgeSet $_relation $object->$subject
  					factnode = new FactNode(verb, "$" + object, subject);
					factnode.putInto(m_factbase);
				}
			}
		}
	}

	private void processSchemeAttributes() throws IOException 
	{

		int				ttype;
		String			token1;
		String			token2;
		String			token3;
		String			msg;
		ItemIdNode		idnode = null;
		FactNode		factnode;
		AttributeNode	attr;
		EdgeSet			inherits = m_factbase.getEdgeSet(INHERIT_RELN);

		//if (inherits == null)
        //    System.out.println("null in processSchemeAttributes().");

		// 
		//  * id "{" {attribute} "}" *
		// 

		while (!m_fatalError) {
		
			m_startLineno = lineno();

			ttype = nextToken();
			msg   = null;

			switch (ttype) {
			case TT_EOF:
				// End of section
				return; 
			case '(':
				// Relation or relation class 
				ttype = nextToken();
				if (ttype != TT_WORD) {
					msg = "Expected ( <relation class name>";
					break;
				} 
				token1 = m_sval;
				ttype  = nextToken();
				if (ttype != ')') {
					msg = "Expected ( " + token1 + "<)>";
					break;
				}
				
				if (token1.charAt(0) == '$') {
					token1 = "$_" + token1.substring(1);
				} else {
					token1 = "$_" + token1;
					if (!inherits.inDom(token1)) {
						factnode = new FactNode(INHERIT_RELN, token1, "$_RELATION");
						factnode.putInto(m_factbase);
				}	}
				idnode = new ItemIdNode(token1);
				
				ttype = nextToken();
				if (ttype != '{') {
					msg = "Expected (...) <{>";
					break;
				}

/*
				if (Ta.m_strict_TA) {
					target = ta.getRelationClass(token1);
					if (target == null) {
						msg = "Strict TA: Undeclared relation class '" + token1 + "' has attributes";
					}
				} else {
					target = ta.addRelationClass(token1);
				}
 */
				break;
			case TT_WORD:
				// Entity class or entity 
				token1 = m_sval;
				ttype  = nextToken();
				if (ttype != '{') {
					if (ttype != TT_WORD) {
						msg = "Expecting " + token1 + " <{>";
						break;
					}
					token2 = m_sval;
					ttype = nextToken();
					if (ttype != ':') {
						msg = "Expecting section header or id <{>";
						break;
					}
					m_token1 = token1; 
					m_token2 = token2;
					m_ttype3 = ttype;
					// End of section 
					return;
				}
				

/*
				if (!Ta.m_strict_TA) {
					target = ta.addEntityClass(token1);
				} else {
					target = ta.getEntityClass(token1);
					if (target == null) {
						msg = "Strict TA: Undeclared entity class '" + token1 + "' has attributes";
				}	}
 */
				if (token1.charAt(0) != '$') {
					token1 = '$' + token1;
					if (!inherits.inDom(token1)) {
						factnode = new FactNode(INHERIT_RELN, token1, "$ENTITY");
						factnode.putInto(m_factbase);
				}	}
				idnode = new ItemIdNode(token1);
				break;
			default:
				msg = "Expecting object id";
			}
			if (msg != null) {
				errorNS(msg);
				skipRecord();
				continue;
			} 
			
			attr = new AttributeNode(idnode);
			parseAttributes(attr); 
			// Adds into each edgeset @attributename $class->value
			attr.putInto(m_factbase);
	}	}
	
	private void processFactTuples() throws IOException 
	{
		FactNode	factnode;
		String		subject;

		while (nextFactTriple()) {
			subject = m_subject;
			if (m_verb.equals(INSTANCE_ID)) {
				subject = "$" + subject;
			}
			factnode = new FactNode(m_verb, m_object, subject);
			// Adds into edgeSet m_verb object->subject
			factnode.putInto(m_factbase);
		}
	}

	public void processFactAttributes() throws IOException 
	{
		int				ttype;
		String			token1;
		String			token2;
		String			token3;
		String			msg;
		ItemIdNode		idnode = null;
		AttributeNode	attr;
		EdgeSet			inherits = m_factbase.getEdgeSet(INHERIT_RELN);

		// m_factbase.print(System.out);
		if(inherits == null)
			System.out.println("null in processFactAttributes()");

		// 
		//  * id "{" {attribute} "}" *
		// 

		while (!m_fatalError) {

			m_startLineno = lineno();

			ttype = nextToken();
			msg   = null;

			switch (ttype) {
			case TT_EOF:
				// End of section
				return; 
			case '(':
			{
				FactNode	factnode;

				// Relation or relation class 
				ttype = nextToken();
				if (ttype != TT_WORD) {
					msg = "Expected ( <class> src dst )";
					break;
				} 
				token1 = m_sval;

				ttype = nextToken();
				if (ttype != TT_WORD) {
					msg = "Expected (" + token1 + " <src> dst)" + " not " + ttype;
					break;
				}
				token2 = m_sval;
				ttype = nextToken();
				if (ttype != TT_WORD) {
					msg = "Expected (" + token1 + " " + token2 + " <dst>)" + " not " + ttype;
					break;
				}
				token3 = m_sval;
				ttype = nextToken();
				if (ttype != ')') {
					msg = "Expected (" + token1 + " " + token2 + " " + token3 + "<)>" + " not " + ttype;
					break;
				}

				if (token1.charAt(0) == '$') {
					token1 = "$_" + token1.substring(1);
				} else {
					token1 = "$_" + token1;
					if (inherits == null || !inherits.inDom(token1)) {
						// System.out.println("reach 8!");
						factnode = new FactNode(INHERIT_RELN, token1, "$_RELATION");
						factnode.putInto(m_factbase);
					}
				}
				idnode = new ItemIdNode(token1, token2, token3);
				factnode = new FactNode(token1, token2, token3);
				factnode.putInto(m_factbase);
				ttype = nextToken();
				if (ttype != '{') {
					msg = "Expected (...) <{>" + " not " + ttype;
					break;
				}

/*
				rc  = ta.getRelationClass(token1);
				if (rc == null && !Ta.m_strict_TA) {
					rc  = ta.addRelationClass(token1);
				}

				src = m_entityCache.get(token2);
				if (src == null && !Ta.m_strict_TA) {
					// Create it
					src = ta.newCachedEntity(ta.m_entityBaseClass, token2);
				}

				dst = m_entityCache.get(token3);
				if (dst == null && !Ta.m_strict_TA) {
					// Create it
					dst = ta.newCachedEntity(ta.m_entityBaseClass, token3);
				}

				if (rc == null || src == null || dst == null) {
					msg = "Strict TA: Relation " + "(" + token1 + " " + token2 + " " + token3 + ")";
					if (rc == null) {
						msg += " member of undeclared relation class '" + token1 + "'";
					}
					if (src == null) {
						msg += " has undeclared source entity '" + token2 + "'";
					} 
					if (dst == null) {
						msg += " has undeclared destination entity '" + token3 + "'";
					}
					break;
				}
				target = src.getRelationTo(rc, dst);
				if (target == null) {
					if (Ta.m_strict_TA) {
						msg = "Strict TA: Undeclared relation (" + token1 + " " + token2 + " " + token3 + ") has attributes";
						break;
					}
					ta.addEdge(rc, src, dst);
					target = src.getRelationTo(rc, dst);
				}
 */
				break;
			}
			case TT_WORD:
				// Entity class or entity
				token1 = m_sval;
				ttype  = nextToken();
				if (ttype != '{') {
					if (ttype != TT_WORD) {
						msg = "Expecting " + token1 + " <{>";
						break;
					}
					token2 = m_sval;
					ttype = nextToken();
					if (ttype != ':') {
						msg = "Expecting section header or id <{>";
						break;
					}
					m_token1 = token1; 
					m_token2 = token2;
					m_ttype3 = ttype;
					// End of section 
					return;
				}
				idnode = new ItemIdNode(token1);

/*
				if (token1.equals(Ta.ROOT_ID)) {
					target = ta.getRootInstance();
				} else {
					target = m_entityCache.get(token1);
					if (target == null) {
						if (Ta.m_strict_TA) {
							msg = "Strict TA: Undeclared entity '" + token1 + "' has attributes";
							break;
						}
						// Create it
						target = ta.newCachedEntity(ta.m_entityBaseClass, token1);
					}
				}
 */
				break;
			default:
				msg = "Expecting object id";
			}
			if (msg != null) {
				errorNS(msg);
				skipRecord();
				continue;
			} 
			
			attr = new AttributeNode(idnode);
			parseAttributes(attr); 
			// adds into all edgeSets attributeName idnode->value
			attr.putInto(m_factbase);
			
//			attr.dump(0);
	}	} 

	private void parseStream(LineNumberReader is) 
	{
		try {
			for (;;) {
				switch(nextSection()) {
				case SCHEME_TUPLE:
					processSchemeTuples();
					continue;
				case SCHEME_ATTRIBUTE:
					processSchemeAttributes();
					continue;
				case FACT_TUPLE:
					processFactTuples();
					continue;
				case FACT_ATTRIBUTE:
					processFactAttributes();
					continue;
				case EOF:
					return;
				}
			}

		} catch (Exception e) {
			m_resString = e.getMessage();
			System.out.println("Parse error      : " + m_resString);
			System.out.println("Start Line number: " + getStartLineno());
			System.out.println("Last line read   : " + lineno());
			e.printStackTrace();
		}	
	}

	private InputStream decompress(InputStream is, String source, String subfile)
	{
		InputStream ret      = is;
		int			lth      = source.length();

		if (lth > 4) {
			String ends = source.substring(lth - 4);
			if (ends.equalsIgnoreCase(".zip")) {
				ZipInputStream	zipInputStream;
				ZipEntry		zipEntry;

				try {
					zipInputStream = new ZipInputStream(is);
					for (;;) {
						zipEntry = zipInputStream.getNextEntry();
						if (subfile == null || subfile.equalsIgnoreCase(zipEntry.getName())) {
							break;
						}
						zipInputStream.closeEntry();
					}
				} catch (Exception e) {
					System.out.println("Attempt to open " + source + ((subfile == null) ? "" : "#" + subfile) + " as zip file failed");
					m_resString    = e.getMessage();
					zipInputStream = null;
				}
				return zipInputStream;
			}
			if (ends.equalsIgnoreCase(".jar")) {
				JarInputStream	jarInputStream;
				ZipEntry		zipEntry;

				try {
					jarInputStream = new JarInputStream(is);
					for (;;) {
						zipEntry = jarInputStream.getNextEntry();
						if (subfile == null || subfile.equalsIgnoreCase(zipEntry.getName())) {
							break;
						}
						jarInputStream.closeEntry();
					}
				} catch (Exception e) {
					System.out.println("Attempt to open " + source + ((subfile == null) ? "" : "#" + subfile) + " as jar file failed");
					m_resString    = e.getMessage();
					jarInputStream = null;
				}
				return jarInputStream;
			}

			if (lth > 5) {
				ends = source.substring(lth - 5);
				if (ends.equalsIgnoreCase(".gzip")) {
					GZIPInputStream	gzipInputStream;

					try {
						gzipInputStream = new GZIPInputStream(is);
					} catch (Exception e) {
						System.out.println("Attempt to open " + source + " as gzip file failed");
						m_resString     = e.getMessage();
						gzipInputStream = null;
					}
					return gzipInputStream;
		}	}	}
		return is;
	}

	protected void parseFile(String taPath) 
	{
		String	entry = null;
		int		lth   = taPath.length();
		File	file  = null;

		if (lth > 0) {
			char	lc	  = taPath.charAt(lth-1);

			if (lth > 2 &&  lc == ']') {
				int	i = taPath.lastIndexOf('['); 

				if (i > 0 && i < lth - 2) {
					entry  = taPath.substring(i+1, lth-1);
					if (m_zipEntry == null) {
						m_zipEntry = entry;
					}
					taPath = taPath.substring(0, i);
					lth    = i;
					lc     = taPath.charAt(lth-1);
			}	}
			if (lc == File.separatorChar) {
				taPath = taPath.substring(0, lth-1);
		}	}	
		
		try {
			InputStream	is   = null;

			if (lth > 0) {
				file = new File(taPath);
				if (file != null) {
					is = new FileInputStream(file);
					is = decompress(is, taPath, entry);
			}	}
			if (is == null) {
				if (m_resString == null) {
					m_resString = "No input stream specified";
				}
				return;
			} 
			InputStreamReader	reader       = new InputStreamReader(is);
			LineNumberReader	linenoReader = new LineNumberReader(reader);
				
			m_reader = linenoReader;

			linenoReader.setLineNumber(1);

			parseStream(linenoReader);
			linenoReader.close();
			linenoReader = null;
			
		} catch (Exception e) {
			m_resString = e.getMessage();
		}
	}

    public TAFileReader() 
    {
		m_ctype = new char[256];
		m_ctype[' ']  = CT_DELIMIT;
		m_ctype['\f'] = CT_DELIMIT;
		m_ctype['\t'] = CT_DELIMIT;
		m_ctype['\r'] = CT_DELIMIT;
		m_ctype['\n'] = CT_DELIMIT;
		m_ctype['=']  = CT_DELIMIT;
//		m_ctype[':']  = CT_DELIMIT;
		m_ctype['{']  = CT_DELIMIT;
		m_ctype['}']  = CT_DELIMIT;
		m_ctype['(']  = CT_DELIMIT;
		m_ctype[')']  = CT_DELIMIT;
		m_ctype['"']  = CT_QUOTE;
		m_ctype['\''] = CT_QUOTE;
    }
    
    // Returning null indicates success
    
    public Factbase read(String taFile)
    {
        m_factbase  = new Factbase(taFile);
		m_resString = null;
		m_zipEntry  = null;
		m_filename  = taFile;
		
		parseFile(taFile);
		if (m_resString != null) {
			System.out.println(m_resString);
		}
		return m_factbase;
	}
}


