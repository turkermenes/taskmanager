import { useCallback, useEffect, useState } from 'react';
import { toErrorMessage } from '../api/errors';
import { tasksApi } from '../api/tasks';
import type { Task, TaskCreateRequest, TaskUpdateRequest } from '../api/types';

export function useTasks() {
    const [tasks, setTasks] = useState<Task[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [reloadKey, setReloadKey] = useState(0);

    useEffect(() => {
        let ignore = false;

        tasksApi
            .getAll()
            .then((data) => {
                if (!ignore) {
                    setTasks(data);
                    setError(null);
                }
            })
            .catch((err) => {
                if (!ignore) setError(toErrorMessage(err));
            })
            .finally(() => {
                if (!ignore) setLoading(false);
            });

        return () => {
            ignore = true;
        };
    }, [reloadKey]);

    const reload = useCallback(() => {
        setLoading(true);
        setReloadKey((key) => key + 1);
    }, []);

    const createTask = useCallback(async (request: TaskCreateRequest) => {
        const created = await tasksApi.create(request);
        setTasks((prev) => [created, ...prev]);
    }, []);

    const updateTask = useCallback(async (id: number, request: TaskUpdateRequest) => {
        const updated = await tasksApi.update(id, request);
        setTasks((prev) => prev.map((task) => (task.id === id ? updated : task)));
    }, []);

    const deleteTask = useCallback(async (id: number) => {
        await tasksApi.remove(id);
        setTasks((prev) => prev.filter((task) => task.id !== id));
    }, []);

    return { tasks, loading, error, reload, createTask, updateTask, deleteTask };
}