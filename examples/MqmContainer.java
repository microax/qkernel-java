package examples;

import com.qkernel.*;

@SuppressWarnings({"unchecked", "fallthrough", "serial" })
/***********************************************************************
 * Creates/loads all of the EnityObjects for the Business server.
 * The <code>load()</code> method must be implemented.
 * <code>load()</code> must do the following for each EnityObject:
 *     <li> Instantiate each entity via "new"
 *     <li> Invoke the create method on the EnityObject
 * </ol>
 ***********************************************************************
 */
public class MqmContainer extends EntityContainer
{

    /**
    * Here is where we create Mqm EnityObjects...
    * Invoke in the following way:
    * <BR><BR>
    * <PRE>
    *          Obj = new ObjClass();
    *          Obj.create(this, "name");
    *          Obj.load();
    * </PRE>
    */
    public void load()
    {
	//stub
    }
}

