package com.qkernel;
//
// IvrProxy.java     Ivr Proxy Abstract Class
// ----------------------------------------------------------------------------
// History:
// --------
// 08/01/01 M. Gill 	Initial creation.
// ----------------------------------------------------------------------------
//

public abstract class IvrProxy extends Joa implements IvrService, IvrEvents
{
    public abstract String dialPrompt(String prompt, String number); 

    public abstract String waitCall(); 

    public abstract String hangup(); 

    public abstract String prompt(String prompt); 

    public abstract String getDigit(); 

    public abstract String quit(); 

    public abstract String register(Eos app); 

    public abstract String restart(Eos app); 

    public IvrProxy(String n, Daemon d)
    {
        super(n, d);
    }
}
