package com.qkernel.eobuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Enumeration;
import java.util.Hashtable;
import com.qkernel.SqlSafe;

@SuppressWarnings("unchecked")

/**
 *  This function generates the Java code for a MetaQueue&trade; entity objects
 *  by using the DatabaseMetaData EntityObjectBuilder is a Java
 *  Application that opens a database, walks its schema and produces the Entity
 *  Objects for MetaQueue. Its usage:<br>
 *  java EntityObjectBuilder <i><jdbc-driver><jdbc-url>[<use-morphers>][
 *  <output-dir>][<package-name>
 *
 *  ][table-name]</i> <br>
 *  Where:
 *  <ul>
 *    <li> <i>jdbc-driver</i> Is the JDBC Driver to be used
 *    <li> <i>jdbc-url</i> Is the URL for the database to be used
 *    <li> <i>use-morphers</i> Is the string "true" which forces the use of get
 *    and set methods , and "false" uses the publicly accessible vars - default
 *    is true.
 *    <li> <i>output-dir</i> is the path which the output files are written to.
 *
 *    <li> <i>package-name</i> is the java package that these entities belong
 *    to. By default, the package string will add '.entity' to the package
 *    statement, and add '.value' to the import statement.
 *    <li> <i>table-name</i> is the name of a single table that you want to
 *    generate data for.
 *  </ul>
 *
 * @since Nathan Oertel<address>oertel@metaqueue.net</address> 1/15/06 - Nathan
 *        Oertel, Added conversion to sqlsafe for other useful fields including
 *        blob...
 *
 *@author     Yonah Wolf <address>yonah@bonerosity.com</address> 4/10/02 - Yonah
 *      Wolf, Added modifications for date fields 4/05/02 - Yonah Wolf, Modified
 *      Formatting to match preferred block style 3/15/02 - Yonah Wolf, Initial
 *      Version
 *@created    May 5, 2002
 */

public class EntityObjectBuilder
{

    //Public Vars

    public String login = "";
    public String password = "";
    public String CATALOG = login;
    public String schema= login;
    /**
     *  Description of the Field
     */
    public String driver = "";
    /**
     *  Description of the Field
     */
    public String url = "";
    /**
     *  Description of the Field
     */
    public boolean useMorphers = true;
    /**
     *  Description of the Field
     */
    public String outputDir = "";
    /**
     *  Description of the Field
     */
    public String packageName = "";
    /**
     *  Description of the Field
     */
    public String aiv = "RECORD_ID";
    /**
     *  Description of the Field
     */
    public String aiq = "SELECT ";
    /**
     *  Description of the Field
     */
    public String[] types = {"TABLE"};
    /**
     *  Description of the Field
     */
    public String catalog=CATALOG;
    /**
     *  Description of the Field
     */
    public DatabaseMetaData dbmd;

    //Member Vars
    String importString = "import java.lang.*;\nimport java.sql.*;\nimport java.util.*;\n\nimport com.qkernel.*;\n";
    FileWriter fw = null;
    Connection conn = null;


    /**
     *  This is called when the app is run from the command line
     *
     *@param  args  Description of the Parameter
     */
    public static void main(String args[])
    {
        boolean single = false;
        String table = "";
        EntityObjectBuilder builder = new EntityObjectBuilder(args[0], args[1], args[2], args[3]);
        switch (args.length) {
            case 6:
                single = true;
                table = args[5];
            case 5:
                builder.packageName = args[4];
            case 4:
                builder.outputDir = args[3];
            case 3:
                builder.useMorphers = Boolean.valueOf(args[2]).booleanValue();
            case 2:
                builder.driver = args[0];
                builder.url = args[1];
                break;
            default:
                System.out.println("Usage: <jdbc-driver><jdbc-url>[<create morphers>][<output-dir>][<package name>]");
                System.exit(1);
                break;
        }
        if(single)
	{
            try
	    {
                builder.buildTable(table);
            }
	    catch (Exception e)
	    {
                e.printStackTrace();
                System.exit(1);
            }
        }
	else
	{
            builder.doBuild();
        }
    }


    /**
     *  The default constructor
     *
     *@param  drv  Description of the Parameter
     *@param  url  Description of the Parameter
     */
    public EntityObjectBuilder(String drv, String url, String user, String pass)
    {
        this.url = url;
        this.driver = drv;
        try
	{
            Class.forName(this.driver);
            conn = DriverManager.getConnection(this.url, user, pass);
            this.dbmd = conn.getMetaData();
            this.catalog = conn.getCatalog();

        }
	catch (Exception e)
	{
            e.printStackTrace();
            System.exit(1);
        }
    }


    /**
     *  This function gets all of the tablenames, and then builds the individual
     *  eos
     */

    public void doBuild()
    {
        String nextTable = "";
        try
	{
            ResultSet tables = dbmd.getTables(catalog, schema, null, types);

            while (tables.next())
	    {
                nextTable = tables.getString("TABLE_NAME");
                buildTable(nextTable);
		buildSubTable(nextTable);
            }
            tables.close();
        }
	catch (Exception e)
	{
            System.err.println(nextTable);
            e.printStackTrace();
        }
    }


