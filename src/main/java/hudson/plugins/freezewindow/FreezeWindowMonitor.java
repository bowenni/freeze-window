/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Sun Microsystems, Inc., Frederik Fromm
 *
 * Changes to this code are Copyright 2015 Yahoo! Inc. Licensed under the
 * project's Open Source license.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package hudson.plugins.freezewindow;

import org.apache.commons.lang.StringUtils;
import org.quartz.CronExpression;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * This class represents a monitor that checks all freeze windows if
 * one of the freeze windows contains current time.
 *
 * The first hit returns the freeze window.
 */
public class FreezeWindowMonitor {

    /**
     * the list of regular expressions from the job configuration
     */
    private List<String> freezeWindows;

    public List<String> getFreezeWindows() {
        return freezeWindows;
    }

    /**
     * Constructor using the job configuration entry for freeze windows
     * @param freezeWindows line feed separated list of freeze windows
     */
    public FreezeWindowMonitor(String freezeWindows) {
        if(StringUtils.isNotBlank(freezeWindows)) {
            this.freezeWindows = Arrays.asList(freezeWindows.split("\n"));
        }
    }

    /**
     * Check if the current time is in one of the freeze windows.
     * @return null if current time is not in any freeze windows.
     * Otherwise return the first freeze window(as a string) that matches current time
     */
    public String shouldBlock(){
        try {
            if (this.freezeWindows == null) {
                return null;
            }
            Date now = new Date();
            for (String fw : this.freezeWindows) {
                CronExpression cr = new CronExpression(normalize(fw));
                if (cr.isSatisfiedBy(now)) {
                    return fw;
                }
            }
            return null;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        System.out.println("Cannot decide if the build should be blocked.");
        return null;
    }

    /**
     * quartz's version of cron is slightly different from the standard cron format.
     * For example: day of week starts with 0 in standard cron format but in quartz it starts with 1.
     * Therefore normalize will convert standard cron to quartz cron, assuming that the input is valid
     * @param fw a cron expression
     * @return string of the corn expression after normailization
     */
    public static String normalize(String fw) {
        StringBuilder sb = new StringBuilder();
        String[] cronParts = fw.split(" ");
        List<Character> validDaysOfWeek = new ArrayList<Character>();
        validDaysOfWeek.add(new Character('0'));
        validDaysOfWeek.add(new Character('1'));
        validDaysOfWeek.add(new Character('2'));
        validDaysOfWeek.add(new Character('3'));
        validDaysOfWeek.add(new Character('4'));
        validDaysOfWeek.add(new Character('5'));
        validDaysOfWeek.add(new Character('6'));

        sb.append("* ");
        sb.append(cronParts[0] + " ");
        sb.append(cronParts[1] + " ");
        sb.append(cronParts[2] + " ");
        sb.append(cronParts[3] + " ");

        // Only increment digits by 1
        // example: standard cron expression: 5-6,MON
        //          quartz cron expression: 6-7,MON
        char[] chars = cronParts[4].toCharArray();
        int i;
        for (i = 0; i < chars.length; i++) {
            if (validDaysOfWeek.contains(chars[i])) {
                sb.append((chars[i] - '0') + 1);
            }
            else {
                sb.append(chars[i]);
            }
        }

        if (cronParts.length == 6) {
            sb.append(" " + cronParts[5]);
        }

        System.out.println("Input freeze window cron format: " + fw);
        System.out.println("After normalize: " + sb);
        return sb.toString();
    }
}
