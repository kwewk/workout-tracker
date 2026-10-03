# Non-Functional Requirements

## Performance

- Application interface should respond to user interactions without noticeable delays
- Exercise lists and workout programs should open without unnecessary delays
- Rest timer should update smoothly during the workout

## Reliability

- Workout progress must be stored locally
- Workout progress must remain available after the application is closed and reopened
- The application should avoid losing the latest saved workout state

## Usability

- Main workout actions must be clearly visible and distinguishable
- Exercise information must be presented in a readable format
- The user should be able to access workout programs and exercise categories from the main screen
- The application must provide light and dark themes

## Compatibility

- The MVP targets Android devices
- Android-specific features must work according to the requirements and behavior of the target Android version

## Offline Operation

- Core application functionality must work without an internet connection
- Exercise browsing must work offline
- Exercise catalog and exercise images must be included in the application and available offline
- Workout program management must work offline
- Workout tracking must work offline

## Maintainability

- User interface and application logic are implemented in React Native + TypeScript
- Android-specific functionality is implemented in Kotlin
- Communication between React Native and Kotlin must use a clearly defined interface

## Security and Privacy

- The application does not require user registration or authentication
- The application does not require a backend
- Workout data and application preferences are stored locally on the device
- The MVP does not send workout data to a remote server
