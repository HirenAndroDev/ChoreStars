package com.chores.app.kids.chores_app_for_kids.base;

import java.lang.ref.WeakReference;

/**
 * Base presenter implementation for MVP architecture
 * Handles view attachment/detachment and common presenter functionality
 * @param <V> View type that extends BaseView
 */
public abstract class BasePresenter<V extends BaseView> {

    private WeakReference<V> viewRef;

    /**
     * Attach view to presenter
     * @param view View to attach
     */
    public void attachView(V view) {
        viewRef = new WeakReference<>(view);
    }

    /**
     * Detach view from presenter
     */
    public void detachView() {
        if (viewRef != null) {
            viewRef.clear();
            viewRef = null;
        }
    }

    /**
     * Check if view is attached
     * @return true if view is attached, false otherwise
     */
    public boolean isViewAttached() {
        return viewRef != null && viewRef.get() != null;
    }

    /**
     * Get attached view
     * @return Attached view or null
     */
    protected V getView() {
        return viewRef != null ? viewRef.get() : null;
    }

    /**
     * Check view attached and throw exception if not
     */
    protected void checkViewAttached() {
        if (!isViewAttached()) {
            throw new MvpViewNotAttachedException();
        }
    }

    /**
     * Show loading in view if attached
     */
    protected void showLoading() {
        if (isViewAttached() && getView() != null) {
            getView().showLoading();
        }
    }

    /**
     * Show loading with message in view if attached
     * @param message Loading message
     */
    protected void showLoading(String message) {
        if (isViewAttached() && getView() != null) {
            getView().showLoading(message);
        }
    }

    /**
     * Hide loading in view if attached
     */
    protected void hideLoading() {
        if (isViewAttached() && getView() != null) {
            getView().hideLoading();
        }
    }

    /**
     * Show error in view if attached
     * @param message Error message
     */
    protected void showError(String message) {
        if (isViewAttached() && getView() != null) {
            getView().showError(message);
        }
    }

    /**
     * Show message in view if attached
     * @param message Message to show
     */
    protected void showMessage(String message) {
        if (isViewAttached() && getView() != null) {
            getView().showMessage(message);
        }
    }

    /**
     * Check if network is available
     * @return true if network available and view attached, false otherwise
     */
    protected boolean isNetworkAvailable() {
        if (isViewAttached() && getView() != null) {
            return getView().isNetworkAvailable();
        }
        return false;
    }

    /**
     * Handle common errors
     * @param throwable Error to handle
     */
    protected void handleError(Throwable throwable) {
        hideLoading();

        if (throwable != null) {
            String message = throwable.getMessage();

            if (message != null && message.toLowerCase().contains("network")) {
                showError("Network error. Please check your connection.");
            } else if (message != null && message.toLowerCase().contains("timeout")) {
                showError("Request timeout. Please try again.");
            } else if (message != null && message.toLowerCase().contains("permission")) {
                showError("Permission denied.");
            } else {
                showError(message != null ? message : "An error occurred");
            }
        } else {
            showError("An unknown error occurred");
        }
    }

    /**
     * Custom exception for when view is not attached
     */
    public static class MvpViewNotAttachedException extends RuntimeException {
        public MvpViewNotAttachedException() {
            super("Please call Presenter.attachView(MvpView) before requesting data to the Presenter");
        }
    }
}