    /**
     *  This is the wrapper function for a single table
     *
     *@param  nextTable         Description of the Parameter
     *@exception  SQLException  Description of the Exception
     *@exception  IOException   Description of the Exception
     */
    public void buildTable(String nextTable) throws SQLException, IOException
    {

        ResultSet pkeys = dbmd.getPrimaryKeys(catalog,  schema, nextTable);
        ResultSet fkeys = dbmd.getImportedKeys(catalog, schema, nextTable);
//      String pk = getTruePrimaryKey(fkeys, pkeys);
        String pk = getTruePrimaryKey(pkeys, nextTable);
	String pkType ="Int";

        if (pk == null)
	{
            System.out.println("*** WARNING:  No primary key for table "+nextTable+", skipping.");
            return;
        }

        Statement stmt1 = conn.createStatement();
        ResultSet rs1 = stmt1.executeQuery("SELECT * FROM " + nextTable);
        ResultSetMetaData rsmd1 = rs1.getMetaData();

        for (int i = 1; i <= rsmd1.getColumnCount() && pkType.equals("Int"); i++)
	{
            if (rsmd1.getColumnName(i).equals(pk))
	    {
		if(getType(rsmd1.getColumnType(i)).equals("String"))
		    pkType = "String";
	    }
	}
	rs1.close();

        nextTable = format(nextTable);
        System.out.println(nextTable+ " primary key is " + pk);
        String data = "";
        data += writeHeader(nextTable);
	if(!pk.equals("nopk"))
            data += writeFind(nextTable);
        data += getEntityDefinitions(nextTable, pk, pkType);
        data += getEntityRowDefinition(nextTable);
        data += getUpdaterDefinitions(nextTable, pk,  pkType);
        data += getLoaderDefinitions(nextTable, pk, pkType);
        data += writeFooter(nextTable);
        writeToFile(nextTable, data);
        pkeys.close();
        fkeys.close();
    }

    /**
     *  This is the function for a sub-class table
     *
     *@param  nextTable         Description of the Parameter
     *@exception  SQLException  Description of the Exception
     *@exception  IOException   Description of the Exception
     */
    public void buildSubTable(String nextTable) throws SQLException, IOException
    {
        nextTable = format(nextTable);
        String data = "";
        data += writeSubHeader(nextTable);
        data += writeFooter(nextTable);
        writeSubToFile(nextTable, data);
    }


    /**
     *  This function computes and returns the header of the eo Object
     *
     *@param  tablename  Description of the Parameter
     *@return            Description of the Return Value
     */

    public String writeHeader(String tablename)
    {
        String retval = "/* eo_" + tablename + "_gen.java\n";
        retval += " *\n";
        retval += " * THE FOLLOWING CODE IS AUTO GENERATED BY GENDB SCRIPT \n";
        retval += " * !!!!!!!!!!!!  DO NOT MODIFY THIS FILE !!!!!!!!!!!\n";
        retval += " */\n";
        retval += "package " + packageName + ";\n\n";
        retval += importString+"\n\n";
	retval += "@SuppressWarnings({\"unchecked\", \"fallthrough\", \"serial\" })\n";
        retval += "/************************************************************\n";
        retval += " * An entity object for the db table " + tablename + "\n";
        retval += " * \n * \n";
        retval += " * @author Initially created by EntityObjectBuilder\n";
        retval += " * @version $" + "Revision$ \n";
        retval += " ************************************************************\n";
        retval += " */\n";
        retval += "public abstract class eo_" + tablename + "_gen extends EntityObject\n";
        retval += "{\n\n";
        try
	{
            retval += getFieldConstants(tablename);
        }
	catch (Exception e)
	{
            // just ignore it, we can do without the field names I guess
            System.out.println("I'm ignoring this exception: "+e);
        }
          retval += "\n";
        return retval;
    }

    public String writeSubHeader(String tablename)
    {
        String retval = "/* eo_" + tablename + ".java\n";
        retval += " *\n";
        retval += " *****************************************************************************\n";
        retval += " * History:\n";
        retval += " * ========\n";
        retval += " * \n *  \n";
        retval += " *****************************************************************************\n";
        retval += " * \n */ \n";
        retval += "package " + packageName + ";\n\n";
        retval += importString+"\n\n";
        retval += "/************************************************************\n";
        retval += " * An entity object for the db table " + tablename + "\n";
        retval += " * \n * \n";
        retval += " * @author  Initially created by EntityObjectBuilder\n";
        retval += " * @version $" + "Revision$ \n";
        retval += " ************************************************************\n";
        retval += " */\n";
        retval += "public class eo_"+tablename+" extends eo_"+tablename+"_gen\n";
        retval += "{\n\n";
        retval += "\n";

        return retval;
    }

    /**
     *  Build a set of constants in the eo that can be used to refer to field names
     *
     *@param  tablename
     *@return                   The entityRowDefinition value
     *@exception  SQLException  Description of the Exception
     */
    public String getFieldConstants(String tablename) throws SQLException
    {
        String retval = "";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + tablename);
        ResultSetMetaData rsmd = rs.getMetaData();

