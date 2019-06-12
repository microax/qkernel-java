package com.qkernel;

/*import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.html.parser.ParserDelegator;
import javax.swing.text.html.parser.DocumentParser;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.MutableAttributeSet;
*/
import java.util.Stack;
import java.util.Enumeration;
import java.util.ArrayList;
import java.io.StringReader;
import java.net.URLEncoder;

/**
//
// SqlSafe.java	      
// ----------------------------------------------------------------------------
// History:
// --------
// 07/27/03 M. Gill     Initial creation.
*
* Revision By: Nathan Oertel
* Revised On: Feb 1, 2006
* Revision: Added more characters to SQL Safe conversion function.
// ----------------------------------------------------------------------------
//
 */
//
public class SqlSafe extends Object
{
    //--------------------------------------------------------------------------------
    // METHOD   sqlSafe()
    //
    // PURPOSE:	Create a SQL safe argument string for finders
    //--------------------------------------------------------------------------------
    public static String sqlSafe(String str)
    {
        StringBuffer buf = new StringBuffer();
        boolean isModified = false;

        if(str != null)
        {
            final char[] chars = str.toCharArray();
            final int numChars = chars.length;

            buf = new StringBuffer(numChars);

            for (int i=0; i < numChars; ++i)
            {
                char c = chars[i];
                switch (c)
                {
//@todo see if there was a reason for the things commented out
                case '\\' : buf.append("\\\\"); 	break;
//                case '%' : buf.append(" ");  	break;
                case '\'' : buf.append("\\\'");  break;
                case '\"' : buf.append("\\\""); 	break;
                case '}' : buf.append("\\}"); break;
                case '{' : buf.append("\\{"); break;
//                case '/' : buf.append(" ");  	break;
                default: buf.append(c);         break;
                }
            }
            // Because all escape sequences are longer than the single characters that
            // they replace, a simple comparison of lengths determines if anything has
            // been changed.
            isModified = (buf.length() > numChars);
        }

        // only return a new String object if it differs from original
        return(isModified?buf.toString():str);
    }

    public SqlSafe()
    {

    }
}
