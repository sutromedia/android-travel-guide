package com.sutromedia.android.lib.model;

public class CommentTest extends junit.framework.TestCase {

    public void testShouldThrowExceptionForNullBody() {
	try {
	    Comment comment = new Comment(null, "Jan 10 2012", "Mozart");
	    fail("should have thrown an exception");
	} catch (DataException error) {
	    assertEquals("Comment body should not be empty", error.getMessage());
	}
    }

    public void testShouldThrowExceptionForEmptyBody() {
	try {
	    Comment comment = new Comment(" ", "Jan 10 2012", "Mozart");
	    fail("should have thrown an exception");
	} catch (DataException error) {
	    assertEquals("Comment body should not be empty", error.getMessage());
	}
    }

    public void testShouldAcceptEmptyOrNullDate() throws DataException {
	assertEquals("", (new Comment("Hello", null, "Mozart")).getDate());
	assertEquals("", (new Comment("Hello", "", "Mozart")).getDate());
	assertEquals("", (new Comment("Hello", "\t", "Mozart")).getDate());
    }

    public void testShouldAcceptEmptyOrNullAuthor() throws DataException {
	assertNull((new Comment("Hello", "today", null)).getAuthor());
	assertNull((new Comment("Hello", "today", "")).getAuthor());
	assertNull((new Comment("Hello", "today", "\t")).getAuthor());
    }

    public void testShouldGetAttributes() throws DataException {
	Comment comment = new Comment("Great!", "today", "Mozart");
	assertEquals("Great!", comment.getMessage());
	assertEquals("today", comment.getDate());
	assertEquals("Mozart", comment.getAuthor());
    }
}