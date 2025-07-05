package com.example.eventify.utils;

import androidx.fragment.app.Fragment;

public interface NavigationManager {
    /**
     * Navigate to a fragment and add it to the back stack
     * @param fragment The fragment to navigate to
     */
    void navigateToFragment(Fragment fragment);
    
    /**
     * Navigate to a fragment with option to add to back stack
     * @param fragment The fragment to navigate to
     * @param addToBackStack Whether to add to back stack
     */
    void navigateToFragment(Fragment fragment, boolean addToBackStack);
    
    /**
     * Navigate back to the previous fragment
     */
    void navigateBack();
    
    /**
     * Check if we can navigate back
     * @return true if there are fragments in the back stack
     */
    boolean canNavigateBack();
} 