import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export type RestTimerOptions = {
  endsAtMs: number;
  ongoingTitle: string;
  /** Static text: it is never updated, so do not put the remaining time here. */
  ongoingText?: string;
  finishedTitle: string;
  finishedBody: string;
};

export interface Spec extends TurboModule {
  getConstants(): {
    maxRestDurationMs: number;
    minRestDurationMs: number;
  };
  vibrate(): void;
  hasNotificationPermission(): Promise<boolean>;
  openNotificationSettings(): void;
  /**
   * Starts the rest timer, or replaces the running one: an ongoing notification
   * with a countdown, a vibration at the end and a "finished" notification if
   * the app is not on screen. Works without notification permission (then only
   * the vibration is noticeable).
   *
   * Resolves once the system accepted the start. In rare cases (the app goes to
   * the background at that very moment) the timer can still be dropped without
   * a signal, so keep the sync rule: when the app becomes active and the saved
   * `endsAtMs` is in the future, call `startRestTimer` again.
   *
   * Rejects with one of these `error.code` values:
   * - `E_ALREADY_FINISHED`: less than `minRestDurationMs` is left, or `endsAtMs`
   *   is in the past. Not a failure: move on to the next set.
   * - `E_TOO_LONG`: more than `maxRestDurationMs` is left. Bug in the caller.
   * - `E_INVALID_ARGS`: `endsAtMs` is not a finite number, a required title or
   *   body is missing or blank, or `ongoingText` is not a string. Bug in the caller.
   * - `E_START_NOT_ALLOWED`: the system refused the start because the app is in
   *   the background (Android 12+, or battery usage set to "Restricted"). Start
   *   it again with the same `endsAtMs` when the app becomes active.
   * - `E_START_FAILED`: unexpected error. Log it.
   */
  startRestTimer(options: RestTimerOptions): Promise<void>;
  cancelRestTimer(): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('NativeWorkout');
