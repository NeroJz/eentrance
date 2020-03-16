package hk.com.uatech.eticket.eticket.delegate.entrance_log;

import hk.com.uatech.eticket.eticket.delegate.DelegateType;

public interface EntranceLogEvent {
    public void completeHandler(DelegateType delegateType);
}
