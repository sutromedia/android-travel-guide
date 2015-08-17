package com.sutromedia.android.lib.model;

import java.util.ArrayList;

public class PhotoLicence {

    public static final int COPYRIGHT = 1;
    public static final int ATTRIBUTION = 2;
    public static final int SHARE_ALIKE = 3;
    public static final int NODERIVATIVE = 4;
    
    
    
    public static Integer[] getIcons(IPhoto photo) {
    
        ArrayList<Integer> all = new ArrayList<Integer>();
        if (!hasValue(photo.getAuthor())) {
            //nothing to do => no icons
        } else {
            //There is an author => put some icons up (except for licence code 7)
            switch (photo.getLicense()) {
                case 0:
                    all.add(COPYRIGHT);
                    break;
                    
                case 1:
                case 5:
                    all.add(ATTRIBUTION);
                    all.add(SHARE_ALIKE);
                    break;
                    
                case 2:
                case 4:
                    all.add(ATTRIBUTION);
                    break;
                    
                case 3:
                case 6:
                    all.add(ATTRIBUTION);
                    all.add(NODERIVATIVE);
                    break;
                    
                default:
                    //include the value 7, which means no icons
            }
        }
        
        Integer[] returnValues = new Integer[all.size()];
        return all.toArray(returnValues);
    }
    

    private static boolean hasValue(String value) {
        return (value!=null) && (value.length()>0);
    }
}