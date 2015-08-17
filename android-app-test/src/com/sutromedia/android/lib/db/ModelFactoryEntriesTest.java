package com.sutromedia.android.lib.db;


import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.List;
import java.util.Set;

import com.sutromedia.android.lib.app.*;
import com.sutromedia.android.lib.db.*;
import com.sutromedia.android.lib.model.*;
 
public class ModelFactoryEntriesTest extends AndroidTestCase {

    GuideDatabase mGuide;

    protected void setUp() throws Exception {
        super.setUp();
        mGuide= new GuideDatabase(getContext(), "content.sqlite3");
        mGuide.createDataBase();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        mGuide.close();
    }
    
    public void testShouldSucceedGettingAllEntries() throws DataException {
        List<IEntrySummary> all = ModelFactory.getEntries(mGuide);
        assertNotNull(all);
        assertEquals(286, all.size());
        
        IEntrySummary first = all.get(0);
        assertEquals("5806", first.getId());
        assertEquals("About the App", first.getName());
        assertNull(first.getGroup());
        assertNotNull(first.getPrice());
        assertFalse(first.getPrice().hasAmount());
        assertEquals("249594", first.getIconPhoto());
    }
    
    public void testShouldSucceedGettingApplicationSettings() throws DataException {
        Settings settings = ModelFactory.getSettings(mGuide);
        assertNotNull(settings);
        assertEquals("$", settings.getValue(Settings.Key.CURRENCY_STRING));
        assertEquals("mi", settings.getValue(Settings.Key.DISTANCE_UNITS));
    }
    
    public void testShouldSucceedGettingInvalidEntryDetails() throws DataException {
        IEntryDetail details = ModelFactory.getEntryDetails(mGuide, "123456789");
        assertNull(details);
    }    
    public void testShouldSucceedGettingValidEntryDetails() throws DataException {
        IEntryDetail details = ModelFactory.getEntryDetails(mGuide, "804");
        assertNotNull(details);
        assertEquals("The Met", details.getSubtitle());
        assertEquals("1000 Fifth Av, Manhattan", details.getAddress());
        assertEquals("2125357710", details.getPhoneRaw());
        assertEquals("(212) 535-7710", details.getPhoneFormatted());
        assertEquals("http://www.metmuseum.org", details.getWebUrl());
        assertNull(details.getTwitter());
        assertNull(details.getAudioUrl());
        assertEquals(
            "$20 adult, $15 senior, $10 student is the recommended donation. Includes same-day access to the Cloisters.", 
            details.getPriceDetails());
        assertEquals(
            "Tue-Thu, Sun 9:30am-5:30pm; Fri-Sat 9:30am-9pm; closed Mon except on holidays.",
            details.getHours());
        assertNotNull(details.getLocation());
        assertEquals(40.7791544, details.getLocation().getLatitude());
        assertEquals(-73.962697, details.getLocation().getLongitude());
        assertNotNull(details.getAudioPrice());
        assertFalse(details.getAudioPrice().hasAmount());
    }
    
    public void testShouldSucceedGettingGroupsForEntry() throws DataException {
        List<IGroup> all = ModelFactory.getGroupsForEntry(mGuide, "804");
        assertNotNull(all);
        assertEquals(4, all.size());
        assertEquals("Manhattan", all.get(0).getName());
        assertEquals("Museums", all.get(1).getName());
        assertEquals("Top 25", all.get(2).getName());
        assertEquals("Top 25 Author", all.get(3).getName());
    }
    
    public void testShouldSucceedGettingGroupsForInvalidEntry() throws DataException {
        List<IGroup> all = ModelFactory.getGroupsForEntry(mGuide, "12345678");
        assertNotNull(all);
        assertEquals(0, all.size());
    }    
    
    public void testShouldSucceedGettingPhotosForEntry() throws DataException {
        List<IPhoto>  all = ModelFactory.getPhotos(mGuide, "804");
        assertNotNull(all);
        assertEquals(10, all.size());
        IPhoto photo = all.get(0);
        assertEquals("83700", photo.getId());
        assertEquals("804", photo.getEntryId());
        assertEquals("Metropolitan Museum of Art", photo.getEntryName());
        assertNull(photo.getCaption());
        assertEquals("Jeffrey Tanenhaus", photo.getAuthor());
        assertNull(photo.getUrl());
        assertEquals(0, photo.getLicense());
        
        photo = all.get(1);
        assertEquals("4885", photo.getId());
        assertEquals("804", photo.getEntryId());
        assertEquals("Metropolitan Museum of Art", photo.getEntryName());
        assertNull(photo.getCaption());
        assertEquals("Mike Lyncheski", photo.getAuthor());
        assertEquals("http://www.flickr.com/photos/mlyncheski/3306611549/", photo.getUrl());
        assertEquals(4, photo.getLicense());
        
        //this collection of photos has an entry with a caption
        all = ModelFactory.getPhotos(mGuide, "33308");
        photo = all.get(0);
        assertEquals("236354", photo.getId());
        assertEquals("33308", photo.getEntryId());
        assertEquals("Bus", photo.getEntryName());
        assertEquals("Regular bus $2.25", photo.getCaption());
        assertEquals("Jeffrey Tanenhaus", photo.getAuthor());
        assertNull(photo.getUrl());
        assertEquals(0, photo.getLicense());
   }    
   
    public void testShouldSucceedGettingEtriesForGroup() throws DataException {
        GroupEntries groups = ModelFactory.getGroupEntries(mGuide);
        Set<String> all = groups.getEntries("2415");
        assertNotNull(all);
        assertEquals(9, all.size());
        assertTrue(all.contains("812"));
        assertTrue(all.contains("4508"));
        assertTrue(all.contains("5925"));
        assertTrue(all.contains("5926"));
        assertTrue(all.contains("7768"));
        assertTrue(all.contains("7811"));
        assertTrue(all.contains("35019"));
        assertTrue(all.contains("40436"));
        assertTrue(all.contains("40437"));
    }    
   
    
}