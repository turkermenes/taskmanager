import type { TaskStatus } from '../api/types';

export const TASK_STATUSES: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE'];

export const STATUS_LABELS: Record<TaskStatus, string> = {
    TODO: 'Yapılacak',
    IN_PROGRESS: 'Devam ediyor',
    DONE: 'Tamamlandı',
};