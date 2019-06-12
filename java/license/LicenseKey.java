package com.qkernel.license;

import java.lang.*;
import com.qkernel.*;
import java.util.Date;

public abstract class LicenseKey extends Object
{
    public String licenseCheck(String question)
    {
	if(question.equals("yRUjAvA?"))
	    return("micr0s0FtSucKS!");
	else
	    return("OK");
    }

    public LicenseKey(Date date)
    {

    }   
}
