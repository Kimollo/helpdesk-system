import { FormEvent, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api, errorMessage } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { hasRole } from '../auth/roles';
import { Alert, PriorityBadge, StatusBadge } from '../components/Badges';
import { Timeline } from '../components/Timeline';
import { formatWhen, fullName, labelize } from '../lib/format';
import {
  CATEGORIES,
  PRIORITIES,
  STATUSES,
  type Comment,
  type PageResponse,
  type Ticket,
  type TicketHistoryEntry,
  type TicketCategory,
  type TicketPriority,
  type TicketStatus,
  type User,
} from '../types';

export function TicketDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const staff = hasRole(user?.role, ['AGENT', 'ADMIN']);
  const [ticket, setTicket] = useState<Ticket | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [history, setHistory] = useState<TicketHistoryEntry[]>([]);
  const [assignees, setAssignees] = useState<User[]>([]);
  const [status, setStatus] = useState<TicketStatus>('OPEN');
  const [priority, setPriority] = useState<TicketPriority>('MEDIUM');
  const [category, setCategory] = useState<TicketCategory>('OTHER');
  const [assigneeId, setAssigneeId] = useState('');
  const [body, setBody] = useState('');
  const [internalNote, setInternalNote] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [commenting, setCommenting] = useState(false);

  useEffect(() => {
    if (!id) return;
    let active = true;
    setError(null);
    Promise.all([
      api.get<Ticket>(`/tickets/${id}`),
      api.get<Comment[]>(`/tickets/${id}/comments`),
      api.get<TicketHistoryEntry[]>(`/tickets/${id}/history`),
    ])
      .then(([ticketResponse, commentResponse, historyResponse]) => {
        if (!active) return;
        const next = ticketResponse.data;
        setTicket(next);
        setComments(commentResponse.data);
        setHistory(historyResponse.data);
        setStatus(next.status);
        setPriority(next.priority);
        setCategory(next.category);
        setAssigneeId(next.assignee ? String(next.assignee.id) : '');
      })
      .catch((err) => {
        if (active) setError(errorMessage(err, 'Could not load this ticket'));
      });
    return () => {
      active = false;
    };
  }, [id]);

  useEffect(() => {
    if (!staff) return;
    Promise.all([
      api.get<PageResponse<User>>('/users', { params: { role: 'AGENT', size: 50 } }),
      api.get<PageResponse<User>>('/users', { params: { role: 'ADMIN', size: 50 } }),
    ]).then(([agents, admins]) => {
      const people = [...agents.data.content, ...admins.data.content].filter((person) => person.enabled);
      people.sort((a, b) => fullName(a).localeCompare(fullName(b)));
      setAssignees(people);
    }).catch(() => setAssignees([]));
  }, [staff]);

  async function saveWorkflow(event: FormEvent) {
    event.preventDefault();
    if (!id) return;
    setSaving(true);
    setError(null);
    try {
      const response = await api.patch<Ticket>(`/tickets/${id}`, {
        status,
        priority,
        category,
        assigneeId: assigneeId ? Number(assigneeId) : null,
        clearAssignee: assigneeId === '',
      });
      setTicket(response.data);
      const historyResponse = await api.get<TicketHistoryEntry[]>(`/tickets/${id}/history`);
      setHistory(historyResponse.data);
    } catch (err) {
      setError(errorMessage(err, 'Could not update the ticket'));
    } finally {
      setSaving(false);
    }
  }

  async function addComment(event: FormEvent) {
    event.preventDefault();
    if (!id) return;
    setCommenting(true);
    setError(null);
    try {
      const response = await api.post<Comment>(`/tickets/${id}/comments`, {
        body,
        internalNote: staff && internalNote,
      });
      setComments((current) => [...current, response.data]);
      setBody('');
      setInternalNote(false);
    } catch (err) {
      setError(errorMessage(err, 'Could not add the comment'));
    } finally {
      setCommenting(false);
    }
  }

  async function removeTicket() {
    if (!id || !window.confirm('Delete this ticket and its comments?')) return;
    try {
      await api.delete(`/tickets/${id}`);
      navigate('/');
    } catch (err) {
      setError(errorMessage(err, 'Could not delete the ticket'));
    }
  }

  if (!ticket && !error) {
    return <p className="text-sm text-ink-soft">Loading ticket…</p>;
  }

  return (
    <div className="space-y-5">
      <Link to="/" className="text-sm font-medium text-ink-soft hover:text-ink">Back to queue</Link>
      <Alert message={error} />
      {ticket && (
        <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_300px]">
          <section className="space-y-5">
            <div>
              <div className="flex flex-wrap items-center gap-2">
                <StatusBadge status={ticket.status} />
                <PriorityBadge priority={ticket.priority} />
                <span className="text-xs font-semibold uppercase tracking-wide text-ink-soft">{labelize(ticket.category)}</span>
              </div>
              <h1 className="mt-3 font-serif text-4xl">{ticket.subject}</h1>
              <p className="mt-2 text-sm text-ink-soft">
                Opened by {fullName(ticket.requester)} · {formatWhen(ticket.createdAt)}
              </p>
            </div>
            <article className="panel whitespace-pre-wrap p-5 text-sm leading-relaxed">{ticket.description}</article>
            <section className="space-y-3">
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink-soft">Conversation</h2>
              {comments.length === 0 && <p className="text-sm text-ink-soft">No replies yet.</p>}
              {comments.map((comment) => (
                <article key={comment.id} className={`panel p-4 ${comment.internalNote ? 'bg-amber-50' : ''}`}>
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <p className="text-sm font-semibold">
                      {fullName(comment.author)}
                      <span className="ml-2 font-medium text-ink-soft">{labelize(comment.author.role)}</span>
                    </p>
                    <p className="text-xs text-ink-soft">{formatWhen(comment.createdAt)}</p>
                  </div>
                  {comment.internalNote && <p className="mt-1 text-xs font-semibold uppercase tracking-wide text-amber-800">Internal note</p>}
                  <p className="mt-2 whitespace-pre-wrap text-sm leading-relaxed">{comment.body}</p>
                </article>
              ))}
              <form onSubmit={addComment} className="panel space-y-3 p-4">
                <label className="block">
                  <span className="label">{staff ? 'Reply or internal note' : 'Reply'}</span>
                  <textarea className="field min-h-28" value={body} maxLength={4000} onChange={(e) => setBody(e.target.value)} required />
                </label>
                {staff && (
                  <label className="flex items-center gap-2 text-sm">
                    <input type="checkbox" checked={internalNote} onChange={(e) => setInternalNote(e.target.checked)} />
                    Visible to agents only
                  </label>
                )}
                <button className="btn-primary" type="submit" disabled={commenting}>
                  {commenting ? 'Sending…' : 'Send'}
                </button>
              </form>
            </section>
          </section>

          <aside className="space-y-4">
            {staff ? (
              <form onSubmit={saveWorkflow} className="panel space-y-4 p-4">
                <h2 className="text-sm font-semibold uppercase tracking-wide text-ink-soft">Workflow</h2>
                <label className="block">
                  <span className="label">Status</span>
                  <select className="field" value={status} onChange={(e) => setStatus(e.target.value as TicketStatus)}>
                    {STATUSES.map((item) => <option key={item} value={item}>{labelize(item)}</option>)}
                  </select>
                </label>
                <label className="block">
                  <span className="label">Priority</span>
                  <select className="field" value={priority} onChange={(e) => setPriority(e.target.value as TicketPriority)}>
                    {PRIORITIES.map((item) => <option key={item} value={item}>{labelize(item)}</option>)}
                  </select>
                </label>
                <label className="block">
                  <span className="label">Category</span>
                  <select className="field" value={category} onChange={(e) => setCategory(e.target.value as TicketCategory)}>
                    {CATEGORIES.map((item) => <option key={item} value={item}>{labelize(item)}</option>)}
                  </select>
                </label>
                <label className="block">
                  <span className="label">Assignee</span>
                  <select className="field" value={assigneeId} onChange={(e) => setAssigneeId(e.target.value)}>
                    <option value="">Unassigned</option>
                    {assignees.map((person) => (
                      <option key={person.id} value={person.id}>{fullName(person)} · {labelize(person.role)}</option>
                    ))}
                  </select>
                </label>
                <button className="btn-primary w-full" type="submit" disabled={saving}>
                  {saving ? 'Saving…' : 'Save changes'}
                </button>
              </form>
            ) : (
              <div className="panel space-y-3 p-4 text-sm">
                <h2 className="text-sm font-semibold uppercase tracking-wide text-ink-soft">Assignment</h2>
                <p><span className="text-ink-soft">Status. </span>{labelize(ticket.status)}</p>
                <p><span className="text-ink-soft">Assignee. </span>{ticket.assignee ? fullName(ticket.assignee) : 'Waiting for an agent'}</p>
                <p><span className="text-ink-soft">Updated. </span>{formatWhen(ticket.updatedAt)}</p>
              </div>
            )}
            <Timeline entries={history} />
            {hasRole(user?.role, ['ADMIN']) && (
              <button className="btn-danger w-full" type="button" onClick={removeTicket}>Delete ticket</button>
            )}
          </aside>
        </div>
      )}
    </div>
  );
}
