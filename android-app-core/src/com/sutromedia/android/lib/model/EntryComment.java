package com.sutromedia.android.lib.model;


public class EntryComment extends ModelBase implements IEntryComment {

    private String   mEntryId;
    private String   mEntryName;
    private String   mEntryIcon;
    private IComment mComment;
    private IComment mAnswer;


    public EntryComment(
        final String entryId,
        final String entryName,
        final String entryIcon) throws DataException {
        
        mEntryId = ensureNullIfEmpty(entryId);
        mEntryName = ensureNullIfEmpty(entryName);
        mEntryIcon = ensureNullIfEmpty(entryIcon);
    }

    public void setComment(final IComment comment) throws DataException {
        if (comment == null) {
            throw new DataException("EntryComment does not accept null comment");
        }
        mComment = comment;
    }

    public void setAnswer(final IComment answer) {
        mAnswer = answer;
    }

    public String getEntryId() {
        return mEntryId;
    }

    public String getEntryIcon() {
        return mEntryIcon;
    }

    public String getEntryName() {
        return mEntryName;
    }

    public IComment getComment() {
        return mComment;
    }

    public IComment getAnswer() {
        return mAnswer;
    }

    public boolean getHasAnswer() {
        return mAnswer != null;
    }

    public boolean getHasIcon() {
        return  mEntryIcon != null;
    }
}