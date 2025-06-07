package com.chores.app.kids.chores_app_for_kids.data.repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.chores.app.kids.chores_app_for_kids.data.models.Notification;
import com.chores.app.kids.chores_app_for_kids.data.models.Task;
import com.chores.app.kids.chores_app_for_kids.data.models.TaskCompletion;
import com.chores.app.kids.chores_app_for_kids.data.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Repository class for task operations
 */
public class TaskRepository {

    private static final String TAG = "TaskRepository";

    private DatabaseReference databaseReference;
    private DatabaseReference tasksRef;
    private DatabaseReference taskCompletionsRef;
    private DatabaseReference usersRef;
    private DatabaseReference notificationsRef;

    public TaskRepository() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        tasksRef = databaseReference.child("tasks");
        taskCompletionsRef = databaseReference.child("taskCompletions");
        usersRef = databaseReference.child("users");
        notificationsRef = databaseReference.child("notifications");
    }

    /**
     * Create new task
     */
    public void createTask(Task task, final CreateTaskCallback callback) {
        String taskId = tasksRef.push().getKey();
        if (taskId == null) {
            callback.onError(new Exception("Failed to generate task ID"));
            return;
        }

        task.setTaskId(taskId);

        tasksRef.child(taskId).setValue(task)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Create notifications for assigned kids
                        createNewTaskNotifications(task);
                        callback.onSuccess(task);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e);
                    }
                });
    }

    /**
     * Create notifications for new task
     */
    private void createNewTaskNotifications(Task task) {
        if (task.getAssignedTo() != null && !task.getAssignedTo().isEmpty()) {
            for (String kidId : task.getAssignedTo()) {
                Notification notification = new Notification();
                notification.setUserId(kidId);
                notification.setType("new_task");
                notification.setTitle("New Task Assigned");
                notification.setMessage("You have a new task: " + task.getTaskName());
                notification.setActionType("open_task");
                notification.setActionId(task.getTaskId());
                notification.setCreatedAt(System.currentTimeMillis());

                Map<String, Object> data = new HashMap<>();
                data.put("taskId", task.getTaskId());
                data.put("taskName", task.getTaskName());
                notification.setData(data);

                String notificationId = notificationsRef.push().getKey();
                if (notificationId != null) {
                    notification.setNotificationId(notificationId);
                    notificationsRef.child(notificationId).setValue(notification);
                }
            }
        }
    }

    /**
     * Get all tasks for family
     */
    public void getTasksForFamily(String familyId, final GetTasksCallback callback) {
        Query query = tasksRef.orderByChild("familyId").equalTo(familyId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Task> tasks = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Task task = snapshot.getValue(Task.class);
                    if (task != null && task.isActive()) {
                        tasks.add(task);
                    }
                }
                callback.onSuccess(tasks);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception(error.getMessage()));
            }
        });
    }

    /**
     * Get tasks for specific kid
     */
    public void getTasksForKid(String kidId, String familyId, final GetTasksCallback callback) {
        Query query = tasksRef.orderByChild("familyId").equalTo(familyId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Task> tasks = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Task task = snapshot.getValue(Task.class);
                    if (task != null && task.isActive() &&
                            task.getAssignedTo() != null &&
                            task.getAssignedTo().contains(kidId)) {
                        tasks.add(task);
                    }
                }
                callback.onSuccess(tasks);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception(error.getMessage()));
            }
        });
    }

    /**
     * Get today's tasks for kid with completion status
     */
    public void getTodayTasksForKid(String kidId, String familyId, final GetTasksWithCompletionCallback callback) {
        // First get all tasks for the kid
        getTasksForKid(kidId, familyId, new GetTasksCallback() {
            @Override
            public void onSuccess(List<Task> allTasks) {
                // Filter tasks due today
                List<Task> todayTasks = new ArrayList<>();
                for (Task task : allTasks) {
                    if (task.isDueToday()) {
                        todayTasks.add(task);
                    }
                }

                // Now check completion status for today
                checkTaskCompletions(todayTasks, kidId, callback);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    /**
     * Check task completions for today
     */
    private void checkTaskCompletions(List<Task> tasks, String kidId,
                                      final GetTasksWithCompletionCallback callback) {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        final Map<String, Boolean> completionStatus = new HashMap<>();

        if (tasks.isEmpty()) {
            callback.onSuccess(tasks, completionStatus);
            return;
        }

        final int[] completedRequests = {0};

        for (Task task : tasks) {
            Query query = taskCompletionsRef
                    .orderByChild("kidId").equalTo(kidId);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean isCompleted = false;

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        TaskCompletion completion = snapshot.getValue(TaskCompletion.class);
                        if (completion != null &&
                                completion.getTaskId().equals(task.getTaskId()) &&
                                completion.getDate().equals(todayDate)) {
                            isCompleted = true;
                            break;
                        }
                    }

                    completionStatus.put(task.getTaskId(), isCompleted);
                    completedRequests[0]++;

                    if (completedRequests[0] == tasks.size()) {
                        callback.onSuccess(tasks, completionStatus);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(new Exception(error.getMessage()));
                }
            });
        }
    }

    /**
     * Complete task
     */
    public void completeTask(String taskId, String kidId, String photoProofUrl,
                             final CompleteTaskCallback callback) {
        // First get task details
        tasksRef.child(taskId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Task task = snapshot.getValue(Task.class);
                    if (task != null) {
                        // Check if already completed today
                        checkIfAlreadyCompletedToday(task, kidId, photoProofUrl, callback);
                    } else {
                        callback.onError(new Exception("Task not found"));
                    }
                } else {
                    callback.onError(new Exception("Task not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception(error.getMessage()));
            }
        });
    }

    /**
     * Check if task already completed today
     */
    private void checkIfAlreadyCompletedToday(Task task, String kidId, String photoProofUrl,
                                              final CompleteTaskCallback callback) {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Query query = taskCompletionsRef
                .orderByChild("kidId").equalTo(kidId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean alreadyCompleted = false;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TaskCompletion completion = snapshot.getValue(TaskCompletion.class);
                    if (completion != null &&
                            completion.getTaskId().equals(task.getTaskId()) &&
                            completion.getDate().equals(todayDate)) {
                        alreadyCompleted = true;
                        break;
                    }
                }

                if (alreadyCompleted) {
                    callback.onError(new Exception("Task already completed today"));
                } else {
                    // Proceed with completion
                    createTaskCompletion(task, kidId, photoProofUrl, callback);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception(error.getMessage()));
            }
        });
    }

    /**
     * Create task completion record
     */
    private void createTaskCompletion(Task task, String kidId, String photoProofUrl,
                                      final CompleteTaskCallback callback) {
        // Create completion
        TaskCompletion completion = new TaskCompletion(
                task.getTaskId(),
                kidId,
                task.getStarsPerCompletion(),
                task.getTaskName()
        );

        if (photoProofUrl != null && !photoProofUrl.isEmpty()) {
            completion.setPhotoProofUrl(photoProofUrl);
        }

        String completionId = taskCompletionsRef.push().getKey();
        if (completionId == null) {
            callback.onError(new Exception("Failed to generate completion ID"));
            return;
        }

        completion.setCompletionId(completionId);

        // Get kid details for notification
        usersRef.child(kidId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User kid = snapshot.getValue(User.class);
                    if (kid != null) {
                        updateStarWalletAndComplete(task, kid, completion, callback);
                    } else {
                        callback.onError(new Exception("Kid not found"));
                    }
                } else {
                    callback.onError(new Exception("Kid not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception(error.getMessage()));
            }
        });
    }

    /**
     * Update star wallet and complete task
     */
    private void updateStarWalletAndComplete(Task task, User kid, TaskCompletion completion,
                                             final CompleteTaskCallback callback) {
        // Calculate new star balance
        int newStarBalance = kid.getStarWallet() + task.getStarsPerCompletion();

        // Prepare updates
        Map<String, Object> updates = new HashMap<>();
        updates.put("/taskCompletions/" + completion.getCompletionId(), completion.toMap());
        updates.put("/users/" + kid.getUserId() + "/starWallet", newStarBalance);

        // Create notification for parent
        if (kid.getParentId() != null) {
            Notification notification = Notification.createTaskCompletion(
                    kid.getParentId(),
                    kid.getName(),
                    task.getTaskName(),
                    task.getTaskId()
            );

            String notificationId = notificationsRef.push().getKey();
            if (notificationId != null) {
                notification.setNotificationId(notificationId);
                updates.put("/notifications/" + notificationId, notification.toMap());
            }
        }

        // Perform batch update
        databaseReference.updateChildren(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess(completion, newStarBalance);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e);
                    }
                });
    }

    /**
     * Update task
     */
    public void updateTask(Task task, final UpdateCallback callback) {
        tasksRef.child(task.getTaskId()).setValue(task)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e);
                    }
                });
    }

    /**
     * Delete task (soft delete)
     */
    public void deleteTask(String taskId, final UpdateCallback callback) {
        tasksRef.child(taskId).child("isActive").setValue(false)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e);
                    }
                });
    }

    /**
     * Get task by ID
     */
    public void getTaskById(String taskId, final GetTaskCallback callback) {
        tasksRef.child(taskId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Task task = snapshot.getValue(Task.class);
                    if (task != null) {
                        callback.onSuccess(task);
                    } else {
                        callback.onError(new Exception("Task not found"));
                    }
                } else {
                    callback.onError(new Exception("Task not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception(error.getMessage()));
            }
        });
    }

    /**
     * Get task completion history for kid
     */
    public void getTaskCompletionHistory(String kidId, final GetCompletionsCallback callback) {
        Query query = taskCompletionsRef
                .orderByChild("kidId")
                .equalTo(kidId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<TaskCompletion> completions = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TaskCompletion completion = snapshot.getValue(TaskCompletion.class);
                    if (completion != null) {
                        completions.add(completion);
                    }
                }

                // Sort by date (newest first)
                completions.sort((c1, c2) -> Long.compare(c2.getCompletedAt(), c1.getCompletedAt()));

                callback.onSuccess(completions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception(error.getMessage()));
            }
        });
    }

    /**
     * Get task completion history for date range
     */
    public void getTaskCompletionHistoryForDateRange(String kidId, long startDate, long endDate,
                                                     final GetCompletionsCallback callback) {
        Query query = taskCompletionsRef
                .orderByChild("kidId")
                .equalTo(kidId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<TaskCompletion> completions = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TaskCompletion completion = snapshot.getValue(TaskCompletion.class);
                    if (completion != null &&
                            completion.getCompletedAt() >= startDate &&
                            completion.getCompletedAt() <= endDate) {
                        completions.add(completion);
                    }
                }

                // Sort by date (newest first)
                completions.sort((c1, c2) -> Long.compare(c2.getCompletedAt(), c1.getCompletedAt()));

                callback.onSuccess(completions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception(error.getMessage()));
            }
        });
    }

    /**
     * Get weekly statistics for kid
     */
    public void getWeeklyStats(String kidId, final GetStatsCallback callback) {
        // Calculate start of week (Sunday)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long weekStart = cal.getTimeInMillis();

        // Get completions for this week
        getTaskCompletionHistoryForDateRange(kidId, weekStart, System.currentTimeMillis(),
                new GetCompletionsCallback() {
                    @Override
                    public void onSuccess(List<TaskCompletion> completions) {
                        int tasksCompleted = completions.size();
                        int starsEarned = 0;

                        for (TaskCompletion completion : completions) {
                            starsEarned += completion.getStarsAwarded();
                        }

                        TaskStats stats = new TaskStats(tasksCompleted, starsEarned, weekStart);
                        callback.onSuccess(stats);
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onError(e);
                    }
                });
    }

    /**
     * Verify task completion with parent
     */
    public void verifyTaskCompletion(String completionId, String parentId, final UpdateCallback callback) {
        taskCompletionsRef.child(completionId).child("verifiedBy").setValue(parentId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e);
                    }
                });
    }

    /**
     * Remove task completion (and refund stars)
     */
    public void removeTaskCompletion(String completionId, final UpdateCallback callback) {
        // First get completion details
        taskCompletionsRef.child(completionId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    TaskCompletion completion = snapshot.getValue(TaskCompletion.class);
                    if (completion != null) {
                        refundStarsAndRemove(completion, callback);
                    } else {
                        callback.onError(new Exception("Completion not found"));
                    }
                } else {
                    callback.onError(new Exception("Completion not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(new Exception(error.getMessage()));
            }
        });
    }

    /**
     * Refund stars and remove completion
     */
    private void refundStarsAndRemove(TaskCompletion completion, final UpdateCallback callback) {
        // Get current star balance
        usersRef.child(completion.getKidId()).child("starWallet")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int currentStars = 0;
                        if (snapshot.exists()) {
                            Integer stars = snapshot.getValue(Integer.class);
                            if (stars != null) {
                                currentStars = stars;
                            }
                        }

                        int newStars = Math.max(0, currentStars - completion.getStarsAwarded());

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("/taskCompletions/" + completion.getCompletionId(), null);
                        updates.put("/users/" + completion.getKidId() + "/starWallet", newStars);

                        databaseReference.updateChildren(updates)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        callback.onSuccess();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        callback.onError(e);
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(new Exception(error.getMessage()));
                    }
                });
    }

    // Task Statistics class
    public static class TaskStats {
        private int tasksCompleted;
        private int starsEarned;
        private long periodStart;

        public TaskStats(int tasksCompleted, int starsEarned, long periodStart) {
            this.tasksCompleted = tasksCompleted;
            this.starsEarned = starsEarned;
            this.periodStart = periodStart;
        }

        public int getTasksCompleted() {
            return tasksCompleted;
        }

        public int getStarsEarned() {
            return starsEarned;
        }

        public long getPeriodStart() {
            return periodStart;
        }
    }

    // Callback interfaces
    public interface CreateTaskCallback {
        void onSuccess(Task task);
        void onError(Exception e);
    }

    public interface GetTaskCallback {
        void onSuccess(Task task);
        void onError(Exception e);
    }

    public interface GetTasksCallback {
        void onSuccess(List<Task> tasks);
        void onError(Exception e);
    }

    public interface GetTasksWithCompletionCallback {
        void onSuccess(List<Task> tasks, Map<String, Boolean> completionStatus);
        void onError(Exception e);
    }

    public interface CompleteTaskCallback {
        void onSuccess(TaskCompletion completion, int newStarTotal);
        void onError(Exception e);
    }

    public interface UpdateCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface GetCompletionsCallback {
        void onSuccess(List<TaskCompletion> completions);
        void onError(Exception e);
    }

    public interface GetStatsCallback {
        void onSuccess(TaskStats stats);
        void onError(Exception e);
    }
}
