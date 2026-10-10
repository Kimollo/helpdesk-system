import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { errorMessage } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { Alert } from '../components/Badges';

export function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', password: '' });
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  function update(field: keyof typeof form, value: string) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await register(form);
      navigate('/');
    } catch (err) {
      setError(errorMessage(err, 'Could not create the account'));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="grid min-h-screen place-items-center px-6 py-16">
      <form onSubmit={onSubmit} className="panel w-full max-w-md space-y-5 p-8">
        <div>
          <p className="font-serif text-2xl">Helpdesk</p>
          <h1 className="mt-3 text-2xl font-semibold">Create a customer account</h1>
          <p className="mt-1 text-sm text-ink-soft">Registration always starts as a customer. An administrator can change roles later.</p>
        </div>
        <Alert message={error} />
        <div className="grid gap-4 sm:grid-cols-2">
          <label className="block">
            <span className="label">First name</span>
            <input className="field" value={form.firstName} onChange={(e) => update('firstName', e.target.value)} required />
          </label>
          <label className="block">
            <span className="label">Last name</span>
            <input className="field" value={form.lastName} onChange={(e) => update('lastName', e.target.value)} required />
          </label>
        </div>
        <label className="block">
          <span className="label">Email</span>
          <input className="field" type="email" value={form.email} onChange={(e) => update('email', e.target.value)} required />
        </label>
        <label className="block">
          <span className="label">Password</span>
          <input className="field" type="password" minLength={8} value={form.password} onChange={(e) => update('password', e.target.value)} required />
        </label>
        <button className="btn-primary w-full" type="submit" disabled={submitting}>
          {submitting ? 'Creating account…' : 'Create account'}
        </button>
        <p className="text-sm text-ink-soft">
          Already registered? <Link className="font-semibold text-accent" to="/login">Sign in</Link>
        </p>
      </form>
    </div>
  );
}
