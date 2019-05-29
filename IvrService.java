package com.qkernel;
//
// IvrService.java   Interface defines Ivr Service        
// ----------------------------------------------------------------------------
// History:
// --------
// 08/01/01 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;

public interface IvrService
{

    public String dialPrompt(String prompt, String number); 

    public String waitCall(); 

    public String hangup(); 

    public String prompt(String prompt); 

    public String getDigit(); 

    public String quit(); 

    public String register(Eos app); 

    public String restart(Eos app); 

}
