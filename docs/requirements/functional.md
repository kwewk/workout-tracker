# Functional Requirements

## User

### 1. Open Application

**When the user opens the application, the user sees:**

- Welcome message
- Exercise categories
- Workout programs section
- Option to create a new program

#### 1.1 View Exercise Categories

- User can select an exercise category
- Leads to the selected category

#### 1.2 View Workout Programs

- User can view available workout programs
- User can open a selected program
- User can create a new program

### 2. Browse Exercises

**The user can browse exercises by category.**

- The exercise catalog, including exercise images and descriptions, is predefined and included in the application
- Creating custom exercises is not supported

#### 2.1 Category View

- User sees a list of exercises in the selected category
- Each exercise contains:
  - Image
  - Name

#### 2.2 Open Exercise

- User can select an exercise from the list
- Leads to the exercise details view

### 3. Exercise Details

**The user sees:**

- Exercise image
- Exercise name
- Exercise description
- Add to program button

#### 3.1 Add Exercise to Program

- If the user has at least one program, the add button is active
- User can select one of their programs
- The exercise is added to the selected program

#### 3.2 No Available Program

- If the user has no programs, the add button is inactive

### 4. Workout Program Management

**Each workout program contains a list of exercises.**

#### 4.1 Create Program

- User can create a new workout program
- User specifies the program name

#### 4.2 Add Exercise

- User can add exercises to a program

#### 4.3 Configure Exercise

For each exercise in a program, the user can configure:

- Number of sets
- Number of repetitions
- Rest duration

#### 4.4 Arrange Programs

- User can change the order of workout programs

#### 4.5 Edit Program

- User can edit an existing workout program
- Editing a program includes:
  - Changing the program name
  - Adding exercises to the program
  - Removing exercises from the program
  - Changing the order of exercises in the program
  - Changing the configuration of exercises (sets, repetitions, rest duration)

#### 4.6 Delete Program

- User can delete a workout program
- The deleted program is removed from the list of workout programs

#### 4.7 View Program

- User can open a workout program
- User sees the list of exercises in the program
- Each exercise has a completion checkbox
- The checkbox is marked automatically when the exercise is completed or skipped
- The program view contains a button to start the workout

#### 4.8 Open Program Exercise

- The user can open an exercise from the program
- The exercise view contains:
  - Exercise number in the program
  - Exercise name
  - Exercise image
  - Exercise description
  - Current set number and total number of sets
  - Number of repetitions
  - Rest duration
  - Rest timer
  - Skip button (skips the current exercise)
  - Button to mark the current set as completed
  - Button to interrupt the workout

### 5. Track Workout

**The user can start and track a workout based on a selected program.**

#### 5.1 Start Workout

- User can start a workout from a selected program
- The application opens the first exercise of the program
- At the start of the workout, all exercises in the program are marked as not completed

#### 5.2 Complete Set

- User can mark the current set as completed
- The number of completed sets increases by one

#### 5.3 Continue Exercise

- If there are remaining sets, the user continues the current exercise
- After each completed set, the rest period starts according to the configured rest duration

#### 5.4 Complete Exercise

- If the completed set is the last set of the exercise, the rest period starts according to the configured rest duration
- After the rest period, the application moves to the next exercise
- If the completed exercise is the last exercise in the program, the workout is finished without a rest period
- When the application moves to the next exercise, the previous exercise is completed and its checkbox is marked

#### 5.5 Skip Exercise

- User can skip the current exercise
- The skipped exercise is considered completed and its checkbox is marked
- The application moves to the next exercise in the program

#### 5.6 Interrupt Workout

- User can interrupt the current workout at any time
- The current workout is stopped

#### 5.7 Finish Workout

- When all exercises in the program are completed or skipped, the workout is finished
- A workout in which some exercises were skipped is considered completed
- When the workout is finished, the user sees a message that the workout was completed successfully

### 6. Rest Timer

**The application provides a timer for the configured rest period.**

#### 6.1 Start Rest Timer

- Timer starts according to the configured rest duration

#### 6.2 Timer Completion

- User receives a notification
- Device vibration is triggered
- User can continue the workout

#### 6.3 Background Timer Notification

- The user can receive the rest-period notification when the application is not in the foreground

### 7. Save and Restore Progress

**The application saves workout programs and workout progress locally in the device local storage.**

#### 7.1 Save Workout Progress

The application saves information required to continue the current workout, including:

- Selected program
- Current exercise
- Completed sets
- Completion state of exercises in the program
- Current workout state

#### 7.2 Restore Workout Progress

- If the application is closed during a workout, the progress is saved
- After reopening the application, the user can continue the workout from the saved state

### 8. Change Application Theme

- User can switch between light and dark theme
- Selected theme is saved locally
- Selected theme is restored after reopening the application
