import { LifeBuoy, LogOut, Plus, Ticket, Users } from 'lucide-react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { hasRole } from '../auth/roles';
import { fullName, labelize } from '../lib/format';

const linkClass = ({ isActive }: { isActive: boolean }) =>
  `flex items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium transition ${
    isActive ? 'bg-white/10 text-white' : 'text-white/70 hover:bg-white/5 hover:text-white'
  }`;

export function AppShell() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  if (!user) {
    return null;
  }

  const links = (
    <>
      <NavLink to="/" end className={linkClass}>
        <Ticket size={16} />
        Tickets
      </NavLink>
      <NavLink to="/tickets/new" className={linkClass}>
        <Plus size={16} />
        New ticket
      </NavLink>
      {hasRole(user.role, ['ADMIN']) && (
        <NavLink to="/admin/users" className={linkClass}>
          <Users size={16} />
          People
        </NavLink>
      )}
    </>
  );

  return (
    <div className="min-h-screen md:grid md:grid-cols-[248px_1fr]">
      <aside className="hidden bg-sidebar text-white md:flex md:flex-col md:px-4 md:py-6">
        <div className="flex items-center gap-3 px-2">
          <span className="grid h-10 w-10 place-items-center rounded-xl bg-accent">
            <LifeBuoy size={18} />
          </span>
          <div>
            <p className="font-serif text-lg leading-none">Helpdesk</p>
            <p className="mt-1 text-xs text-white/50">Support desk</p>
          </div>
        </div>
        <nav className="mt-8 flex flex-1 flex-col gap-1">{links}</nav>
        <div className="border-t border-white/10 px-2 pt-4">
          <p className="text-sm font-medium">{fullName(user)}</p>
          <p className="text-xs text-white/50">{labelize(user.role)}</p>
          <button
            className="mt-3 inline-flex items-center gap-2 text-sm text-white/70 hover:text-white"
            onClick={() => {
              logout();
              navigate('/login');
            }}
          >
            <LogOut size={15} />
            Sign out
          </button>
        </div>
      </aside>

      <div className="min-w-0">
        <header className="flex items-center justify-between border-b border-line bg-sidebar px-4 py-3 text-white md:hidden">
          <p className="font-serif text-lg">Helpdesk</p>
          <button
            className="text-sm text-white/70"
            onClick={() => {
              logout();
              navigate('/login');
            }}
          >
            Sign out
          </button>
        </header>
        <nav className="flex gap-2 overflow-x-auto border-b border-line bg-sidebar px-3 py-2 md:hidden">{links}</nav>
        <main className="mx-auto max-w-6xl px-4 py-6 md:px-8 md:py-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
