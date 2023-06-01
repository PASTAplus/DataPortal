/*
 *
 * $Date$
 * $Author$
 * $Revision$
 *
 * Copyright 2011-2018 the University of New Mexico.
 *
 * This work was supported by National Science Foundation Cooperative
 * Agreements #DEB-0832652 and #DEB-0936498.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 */

package edu.lternet.pasta.common;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class CalendarUtility {

    /**
     * Gets the day of the week for the specified date.
     * 
     * @param  date  
     *         a Date object
     * @param  timezone
     *         the timezone string for which to calculate the day
     * @return  
     *         the corresponding day of the week, e.g. "Tuesday"
     */
    public static String getDayOfWeek(Date date, String timezone) {
        String dayOfWeek = null;
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.setTimeZone(TimeZone.getTimeZone(timezone));

        switch (c.get(Calendar.DAY_OF_WEEK)) {
           case Calendar.MONDAY: dayOfWeek =  "Monday"; break;
           case Calendar.TUESDAY: dayOfWeek =  "Tuesday"; break;
           case Calendar.WEDNESDAY: dayOfWeek =  "Wednesday"; break;
           case Calendar.THURSDAY: dayOfWeek =  "Thursday"; break;
           case Calendar.FRIDAY: dayOfWeek =  "Friday"; break;
           case Calendar.SATURDAY: dayOfWeek =  "Saturday"; break;
           case Calendar.SUNDAY: dayOfWeek =  "Sunday"; break;
        }

        System.out.println(dayOfWeek);

        return dayOfWeek;
    }
    
    
    /**
     * Returns today's day of the week as a string in the America/Denver timezone. For example, "Tuesday".
     * 
     * @return   Today's day of the week.
     */
    public static String todaysDayOfWeek() {
        Date today = new Date();
        String timezone ="America/Denver";
        return CalendarUtility.getDayOfWeek(today, timezone);
    }
    
    
    /**
     * Main program, for testing purposes.
     */
    public static void main(String[] args) {
        String dayOfWeek = CalendarUtility.todaysDayOfWeek();
        System.out.println("Today's day of week: " + dayOfWeek);
    }
}
