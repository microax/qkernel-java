package com.qkernel.eobuilder;

import java.sql.*;
import java.io.*;
import java.util.*;

/**
 *  This is a Java Application that opens a database, walks its schema and
 *  produces the Value Objects for MetaQueue &trade;. 3/14/04 - Initial Version
 *
 *@author     mhall
 *@created    May 5, 2004
 *
 * Revised By: Nathan Oertel
 * Revision Date: Mar 21, 2006
 * Revision: Added getString method declaration
 * Purpose: This method is being used to access string versions of the fields for replace tags
 */
public class ValueObjectBuilder
{

    //Public Vars

    private String LOGIN = "";
    private String PASSWORD = "";
    private String CATALOG = LOGIN;
    private final String schema = LOGIN;


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
    public boolean createMorphers = false;
    /**
     *  Description of the Field
     */
    public String outputDir = "";
    /**
     *  Description of the Field
     */
    public String packageName = "";

    //Member Vars
    String importString = "import com.qkernel.*;\n";
    FileWriter fw = null;
    Connection conn = null;
    Hashtable types = new Hashtable();

    // Constants
    public static String PROPTYPE_STRING = "String";
    public static String PROPTYPE_INT = "int";
    public static String PROPTYPE_DATE = "java.util.Date";


    /**
     *  This is the function called when the app is run, see the banner for
     *  usage
     *
     *@param  args  Description of the Parameter
     */
    public static void main(String args[])
    {

        boolean single = false;
        String table = "";
        ValueObjectBuilder bv;
        try
  	{
            bv = new ValueObjectBuilder(args[0], args[1]);

            switch (args.length)
	    {
                case 6:
                    single = true;
                    table = args[5];
                case 5:
                    bv.packageName = args[4];
                case 4:
                    bv.outputDir = args[3];
                case 3:
                    bv.createMorphers = Boolean.valueOf(args[2]).booleanValue();
                case 2:
                    bv.driver = args[0];
                    bv.url = args[1];
                    break;
                default:
                    System.out.println("Usage: <jdbc-driver><jdbc-url>[<create morphers>][<output-dir>][<package name>]");
                    System.exit(1);
                    break;
            }

            if (!single)
	    {
                bv.doBuild();
            }
	    else
	    {
                try
		{
                    bv.getTableSchema(table);
                }
		catch (Exception ex)
		{
                    ex.printStackTrace();
                }
            }
        }
	catch (Exception e)
	{
            e.printStackTrace();
            System.exit(1);
        }
    }


    /**
     *  The default Constructor
     *
     *@param  driver                      Description of the Parameter
     *@param  url                         Description of the Parameter
     *@exception  SQLException            Description of the Exception
     *@exception  ClassNotFoundException  Description of the Exception
     */
    public ValueObjectBuilder(String driver, String url) throws SQLException, ClassNotFoundException
    {
        this.url = url;
        this.driver = driver;
        Class.forName(this.driver);
        conn = DriverManager.getConnection(this.url, LOGIN, PASSWORD);
    }


