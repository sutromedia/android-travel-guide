package com.sutromedia.android.lib.db;

import android.test.AndroidTestCase;
import com.sutromedia.android.lib.model.*;
 
public class PhotoTest extends AndroidTestCase {

    public void testShouldFailInvalidContructor() {
    
        String invalidId = "Photo.id can not be null or empty";
        ensureErrorForInvalidConstructor(null, "10", "New York", invalidId);
        ensureErrorForInvalidConstructor("", "10", "New York", invalidId);
        ensureErrorForInvalidConstructor("\t", "10", "New York", invalidId);
        
        String invalidEntryId = "Photo.entryId can not be null or empty";
        ensureErrorForInvalidConstructor("101", null, "New York", invalidEntryId);
        ensureErrorForInvalidConstructor("101", "", "New York", invalidEntryId);
        ensureErrorForInvalidConstructor("101", "  \t", "New York", invalidEntryId);
        
        String invalidName = "Photo.entryName can not be null or empty";
        ensureErrorForInvalidConstructor("101", "10", null, invalidName);
        ensureErrorForInvalidConstructor("101", "10", "", invalidName);
        ensureErrorForInvalidConstructor("101", "10", "  \t\t", invalidName);
    }

    public void testShouldSetGetAttributesSucceed() throws DataException {
        Photo photo = new Photo("101", "10", "New York");
        assertEquals("101", photo.getId());
        assertEquals("10", photo.getEntryId());
        assertEquals("New York", photo.getEntryName());
        assertNull(photo.getAuthor());
        assertNull(photo.getCaption());
        assertEquals(0, photo.getLicense());
        assertNull(photo.getUrl());
        
        photo.setAuthor("Tobin");
        assertEquals("Tobin", photo.getAuthor());
        photo.setAuthor("");
        assertNull(photo.getAuthor());

        photo.setCaption("Best place");
        assertEquals("Best place", photo.getCaption());
        photo.setCaption("");
        assertNull(photo.getCaption());

        photo.setLicense(5);
        assertEquals(5, photo.getLicense());

        photo.setUrl("www.flicker.com/look-there");
        assertEquals("www.flicker.com/look-there", photo.getUrl());
        photo.setUrl("");
        assertNull(photo.getUrl());
    }
    
    private void ensureErrorForInvalidConstructor(
        String id, 
        String entryId, 
        String name, 
        String message) {

        try {
            new Photo(id, entryId, name);
            fail("should have thrown an exception");
        } catch (DataException error) {
            assertEquals(message, error.getMessage());
        }
    }

}