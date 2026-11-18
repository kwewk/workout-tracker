import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export type RestTimerOptions = {
  endsAtMs: number;
  ongoingTitle: string;
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
  startRestTimer(options: RestTimerOptions): Promise<void>;
  cancelRestTimer(): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('NativeWorkout');
