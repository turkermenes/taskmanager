import { apiFetch } from './client';
import type { Task, TaskCreateRequest, TaskUpdateRequest } from './types';

export const tasksApi = {
    getAll: () =>
        apiFetch<Task[]>('/api/v1/tasks'),

    getById: (id: number) =>
        apiFetch<Task>(`/api/v1/tasks/${id}`),

    create: (request: TaskCreateRequest) =>
        apiFetch<Task>('/api/v1/tasks', {
            method: 'POST',
            body: JSON.stringify(request),
        }),

    update: (id: number, request: TaskUpdateRequest) =>
        apiFetch<Task>(`/api/v1/tasks/${id}`, {
            method: 'PUT',
            body: JSON.stringify(request),
        }),

    remove: (id: number) =>
        apiFetch<void>(`/api/v1/tasks/${id}`, { method: 'DELETE' }),
};