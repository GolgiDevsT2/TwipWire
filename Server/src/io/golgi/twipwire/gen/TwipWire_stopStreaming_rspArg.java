/* IS_AUTOGENERATED_SO_ALLOW_AUTODELETE=YES */
/* The previous line is to allow auto deletion */

package io.golgi.twipwire.gen;

import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Iterator;
import com.openmindnetworks.golgi.JavaType;
import com.openmindnetworks.golgi.GolgiPayload;
import com.openmindnetworks.golgi.B64;
import com.openmindnetworks.golgi.api.GolgiException;
import com.openmindnetworks.golgi.api.GolgiAPI;

public class TwipWire_stopStreaming_rspArg
{

    private boolean corrupt = false;

    public boolean isCorrupt() {
        return corrupt;
    }

    public void setCorrupt() {
        corrupt = true;
    }

    private boolean internalSuccess_IsSet = false;
    private int internalSuccess_;
    private boolean golgiExceptionIsSet = false;
    private GolgiException golgiException;

    public int getInternalSuccess_(){
        return internalSuccess_;
    }
    public boolean internalSuccess_IsSet(){
        return internalSuccess_IsSet;
    }
    public void setInternalSuccess_(int internalSuccess_){
        this.internalSuccess_ = internalSuccess_;
        this.internalSuccess_IsSet = true;
    }

    public GolgiException getGolgiException(){
        return golgiException;
    }
    public boolean golgiExceptionIsSet(){
        return golgiExceptionIsSet;
    }
    public void setGolgiException(GolgiException golgiException){
        this.golgiException = golgiException;
        this.golgiExceptionIsSet = true;
    }

    public StringBuffer serialise(){
        return serialise(null);
    }

    public StringBuffer serialise(StringBuffer sb){
        return serialise("", sb);
    }

    public StringBuffer serialise(String prefix, StringBuffer sb){
        if(sb == null){
            sb = new StringBuffer();
        }

        if(this.internalSuccess_IsSet){
            sb.append(prefix + "1: " + this.internalSuccess_+"\n");
        }
        if(this.golgiExceptionIsSet){
            golgiException.serialise(prefix + "" + 3 + "." , sb);
        }

        return sb;
    }

    private void deserialise(GolgiPayload payload){
        if(!isCorrupt() && payload.containsFieldKey("1:")){
            String str = payload.getField("1:");
            try{
                setInternalSuccess_(Integer.valueOf(str));
            }
            catch(NumberFormatException nfe){
                setCorrupt();
            }
        }
        if(!isCorrupt() && payload.containsNestedKey("3")){
            GolgiException inst = new GolgiException(payload.getNested("3"));
            setGolgiException(inst);
        }
    }

    public TwipWire_stopStreaming_rspArg(){
        this(true);
    }

    public TwipWire_stopStreaming_rspArg(boolean isSetDefault){
        super();
        internalSuccess_IsSet = isSetDefault;
        internalSuccess_ = 0;
        golgiExceptionIsSet = isSetDefault;
        golgiException = new GolgiException(isSetDefault);
    }

    public TwipWire_stopStreaming_rspArg(GolgiPayload payload){
        this(false);
        deserialise(payload);
    }

    public TwipWire_stopStreaming_rspArg(String payload){
        this(JavaType.createPayload(payload));
    }

}