    /**
     *  This is the function that actually builds the VOs
     */
    public void doBuild()
    {
        String nextTable = "";
        try
	{

            DatabaseMetaData dbmd = conn.getMetaData();
            String catalog = conn.getCatalog();
            String[] types = {"TABLE"};
            ResultSet tables = dbmd.getTables(catalog, schema, null, types);
            while (tables.next())
	    {
                nextTable = format(tables.getString("TABLE_NAME"));
                System.out.println("Working on table "+nextTable);
                getTableSchema(nextTable);
		makeSubClazz(nextTable);
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
     *  This function builds the individual vo sub classes. If you want to re-run the
     *  application for individual tables, simply instantiate
     *  ValueObjectBuilder, and call this with the name of a single table *
     *
     *@param  table             Description of the Parameter
     *@exception  SQLException  Description of the Exception
     *@exception  IOException   Description of the Exception
     */
    public void makeSubClazz(String table) throws SQLException, IOException
    {
        String output = getSubTableHeader(table);

        output += getTableFooter(table);

        writeSubToFile(table, output);
    }



    /**
     *  This function builds the individual vo's. If you want to re-run the
     *  application for individual tables, simply instantiate
     *  ValueObjectBuilder, and call this with the name of a single table *
     *
     *@param  table             Description of the Parameter
     *@exception  SQLException  Description of the Exception
     *@exception  IOException   Description of the Exception
     */

    public void getTableSchema(String table) throws SQLException, IOException
    {
        table = format(table);
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM " + table);
        ResultSetMetaData rsmd = rs.getMetaData();
        int i = 0;
        String output = getTableHeader(table);

        StringBuffer sbDefs = new StringBuffer(1000);
        StringBuffer sbMorphers = new StringBuffer(1000);
        StringBuffer sbBuild = new StringBuffer(1000);
        StringBuffer sbEncapsulate = new StringBuffer(1000);
        StringBuffer sbGetString = new StringBuffer(1000);
        StringBuffer sbSetFields = new StringBuffer(1000);

        sbBuild.append( getBuildStart() );
        sbEncapsulate.append ( getEncapsulateStart() );
        sbGetString.append( getGetStringStart());
        sbSetFields.append(getSetFieldsStart());

        for (i = 1; i <= rsmd.getColumnCount(); i++)
	    {
            sbDefs.append( getPropertyDefinition(format(rsmd.getColumnName(i)), rsmd.getColumnType(i), rsmd.getColumnTypeName(i)) );
            if ( createMorphers )
	        {
                sbMorphers.append ( getMorpherDefinition(format(rsmd.getColumnName(i)), rsmd.getColumnType(i), rsmd.getColumnDisplaySize(i)) );
            }
            sbBuild.append( getBuildLine(format(rsmd.getColumnName(i)), rsmd.getColumnType(i), rsmd.getColumnDisplaySize(i)) );
            sbEncapsulate.append ( getEncapsulateLine(rsmd.getColumnName(i),rsmd.getColumnType(i)) );
            sbGetString.append(getGetStringLine(table, format(rsmd.getColumnName(i)), rsmd.getColumnType(i)));
            sbSetFields.append(getSetFieldsLine(table, format(rsmd.getColumnName(i)), rsmd.getColumnType(i)));
        }
        sbBuild.append( getBuildEnd() );
        sbEncapsulate.append ( getEncapsulateEnd() );
        sbGetString.append(getGetStringEnd());
        sbSetFields.append(getSetFieldsEnd());

        output += sbDefs.toString ();
        output += sbMorphers.toString ();
        output += sbGetString.toString();
        output += sbSetFields.toString();
        //output += sbBuild.toString ();
        //output += sbEncapsulate.toString ();
        //output += getValidateStub();
        //output += getErrorTextDefinition();

        output += getTableFooter(table);
        writeToFile(table, output);
        rs.close();
        st.close();
    }


    /**
     *  This function computes and returns the header for each vo
     *
     *@param  tablename  Description of the Parameter
     *@return            The tableHeader value
     */
    public String getTableHeader(String tablename)
    {

        String retval = "/* vo_" + tablename + "_gen \n";
        retval += " *\n";
        retval += " * THE FOLLOWING CODE IS AUTO GENERATED BY GENDB SCRIPT \n";
        retval += " * !!!!!!!!!!!!  DO NOT MODIFY THIS FILE !!!!!!!!!!!\n";
        retval += " */\n";
        retval += "package " + packageName + ";\n\n" + importString + "\n";
        Calendar c = Calendar.getInstance();
        c.setTime(new java.util.Date(System.currentTimeMillis()));
	retval += "@SuppressWarnings({\"unchecked\", \"fallthrough\", \"serial\" })\n";
        retval += "/************************************************************\n";
        retval += " * A ValueObject for the db table " + tablename + "\n";
        retval += " * @author Initially created by ValueObjectBuilder\n";
        retval += " * @version $" + "Revision$ \n";
        retval += " ************************************************************\n";
        retval += " */\n";
        retval += "public abstract class vo_" + tablename + "_gen extends CloneableObject\n{\n";

        return retval;
    }




    /**
     *  This function computes and returns the header for each vo Subclass
     *
     *@param  tablename  Description of the Parameter
     *@return            The tableHeader value
     */
    public String getSubTableHeader(String tablename)
    {

        String retval = "/* vo_" + tablename + " \n";
        retval += " *\n";
        retval += " *****************************************************************************\n";
        retval += " * History:\n";
        retval += " * ========\n";
        retval += " * \n *  \n";
        retval += " *****************************************************************************\n";
        retval += " * \n */ \n";
        retval += "package " + packageName + ";\n\n" + importString + "\n";
        Calendar c = Calendar.getInstance();
        c.setTime(new java.util.Date(System.currentTimeMillis()));
        retval += "/************************************************************\n";
        retval += " * A ValueObject for the db table " + tablename + "\n";
        retval += " * @author Initially created by ValueObjectBuilder\n";
        retval += " * @version $" + "Revision$ \n";
        retval += " ************************************************************\n";
        retval += " */\n";
        retval += "public class vo_" + tablename + " extends vo_"+tablename+"_gen\n{\n";
	//retval += "\n    String errorText = \"\";       // Contains any errors that this object has after validate()\n";
        return retval;
    }


    /**
     *  This function computes and returns the footer for each vo
     *
     *@param  tablename  Description of the Parameter
     *@return            The tableFooter value
     */
    public String getTableFooter(String tablename)
    {
        return "\n}\n\n";
    }


    /**
     *  This function returns a property definition for a given column name,
     *  type and database type definition
     *
     *@param  propname  Description of the Parameter
     *@param  proptype  Description of the Parameter
     *@param  dbtype    Description of the Parameter
     *@return           a String in the form 'javaType Propname=""'
     */
    public String getPropertyDefinition(String propname, int proptype, String dbtype)
    {
        String Jtype;
        String Jdefault = "";
        propname = nonUnique(propname);
        switch (proptype)
	{
            case -1:
            case 2:
            case 4:
                Jtype = "int";
                Jdefault = "0";
                break;
//	    case 93:
//	    	Jtype = "java.util.Date";
//	    	Jdefault = "new java.util.Date()";
//		break;
            default:
                Jtype = "String";
                Jdefault = "\"\"";
        }

        String retval = "\n    /** " + propname + " database type:" + dbtype + " */\n    public " + Jtype + " " + propname + "=" + Jdefault + ";\n";
        return retval;
    }


    /**
     *  This function builds the getter and setter for a given column
     *
     *@param  propname    Description of the Parameter
     *@param  proptype    Description of the Parameter
     *@param  proplength  Description of the Parameter
     *@return             The morpherDefinition value
     */
    public String getMorpherDefinition(String propname, int proptype, int proplength)
    {
        String Jtype;
        String Jdefault = "";
        boolean isDate = false;
        boolean isHidden = false;
        switch (proptype)
	{
            case -1:
            case 2:
            case 4:
                Jtype = "int";
                Jdefault = "0";
                break;
            case 93:
                Jtype = "java.util.Date";
                isDate = true;
                if (propname.equals("Create_date") || propname.equals("Update_date")) {
                    isHidden = true;
                }
                break;
            default:
                Jtype = "String";
                Jdefault = "\"\"";
        }

        // In this case, we'll use the non-unique version of the field name
        propname = nonUnique(propname);

        String retval = "";
        if (Jtype == "String")
	{
            retval += "\n    /** Sets the value of the " + propname + " property.";
            retval += "\n      * If the value passed is longer than " + proplength + ", it will be truncated.";
            retval += "\n      * @param prop the value to set " + propname + " to.";
            retval += "\n      */";
            retval += "\n    public void set" + propname + "(" + Jtype + " prop)";
            retval += "\n    {";
            retval += "\n        if(prop!=null) {";
            retval += "\n            prop = prop.trim();";
            retval += "\n            if (prop.length() > "+proplength+") { prop = prop.substring(0,"+proplength+"); }";
            retval += "\n            this." + propname + " = prop;";
            retval += "\n        }";
            retval += "\n        else this." + propname + " = \"\";";
            retval += "\n    }";
            retval += "\n ";
            retval += "\n    /** Gets the current value of the " + propname + " property";
            retval += "\n      * @return a " + Jtype + " object containing the value of " + propname;
            retval += "\n      */";
            retval += "\n    public " + Jtype + " get" + propname + "()";
            retval += "\n    {";
            retval += "\n        return this." + propname + ";";
            retval += "\n    }";
            retval += "\n";
        }
	else if (Jtype == "java.util.Date")
	{
            retval += "\n    /** Sets the value of the " + propname + " property. ";
            retval += "\n      * This method is passed two strings - ,";
            retval += "\n      * One represents the date, the other represents a ";
            retval += "\n      * java.text.SimpleDateFormat compliant mask";
            retval += "\n      * @param prop A String representing the date";
            retval += "\n      * @param mask The date format mask";
            retval += "\n      * @see java.text.SimpleDateFormat";
            retval += "\n      */";
            retval += "\n    public void set" + propname + "(String prop,String mask)";
            retval += "\n    {";
            retval += "\n        SimpleDateFormat sdf = new SimpleDateFormat(mask);";
            retval += "\n        ParsePosition pp = new ParsePosition(0);";
            retval += "\n        this." + propname + " = sdf.parse(prop,pp);";
            retval += "\n    }";
            retval += "\n\n ";
            retval += "\n    /** Sets the value of the " + propname + " property ";
            retval += "\n      * by accepting a java.util.Date object as a param.";
            retval += "\n      * @param prop a java.util.Date object representing the date";
            retval += "\n      */";
            retval += "\n    public void set" + propname + "(" + Jtype + " prop)";
            retval += "\n    {";
            retval += "\n        this." + propname + " = prop;";
            retval += "\n    }";
            retval += "\n";
            retval += "\n    /** Gets the current value of the " + propname + " property as a String";
            retval += "\n      * using the SimpleDateFormat mask passed in";
            retval += "\n      * @param mask The date format mask";
            retval += "\n      * @return A String object containing the current value of " + propname + " formatted by <code>mask</code>";
            retval += "\n      */";
            retval += "\n    public String get" + propname + "(String mask)";
            retval += "\n    {";
            retval += "\n        if ( this." + propname + " != null ) {";
            retval += "\n            SimpleDateFormat sdf=new SimpleDateFormat(mask);";
            retval += "\n            return sdf.format(this." + propname + ");";
            retval += "\n        } else { return \"\"; }";
            retval += "\n    }";
            retval += "\n    /** Gets the current value of the " + propname + " property as a java.util.Date object";
            retval += "\n      * @return A java.util.Date object containing the current value of" + propname;
            retval += "\n      */";
            retval += "\n    public " + Jtype + " get" + propname + "() {";
            retval += "\n        return this." + propname + ";";
            retval += "\n    }";
            retval += "\n";
        }
	else
	{
            retval += "\n    /** Sets the value of the " + propname + " property";
            retval += "\n      * @param prop The value to set this property to.";
            retval += "\n      */";
            retval += "\n    public void set" + propname + "(" + Jtype + " prop)";
            retval += "\n    {";
            retval += "\n        this." + propname + " = prop;";
            retval += "\n    }";
            retval += "\n ";
            retval += "\n    /** Gets the current value of the " + propname + " property";
            retval += "\n      * @return A " + Jtype + " object containing the current value of " + propname;
            retval += "\n      */";
            retval += "\n    public " + Jtype + " get" + propname + "()";
            retval += "\n    {";
            retval += "\n        return this." + propname + ";";
            retval += "\n    }";
            retval += "\n";

        }

        return retval;
    }

    private String getBuildStart()
    {
        String retval = "";
        retval += "\n    /** Builds this VO based on the unique values in the request.";
        retval += "\n      * @param req The request containing the values for this VO.";
        retval += "\n      */";
        retval += "\n    public void build(QMessage req) {";

        return retval;
    }

    private String getBuildEnd()
    {
        return "\n    }\n";
    }

    private String getBuildLine(String propname, int proptype, int proplength)
    {
        String retval = "\n        if (req.get(\""+propname.toUpperCase()+"\") != null) ";
        String jType = getJType(proptype);
        String propnameUpper = propname.toUpperCase ();
        if (jType.equals(PROPTYPE_INT))
            retval += "set"+nonUnique(propname)+"(req.getInt(\""+propnameUpper+"\"));";
        else if (jType.equals(PROPTYPE_STRING))
            retval += "set"+nonUnique(propname)+"(req.getString(\""+propnameUpper+"\"));";
        else if (jType.equals(PROPTYPE_DATE))
            retval += "set"+nonUnique(propname)+"(req.getString(\""+propnameUpper+"\"), CBConstants.FORMAT_SDF);";
        else System.out.println("Field type ("+jType+") not found for field "+propname);
        //retval += "\n";

        return retval;
    }



    private String getEncapsulateStart()
    {
        String retval="";
        retval += "\n\n    /** Encapsulates the vo_Data in a QMessage object";
        retval += "\n      * all fields are stored with their UNIQUE field name as a key";
        retval += "\n      * @return A QMessage for this object";
        retval += "\n    */ ";
        retval += "\n    public QMessage encapsulate()";
        retval += "\n    {";
        retval += "\n        QMessage ret = new QMessage();";
        return retval;
    }

    private String getEncapsulateEnd()
    {
        String retval = "";
        retval += "\n        return ret;";
        retval += "\n    }\n";
        return retval;
    }

    private String getEncapsulateLine(String propname, int proptype)
    {
        String retval="";
        String jType=getJType(proptype);
        if(jType.equals(PROPTYPE_INT))
	{
            retval +="\n        ret.putInt(\""+propname+"\",this.get"+nonUnique(propname)+"());";
        }
	else if ( jType.equals (PROPTYPE_DATE))
	{
            retval += "\n        if ( this.get"+nonUnique(propname)+"() != null ) { ret.put(\""+propname+"\",this.get"+nonUnique(propname)+"()); }";
        }
        else
	{
                retval +="\n        if (this.get"+nonUnique(propname)+"() != null) ret.put(\""+propname+"\",this.get"+nonUnique(propname)+"());";
        }
        return retval;
    }

    private String getGetStringStart()
    {
        StringBuffer start = new StringBuffer();

        start.append("\n    /**\n");
        start.append("     * Gets a string representation of the requested field, or an empty string if not available\n");
        start.append("     * @param field - field name being requested (matching defined field name in the entity object)\n");
        start.append("     * @return String representation of the field requested\n");
        start.append("     */\n");
        start.append("    public String getString(String field)\n    {\n");

        return start.toString();
    }

    private String getGetStringEnd()
    {
        StringBuffer end = new StringBuffer();

        end.append("        return \"\";\n");
        end.append("    }\n");

        return end.toString();
    }

    private String getGetStringLine(String tablename, String propname, int proptype)
    {
        StringBuffer line = new StringBuffer();

        line.append("        ");
        line.append("if(field.equals(eo_").append(tablename).append(".FIELD_").append(propname.toUpperCase()).append("))");

        String jType = getJType(proptype);
        if(jType.equals(PROPTYPE_INT))
        {
            line.append(" {return ").append("Integer.toString(").append(propname).append(")");
        }
        else
        {
            line.append(" {return ").append(propname).append("==null ? \"\" : ").append(propname);
        }

        line.append(";}\n");

        return line.toString();
    }

    private String getSetFieldsStart()
    {
        StringBuffer start = new StringBuffer();

        start.append("\n    /**\n");
        start.append("     * Sets the field value if it is available in the given Hashtable\n");
        start.append("     * @param values - list of fields and their associated values to be set\n");
        start.append("     */\n");
        start.append("    public void setFields(QMessage values)\n    {\n");

        return start.toString();
    }

    private String getSetFieldsEnd()
    {
        StringBuffer end = new StringBuffer();

        end.append("    }\n");

        return end.toString();
    }

    private String getSetFieldsLine(String tablename, String propname, int proptype)
    {
        StringBuffer line = new StringBuffer();

        line.append("        ");
        line.append("if(values.containsKey(eo_").append(tablename).append(".FIELD_").append(propname.toUpperCase()).append("))");
        line.append(" { ").append(propname).append(" = values.get");

        String jType = getJType(proptype);
        if(jType.equals(PROPTYPE_INT))
        {
            line.append("Int");
        }
        else
        {
            line.append("String");
        }

        line.append("(eo_").append(tablename).append(".FIELD_").append(propname.toUpperCase()).append(")").append(";}\n");

        return line.toString();
    }

    private String getValidateStub()
    {
        String retval = "";
        retval += "\n\n    /** Validate this VO based on the rules contained in this method, return true";
        retval += "\n        if this VO is properly formed and valid, false otherwise.  Store your error";
        retval += "\n        information in the errorText value so it can be returned via getErrorText().";
        retval += "\n      */";

        retval += "\n    public boolean validate() {";
        retval += "\n        // Replace this with object specific validation code";
        retval += "\n        return true;";
        retval += "\n    }";
        return retval;
    }

    private String getErrorTextDefinition()
    {
        String retval = "";
        retval += "\n\n    /** Return the text of any errors detected in the validate() method.";
        retval += "\n      */";

        retval += "\n    public String getErrorText() {";
        retval += "\n        return errorText;";
        retval += "\n    }";
        return retval;
    }

    private String getJType(int proptype)
    {
        String Jtype = "undefined";
        switch (proptype)
	{
            case -1:
            case 2:
            case 4:
                Jtype = PROPTYPE_INT;
                break;
            case 93:
                Jtype = PROPTYPE_DATE;
                break;
            default:
                Jtype = PROPTYPE_STRING;
        }
        return Jtype;
    }


    /**
     *  This function takes the string with all of the vo content and writes it
     *  to a file called <i>vo_Tablename.java</i>
     *
     *@param  tablename        Description of the Parameter
     *@param  data             Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void writeToFile(String tablename, String data) throws IOException
    {
        fw = new FileWriter(outputDir + "/vo_" + tablename + "_gen.java");
        fw.write(data, 0, data.length());
        fw.flush();
        fw.close();
    }

    /**
     *  This function takes the string with all of the vo Subclass content and writes it
     *  to a file called <i>vo_Tablename.java</i>
     *
     *@param  tablename        Description of the Parameter
     *@param  data             Description of the Parameter
     *@exception  IOException  Description of the Exception
     */
    public void writeSubToFile(String tablename, String data) throws IOException
    {
	File fname = new File(outputDir + "/vo_" + tablename + ".java");
	if(!fname.exists())
	{
            fw = new FileWriter(fname);
            fw.write(data, 0, data.length());
            fw.flush();
            fw.close();
	}
    }


    /**
     *  This function converts an uppercase String to lowercase except for the
     *  first letter
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
        return format(param.substring(param.indexOf('_')+1));
	****/
    }

    public void getAllEncapsulations(String outfile)
    {
	try
	{
	    DatabaseMetaData dbmd = conn.getMetaData();
	    String catalog = conn.getCatalog();
	    String[] types = {"TABLE"};
	    ResultSet tables = dbmd.getTables(catalog, schema, null, types);
	    String table="";
	    String output="";
	    while (tables.next())
	    {

		table=tables.getString("TABLE_NAME");
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM " + table);
		ResultSetMetaData rsmd = rs.getMetaData();
		int i = 0;
		output+="/**********************************************/\n";
		output+="Data for table "+table+"\n";
		output+="/**********************************************/\n";
		output += getEncapsulateStart();
		for(i=1;i<=rsmd.getColumnCount();i++)
		{
		    output+= getEncapsulateLine(rsmd.getColumnName(i),rsmd.getColumnType(i));
		}
		output += getEncapsulateEnd();
		output+="\n/*****/\n";
		rs.close();
		st.close();
	    }
	    tables.close();
	    writeToFile(outfile,output);
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	}
    }
}

