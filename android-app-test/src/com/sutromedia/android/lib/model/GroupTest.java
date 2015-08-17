package com.sutromedia.android.lib.db;

import android.test.AndroidTestCase;
import com.sutromedia.android.lib.model.*;
 
public class GroupTest extends AndroidTestCase {

    public void testShouldFailInvalidId() {
        String error = "Group.id is required";
        doTestForException(null, "New York", error);
        doTestForException("", "New York", error);
        doTestForException("\t ", "New York", error);
    }

    public void testShouldFailInvalidName() {
        String error = "Group.name is required";
        doTestForException("123", null, error);
        doTestForException("123", "", error);
        doTestForException("123", " \t ", error);
    }
    
    private void doTestForException(String id, String name, String expectedError) {
        try {
            new Group(id, name);
            fail("should throw an exception");
        } catch (DataException error) {
            assertEquals(expectedError, error.getMessage());
        }
    }
}