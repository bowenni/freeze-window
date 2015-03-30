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

import hudson.model.Job;
import net.sf.json.JSONObject;
import org.easymock.EasyMock;
import org.jvnet.hudson.test.HudsonTestCase;
import org.kohsuke.stapler.StaplerRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests
 */
public class FreezeWindowPropertyTest extends HudsonTestCase {

    /**
     * Simple property test
     * @throws Exception
     */
    public void testFreezeWindow() throws Exception {
        FreezeWindowProperty property = new FreezeWindowProperty();

        property.setUseFreezeWindow(true);
        assertTrue(property.isUseFreezeWindow());
    }

    /**
     * Simple property test
     * @throws Exception
     */
    public void testFreezeWindows() throws Exception {
        FreezeWindowProperty property = new FreezeWindowProperty();

        property.setFreezeWindows("* * * * ?");
        assertEquals("* * * * ?", property.getFreezeWindows());
    }

    /**
     * Simple property test
     * @throws Exception
     */
    public void testIsApplicable() throws Exception {
        FreezeWindowProperty property = new FreezeWindowProperty();

        assertTrue(property.getDescriptor().isApplicable(Job.class));
    }

    /**
     * Use different form data to test descriptor newInstance
     * @throws Exception
     */
    public void testDescriptorNewInstance() throws Exception {
        JSONObject formData = new JSONObject();

        StaplerRequest staplerRequest = EasyMock.createNiceMock(StaplerRequest.class);

        // Not using freeze window, should get null
        FreezeWindowProperty property = new FreezeWindowProperty();
        property = (FreezeWindowProperty) property.getDescriptor().newInstance(staplerRequest, formData);
        assertNull(property.getFreezeWindows());

        Map<String, Map<String, String>> formDataMap = new HashMap<String, Map<String, String>>();
        Map<String, String> subMap = new HashMap<String, String>();

        formDataMap.put("useFreezeWindow", subMap);
        formData.accumulateAll(formDataMap);

        // Use freeze window but not specifying, should get null
        property = (FreezeWindowProperty) property.getDescriptor().newInstance(staplerRequest, formData);
        assertFalse(property.isUseFreezeWindow());
        assertNull(property.getFreezeWindows());

        // set the freeze window
        String key = "freezeWindows";
        String value = "* * 3 1-12 ?";

        subMap.put(key, value);
        formDataMap.put("useFreezeWindow", subMap);

        formData = new JSONObject();
        formData.accumulateAll(formDataMap);

        property = (FreezeWindowProperty) property.getDescriptor().newInstance(staplerRequest, formData);
        assertTrue(property.isUseFreezeWindow());
        assertNotNull(property.getFreezeWindows());
        assertEquals(value, property.getFreezeWindows());
    }
}
