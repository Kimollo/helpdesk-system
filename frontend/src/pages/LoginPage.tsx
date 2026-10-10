import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { errorMessage } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { Alert } from '../components/Badges';

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('admin@helpdesk.local');
  const [password, setPassword] = useState('Admin123!');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await login(email, password);
      navigate('/');
    } catch (err) {
      setError(errorMessage(err, 'Could not sign in'));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="grid min-h-screen lg:grid-cols-[1.05fr_0.95fr]">
      <section className="hidden flex-col justify-between bg-sidebar px-12 py-12 text-white lg:flex">
        <p className="font-serif text-2xl">Helpdesk</p>
        <div className="max-w-md">
          <h1 className="font-serif text-5xl leading-tight">Every request has an owner, a status, and a next step.</h1>
          <p className="mt-6 text-base leading-relaxed text-white/65">
            Customers open tickets. Agents work the queue. Administrators keep roles honest.
          </p>
        </div>
        <p className="text-sm text-white/40">Demo desk · local only</p>
      </section>
      <section className="flex items-center justify-center px-6 py-16">
        <form onSubmit={onSubmit} className="w-full max-w-sm space-y-5">
          <div>
            <p className="font-serif text-3xl lg:hidden">Helpdesk</p>
            <h2 className="mt-2 text-2xl font-semibold">Sign in</h2>
            <p className="mt-1 text-sm text-ink-soft">Use a desk account to open the queue.</p>
          </div>
          <Alert message={error} />
          <label className="block">
            <span className="label">Email</span>
            <input className="field" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </label>
          <label className="block">
            <span className="label">Password</span>
            <input className="field" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </label>
          <button className="btn-primary w-full" type="submit" disabled={submitting}>
            {submitting ? 'Signing in…' : 'Sign in'}
          </button>
          <p className="text-sm text-ink-soft">
            New here? <Link className="font-semibold text-accent" to="/register">Create a customer account</Link>
          </p>
          <div className="rounded-xl bg-white/70 p-3 text-xs leading-relaxed text-ink-soft">
            <p>admin@helpdesk.local / Admin123!</p>
            <p>agent@helpdesk.local / Agent123!</p>
            <p>customer@helpdesk.local / Customer123!</p>
          </div>
        </form>
      </section>
    </div>
  );
}
