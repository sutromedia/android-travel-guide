package com.sutromedia.android.lib.model;

public class ModelBase {

  
    protected String ensureNotEmpty(String data, String message)  throws DataException {
        if (data == null || data.length()==0) {
            throw new DataException(message);
        }
        return data;
    }
    
    protected String ensureNotEmptyTrimmed(String data, String message)  throws DataException {
        ensureNotEmpty(data, message);
        return ensureNotEmpty(data.trim(), message);
    }
    
    protected String ensureNullIfEmpty(String data) {
        if (data == null || data.length()==0) {
            return null;
        }
        return data;
    }

    protected String ensureNullIfTrimmedEmpty(String data) {
        if (data == null || data.trim().length()==0) {
            return null;
        }
        return data;
    }

    protected String ensureNotNullIfEmpty(String data) {
        if (data == null || data.trim().length()==0) {
            return "";
        }
        return data;
    }
}