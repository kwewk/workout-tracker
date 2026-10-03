# Workout Tracker

## Actors

**User** - the person who creates and manages workout programs, configures exercises, and tracks workout progress.

## System Overview

Workout Tracker is a mobile application for creating and following personalized workout programs. Users can browse exercises by category, view exercise information, add exercises to programs, configure sets, repetitions, and rest periods, edit and delete programs, and start, interrupt, and track workouts. The exercise catalog is predefined and included in the application; creating custom exercises is not supported.

## Architecture

| Aspect | Description |
| --- | --- |
| Type | Mobile application |
| Platform | Android |
| Frontend and application logic | React Native + TypeScript |
| Native Android functionality | Kotlin |
| Native integration | React Native Native Module |
| Local storage | Device local storage (workout programs, workout progress, and application preferences) |
| Exercise data | Predefined, included in the application (exercise catalog and images) |
| Backend | None |
| Database | None (not required; data is stored in device local storage) |
| Deployment | No server deployment required |
