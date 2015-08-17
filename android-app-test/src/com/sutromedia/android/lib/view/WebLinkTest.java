package com.sutromedia.android.lib.view;


import android.test.AndroidTestCase;
import com.sutromedia.android.lib.view.WebLink;
 
public class WebLinkTest extends AndroidTestCase {

    public void testShouldNotMatch() {
        assertNull(WebLink.parse(null));
        assertNull(WebLink.parse(""));
    }

    public void testShouldMatchDetailEntry() {
    
        assertEquals(
            WebLink.LinkType.EntryDetail, 
            WebLink.parse("SMEntryLink://123").getType());
        
        assertEquals("123", WebLink.parse("SMEntryLink://123").getData());
        assertEquals("123", WebLink.parse(" SMEntryLink://123").getData());
        assertEquals("123", WebLink.parse(" SMEntryLink://123 ").getData());
    }
    
    public void testShouldMatchCatgory() {
    
        assertEquals(
            WebLink.LinkType.Category, 
            WebLink.parse("SMTag:15671").getType());
        
        assertEquals("15671", WebLink.parse("SMTag:15671").getData());
        assertEquals("15671", WebLink.parse(" SMTag:15671").getData());
        assertEquals("15671", WebLink.parse(" SMTag:15671 ").getData());
    }    
}