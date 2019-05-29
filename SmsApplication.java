package com.qkernel;
//
// SmsApplication.java     SMS Application Abstract Class
// ----------------------------------------------------------------------------
// History:
// --------
// 07/13/01 M. Gill 	Initial creation.
// ----------------------------------------------------------------------------
//
public abstract class SmsApplication extends Eos implements SmsServiceEvents
{
    public SmsProxy smsProxy;

    public SmsApplication(String n, SmsProxy proxy, Daemon d)
    {
        super(n, d);
	smsProxy = proxy;
    }
}
