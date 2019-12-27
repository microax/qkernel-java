/* Licence:
*   Use this however/wherever you like, just don't blame me if it breaks anything.
*
* Credit:
*   If you're nice, you'll leave this bit:
*
*   Class by Pierre-Alexandre Losson -- http://www.telio.be/blog
*   email : plosson@users.sourceforge.net
*/
package com.qkernel.upload;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Original : plosson on 04-janv.-2006 9:59:27 - Last modified  by $Author: oertel $ on $Date: 2008/04/05 06:43:10 $
 * @version 1.0 - Rev. $Revision: 1.1.1.1 $
 */
public interface OutputStreamListener
{
    public void start();
    public void bytesRead(int bytesRead);
    public void error(String message);
    public void done();
}
