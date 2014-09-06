package io.golgi.twipwire;


import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;


public class DateHelper {
	private static DateFormat dateFormat = null;
	
    public static String dateToHeaderString(Context context, long when){
        long now = System.currentTimeMillis();
        long age;
        String dstr;
        
        if(dateFormat == null){
        	dateFormat = android.text.format.DateFormat.getDateFormat(context);
        }

        //Some time in the last 48 hours
        if ((age = (now - when)) < 172800000){
        	Date myDate = new Date(when);
        	Calendar c1 = Calendar.getInstance(); // today
            
        	Calendar c2 = Calendar.getInstance();
        	c2.add(Calendar.DAY_OF_YEAR, -1); // yesterday

        	Calendar c3 = Calendar.getInstance();
        	c3.setTime(myDate); // date to check

        	if (c1.get(Calendar.DAY_OF_YEAR) == c3.get(Calendar.DAY_OF_YEAR)) {
        		dstr = "Today";
        	}
        	else if (c2.get(Calendar.DAY_OF_YEAR) == c3.get(Calendar.DAY_OF_YEAR))  {
        		dstr = "Yesterday";
        	}
        	else{
        		dstr = dateFormat.format(new Date(when)).toString();
        	}
        }
        else{
            dstr = dateFormat.format(new Date(when)).toString();
        }
        return dstr;
    }
    
    public static String dateToNowRelativeString(Context context, long when){
    	long now = System.currentTimeMillis();
    	long age;
    	String dstr;
    	
        if(dateFormat == null){
        	dateFormat = android.text.format.DateFormat.getDateFormat(context);
        }

    	//
    	// BK: zoneOffset seems like it should do the right thing but it
    	// doesn't (as observed in Czech Republic) so don't use it
    	//
        if ((age = (now - when)) < 172800000){
        	age /= 1000;
        	age += 4;
        	age -= (age%5);
        	if(age <= 15){
        	    dstr = "Just Now";
        	}
        	else if(age < 60){
        		dstr = "" + age + " seconds ago";
        	}
        	else if(age < 3600){
        		age /= 60;
        		dstr = "" + age + ((age > 1) ? " minutes ago" : " minute ago");
        	}
        	else{
        		Date myDate = new Date(when);
        		Calendar c1 = Calendar.getInstance(); // today
    	    
        		Calendar c2 = Calendar.getInstance();
        		c2.add(Calendar.DAY_OF_YEAR, -1); // yesterday

        		Calendar c3 = Calendar.getInstance();
        		c3.setTime(myDate); // date to check

        		if (c1.get(Calendar.DAY_OF_YEAR) == c3.get(Calendar.DAY_OF_YEAR)) {
        			dstr = "Today - " + android.text.format.DateFormat.format("h:mmaa", myDate);
        		}
        		else if (c2.get(Calendar.DAY_OF_YEAR) == c3.get(Calendar.DAY_OF_YEAR))  {
        			dstr = "Yesterday - " + android.text.format.DateFormat.format("h:mmaa", myDate);
        		}
        		else{
        			dstr = dateFormat.format(myDate).toString() + android.text.format.DateFormat.format(" - h:mmaa", myDate).toString();
        		}
        	}
   		}
   		else{
    	    Date myDate = new Date(when);
            dstr = dateFormat.format(myDate).toString() + android.text.format.DateFormat.format(" - h:mmaa", myDate).toString();
   		}
   		return dstr;
    }
    
    
    public static String dateToString(Context context, long when, boolean fineFormat){
    	long now = System.currentTimeMillis();
    	String dstr;
    	
        if(dateFormat == null){
        	dateFormat = android.text.format.DateFormat.getDateFormat(context);
        }

    	//
    	// BK: zoneOffset seems like it should do the right thing but it
    	// doesn't (as observed in Czech Republic) so don't use it
    	//
    	if(fineFormat){
   			dstr = android.text.format.DateFormat.format("kk:mm:ss", new Date(when /* + zoneOffset*/)).toString();
    	}
    	//Some time in the last 48 hours
    	else if (now - when < 172800000){
    	    Date myDate = new Date(when);
    	    Calendar c1 = Calendar.getInstance(); // today
    	    
    	    Calendar c2 = Calendar.getInstance();
    	    c2.add(Calendar.DAY_OF_YEAR, -1); // yesterday

    	    Calendar c3 = Calendar.getInstance();
    	    c3.setTime(myDate); // date to check

    	    if (c1.get(Calendar.DAY_OF_YEAR) == c3.get(Calendar.DAY_OF_YEAR)) {
    	        dstr = "Today - " + android.text.format.DateFormat.format("h:mmaa", myDate);
    	    }
    	    else if (c2.get(Calendar.DAY_OF_YEAR) == c3.get(Calendar.DAY_OF_YEAR))  {
    	        dstr = "Yesterday - " + android.text.format.DateFormat.format("h:mmaa", myDate);
    	    }
    	    else{
                dstr = dateFormat.format(myDate).toString() + android.text.format.DateFormat.format(" - h:mmaa", myDate).toString();
    	    }
   		}
   		else{
    	    Date myDate = new Date(when);
            dstr = dateFormat.format(myDate).toString() + android.text.format.DateFormat.format(" - h:mmaa", myDate).toString();
   		}
   		return dstr;
    }
    
    public static String dateToString(Context context, long when){
    	return dateToString(context, when, false);
    }
    
    public static DateHelper init(Context context){
    	dateFormat = android.text.format.DateFormat.getDateFormat(context);
    	return new DateHelper();
    }

}
