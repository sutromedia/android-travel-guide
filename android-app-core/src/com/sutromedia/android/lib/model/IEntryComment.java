package com.sutromedia.android.lib.model;


public interface IEntryComment {
    String getEntryId();
    String getEntryName();
    String getEntryIcon();
    IComment getComment();
    IComment getAnswer();

    boolean getHasAnswer();
    boolean getHasIcon();
}
