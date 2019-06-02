package com.qkernel;
import com.qkernel.json.*;
//
// SmsProxy.java     SMS Proxy Abstract Class
// ----------------------------------------------------------------------------
// History:
// --------
// 07/13/01 M. Gill 	Initial creation.
// ----------------------------------------------------------------------------
//

public abstract class SmsProxy extends Joa implements SmsService, SmsServiceEvents
{

    public abstract void sendSmsMessage(SmsMessage sms); 

    public abstract void register(SmsApplication app); 

    public abstract void restart(SmsApplication app); 

    public JSONObject jsonObjectRequest(QMessage request, JSONObject argvs)
    {
	return new JSONObject();
    }

    public SmsProxy(String n, Daemon d)
    {
        super(n, d);
    }
}
