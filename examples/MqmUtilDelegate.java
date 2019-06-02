package examples;

import java.lang.*;
import com.qkernel.*;

public class MqmUtilDelegate extends BusinessDelegate
{

    public String getOrbAddress(Daemon d)
    {
        String CONFIG    = "Configuration Object";
    	MqmConfig config = (MqmConfig)d.lookup(CONFIG);
	return( config.getIpAddress() );
    }

    public int getOrbPort(Daemon d)
    {

        String CONFIG    = "Configuration Object";
    	MqmConfig config = (MqmConfig)d.lookup(CONFIG);
	return( config.getBusinessPort() );
    }

    public boolean isRemote()
    {
	return(true);
    }

    public MqmUtilDelegate(String h, int p)
    {
	super(h,p);
    }
}
