# Firebase Setup Instructions

## The Problem

The error "This operation is restricted to administrators only" occurs because:

1. Firebase Anonymous Authentication is disabled in your project
2. Your Firestore security rules require authentication for all operations
3. Child users need access without Firebase Authentication

## Solution Implemented

I've modified the code to create child users without Firebase Authentication, using only Firestore
documents with custom session management.

## Steps to Complete the Fix

### 1. Update Firestore Security Rules

Copy the rules from `firestore-rules-updated.txt` and paste them in your Firebase Console:

1. Go to Firebase Console → Firestore Database → Rules
2. Replace your current rules with the updated rules
3. Click "Publish"

### 2. Code Changes Made

✅ Modified `AuthHelper.createChildUser()` to create children without Firebase Auth
✅ Added `saveChildSession()` and `getChildId()` methods for child session management  
✅ Added `getCurrentUserId(Context)` method that works for both parents and children
✅ Updated `KidTasksFragment`, `KidRewardsFragment`, and `KidDashboardActivity` to use the new method

### 3. How It Works Now

- **Parents**: Sign in with Google Authentication (Firebase Auth)
- **Children**: Create accounts without Firebase Auth, using custom ID generation
- **Database Access**: Updated security rules allow children to access their data
- **Session Management**: Children's sessions are managed locally with SharedPreferences

### 4. Testing the Fix

1. Update your Firestore rules as described above
2. Run the app
3. Parent can generate invite code
4. Child can enter invite code and join family
5. Child should see assigned tasks without authentication errors

The system now works around the Firebase Auth limitation while maintaining security through
Firestore rules.