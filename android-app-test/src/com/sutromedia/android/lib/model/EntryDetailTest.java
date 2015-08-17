package com.sutromedia.android.lib.model;

import android.test.AndroidTestCase;
import com.sutromedia.android.lib.model.*;
 
public class EntryDetailTest extends AndroidTestCase {

    public void testShouldFailWithNullId() {
        try {
            new EntryDetail(null);
            fail("Expect exception with null ID");
        } catch (DataException error) {
        }
    }
    
    public void testShouldFailWithEmptyId() {
        try {
            new EntryDetail("");
            fail("Expect exception with empty ID");
        } catch (DataException error) {
        }
    }
    
    public void testShouldFailWithTrimmedEmptyId() {
        try {
            new EntryDetail("  \t");
            fail("Expect exception with empty trimmed ID");
        } catch (DataException error) {
        }
    }
    
    public void testShouldSucceedValidConstructor() throws DataException {
        EntryDetail entry = new EntryDetail("1234");
        assertEquals("1234", entry.getId());
        assertNull(entry.getSubtitle());
        assertNull(entry.getDescription());
        assertNull(entry.getLocation());
        assertEquals(0, entry.getGroups().size());
        assertNull(entry.getAddress());
        assertNull(entry.getPhoneRaw());
        assertNull(entry.getPhoneFormatted());
        assertNull(entry.getWebUrl());
        assertNull(entry.getAudioUrl());
        assertNull(entry.getTwitter());
        assertNull(entry.getAudioPrice());
        assertNull(entry.getPriceDetails());
        assertNull(entry.getHours());
    }
    
    public void testShouldSucceedSettingSubtitle()  throws DataException {
        EntryDetail entry = new EntryDetail("1234");
        entry.setSubtitle("The king");
        assertEquals("The king", entry.getSubtitle());
        entry.setSubtitle(null);
        assertNull(entry.getSubtitle());
    }
    
    public void testShouldSucceedSettingDescription() throws DataException {
        EntryDetail entry = new EntryDetail("1234");
        entry.setDescription("some html");
        assertEquals("some html", entry.getDescription());
        entry.setDescription(null);
        assertNull(entry.getDescription());
    }    

    public void testShouldSucceedSettingLocation()  throws DataException {
        EntryDetail entry = new EntryDetail("1234");
        entry.setLocation(1.1, 2.2);
        assertEquals(1.1, entry.getLocation().getLongitude());
        assertEquals(2.2, entry.getLocation().getLatitude());
        entry.resetLocation();
        assertNull(entry.getLocation());
    }
    
    public void testShouldSucceedSettingGroups() throws DataException {
        EntryDetail entry = new EntryDetail("1234");
        entry.addGroup(new Group("001", "Best of"));
        entry.addGroup(new Group("001", "Free"));
        assertEquals("Best of", entry.getGroups().get(0).getName());
        assertEquals("Free", entry.getGroups().get(1).getName());
        entry.resetGroups();
        assertEquals(0, entry.getGroups().size());
    }
    
    public void testShouldSucceedSettingAddress() throws DataException {
        EntryDetail entry = new EntryDetail("1234");
        entry.setAddress("Somewhere over the rainbow");
        assertEquals("Somewhere over the rainbow", entry.getAddress());
        entry.setAddress("");
        assertNull(entry.getAddress());
    }    
    
    public void testShouldSucceedSettingPhoneRaw() throws DataException {
        EntryDetail entry = new EntryDetail("1234");
        entry.setPhoneRaw("415");
        assertEquals("415", entry.getPhoneRaw());
        entry.setPhoneRaw("");
        assertNull(entry.getPhoneRaw());
    }
    
    public void testShouldSucceedSettingPhoneFormatted() throws DataException {
        EntryDetail entry = new EntryDetail("1234");
        entry.setPhoneFormatted("415-111-2222");
        assertEquals("415-111-2222", entry.getPhoneFormatted());
        entry.setPhoneFormatted("");
        assertNull(entry.getPhoneFormatted());
    }

    public void testShouldSucceedSettingWebsiteUrl() throws DataException {
        EntryDetail entry = new EntryDetail("1234");
        entry.setWebUrl("ny.com");
        assertEquals("ny.com", entry.getWebUrl());
        entry.setWebUrl("");
        assertNull(entry.getWebUrl());
    }

   public void testShouldSucceedSettingAudioUrl() throws DataException {
        EntryDetail entry = new EntryDetail("1234");
        entry.setAudioUrl("ny.com/sound.mp3");
        assertEquals("ny.com/sound.mp3", entry.getAudioUrl());
        entry.setAudioUrl("");
        assertNull(entry.getAudioUrl());
    }    

   public void testShouldSucceedSettingAudioPrice() throws DataException {
        EntryDetail entry = new EntryDetail("1234");
        entry.setAudioPrice("Not a number");
        assertNull(entry.getAudioPrice().getFormatted("$"));
        entry.setAudioPrice("");
        assertNull(entry.getAudioPrice().getFormatted("$"));
        entry.setAudioPrice("0");
        assertEquals("Free!", entry.getAudioPrice().getFormatted("$"));
        entry.setAudioPrice("20");
        assertEquals("$20", entry.getAudioPrice().getFormatted("$"));
    }    
    
   public void testShouldSucceedSettingTwitter() throws DataException {
        EntryDetail entry = new EntryDetail("1234");
        entry.setTwitter("Free on mondays");
        assertEquals("Free on mondays", entry.getTwitter());
        entry.setTwitter("");
        assertNull(entry.getTwitter());
    }    
    
   public void testShouldSucceedSettingPriceDetails() throws DataException {
        EntryDetail entry = new EntryDetail("1234");
        entry.setPriceDetails("Free on mondays");
        assertEquals("Free on mondays", entry.getPriceDetails());
        entry.setPriceDetails("");
        assertNull(entry.getPriceDetails());
    }    
    
   public void testShouldSucceedSettingHours() throws DataException {
        EntryDetail entry = new EntryDetail("1234");
        entry.setHours("Free on mondays");
        assertEquals("Free on mondays", entry.getHours());
        entry.setHours("");
        assertNull(entry.getHours());
    }   
}