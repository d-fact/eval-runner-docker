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
import ca.uwaterloo.cs.jgrok.io.FactbaseReader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.sql.Statement;
import java.sql.ResultSet;

// create user 'jdbc'@'localhost' identified by 'jdbc1';
// grant all on gmysql.* to 'jdbc'@'localhost';

public class MysqlReader implements FactbaseReader {

	private static boolean		g_loaded   = false;    
	private static Connection	g_conn     = null;
	private static Factbase		m_factbase = null;
	private static String		m_filename = null;

	private final static String[] g_tables = {
		/* 0 */ "schema_node_inherits",
		/* 1 */ "schema_edge_inherits",
		/* 2 */ "schema_edge_connects",
		/* 3 */ "schema_node_attributes",
		/* 4 */ "schema_edge_attributes",
		/* 5 */ "nodes",
		/* 6 */ "edges",
		/* 7 */ "node_attributes",
		/* 8 */ "edge_attributes"
	};

    private static void sqlerror(SQLException ex)
    {
        System.err.println("SQLException: " + ex.getMessage());
        System.err.println("SQLState: " + ex.getSQLState());
        System.err.println("VendorError: " + ex.getErrorCode());
    }

    private static boolean loadDriver()
    {
        try {
            // The newInstance() call is a work around for some
            // broken Java implementations

            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
            return false;
        }
        return true;
    }

