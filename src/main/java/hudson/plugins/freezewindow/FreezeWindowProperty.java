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

import hudson.Extension;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Job property that stores the line feed separated list of
 * regular expressions that define the freeze windows.
 */
public class FreezeWindowProperty extends JobProperty<Job<?, ?>> {
    /**
     * the logger
     */
    private static final Logger LOG = Logger.getLogger(FreezeWindowProperty.class.getName());

    /**
     * the enable checkbox in the job's config
     */
    public static final String USE_FREEZE_WINDOW = "useFreezeWindow";

    /**
     * freeze windows form field name
     */
    public static final String FREEZE_WINDOW_KEYS = "freezeWindows";

    /**
     * flag if freeze window should be used
     */
    private boolean useFreezeWindow;

    /**
     * the freeze windows that block the build from starting
     */
    private String freezeWindows;

    /**
     * Returns true if the freeze window is enabled.
     * @return true if the freeze window is enabled
     */
    @SuppressWarnings("unused")
    public boolean isUseFreezeWindow() {
        return useFreezeWindow;
    }

    /**
     * Sets the freeze window flag.
     * @param useFreezeWindow the freeze window flag
     */
    public void setUseFreezeWindow(boolean useFreezeWindow) {
        this.useFreezeWindow = useFreezeWindow;
    }

    /**
     * Returns the text of the freeze windows field.
     * @return the text of the freeze windows field
     */
    public String getFreezeWindows() {
        return freezeWindows;
    }

    /**
     * Sets the freeze windows field
     * @param freezeWindows the freeze windows entry
     */
    public void setFreezeWindows(String freezeWindows) {
        this.freezeWindows = freezeWindows;
    }

    /**
     * Descriptor
     */
    @SuppressWarnings("unused")
    @Extension
    public static final class FreezeWindowDescriptor extends JobPropertyDescriptor {

        /**
         * Constructor loading the data from the config file
         */
        public FreezeWindowDescriptor() {
            load();
        }

        /**
         * Returns the name to be shown on the website
         * @return the name to be shown on the website.
         */
        @Override
        public String getDisplayName() {
            return Messages.DisplayName();
        }

        /**
         * Returns a new instance of the freeze window property
         * when job config page is saved.
         * @param req stapler request
         * @param formData  the form data
         * @return a new instance of the freeze window property
         * @throws FormException
         */
        @Override
        public FreezeWindowProperty newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            FreezeWindowProperty freezeWindowProperty = new FreezeWindowProperty();

            if(formData.containsKey(USE_FREEZE_WINDOW)) {
                try {
                    freezeWindowProperty.setUseFreezeWindow(true);
                    freezeWindowProperty.setFreezeWindows(formData.getJSONObject(USE_FREEZE_WINDOW).getString(FREEZE_WINDOW_KEYS));

                } catch(JSONException e) {
                    freezeWindowProperty.setUseFreezeWindow(false);
                    LOG.log(Level.WARNING, "could not get freeze windows from " + formData.toString());
                }
            }

            return freezeWindowProperty;
        }

