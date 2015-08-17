package com.sutromedia.android.lib.model;

public class EntryCommentTest extends junit.framework.TestCase {

    public void testShouldThrowExceptionWhenNullId() {
	try {
	    new EntryComment(null, "New York", "icon");
	    fail("should have thrown an exception");
	} catch (DataException error) {
	    assertEquals("EntryComment does not accept empty/null id", error.getMessage());
	}
    }

    public void testShouldThrowExceptionWhenEmptyId() {
	try {
	    new EntryComment(null, "New York", "icon");
	    fail("should have thrown an exception");
	} catch (DataException error) {
	    assertEquals("EntryComment does not accept empty/null id", error.getMessage());
	}
    }

    public void testShouldThrowExceptionWhenNullName() {
	try {
	    new EntryComment("001", null, "icon");
	    fail("should have thrown an exception");
	} catch (DataException error) {
	    assertEquals("EntryComment does not accept empty/null name", error.getMessage());
	}
    }

    public void testShouldThrowExceptionWhenEmptyName() {
	try {
 	    new EntryComment("001", "\t", "icon");
	    fail("should have thrown an exception");
	} catch (DataException error) {
	    assertEquals("EntryComment does not accept empty/null name", error.getMessage());
	}
    }

    public void testShouldAcceptNullOrEmptyIcon() throws DataException {
 	assertNull((new EntryComment("001", "NY", null)).getEntryIcon());
 	assertNull((new EntryComment("001", "NY", "")).getEntryIcon());
    }

    public void testShouldThrowExceptionWhenEmptyComment() {
	try {
	    EntryComment entry = new EntryComment("001", "New York", "icon");
	    entry.setComment(null);
	    fail("should have thrown an exception");
	} catch (DataException error) {
	    assertEquals("EntryComment does not accept null comment", error.getMessage());
	}
    }

    public void testShouldAcceptEmptyAnswer() throws DataException {
	EntryComment entry = new EntryComment("001", "New York", "icon");
	assertFalse(entry.getHasAnswer());
    }

    public void testShouldWorkWithoutAnswer() throws DataException {
	EntryComment entry = new EntryComment("001", "New York", "icon");
	entry.setComment(new Comment("Hello", "Today", "Mozart"));
	assertEquals("Hello", entry.getComment().getMessage());
	assertEquals("Today", entry.getComment().getDate());
	assertEquals("Mozart", entry.getComment().getAuthor());
	assertFalse(entry.getHasAnswer());
    }

    public void testShouldWorkWithAnswer() throws DataException {
	EntryComment entry = new EntryComment("001", "New York", "icon");
	entry.setComment(new Comment("Great!", "Today", "Mozart"));
	entry.setAnswer(new Comment("I agree!", "Tomorrow", "Bach"));
	assertEquals("Great!", entry.getComment().getMessage());
	assertEquals("Today", entry.getComment().getDate());
	assertEquals("Mozart", entry.getComment().getAuthor());
	assertTrue(entry.getHasAnswer());
	assertEquals("I agree!", entry.getAnswer().getMessage());
	assertEquals("Tomorrow", entry.getAnswer().getDate());
	assertEquals("Bach", entry.getAnswer().getAuthor());
    }

}