    private static Connection getConnection()
    {
        Connection conn = null;

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/gmysql?user=jdbc&password=jdbc1");
        } catch (SQLException ex) {
            // handle any errors
            sqlerror(ex);
            conn = null;
        }
        return conn;
    }

    public MysqlReader()
	{
    }
    
	public boolean readTable(String schema, String table)
	{
		Factbase	factbase = m_factbase;
		Statement	stmt;
		ResultSet	rs;
		String		query;
		String		from;
		String		verb;
		String		object;
		String		subject;
		int			mode;
		FactNode	factnode;

       	try {
        	stmt = g_conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                       	  ResultSet.CONCUR_READ_ONLY);

			for (mode = 0; ; ++mode) {
				if (mode == g_tables.length) {
					System.err.println("Unknown table " + table);
					return false;
				}
				if (g_tables[mode].equals(table)) {
					break;
			}	}
			from = " from " + schema + "." + table;
			switch (mode) {
			case 0: // schema_node_inherits
			case 1:	// schema_edge_inherits
				query = "select class,superclass" + from;
				break;
			case 2: // schema_edge_connects
				query = "select class,start_class,end_class" + from;
				break;
			case 3:	// schema_node_attributes
			case 4: // schema_edge_attributes
				query = "select class,attribute,value" + from;
				break;
			case 5:	// nodes
				query = "select id,class" + from;
				break;
			case 6: // edges
				query = "select class,start,end" + from;
				break;
			case 7:	// node_attributes
				query = "select id,attribute,value" + from;
				break;
			case 8:	// edge_attributes
				query = "select class,start,end,attribute,value" + from;
				break;
			default:
				return false;
			}

           	rs   = stmt.executeQuery(query);
           	while(rs.next()) {
				switch (mode) {
				case 0: // schema_node_inherits
					object  = rs.getString(1);
					subject = rs.getString(2);
					if (subject.charAt(0) != '$') {
						subject = '$' + subject;
					}
					factnode = new FactNode(TAFileReader.INHERIT_RELN, "$" + object, subject);
					break;
				case 1:	// schema_edge_inherits
					object  = rs.getString(1);
					subject = rs.getString(2);
					if (subject.charAt(0) == '$') {
						subject = subject.substring(1);
					}
					factnode = new FactNode(TAFileReader.INHERIT_RELN, "$_" + object, "$_" + subject);
					break;
				case 2: // schema_edge_connects
					verb    = rs.getString(1);
					if (verb.charAt(0) == '$') {
						verb = verb.substring(1);
					}
					object  = rs.getString(2);
					if (object.charAt(0) != '$') {
						object = '$' + object;
					}
					subject = rs.getString(3);
					if (subject.charAt(0) != '$') {
						subject = '$' + subject;
					}
					factnode = new FactNode("$_" + verb, object, subject);
					break;
				case 3:	// schema_node_attributes
					verb    = rs.getString(1);
					object  = rs.getString(2);
					subject = rs.getString(3);
					if (verb.charAt(0) != '$') {
						verb = '$' + verb;
					}
					factnode = new FactNode("@" + object, verb, subject);
					break;
				case 4: // schema_edge_attributes
					verb    = rs.getString(1);
					object  = rs.getString(2);
					subject = rs.getString(3);
					if (verb.charAt(0) == '$') {
						verb = verb.substring(1);
					}
					factnode = new FactNode("@" + object, "$_" + verb, subject);
					break;
				case 5:	// nodes
					object  = rs.getString(1);
					subject = rs.getString(2);
					if (subject.charAt(0) != '$') {
						subject = '$' + subject;
					}
					factnode = new FactNode(TAFileReader.INSTANCE_ID, object, subject);
					break;
				case 6: // edges
					verb    = rs.getString(1);
					object  = rs.getString(2);
					subject = rs.getString(3);
					factnode = new FactNode(verb, object, subject);
					break;
				case 7:	// node_attributes
					verb    = rs.getString(1);
					object  = rs.getString(2);
					subject = rs.getString(3);
					factnode = new FactNode('@' + object, verb, subject);
					break;
				case 8:	// edge_attributes
					verb    = rs.getString(1);
					object  = rs.getString(2);
					subject = rs.getString(3);

					if (verb.charAt(0) == '$') {
						verb = verb.substring(1);
					}
					verb    = "($_" + verb + ' ' + object + ' ' + subject + ')';
					object  = rs.getString(4);
					subject = rs.getString(5);
					factnode = new FactNode('@' + object, verb, subject);
					break;
				default:
					return false;
				}
				factnode.putInto(factbase);
            }
       	} catch (SQLException ex) {
            sqlerror(ex);
			return false;
        }
		return true;
	}

	public Factbase readAll(String schema, String like)
	{
		Statement	stmt;
		ResultSet	rs;
		String		query;
		String		table;

       	try {
        	stmt = g_conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
                                       	  ResultSet.CONCUR_READ_ONLY);
           	query =
"select table_name\n" +
"  from information_schema.tables\n" +
" where table_schema = '" + schema + "'\n";

			if (0 < like.length()) {
				query +=
"   and table_name like '" + like + "'\n";
			}
			query +=
"   and table_name in (\n" +
"         'schema_node_inherits',\n" +
"         'schema_edge_inherits',\n" +
"         'schema_edge_connects',\n" +
"         'schema_node_attributes',\n" +
"         'schema_edge_attributes',\n" +
"         'nodes',\n" +
"         'edges',\n" +
"         'node_attributes',\n" +
"         'edge_attributes')\n";

           	for (rs = stmt.executeQuery(query); rs.next(); ) {
            	table = rs.getString(1);
				if (!readTable(schema, table)) {
                	System.err.println("Can't read table " + schema + "." + table);
					return null;
				}
            }
       	} catch (SQLException ex) {
            sqlerror(ex);
			return null;
        }
		return m_factbase;
    }
    
    public Factbase read(String taFile)
    {
		String	schema;
		String	like;
		int		dot;

		if (!g_loaded) {
			g_loaded = true;
			if (!loadDriver()) {
				System.err.println("JDBC MySQL Driver not loaded");
				return null;
			}
			g_conn = getConnection();
		}
		if (g_conn == null) {
			System.err.println("No JDBC connection");
			return null;
		}

        m_factbase  = new Factbase(taFile);
		m_filename  = taFile;

		dot = taFile.indexOf('.');
		if (dot < 0) {
			schema = taFile;
			like   = "";
		} else {
			schema = taFile.substring(0,dot);
			like   = taFile.substring(dot+1);
		}
		if (schema.length() == 0) {
			schema = "gmysql";
		}
		
		return readAll(schema, like);	
	}
}


