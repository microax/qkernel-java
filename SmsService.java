package com.qkernel;
//
// SmsService.java   Interface defines Sms Service        
// ----------------------------------------------------------------------------
// History:
// --------
// 04/23/01 M. Gill	Initial creation.
// ----------------------------------------------------------------------------
//
import java.lang.*;

public interface SmsService
{

    public void sendSmsMessage(SmsMessage sms);

    public void register(SmsApplication app);

    public void restart(SmsApplication app);

}
