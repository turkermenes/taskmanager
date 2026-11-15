import { useState } from 'react';
import type { TaskStatus } from '../api/types';
import { useAuth } from '../auth/useAuth';
import { TaskForm } from '../tasks/TaskForm';
import { TaskItem } from '../tasks/TaskItem';
import { STATUS_LABELS, TASK_STATUSES } from '../tasks/taskStatus';
import { useTasks } from '../tasks/useTasks';

type Filter = TaskStatus | 'ALL';

export function TasksPage() {
    const { username, logout } = useAuth();
    const { tasks, loading, error, reload, createTask, updateTask, deleteTask } = useTasks();
    const [filter, setFilter] = useState<Filter>('ALL');

    const visibleTasks = filter === 'ALL' ? tasks : tasks.filter((task) => task.status === filter);
    const countOf = (status: TaskStatus) => tasks.filter((task) => task.status === status).length;

    return (
        <main className="container">
            <header className="topbar">
                <h1>Görevlerim</h1>
                <div>
                    <span>Merhaba, {username}</span>{' '}
                    <button onClick={logout} className="secondary">Çıkış yap</button>
                </div>
            </header>

            <TaskForm onCreate={createTask} />

            <nav className="filters">
                <button className={filter === 'ALL' ? '' : 'secondary'} onClick={() => setFilter('ALL')}>
                    Tümü ({tasks.length})
                </button>
                {TASK_STATUSES.map((status) => (
                    <button
                        key={status}
                        className={filter === status ? '' : 'secondary'}
                        onClick={() => setFilter(status)}
                    >
                        {STATUS_LABELS[status]} ({countOf(status)})
                    </button>
                ))}
            </nav>

            {loading && <p>Görevler yükleniyor...</p>}

            {!loading && error && (
                <div className="card">
                    <p className="error">{error}</p>
                    <button onClick={reload}>Tekrar dene</button>
                </div>
            )}

            {!loading && !error && visibleTasks.length === 0 && (
                <p className="empty">
                    {tasks.length === 0 ? 'Henüz görevin yok. Yukarıdan ilk görevini ekle!' : 'Bu filtrede görev yok.'}
                </p>
            )}

            {!loading && !error && visibleTasks.length > 0 && (
                <ul className="task-list">
                    {visibleTasks.map((task) => (
                        <TaskItem key={task.id} task={task} onUpdate={updateTask} onDelete={deleteTask} />
                    ))}
                </ul>
            )}
        </main>
    );
}