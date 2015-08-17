package com.sutromedia.android.lib.db;

import android.test.AndroidTestCase;
import com.sutromedia.android.lib.model.*;
 
public class PhotoLicenceTest extends AndroidTestCase {

    private Photo mPhoto;

    public void setUp() throws DataException {
        mPhoto = new Photo("123", "456", "New York");
    }
    
    private Integer[] getIcons() {
        return PhotoLicence.getIcons(mPhoto);
    }
    
    private Integer[] makeArray(Integer... values) {
        return values;
    }
    
    private void expectLicenses(int code, Integer[] expected) {
        mPhoto.setLicense(code);
        Integer[] icons = getIcons();
        assertNotNull(icons);
        assertNotNull(expected);
        assertEquals(expected.length, icons.length);
        for (int i=0;i<expected.length;i++) {
            assertEquals(expected[i], icons[i]);
        }
    }

    public void testShouldReturnEmptyCollectionForNoAuthorAnyCode() {
        for (int i=0;i<10;i++) {
            mPhoto.setLicense(i);
            assertEquals(0, getIcons().length);
        }
    }

    public void testCodesWhenNonEmptyAuthor() {
        mPhoto.setAuthor("Tobin");
        expectLicenses(0, makeArray(PhotoLicence.COPYRIGHT));
        expectLicenses(1, makeArray(PhotoLicence.ATTRIBUTION, PhotoLicence.SHARE_ALIKE));
        expectLicenses(2, makeArray(PhotoLicence.ATTRIBUTION));
        expectLicenses(3, makeArray(PhotoLicence.ATTRIBUTION, PhotoLicence.NODERIVATIVE));
        expectLicenses(4, makeArray(PhotoLicence.ATTRIBUTION));
        expectLicenses(5, makeArray(PhotoLicence.ATTRIBUTION, PhotoLicence.SHARE_ALIKE));
        expectLicenses(6, makeArray(PhotoLicence.ATTRIBUTION, PhotoLicence.NODERIVATIVE));
        expectLicenses(7, makeArray());
        expectLicenses(8, makeArray());
    }        
}