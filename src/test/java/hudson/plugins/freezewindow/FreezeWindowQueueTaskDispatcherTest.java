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

import hudson.model.*;
import hudson.model.queue.CauseOfBlockage;
import org.jvnet.hudson.test.HudsonTestCase;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Unit tests
 */
public class FreezeWindowQueueTaskDispatcherTest extends HudsonTestCase {

    /**
     * One test for all for faster execution.
     * @throws Exception
     */
    public void testCanRun() throws Exception {
        FreezeWindowQueueTaskDispatcher dispatcher = new FreezeWindowQueueTaskDispatcher();
        FreeStyleProject project = this.createFreeStyleProject();
        Queue.BuildableItem item = new Queue.BuildableItem(new Queue.WaitingItem(Calendar.getInstance(), project, new ArrayList<Action>()));

        CauseOfBlockage causeOfBlockage = dispatcher.canRun(item);
        assertNull(causeOfBlockage);

        FreezeWindowProperty property = new FreezeWindowProperty();
        property.setFreezeWindows("* * * * ?");
        project.addProperty(property);

        causeOfBlockage = dispatcher.canRun(item);
        assertNotNull(causeOfBlockage);
        assertTrue(causeOfBlockage.getShortDescription().contains("is blocked by freeze window"));
    }
}
