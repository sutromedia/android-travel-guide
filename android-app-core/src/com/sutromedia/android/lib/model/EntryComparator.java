package com.sutromedia.android.lib.model;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import android.location.Location;

import com.sutromedia.android.lib.model.EntrySorter.SortField;

public class EntryComparator implements Comparator<IEntrySummary> {
    
    private SortField   mSort;
    private Set<String> mFavorites;
    private Location    mCurrentLocation;
    
    public EntryComparator(SortField sort) {
        mSort = sort;
        mFavorites = new HashSet<String>();
    }
    
    public void setFavorites(Set<String> favorites) {
        mFavorites = new HashSet<String>(favorites);
    }

    public void setLocation(Location location) {
        mCurrentLocation = location;
    }

    public int compare(IEntrySummary o1, IEntrySummary o2) {
        
        int sortFavorite = compareByFavorites(o1, o2);
        if (sortFavorite==0) {
            switch (mSort) {
                case BY_NAME:
                    return compareByName(o1, o2);

                case BY_DISTANCE:
                    return compareByDistance(o1, o2);
                
                case BY_COST:
                    return compareByCost(o1, o2);
                    
                case BY_NEIGHBORHOOD:
                    return compareByNeighborhood(o1, o2);
                    
                default:
                    throw new RuntimeException("Internal error: invalid sort order");
            }
        }
        
        return sortFavorite;
    }
    
    public int compareByName(IEntrySummary o1, IEntrySummary o2) {
        return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
    }    
    
    public int compareByDistance(IEntrySummary o1, IEntrySummary o2) {
        Double d1 = getDistance(o1);
        Double d2 = getDistance(o2);
        int compare = d1.compareTo(d2);
        if (compare==0) {
            compare = compareByName(o1, o2);
        }
        return compare;
    }        
    
    public int compareByCost(IEntrySummary o1, IEntrySummary o2) {
        Double cost1 = getCost(o1);
        Double cost2 = getCost(o2);
        int compare = cost1.compareTo(cost2);
        if (compare==0) {
            compare = compareByName(o1, o2);
        }
        return compare;
    }
    
    public int compareByNeighborhood(IEntrySummary o1, IEntrySummary o2) {
        int compare = comparePossiblyNull(o1.getGroup(), o2.getGroup());
        if (compare==0) {
            compare = compareByName(o1, o2);
        }
        return compare;
    }    
    
    public int compareByFavorites(IEntrySummary o1, IEntrySummary o2) {
        Integer fav1 = mFavorites.contains(o1.getId()) ? 0 : 1;
        Integer fav2 = mFavorites.contains(o2.getId()) ? 0 : 1;
        return fav1.compareTo(fav2);
    }
    
    private Double getDistance(IEntrySummary entry) {
        double distance = Double.MAX_VALUE;
        if (mCurrentLocation!=null && entry.getLocation()!=null) {
            distance = mCurrentLocation.distanceTo(entry.getLocation());
        }
        return distance;
    }
    
    private Double getCost(IEntrySummary entry) {
        double cost = Double.MAX_VALUE;
        CurrencyAmount price = entry.getPrice();
        if (price.hasAmount()) {
            cost = price.getAmount();
        }
        return cost;
    }
    
    private int comparePossiblyNull(String data1, String data2) {
        if (data1!=null && data2!=null) {
            return data1.compareTo(data2);
        } else if (data1==null && data2==null) {
            return 0;
        } else if (data1==null && data2!=null) {
            return 1;
        } else  {
            //(data1!=null && data2==null)
            return -1;
        }
    }
}