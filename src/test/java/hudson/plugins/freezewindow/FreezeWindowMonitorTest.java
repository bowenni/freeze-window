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

import org.powermock.api.easymock.PowerMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * Unit tests
 */
public class FreezeWindowMonitorTest {

    /**
     * Testing constructor
     * @throws Exception
     */
    @Test
    public void testConstructor() throws Exception {
        String freezeWidnows = "* * * 1 ?\n* * * 2 ?\n* * * 3 ?\n";
        List<String> expectedFW = new ArrayList<String>();
        expectedFW.add("* * * 1 ?");
        expectedFW.add("* * * 2 ?");
        expectedFW.add("* * * 3 ?");

        FreezeWindowMonitor validFreezeWindow = new FreezeWindowMonitor(freezeWidnows);
        assertEquals(validFreezeWindow.getFreezeWindows(), expectedFW);
    }

    /**
     * Testing shouldBlock
     * @throws Exception
     */
    @Test
    public void testShouldBlock() throws Exception {
        // Mock the current time to be Mar 19, 2015
        long longNow = 1426724690107L;
        Date now = new Date(longNow);
        PowerMock.expectNew(Date.class).andReturn(now);

        // Freeze all the time
        String freezeWindows = "0-59 0-23 1-31 * ?";
        FreezeWindowMonitor validFreezeWindow = new FreezeWindowMonitor(freezeWindows);
        assertEquals(validFreezeWindow.shouldBlock(), freezeWindows);

        // Freeze only in Aug, Sept, Oct, Nov and Dec
        freezeWindows = "0-59 0-23 1-31 8-12 ?";
        validFreezeWindow = new FreezeWindowMonitor(freezeWindows);
        assertNull(validFreezeWindow.shouldBlock());
    }

    /**
     * Testing normailize
     * @throws Exception
     */
    @Test
    public void testNormalize() throws Exception {
        String beforeNormalize[] = new String[]{"1 3 2 4-5 ?", "2 * ? 1-12 * 2015", "3 * ? * 3-5", "4 * ? * 4-5,MON"};
        String afterNormalize[] = new String[]{"* 1 3 2 4-5 ?", "* 2 * ? 1-12 * 2015", "* 3 * ? * 4-6", "* 4 * ? * 5-6,MON"};

        for (int i = 0; i < beforeNormalize.length; i += 1){
            assertEquals(FreezeWindowMonitor.normalize(beforeNormalize[i]), afterNormalize[i]);
        }
    }
}
