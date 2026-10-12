import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, errorMessage } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { hasRole } from '../auth/roles';
import { PriorityBadge, StatusBadge, Alert } from '../components/Badges';
import { formatWhen, fullName, labelize } from '../lib/format';
import {
  CATEGORIES,
  PRIORITIES,
  STATUSES,
  type PageResponse,
  type Ticket,
  type TicketCategory,
  type TicketPriority,
  type TicketStats,
  type TicketStatus,
} from '../types';

type Queue = '' | 'mine' | 'unassigned' | 'reported';

export function TicketsPage() {
  const { user } = useAuth();
  const staff = hasRole(user?.role, ['AGENT', 'ADMIN']);
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [stats, setStats] = useState<TicketStats | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [status, setStatus] = useState<TicketStatus | ''>('');
  const [priority, setPriority] = useState<TicketPriority | ''>('');
  const [category, setCategory] = useState<TicketCategory | ''>('');
  const [queue, setQueue] = useState<Queue>('');
  const [query, setQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const handle = window.setTimeout(() => setDebouncedQuery(query), 300);
    return () => window.clearTimeout(handle);
  }, [query]);

  useEffect(() => {
    setPage(0);
  }, [status, priority, category, queue, debouncedQuery]);

  useEffect(() => {
    let active = true;
    setLoading(true);
    api
      .get<PageResponse<Ticket>>('/tickets', {
        params: {
          page,
          size: 10,
          status: status || undefined,
          priority: priority || undefined,
          category: category || undefined,
          queue: queue || undefined,
          q: debouncedQuery || undefined,
        },
      })
      .then((response) => {
        if (!active) return;
        setTickets(response.data.content);
        setTotalPages(response.data.totalPages);
        setError(null);
      })
      .catch((err) => {
        if (!active) return;
        setError(errorMessage(err, 'Could not load tickets'));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [page, status, priority, category, queue, debouncedQuery]);

  useEffect(() => {
    api.get<TicketStats>('/tickets/stats').then((response) => setStats(response.data)).catch(() => setStats(null));
  }, [tickets]);

  const cards: { label: string; value: number; status: TicketStatus | '' }[] = stats
    ? [
        { label: 'Open', value: stats.open, status: 'OPEN' },
        { label: 'In progress', value: stats.inProgress, status: 'IN_PROGRESS' },
        { label: 'Waiting', value: stats.waitingOnCustomer, status: 'WAITING_ON_CUSTOMER' },
        { label: 'Resolved', value: stats.resolved, status: 'RESOLVED' },
        { label: 'All', value: stats.total, status: '' },
      ]
    : [];

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="font-serif text-4xl">{staff ? 'Queue' : 'My tickets'}</h1>
          <p className="mt-1 text-sm text-ink-soft">
            {staff ? 'Work assigned requests, or pick up anything still unowned.' : 'Tickets you opened, and the people working them.'}
          </p>
        </div>
        <Link to="/tickets/new" className="btn-primary">New ticket</Link>
      </div>

      {cards.length > 0 && (
        <div className="grid grid-cols-2 gap-3 md:grid-cols-5">
          {cards.map((card) => (
            <button
              key={card.label}
              type="button"
              onClick={() => setStatus(card.status)}
              className={`panel px-4 py-3 text-left transition ${status === card.status ? 'ring-2 ring-accent' : ''}`}
            >
              <p className="text-xs font-semibold uppercase tracking-wide text-ink-soft">{card.label}</p>
              <p className="mt-1 font-serif text-3xl">{card.value}</p>
            </button>
          ))}
        </div>
      )}

      <div className="panel flex flex-col gap-3 p-4 md:flex-row md:flex-wrap">
        <input
          className="field"
          placeholder="Search subject or description"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <select className="field md:max-w-44" value={status} onChange={(e) => setStatus(e.target.value as TicketStatus | '')}>
          <option value="">All statuses</option>
          {STATUSES.map((item) => <option key={item} value={item}>{labelize(item)}</option>)}
        </select>
        <select className="field md:max-w-40" value={priority} onChange={(e) => setPriority(e.target.value as TicketPriority | '')}>
          <option value="">All priorities</option>
          {PRIORITIES.map((item) => <option key={item} value={item}>{labelize(item)}</option>)}
        </select>
        <select className="field md:max-w-40" value={category} onChange={(e) => setCategory(e.target.value as TicketCategory | '')}>
          <option value="">All categories</option>
          {CATEGORIES.map((item) => <option key={item} value={item}>{labelize(item)}</option>)}
        </select>
        {staff && (
          <select className="field md:max-w-44" value={queue} onChange={(e) => setQueue(e.target.value as Queue)}>
            <option value="">All tickets</option>
            <option value="mine">Assigned to me</option>
            <option value="unassigned">Unassigned</option>
            <option value="reported">Opened by me</option>
          </select>
        )}
      </div>

      <Alert message={error} />

      <div className="panel overflow-hidden">
        <table className="w-full text-left text-sm">
          <thead className="bg-stone-50 text-xs uppercase tracking-wide text-ink-soft">
            <tr>
              <th className="px-4 py-3 font-semibold">Ticket</th>
              <th className="hidden px-4 py-3 font-semibold md:table-cell">Requester</th>
              <th className="hidden px-4 py-3 font-semibold lg:table-cell">Assignee</th>
              <th className="px-4 py-3 font-semibold">Status</th>
              <th className="hidden px-4 py-3 font-semibold sm:table-cell">Updated</th>
            </tr>
          </thead>
          <tbody>
            {loading && (
              <tr><td className="px-4 py-8 text-ink-soft" colSpan={5}>Loading tickets…</td></tr>
            )}
            {!loading && tickets.length === 0 && (
              <tr><td className="px-4 py-8 text-ink-soft" colSpan={5}>No tickets match this view.</td></tr>
            )}
            {!loading && tickets.map((ticket) => (
              <tr key={ticket.id} className="border-t border-line hover:bg-orange-50/40">
                <td className="px-4 py-3">
                  <Link to={`/tickets/${ticket.id}`} className="font-semibold hover:text-accent">{ticket.subject}</Link>
                  <div className="mt-1 flex items-center gap-2">
                    <PriorityBadge priority={ticket.priority} />
                    <span className="text-xs text-ink-soft">{labelize(ticket.category)}</span>
                  </div>
                </td>
                <td className="hidden px-4 py-3 text-ink-soft md:table-cell">{fullName(ticket.requester)}</td>
                <td className="hidden px-4 py-3 text-ink-soft lg:table-cell">{ticket.assignee ? fullName(ticket.assignee) : 'Unassigned'}</td>
                <td className="px-4 py-3"><StatusBadge status={ticket.status} /></td>
                <td className="hidden px-4 py-3 text-ink-soft sm:table-cell">{formatWhen(ticket.updatedAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <div className="flex items-center justify-end gap-2">
          <button className="btn-secondary" type="button" disabled={page === 0} onClick={() => setPage((current) => current - 1)}>Previous</button>
          <span className="text-sm text-ink-soft">Page {page + 1} of {totalPages}</span>
          <button className="btn-secondary" type="button" disabled={page + 1 >= totalPages} onClick={() => setPage((current) => current + 1)}>Next</button>
        </div>
      )}
    </div>
  );
}
