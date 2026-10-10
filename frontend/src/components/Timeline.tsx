import { timeAgo, fullName, labelize } from '../lib/format';
import type { TicketHistoryEntry } from '../types';

function sentence(entry: TicketHistoryEntry) {
  const actor = entry.changedBy.firstName;
  if (entry.changeType === 'ASSIGNMENT') {
    if (entry.newAssignee) {
      return `${actor} assigned ${fullName(entry.newAssignee)}`;
    }
    return `${actor} unassigned the ticket`;
  }
  return `${actor} changed status to ${labelize(entry.newStatus)}`;
}

export function Timeline({ entries }: { entries: TicketHistoryEntry[] }) {
  return (
    <section className="panel p-4">
      <h2 className="text-sm font-semibold uppercase tracking-wide text-ink-soft">Activity</h2>
      {entries.length === 0 ? (
        <p className="mt-3 text-sm text-ink-soft">No activity yet.</p>
      ) : (
        <ol className="mt-4">
          {entries.map((entry, index) => (
            <li key={entry.id} className="relative pb-4 pl-5 last:pb-0">
              {index < entries.length - 1 && (
                <span className="absolute bottom-0 left-[4px] top-2 w-px bg-line" aria-hidden="true" />
              )}
              <span className="absolute left-0 top-1.5 h-2.5 w-2.5 rounded-full bg-accent" aria-hidden="true" />
              <p className="text-sm leading-snug">
                {sentence(entry)}
                <span className="text-ink-soft"> · {timeAgo(entry.changedAt)}</span>
              </p>
            </li>
          ))}
        </ol>
      )}
    </section>
  );
}
