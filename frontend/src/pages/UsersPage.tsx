import { useEffect, useState } from 'react';
import { api, errorMessage } from '../api/client';
import { Alert } from '../components/Badges';
import { formatWhen, fullName, labelize } from '../lib/format';
import { ROLES, type PageResponse, type Role, type User } from '../types';

interface Draft {
  role: Role;
  enabled: boolean;
}

export function UsersPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [drafts, setDrafts] = useState<Record<number, Draft>>({});
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [savingId, setSavingId] = useState<number | null>(null);

  function apply(list: User[]) {
    setUsers(list);
    setDrafts(Object.fromEntries(list.map((user) => [user.id, { role: user.role, enabled: user.enabled }])));
  }

  useEffect(() => {
    let active = true;
    api
      .get<PageResponse<User>>('/users', { params: { page, size: 12 } })
      .then((response) => {
        if (!active) return;
        apply(response.data.content);
        setTotalPages(response.data.totalPages);
        setError(null);
      })
      .catch((err) => {
        if (active) setError(errorMessage(err, 'Could not load people'));
      });
    return () => {
      active = false;
    };
  }, [page]);

  async function save(user: User) {
    const draft = drafts[user.id];
    if (!draft) return;
    setSavingId(user.id);
    setError(null);
    try {
      const response = await api.patch<User>(`/users/${user.id}`, draft);
      setUsers((current) => current.map((item) => (item.id === user.id ? response.data : item)));
      setDrafts((current) => ({ ...current, [user.id]: { role: response.data.role, enabled: response.data.enabled } }));
    } catch (err) {
      setError(errorMessage(err, 'Could not update this person'));
    } finally {
      setSavingId(null);
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-serif text-4xl">People</h1>
        <p className="mt-1 text-sm text-ink-soft">Grant agent or administrator access. The last enabled administrator cannot be removed.</p>
      </div>
      <Alert message={error} />
      <div className="panel overflow-hidden">
        <table className="w-full text-left text-sm">
          <thead className="bg-stone-50 text-xs uppercase tracking-wide text-ink-soft">
            <tr>
              <th className="px-4 py-3 font-semibold">Person</th>
              <th className="px-4 py-3 font-semibold">Role</th>
              <th className="px-4 py-3 font-semibold">Access</th>
              <th className="hidden px-4 py-3 font-semibold md:table-cell">Joined</th>
              <th className="px-4 py-3" />
            </tr>
          </thead>
          <tbody>
            {users.map((user) => {
              const draft = drafts[user.id] ?? { role: user.role, enabled: user.enabled };
              return (
                <tr key={user.id} className="border-t border-line">
                  <td className="px-4 py-3">
                    <p className="font-semibold">{fullName(user)}</p>
                    <p className="text-xs text-ink-soft">{user.email}</p>
                  </td>
                  <td className="px-4 py-3">
                    <select
                      className="field"
                      value={draft.role}
                      onChange={(e) => setDrafts((current) => ({ ...current, [user.id]: { ...draft, role: e.target.value as Role } }))}
                    >
                      {ROLES.map((role) => <option key={role} value={role}>{labelize(role)}</option>)}
                    </select>
                  </td>
                  <td className="px-4 py-3">
                    <label className="flex items-center gap-2">
                      <input
                        type="checkbox"
                        checked={draft.enabled}
                        onChange={(e) => setDrafts((current) => ({ ...current, [user.id]: { ...draft, enabled: e.target.checked } }))}
                      />
                      Enabled
                    </label>
                  </td>
                  <td className="hidden px-4 py-3 text-ink-soft md:table-cell">{formatWhen(user.createdAt)}</td>
                  <td className="px-4 py-3 text-right">
                    <button className="btn-secondary" type="button" disabled={savingId === user.id} onClick={() => save(user)}>
                      {savingId === user.id ? 'Saving…' : 'Save'}
                    </button>
                  </td>
                </tr>
              );
            })}
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
