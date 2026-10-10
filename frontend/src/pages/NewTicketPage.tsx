import { FormEvent, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api, errorMessage } from '../api/client';
import { Alert } from '../components/Badges';
import { CATEGORIES, PRIORITIES, type Ticket, type TicketCategory, type TicketPriority } from '../types';
import { labelize } from '../lib/format';

export function NewTicketPage() {
  const navigate = useNavigate();
  const [subject, setSubject] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<TicketPriority>('MEDIUM');
  const [category, setCategory] = useState<TicketCategory>('SOFTWARE');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    document.title = 'New ticket · Helpdesk';
  }, []);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const response = await api.post<Ticket>('/tickets', { subject, description, priority, category });
      navigate(`/tickets/${response.data.id}`);
    } catch (err) {
      setError(errorMessage(err, 'Could not open the ticket'));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="mx-auto max-w-2xl">
      <Link to="/" className="text-sm font-medium text-ink-soft hover:text-ink">Back to queue</Link>
      <h1 className="mt-3 font-serif text-4xl">Open a ticket</h1>
      <p className="mt-2 text-sm text-ink-soft">Describe what broke and how urgent it is. The ticket is filed under your account.</p>
      <form onSubmit={onSubmit} className="panel mt-6 space-y-5 p-6">
        <Alert message={error} />
        <label className="block">
          <span className="label">Subject</span>
          <input className="field" value={subject} minLength={5} maxLength={160} onChange={(e) => setSubject(e.target.value)} required />
        </label>
        <label className="block">
          <span className="label">Description</span>
          <textarea className="field min-h-36" value={description} minLength={10} maxLength={5000} onChange={(e) => setDescription(e.target.value)} required />
        </label>
        <div className="grid gap-4 sm:grid-cols-2">
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
        </div>
        <button className="btn-primary" type="submit" disabled={submitting}>
          {submitting ? 'Opening…' : 'Open ticket'}
        </button>
      </form>
    </div>
  );
}