        int i = 0;
        for (i = 1; i <= rsmd.getColumnCount(); i++)
	{
            retval += "    public final static String FIELD_"+nonUnique(rsmd.getColumnName(i)).toUpperCase()+" = \""+rsmd.getColumnName(i)+"\";\n";
        }
        rs.close();
        stmt.close();
        return retval;
    }

    /**
     * Use this to have the methods in the object refer to the field constants
     * rather than using a string constant all over the place to refer to fields
     * @param databaseFieldName
     * @return
     */
    private String getFieldConstantName(String databaseFieldName)
    {
        return "FIELD_"+nonUnique(databaseFieldName).toUpperCase();
    }

    /**
     *  This function computes and returns the footer of the eo_object
     *
     *@param  tablename  Description of the Parameter
     *@return            Description of the Return Value
     */
    public String writeFooter(String tablename)
    {
        return "}\n\n";
    }


    /**
     *  This function computes and returns the find(), find(id) and
     *  findInCache(id) functions of the eo
     *
     *@param  tablename  Description of the Parameter
     *@return            Description of the Return Value
     */
    public String writeFind(String tablename)
    {
        String retval = "";
        retval += "    /****************************************************\n";
        retval += "     * find(): This method implements find by primary key\n";
        retval += "     * key for the table " + tablename + ".\n";
        retval += "     * If it doesn't find the object in the existing \n";
        retval += "     * cache, it loads the necessary data from the db.\n     *\n";
        retval += "     * @param id The primary key value for the record we\n";
        retval += "     *  searching for\n";
        retval += "     * @return a vo_" + tablename + " object, representing\n";
        retval += "     *  the record\n";
        retval += "     ****************************************************\n";
        retval += "     */\n";
        retval += "    public vo_" + tablename + " find(int id)\n";
        retval += "    {\n";
        retval += "        vo_" + tablename + " e = findInCache(id);\n";
        retval += "\n";
        retval += "        if(e == null)\n";
        retval += "        {\n";
        retval += "            // Try to load cache first\n";
        retval += "            load(id);\n";
        retval += "            return(findInCache(id));\n";
        retval += "        }\n";
        retval += "        else \n";
        retval += "        {\n";
        retval += "            // return cache value\n";
        retval += "            return(e);\n";
        retval += "        }\n";
        retval += "    }\n\n";
        retval += "    /*****************************************************\n";
        retval += "     * This method checks the cache for the requested item\n";
        retval += "     *\n";
        retval += "     * @param id the primary key of the record we're \n";
        retval += "     *  looking for\n     *\n";
        retval += "     * @return a vo_" + tablename + " that represents the \n";
        retval += "     *  record or null, if it doesn't exist\n";
        retval += "     *\n";
        retval += "     *****************************************************\n";
        retval += "     */\n";
        retval += "    public vo_" + tablename + " findInCache(int id)\n";
        retval += "    {\n";
        retval += "        vo_" + tablename + " e = null;\n";
        retval += "        e = (vo_" + tablename + ")entityCache.get(new Integer(id));\n";
        retval += "        return(e);\n";
        retval += "    }\n\n";
        retval += "    /****************************************************\n";
        retval += "     * find(): This method implements find by primary key\n";
        retval += "     * key for the table " + tablename + ".\n";
        retval += "     * If it doesn't find the object in the existing \n";
        retval += "     * cache, it loads the necessary data from the db.\n     *\n";
        retval += "     * @param id The primary key value for the record we\n";
        retval += "     *  searching for\n";
        retval += "     * @return a vo_" + tablename + " object, representing\n";
        retval += "     *  the record\n";
        retval += "     ****************************************************\n";
        retval += "     */\n";
        retval += "    public vo_" + tablename + " find(String id)\n";
        retval += "    {\n";
        retval += "        vo_" + tablename + " e = findInCache(id);\n";
        retval += "\n";
        retval += "        if(e == null)\n";
        retval += "        {\n";
        retval += "            // Try to load cache first\n";
        retval += "            load(id);\n";
        retval += "            return(findInCache(id));\n";
        retval += "        }\n";
        retval += "        else \n";
        retval += "        {\n";
        retval += "            // return cache value\n";
        retval += "            return(e);\n";
        retval += "        }\n";
        retval += "    }\n\n";
        retval += "    /*****************************************************\n";
        retval += "     * This method checks the cache for the requested item\n";
        retval += "     *\n";
        retval += "     * @param id the primary key of the record we're \n";
        retval += "     *  looking for\n     *\n";
        retval += "     * @return a vo_" + tablename + " that represents the \n";
        retval += "     *  record or null, if it doesn't exist\n";
        retval += "     *\n";
        retval += "     *****************************************************\n";
        retval += "     */\n";
        retval += "    public vo_" + tablename + " findInCache(String id)\n";
        retval += "    {\n";
        retval += "        vo_" + tablename + " e = null;\n";
        retval += "        e = (vo_" + tablename + ")entityCache.get(id);\n";
        retval += "        return(e);\n";
        retval += "    }\n\n";
        retval += "    /*****************************************************\n";
        retval += "     * This function returns an ArrayList  of all Value \n";
        retval += "     * Objects in this Entity Object\n";
        retval += "     * @return an ArrayList of all the vo_" + tablename + " \n";
        retval += "     *  objects\n";
        retval += "     *\n";
        retval += "     *****************************************************\n";
        retval += "     */\n";
        retval += "    public ArrayList find()\n";
        retval += "    {\n";
        retval += "        String query = \"SELECT * FROM " + tablename + " \";\n";
        retval += "        ArrayList al = new ArrayList();\n";
        retval += "        executeQuery(query, \"setEntity\", al);\n";
        retval += "        return al;\n";
        retval += "    }\n\n";
        retval += "    /*****************************************************\n";
        retval += "     * This function returns an ArrayList  of all Value \n";
        retval += "     * Objects based on query\n";
        retval += "     * @return an ArrayList of all the vo_" + tablename + " \n";
        retval += "     *  objects\n";
        retval += "     *\n";
        retval += "     *****************************************************\n";
        retval += "     */\n";
        retval += "    public ArrayList executeQueryList(String query)\n";
        retval += "    {\n";
        retval += "        ArrayList al = new ArrayList();\n";
        retval += "        executeQuery(query, \"setEntity\", al);\n";
        retval += "        return al;\n";
        retval += "    }\n\n";
        retval += "    /*****************************************************\n";
        retval += "     * This function returns an ArrayList  of all Value \n";
        retval += "     * Objects based on query\n";
        retval += "     * @return an ArrayList of all the vo_" + tablename + " \n";
        retval += "     *  objects\n";
        retval += "     *\n";
        retval += "     *****************************************************\n";
        retval += "     */\n";
        retval += "    public ArrayList executeQueryList(String query, String mapper)\n";
        retval += "    {\n";
        retval += "        ArrayList al = new ArrayList();\n";
        retval += "        executeQuery(query, mapper, al);\n";
        retval += "        return al;\n";
        retval += "    }\n\n";
        retval += "    /****************************************************\n";
        retval += "     * executeQueryObject(): This method implements \n";
        retval += "     * executeQuery for the table " + tablename + ".\n";
        retval += "     * -- and returns vo_"+ tablename + "  \n";
        retval += "     * @param query string\n";
        retval += "     * @return a vo_" + tablename + " object\n";
        retval += "     ****************************************************\n";
        retval += "     */\n";
        retval += "    public vo_" + tablename + " executeQueryObject(String query)\n";
        retval += "    {\n";
        retval += "        Vector vec = new Vector();\n";
        retval += "        executeQuery(query, \"setEntityV\", vec );\n";
        retval += "        Enumeration enm = vec.elements();\n";
        retval += "        if(enm.hasMoreElements())\n";
        retval += "            return((vo_"+tablename+")enm.nextElement());\n";
        retval += "        else\n";
        retval += "            return(null);\n";
        retval += "    }\n\n";
        retval += "    /****************************************************\n";
        retval += "     * executeQueryObject(): This method implements \n";
        retval += "     * executeQuery for the table " + tablename + ".\n";
        retval += "     * -- and returns vo_"+ tablename + "  \n";
        retval += "     * @param query string\n";
        retval += "     * @return a vo_" + tablename + " object\n";
        retval += "     ****************************************************\n";
        retval += "     */\n";
        retval += "    public vo_" + tablename + " executeQueryObject(String query, String mapper)\n";
        retval += "    {\n";
        retval += "        Vector vec = new Vector();\n";
        retval += "        executeQuery(query, mapper, vec );\n";
        retval += "        Enumeration enm = vec.elements();\n";
        retval += "        if(enm.hasMoreElements())\n";
        retval += "            return((vo_"+tablename+")enm.nextElement());\n";
        retval += "        else\n";
        retval += "            return(null);\n";
        retval += "    }\n\n";
        return retval;
    }


    /**
     *  This function computes and returns the setEntity, and setEntityC
     *  functions for the eo
     *
     *@param  tablename         Description of the Parameter
     *@param  pk                Description of the Parameter
     *@return                   The entityDefinitions value
     *@exception  SQLException  Description of the Exception
     */
    public String getEntityDefinitions(String tablename, String pk, String pkType) throws SQLException
    {
        String retval = "";
        retval += "    /*****************************************************\n";
        retval += "     * This function takes the current row in a ResultSet,\n";
        retval += "     * creates a vo_" + tablename + " object with the data, \n";
        retval += "     * @param rs A result set of a query on the " + tablename + " table\n";
        retval += "     *****************************************************\n";
        retval += "     */\n";
        retval += "     public void setEntity(ResultSet rs, ArrayList al) throws SQLException\n";
        retval += "     {\n";
        retval += "         vo_" + tablename + " e = entityRow(rs);\n";
        retval += "         al.add(e);\n";
        retval += "     }\n\n";
        retval += "    /*****************************************************\n";
        retval += "     * This function takes the current row in a ResultSet,\n";
        retval += "     * creates a vo_" + tablename + " object with the data, \n";
        retval += "     * @param rs A result set of a query on the " + tablename + " table\n";
        retval += "     *****************************************************\n";
        retval += "     */\n";
        retval += "     public void setEntityV(ResultSet rs, Vector vec) throws SQLException\n";
        retval += "     {\n";
        retval += "         vo_" + tablename + " e = entityRow(rs);\n";
        retval += "         vec.addElement(e);\n";
        retval += "     }\n\n";
	if(!pk.equals("nopk"))
	{
        retval += "    /*****************************************************\n";
        retval += "     * This function is similar to the setEntity() method.\n";
        retval += "     * It takes the current row in a ResultSet,\n";
        retval += "     * creates a vo_" + tablename + " object with the data, \n";
        retval += "     * but instead of placing it in the cache,\n";
        retval += "     * it places it in the ArrayList that is passed in.\n    *\n";
        retval += "     * @param al An ArrayList to place the resulting vo_" + tablename + " object into.\n";
        retval += "     * @param rs A result set of a query on the " + tablename + " table\n";
        retval += "     * @see #setEntity()\n";
        retval += "     ****************************************************\n";
        retval += "     */\n";
        retval += "    public void setEntityC(ResultSet rs) throws SQLException\n";
        retval += "    {\n";
        retval += "        vo_" + tablename + " e = entityRow(rs);\n";
	if(pkType.equals("Int"))
	{
        retval += "        entityCache.put(new Integer(e."+nonUnique(pk)+"), e);\n";
	}
	else
	{
        retval += "        entityCache.put(e."+nonUnique(pk)+", e);\n";
	}
        retval += "     }\n\n";
	}
        return retval;
    }


    /**
     *  This function computes and returns the entityRow function for the eo
     *
     *@param  tablename         Description of the Parameter
     *@return                   The entityRowDefinition value
     *@exception  SQLException  Description of the Exception
     */
    public String getEntityRowDefinition(String tablename) throws SQLException
    {
        String retval = "";
        retval += "    /****************************************************\n";
        retval += "     * This function maps an SQL row to a Value Object\n";
        retval += "     *\n";
        retval += "     * @param rs a ResultSet that contains the record to be mapped\n";
        retval += "     * @return a vo_" + tablename + " value object\n";
        retval += "     ****************************************************\n";
        retval += "     */\n";
        retval += "    public vo_" + tablename + " entityRow(ResultSet rs) throws SQLException\n";
        retval += "    {\n";
        retval += "        vo_" + tablename + " e = new vo_" + tablename + "();\n";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + tablename);
        ResultSetMetaData rsmd = rs.getMetaData();

        int i = 0;
        for (i = 1; i <= rsmd.getColumnCount(); i++)
	{
            if (useMorphers)
	    {
		if((getType(rsmd.getColumnType(i))).equals("Timestamp"))
		    retval += "        e.set" + nonUnique(rsmd.getColumnName(i)) + "(rs.getString(" + getFieldConstantName(rsmd.getColumnName(i)) + "));\n";
		else
		    retval += "        e.set" + nonUnique(rsmd.getColumnName(i)) + "(rs.get" + getType(rsmd.getColumnType(i)) + "(" + getFieldConstantName(rsmd.getColumnName(i)) + "));\n";

            }
	    else
	    {
		if((getType(rsmd.getColumnType(i))).equals("Timestamp"))
                    retval += "        e." + nonUnique(rsmd.getColumnName(i)) + "= rs.getString(" + getFieldConstantName(rsmd.getColumnName(i)) + ");\n";
		else
                    retval += "        e." + nonUnique(rsmd.getColumnName(i)) + "= rs.get" + getType(rsmd.getColumnType(i)) + "(" + getFieldConstantName(rsmd.getColumnName(i)) + ");\n";
            }
        }
        rs.close();
        stmt.close();
        retval += "        return(e);\n";
        retval += "    }\n\n";
        return retval;
    }


    /**
     *  This function returns the Java type for a given data field property
     *
     *@param  type  Description of the Parameter
     *@return       The type value
     */
    public String getType(int type)
    {
        switch (type)
 	{
            case -1:
            case 2:
            case 4:
                return "Int";
            case 93:
                return "Timestamp";
            default:
                return "String";
        }
    }


    /**
     *  This function computes and returns the definitions of insert, insert2
     *  and update functions of an eo
     *
     *@param  tablename         Description of the Parameter
     *@param  pk                Description of the Parameter
     *@return                   The updaterDefinitions value
     *@exception  SQLException  Description of the Exception
     */
    public String getUpdaterDefinitions(String tablename, String pk,  String pkType) throws SQLException
    {
        String retval = "";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " + tablename);
        ResultSetMetaData rsmd = rs.getMetaData();
        String insert = getInsertString(tablename, rsmd, pk, pkType, true);
        String insert2 = getInsertString(tablename, rsmd, pk, pkType, false);
        String update = getUpdateString(tablename, rsmd, pk, pkType);

        retval += "    /****************************************************\n";
        retval += "     * This method inserts a new record into the table,\n";
        retval += "     * but DOES NOT return the new ID or a reference object\n";
        retval += "     * @param vo_" + tablename + " a value object to be written\n";
        retval += "     *  to the DB\n";
        retval += "     * @see #insert2()\n";
        retval += "     ****************************************************\n";
        retval += "     */\n";
        retval += "    public void insert(vo_" + tablename + " e){\n";
        retval += "        StringBuffer qsb=new StringBuffer();\n";
        retval += insert + "\n";
        retval += "        executeUpdate(qsb.toString());\n";
        retval += "    }\n\n";
        retval += "    /****************************************************\n";
        retval += "     * This method inserts a new record into the table,\n";
        retval += "     * using the local entityQueue\n";
        retval += "     * @param vo_" + tablename + " a value object to be written\n";
        retval += "     *  to the DB\n";
        retval += "     * @see #insert2()\n";
        retval += "     ****************************************************\n";
        retval += "     */\n";
        retval += "    public void insertQueue(vo_" + tablename + " e){\n";
        retval += "        StringBuffer qsb=new StringBuffer();\n";
        retval += insert + "\n";
        retval += "        entityQueue.put(qsb.toString());\n";
        retval += "    }\n\n";
	if(!pk.equals("nopk") && !pkType.equals("String"))
	{
        retval += "    /****************************************************\n";
        retval += "     * This method inserts a new record into the table,\n";
        retval += "     * AND returns a reference object with the new\n";
        retval += "     * auto-generated PK;\n     *\n";
        retval += "     * @param vo_" + tablename + " a value object to be written to the DB\n ";
        retval += "     * @return a vo_" + tablename + " object that represents\n";
        retval += "     *  the original object, with the addition of the primary key\n";
        retval += "     * @see #insert()\n";
        retval += "     ****************************************************\n";
        retval += "     */\n";
        retval += "    public vo_" + tablename + " insert2(vo_" + tablename + " e)\n";
        retval += "    {\n";
        retval += "        vo_" + tablename + " e1=(vo_" + tablename + ") e.clone();\n\n";
        retval += "        StringBuffer qsb=new StringBuffer();\n";
        retval += insert + "\n";
        retval += "        try\n";
        retval += "        {\n";
	retval += "            Connection conn    = entityDbPool.getConnection();\n";
	retval += "            Statement stmt     = conn.createStatement();\n";
	retval += "            stmt.executeUpdate(qsb.toString());\n";
	retval += "            //------------------------------------------------------------------\n";
	retval += "            // Retrieve new AUTO_INCREMENT "+pk+" \n";
	retval += "            //------------------------------------------------------------------\n";
	retval += "            ResultSet r = stmt.executeQuery(\"SELECT LAST_INSERT_ID() AS cId\");\n";
	retval += "            r.next();\n";
	retval += "            e1."+pk+"= r.getInt(\"cId\");\n";
	retval += "            stmt.close();\n";
	retval += "            entityDbPool.freeConnection(conn);\n";
	retval += "        }\n";
	retval += "        catch(Exception ept)\n";
	retval += "        {\n";
	retval += "            daemon.eventLog.sendMessage(ept);\n";
	retval += "        }\n";
        retval += "        return(e1);\n";
        retval += "    }\n\n";
	}
	if(!pk.equals("nopk"))
	{
        retval += "    /****************************************************\n";
        retval += "     * This function updates the record in the database\n";
        retval += "     * who's primary key matches the \n";
        retval += "     * " + pk + " value of the passed-in \n";
        retval += "     * vo_" + tablename + " object.\n     *\n";
        retval += "     * @param e a vo_" + tablename + " object\n";
        retval += "     ****************************************************\n";
        retval += "     */\n";
        retval += "    public void update(vo_" + tablename + " e)\n";
        retval += "    {\n";
        retval += "        StringBuffer qsb=new StringBuffer();\n";
        retval += update + "\n";
        retval += "        executeUpdate(qsb.toString());\n";
        retval += "    }\n\n";
        retval += "    /****************************************************\n";
        retval += "     * This function updates the record in the database\n";
        retval += "     * who's primary key matches the \n";
        retval += "     * " + pk + " value of the passed-in \n";
        retval += "     * vo_" + tablename + " object.\n     *\n";
        retval += "     * @param e a vo_" + tablename + " object\n";
        retval += "     ****************************************************\n";
        retval += "     */\n";
        retval += "    public void updateQueue(vo_" + tablename + " e)\n";
        retval += "    {\n";
        retval += "        StringBuffer qsb=new StringBuffer();\n";
        retval += update + "\n";
        retval += "        entityQueue.put(qsb.toString());\n";
        retval += "    }\n\n";
        retval += "    /****************************************************\n";
        retval += "     * delete(id): This method deletes the object in the\n";
        retval += "     * who's primary ket matches the value passed in\n";
        retval += "     * \n";
        retval += "     * @param id a "+pkType+" representing the primary key\n";
        retval += "     ****************************************************\n";
        retval += "     */\n";
        retval += "    public void delete(int id)\n";
        retval += "    {\n";
        retval += "        String query;\n";
	if(pkType.equals("Int"))
	{
        retval += "        query=\"DELETE FROM " + tablename + " WHERE " + pk + "=\"+id;\n";
	}
	else
	{
        retval += "        query=\"DELETE FROM " + tablename + " WHERE "+pk+"='\"+id+\"'\";\n";
	}
        retval += "        executeUpdate(query);\n";
        retval += "    }\n\n";
	}
        rs.close();
        stmt.close();
        return retval;
    }


    /**
     *  This function computes the SQL to insert a record for the given table
     *
     *@param  tablename         Description of the Parameter
     *@param  rsmd              Description of the Parameter
     *@param  pk                Description of the Parameter
     *@param  auto              Description of the Parameter
     *@return                   The insertString value
     *@exception  SQLException  Description of the Exception
     */
    public String getInsertString(String tablename, ResultSetMetaData rsmd, String pk, String pkType, boolean auto) throws SQLException
    {
        String retval = "";
        retval += "        qsb.append(\"INSERT INTO " + tablename + "(\");\n";
        for (int i = 1; i <= rsmd.getColumnCount(); i++)
	{
	    if (!((rsmd.getColumnName(i)).equals(pk)))
	    {
            	retval += "        qsb.append(" + getFieldConstantName(rsmd.getColumnName(i));
            	if (i != rsmd.getColumnCount())
		{
                    retval += "+\",\");\n";
            	}
		else
		{
                    retval += "+\")\");\n";
            	}
	    }
	    else if(pkType.equals("String"))
	    {
            	retval += "        qsb.append(" + getFieldConstantName(rsmd.getColumnName(i));
            	if (i != rsmd.getColumnCount())
		{
                    retval += "+\",\");\n";
            	}
		else
		{
                    retval += "+\")\");\n";
            	}
	    }
        }
        retval += "        qsb.append(\"VALUES(\");\n";
        for (int i = 1; i <= rsmd.getColumnCount(); i++)
	{
            if (!format(rsmd.getColumnName(i)).equals(pk))
	    {
                retval += "        qsb.append(\"" + getQualifiedField("e", rsmd.getColumnName(i), rsmd.getColumnType(i));

            	if (i != rsmd.getColumnCount())
	    	{
                    retval += ",\");\n";
            	}
	    	else
	    	{
                    retval += ")\");\n\n";
            	}
            }
	    else if(pkType.equals("String"))
	    {
                retval += "        qsb.append(\"" + getQualifiedField("e", rsmd.getColumnName(i), rsmd.getColumnType(i));

            	if (i != rsmd.getColumnCount())
	    	{
                    retval += ",\");\n";
            	}
	    	else
	    	{
                    retval += ")\");\n\n";
            	}
	    }
        }
        return retval;
    }


    /**
     *  This function computers the SQL needed to update a record in the table
     *
     *@param  tablename         Description of the Parameter
     *@param  rsmd              Description of the Parameter
     *@param  pk                Description of the Parameter
     *@return                   The updateString value
     *@exception  SQLException  Description of the Exception
     */
    public String getUpdateString(String tablename, ResultSetMetaData rsmd, String pk, String pkType) throws SQLException
    {
        String retval = "";
        retval += "        qsb.append(\"UPDATE " + tablename + " \").append(\"SET \");\n";
        for (int i = 1; i <= rsmd.getColumnCount(); i++)
	{

            if (!(rsmd.getColumnName(i).equals(pk)))
	    {
                retval += "        qsb.append(" + getFieldConstantName(rsmd.getColumnName(i)) + "+\"=" + getQualifiedField("e", rsmd.getColumnName(i), rsmd.getColumnType(i));
                if (i != rsmd.getColumnCount())
		{
                    retval += ",\");\n";
                }
		else
		{
                    retval += "\");\n";
                }
            }
        }
        retval += "        qsb.append(\" WHERE \");\n";
	if(pkType.equals("Int"))
	{
        retval += "                qsb.append(\"" + pk + "=\"+e." + nonUnique(pk) + ");\n";
	}
	else
	{
        retval += "                qsb.append(\"" + pk + "='\"+e."+nonUnique(pk)+"+\"'\");\n";
	}
        return retval;
    }


    /**
     *  This returns a field wrapped in quotes if it is a String, or not if it
     *  is an int
     *
     *@param  label  Description of the Parameter
     *@param  field  Description of the Parameter
     *@param  type   Description of the Parameter
     *@return        The qualifiedField value
     */
    public String getQualifiedField(String label, String field, int type)
    {
        String retval = "";
        field = format(field);
        if (useMorphers)
	{
            retval += label + ".get" + nonUnique(field) + "()";
            /*if (nonUnique(field).equals("Account_id")) {
                System.out.println("Type for "+field+": "+type+" Char: "+Types.CHAR+", VARCHAR"+Types.VARCHAR);
            }*/
        }
	else
	{
            retval += label + "." + field;
        }
        switch (type)
    	{
            case Types.NUMERIC:
                retval = "\").append(" + retval + ").append(\"";
                break;
            case Types.INTEGER:
                retval = "\").append(" + retval + ").append(\"";
                break;
            case Types.CHAR:
                retval = "'\").append(SqlSafe.sqlSafe( " + retval + ")).append(\"'";
                break;
            case Types.VARCHAR:
                retval = "'\").append(SqlSafe.sqlSafe( " + retval + ")).append(\"'";
                break;
            case Types.LONGVARCHAR:
                retval = "'\").append(SqlSafe.sqlSafe( " + retval + ")).append(\"'";
                break;
            case Types.BLOB:
                retval = "'\").append(SqlSafe.sqlSafe( " + retval + ")).append(\"'";
                break;
            case Types.BINARY:
                retval = "'\").append(SqlSafe.sqlSafe( " + retval + ")).append(\"'";
                break;
            case Types.VARBINARY:
                retval = "'\").append(SqlSafe.sqlSafe( " + retval + ")).append(\"'";
                break;
            case Types.LONGVARBINARY:
                retval = "'\").append(SqlSafe.sqlSafe( " + retval + ")).append(\"'";
                break;
            case Types.TIMESTAMP:
                retval = "'\").append(SqlSafe.sqlSafe( " + retval + ")).append(\"'";
                break;
            default:
                retval = "'\").append(" + retval + ").append(\"'";
        }
        return retval;
    }


    /**
     *  This function computes and returns the load(),load(id), getCount(), and
     *  start() methods of the eo
     *
     *@param  tablename  Description of the Parameter
     *@param  pk         Description of the Parameter
     *@return            The loaderDefinitions value
     */
    public String getLoaderDefinitions(String tablename, String pk, String pkType)
    {
        String retval = "";
	if(!pk.equals("nopk"))
	{
        retval += "    /****************************************************\n";
        retval += "     * This is the default load method that is called by\n";
        retval += "     * the container at startup\n";
        retval += "     ****************************************************\n";
        retval += "     */\n";
        retval += "    public void load()\n";
        retval += "    {\n";
        retval += "         //This is empty for now\n\n";
        retval += "    }\n\n";
	if(pkType.equals("Int"))
	{
        retval += "    /****************************************************\n";
        retval += "     * This function loads an individual record into the\n";
        retval += "     * cache\n     *\n";
        retval += "     * @param id The primary key to use for record lookup\n";
        retval += "     ****************************************************\n";
        retval += "     */\n";
        retval += "    public void load(int id)\n";
        retval += "    {\n";
        retval += "       String qs=\n";
        retval += "           \"SELECT * FROM " + tablename + " WHERE " + pk + "=\"+id;\n\n";
        retval += "       executeQuery(qs,\"setEntityC\");\n";
        retval += "    }\n\n";
        retval += "    /****************************************************\n";
        retval += "     * This function loads an individual record into the\n";
        retval += "     * cache\n     *\n";
        retval += "     * @param id The  key to use for record lookup\n";
        retval += "     ****************************************************\n";
        retval += "     */\n";
        retval += "    public void load(String id)\n";
        retval += "    {\n";
        retval += "         //Override this method to implement cache scheme\n\n";
        retval += "    }\n\n";
	}
	else
	{
        retval += "    /****************************************************\n";
        retval += "     * This function loads an individual record into the\n";
        retval += "     * cache\n     *\n";
        retval += "     * @param id The primary key to use for record lookup\n";
        retval += "     ****************************************************\n";
        retval += "     */\n";
        retval += "    public void load(String id)\n";
        retval += "    {\n";
        retval += "       String qs=\n";
        retval += "           \"SELECT * FROM " + tablename + " WHERE " + pk + "='\"+id+\"'\";\n\n";
        retval += "       executeQuery(qs,\"setEntityC\");\n";
        retval += "    }\n\n";
        retval += "    /****************************************************\n";
        retval += "     * This function loads an individual record into the\n";
        retval += "     * cache\n     *\n";
        retval += "     * @param id The  key to use for record lookup\n";
        retval += "     ****************************************************\n";
        retval += "     */\n";
        retval += "    public void load(int id)\n";
        retval += "    {\n";
        retval += "         //Override this method to implement cache scheme\n\n";
        retval += "    }\n\n";
	}
        retval += "    /****************************************************\n";
        retval += "     * This function returns the number of records\n";
        retval += "     * in the table represented by this entity. \n";
        retval += "     * \n";
        retval += "     * @return The number of records in the table\n";
        retval += "     ****************************************************\n";
        retval += "     */\n";
        retval += "    public int getCount()\n";
        retval += "    {\n";
        retval += "        int count=0;\n";
        retval += "        String qs=\n";
        retval += "            \"SELECT COUNT(" + pk + ") AS Count FROM " + tablename + " \";\n";
        retval += "        ResultSet r=executeQuery(qs);\n";
        retval += "        try\n";
        retval += "        {\n";
        retval += "            r.next();\n";
        retval += "            count=r.getInt(\"Count\");\n";
        retval += "            closeResultSet(r);\n";
        retval += "        }\n";
        retval += "        catch(Exception e)\n ";
        retval += "        {\n";
        retval += "            daemon.event_log.sendMessage(\"Can't retrieve ResultSet because: \"+e.getMessage());\n";
        retval += "        }\n";
        retval += "        return(count);\n";
        retval += "    }\n\n";
	}
        retval += "    /****************************************************\n";
        retval += "     * This function is called by the daemon when this \n";
        retval += "     * Entity Object is initialized\n";
        retval += "     * Sub-class should invoke super.init() then:\n";
        retval += "     * entityCache.start(t)\n";
        retval += "     * useLocalEntityQueue() (if you want a local queue)\n";
        retval += "     ****************************************************\n";
        retval += "     */\n";
        retval += "    public void init()\n";
        retval += "    {\n";
        retval += "        entityQueue.start();          // Start Global Entity Queue\n";
        retval += "        //entityCache.start(30);      // Start Entity Cache\n";
        retval += "        //useLocalEntityQueue();      // Set and Start Local Entity Queue\n";
        retval += "    }\n\n";
        return retval;
    }


    /**
     *  This function writes an eo to a file in the form <i>eo_Tablename.java
     *  </i>
     *
     *@param  tablename        Description of the Parameter
     *@param  data             Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void writeToFile(String tablename, String data) throws IOException
    {
        fw = new FileWriter(outputDir + "/eo_" + tablename + "_gen.java");
        fw.write(data, 0, data.length());
        fw.flush();
        fw.close();
    }

    /**
     *  This function writes an eo to a file in the form <i>eo_Tablename.java
     *  </i>
     *
     *@param  tablename        Description of the Parameter
     *@param  data             Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void writeSubToFile(String tablename, String data) throws IOException
    {
	File fname = new File(outputDir + "/eo_" + tablename + ".java");
	if(!fname.exists())
	{
	    fw = new FileWriter(fname);
	    fw.write(data, 0, data.length());
	    fw.flush();
	    fw.close();
	}
    }


    /**
     *  This function formats and returns the string passed in, so that only the
     *  first letter is capitalized
     *
     *@param  name  Description of the Parameter
     *@return       Description of the Return Value
     */
    public String format(String name)
    {
	return(name); // No format change

	/**** mgill stub for metaQueue DB's

        StringBuffer sb = new StringBuffer(name.toLowerCase());
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
	****/
    }

    private String nonUnique(String param)
    {
	return(param); // No format change

	/**** mgill stub for metaQueue DB's
        String firstbit = param.substring(0, param.indexOf('_'));
        if (firstbit.length()>3)
	{
            // this ain't no proper field name
            System.out.println("I'm not formatting this thing: "+param);
            return param;
        }
	else return format(param.substring(param.indexOf('_')+1));
	****/
   }



    /**
     *  This function determines the value of the table's primary key from
     *  metadata
     *
     *@param  primary           Description of the Parameter
     *@param  tablename         Description of the Parameter
     *@return                   The truePrimaryKey value
     *@exception  SQLException  Description of the Exception
     */
    public String getTruePrimaryKey(ResultSet primary, String tablename) throws SQLException
    {
        Hashtable p = new Hashtable();
        try
	{
            while (primary.next())
	    {
                p.put(primary.getString("COLUMN_NAME"), primary.getString("COLUMN_NAME"));
            }
        }
	catch (Exception e)
	{
            e.printStackTrace();
        }

	String pk = (String)p.get(tablename+"_id");

	if(pk == null)
	{
	    pk = (String)p.get(tablename+"Id");
	    if(pk == null)
	        pk ="nopk";		
	} 
	return(pk);
    }

    /**
     *  This function determines the value of the table's primary key from
     *  metadata
     *
     *@param  foreign           Description of the Parameter
     *@param  primary           Description of the Parameter
     *@return                   The truePrimaryKey value
     *@exception  SQLException  Description of the Exception
     */
    public String getTruePrimaryKey(ResultSet foreign, ResultSet primary) throws SQLException
    {

        String pk = "";

        String seq1 = "";
        String nextp = "";
        String nexts;
        Hashtable p = new Hashtable();
        Hashtable f = new Hashtable();
        try
	{
            while (primary.next())
	    {
                p.put(primary.getString("COLUMN_NAME"), primary.getString("KEY_SEQ"));
            }
            while (foreign.next())
	    {
                f.put(foreign.getString("COLUMN_NAME"), foreign.getString("COLUMN_NAME"));
            }
        }
	catch (Exception e)
	{
            e.printStackTrace();
        }
        Enumeration e = p.keys();
        int highseq = 99;
        int seq = 0;
        while (e.hasMoreElements())
	{
            nextp = (String) e.nextElement();
            if (f.get(nextp) == null)
	    {
                seq = Integer.parseInt((String) p.get(nextp));
                if (seq < highseq)
		{
                    pk = nextp;
                    highseq = seq;
                }
            }
            nexts = (String) p.get(nextp);
            if (nexts.equals("1"))
	    {
                seq1 = nextp;
            }
        }
        if (pk.equals(""))
	{
            if (!seq1.equals(""))
                return format(seq1);
            else return null;
        }
	else
	{
            return format(pk);
        }
    }
}