        /**
        * Validate the cron expression entered by the user
        * @param string of a cron expression to validate
        * @return FormValidation ok or error
        */
        public FormValidation doCheckCron(@QueryParameter final String freezeWindows) {
            List<String> freezeWindowList = null;
            String validChar[] = new String[]{"*","?"};
            // Regular expression obtained from http://www.quartz-scheduler.org/xml/job_scheduling_data_2_0.xsd
            // Modified to support true cron expression
            // Note: order for expressions like 3-0 is not legal but will pass, and month and day names must be capitalized.
            // MINUTES:             ( ((([0-9]|[0-5][0-9])(-([0-9]|[0-5][0-9]))?,)*([0-9]|[0-5][0-9])(-([0-9]|[0-5][0-9]))?) | (([\*]|[0-9]|[0-5][0-9])/([0-9]|[0-5][0-9])) | ([\?]) | ([\*]) ) [\s]
            // HOURS: ( ((([0-9]|[0-1][0-9]|[2][0-3])(-([0-9]|[0-1][0-9]|[2][0-3]))?,)*([0-9]|[0-1][0-9]|[2][0-3])(-([0-9]|[0-1][0-9]|[2][0-3]))?) | (([\*]|[0-9]|[0-1][0-9]|[2][0-3])/([0-9]|[0-1][0-9]|[2][0-3])) | ([\?]) | ([\*]) ) [\s]
            // DAY OF MONTH: ( ((([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1])(-([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1]))?,)*([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1])(-([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1]))?(C)?) | (([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1])/([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1])(C)?) | (L(-[0-9])?) | (L(-[1-2][0-9])?) | (L(-[3][0-1])?) | (LW) | ([1-9]W) | ([1-3][0-9]W) | ([\?]) | ([\*]) )[\s]
            // MONTH: ( ((([1-9]|0[1-9]|1[0-2])(-([1-9]|0[1-9]|1[0-2]))?,)*([1-9]|0[1-9]|1[0-2])(-([1-9]|0[1-9]|1[0-2]))?) | (([1-9]|0[1-9]|1[0-2])/([1-9]|0[1-9]|1[0-2])) | (((JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(-(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))?,)*(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(-(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))?) | ((JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)/(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)) | ([\?]) | ([\*]) )[\s]
            // DAY OF WEEK: ( (([0-7](-([0-7]))?,)*([0-7])(-([0-7]))?) | ([0-7]/([0-7])) | (((MON|TUE|WED|THU|FRI|SAT|SUN)(-(MON|TUE|WED|THU|FRI|SAT|SUN))?,)*(MON|TUE|WED|THU|FRI|SAT|SUN)(-(MON|TUE|WED|THU|FRI|SAT|SUN))?(C)?) | ((MON|TUE|WED|THU|FRI|SAT|SUN)/(MON|TUE|WED|THU|FRI|SAT|SUN)(C)?) | (([0-7]|(MON|TUE|WED|THU|FRI|SAT|SUN))(L|LW)?) | (([0-7]|MON|TUE|WED|THU|FRI|SAT|SUN)#([0-7])?) | ([\?]) | ([\*]) )
            // YEAR (OPTIONAL): ( [\s]? ([\*])? | ((19[7-9][0-9])|(20[0-9][0-9]))? | (((19[7-9][0-9])|(20[0-9][0-9]))/((19[7-9][0-9])|(20[0-9][0-9])))? | ((((19[7-9][0-9])|(20[0-9][0-9]))(-((19[7-9][0-9])|(20[0-9][0-9])))?,)*((19[7-9][0-9])|(20[0-9][0-9]))(-((19[7-9][0-9])|(20[0-9][0-9])))?)? )
            String pattern = "(((([0-9]|[0-5][0-9])(-([0-9]|[0-5][0-9]))?,)*([0-9]|[0-5][0-9])(-([0-9]|[0-5][0-9]))?)|(([\\*]|[0-9]|[0-5][0-9])/([0-9]|[0-5][0-9]))|([\\?])|([\\*]))[\\s](((([0-9]|[0-1][0-9]|[2][0-3])(-([0-9]|[0-1][0-9]|[2][0-3]))?,)*([0-9]|[0-1][0-9]|[2][0-3])(-([0-9]|[0-1][0-9]|[2][0-3]))?)|(([\\*]|[0-9]|[0-1][0-9]|[2][0-3])/([0-9]|[0-1][0-9]|[2][0-3]))|([\\?])|([\\*]))[\\s](((([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1])(-([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1]))?,)*([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1])(-([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1]))?(C)?)|(([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1])/([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1])(C)?)|(L(-[0-9])?)|(L(-[1-2][0-9])?)|(L(-[3][0-1])?)|(LW)|([1-9]W)|([1-3][0-9]W)|([\\?])|([\\*]))[\\s](((([1-9]|0[1-9]|1[0-2])(-([1-9]|0[1-9]|1[0-2]))?,)*([1-9]|0[1-9]|1[0-2])(-([1-9]|0[1-9]|1[0-2]))?)|(([1-9]|0[1-9]|1[0-2])/([1-9]|0[1-9]|1[0-2]))|(((JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(-(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))?,)*(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(-(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))?)|((JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)/(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))|([\\?])|([\\*]))[\\s]((([0-7](-([0-7]))?,)*([0-7])(-([0-7]))?)|([0-7]/([0-7]))|(((MON|TUE|WED|THU|FRI|SAT|SUN)(-(MON|TUE|WED|THU|FRI|SAT|SUN))?,)*(MON|TUE|WED|THU|FRI|SAT|SUN)(-(MON|TUE|WED|THU|FRI|SAT|SUN))?(C)?)|((MON|TUE|WED|THU|FRI|SAT|SUN)/(MON|TUE|WED|THU|FRI|SAT|SUN)(C)?)|(([0-7]|(MON|TUE|WED|THU|FRI|SAT|SUN))?(L|LW)?)|(([0-7]|MON|TUE|WED|THU|FRI|SAT|SUN)#([0-7])?)|([\\?])|([\\*]))([\\s]?(([\\*])?|(19[7-9][0-9])|(20[0-9][0-9]))?| (((19[7-9][0-9])|(20[0-9][0-9]))/((19[7-9][0-9])|(20[0-9][0-9])))?| ((((19[7-9][0-9])|(20[0-9][0-9]))(-((19[7-9][0-9])|(20[0-9][0-9])))?,)*((19[7-9][0-9])|(20[0-9][0-9]))(-((19[7-9][0-9])|(20[0-9][0-9])))?)?)";

            if(StringUtils.isNotBlank(freezeWindows)) {
                freezeWindowList = Arrays.asList(freezeWindows.split("\n"));
            }

            for (String fw: freezeWindowList) {
                boolean matches = Pattern.matches(pattern, fw);
                String[] cronParts = fw.split(" ");
                if ((cronParts.length != 5) && cronParts.length != 6) {
                    matches = false;
                }
                // Either day of week or day of month should be "?"
                else if (! ((cronParts[2].trim().equals("?")) ^ (cronParts[4].trim().equals("?")))) {
                    matches = false;
                }
                System.out.println("Validating freeze window " + fw);
                System.out.println("Validation of " + fw + " == " + matches);
                if (!matches) {
                    return FormValidation.error("Invalid cron expression " + fw);
                }
            }
            return FormValidation.ok();
        }

        /**
         * Returns always true a it can be used in all types of jobs.
         * @param jobType the job type to be checked if this property is applicable.
         * @return true
         */
        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return true;
        }
    }

}
