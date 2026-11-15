import { useState } from 'react';
import { toErrorMessage, toFieldErrors } from '../api/errors';
import type { Task, TaskStatus, TaskUpdateRequest } from '../api/types';
import { STATUS_LABELS, TASK_STATUSES } from './taskStatus';

interface TaskItemProps {
    task: Task;
    onUpdate: (id: number, request: TaskUpdateRequest) => Promise<void>;
    onDelete: (id: number) => Promise<void>;
}

export function TaskItem({ task, onUpdate, onDelete }: TaskItemProps) {
    const [editing, setEditing] = useState(false);
    const [draft, setDraft] = useState({ title: task.title, description: task.description ?? '' });
    const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
    const [error, setError] = useState<string | null>(null);
    const [busy, setBusy] = useState(false);

    async function run(action: () => Promise<void>) {
        setError(null);
        setFieldErrors({});
        setBusy(true);
        try {
            await action();
            return true;
        } catch (err) {
            setFieldErrors(toFieldErrors(err));
            setError(toErrorMessage(err));
            return false;
        } finally {
            setBusy(false);
        }
    }

    function changeStatus(status: TaskStatus) {
        run(() =>
            onUpdate(task.id, {
                title: task.title,
                description: task.description ?? undefined,
                status,
            }),
        );
    }

    async function saveEdit() {
        const ok = await run(() =>
            onUpdate(task.id, {
                title: draft.title,
                description: draft.description || undefined,
                status: task.status,
            }),
        );
        if (ok) setEditing(false);
    }

    function startEdit() {
        setDraft({ title: task.title, description: task.description ?? '' });
        setEditing(true);
    }

    function handleDelete() {
        if (window.confirm(`"${task.title}" görevi silinsin mi?`)) {
            run(() => onDelete(task.id));
        }
    }

    const createdAt = new Date(task.createdAt).toLocaleString('tr-TR');

    return (
        <li className={`card task status-${task.status.toLowerCase()}`}>
            {editing ? (
                <div className="task-edit">
                    <input value={draft.title} onChange={(e) => setDraft({ ...draft, title: e.target.value })} />
                    {fieldErrors.title && <span className="field-error">{fieldErrors.title}</span>}
                    <textarea
                        value={draft.description}
                        onChange={(e) => setDraft({ ...draft, description: e.target.value })}
                        rows={2}
                    />
                    {fieldErrors.description && <span className="field-error">{fieldErrors.description}</span>}
                    <div className="actions">
                        <button onClick={saveEdit} disabled={busy}>Kaydet</button>
                        <button onClick={() => setEditing(false)} className="secondary" disabled={busy}>Vazgeç</button>
                    </div>
                </div>
            ) : (
                <div>
                    <h3>{task.title}</h3>
                    {task.description && <p>{task.description}</p>}
                    <small>Oluşturulma: {createdAt}</small>
                </div>
            )}

            <div className="actions">
                <select
                    value={task.status}
                    onChange={(e) => changeStatus(e.target.value as TaskStatus)}
                    disabled={busy || editing}
                >
                    {TASK_STATUSES.map((status) => (
                        <option key={status} value={status}>{STATUS_LABELS[status]}</option>
                    ))}
                </select>
                {!editing && (
                    <>
                        <button onClick={startEdit} className="secondary" disabled={busy}>Düzenle</button>
                        <button onClick={handleDelete} className="danger" disabled={busy}>Sil</button>
                    </>
                )}
            </div>
            {error && !fieldErrors.title && !fieldErrors.description && <p className="error">{error}</p>}
        </li>
    );
}