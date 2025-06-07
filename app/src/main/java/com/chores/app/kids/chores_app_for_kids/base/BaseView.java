package com.chores.app.kids.chores_app_for_kids.base;

/**
 * Base interface for all views in MVP architecture
 * Provides common functionality that all views should implement
 */
public interface BaseView {

    /**
     * Show loading indicator
     */
    void showLoading();

    /**
     * Show loading indicator with custom message
     * @param message Loading message to display
     */
    void showLoading(String message);

    /**
     * Hide loading indicator
     */
    void hideLoading();

    /**
     * Show error message
     * @param message Error message to display
     */
    void showError(String message);

    /**
     * Show success or info message
     * @param message Message to display
     */
    void showMessage(String message);

    /**
     * Check if network is available
     * @return true if network is available, false otherwise
     */
    boolean isNetworkAvailable();
}