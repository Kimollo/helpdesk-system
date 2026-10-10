import { labelize } from '../lib/format';
import type { TicketPriority, TicketStatus } from '../types';

const statusClass: Record<TicketStatus, string> = {
  OPEN: 'bg-sky-50 text-sky-800 ring-sky-200',
  IN_PROGRESS: 'bg-amber-50 text-amber-800 ring-amber-200',
  WAITING_ON_CUSTOMER: 'bg-violet-50 text-violet-800 ring-violet-200',
  RESOLVED: 'bg-emerald-50 text-emerald-800 ring-emerald-200',
  CLOSED: 'bg-stone-100 text-stone-600 ring-stone-200',
};

const priorityClass: Record<TicketPriority, string> = {
  LOW: 'bg-stone-100 text-stone-700',
  MEDIUM: 'bg-blue-50 text-blue-800',
  HIGH: 'bg-orange-50 text-orange-800',
  URGENT: 'bg-red-50 text-red-800',
};

export function StatusBadge({ status }: { status: TicketStatus }) {
  return (
    <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ring-inset ${statusClass[status]}`}>
      {labelize(status)}
    </span>
  );
}

export function PriorityBadge({ priority }: { priority: TicketPriority }) {
  return (
    <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${priorityClass[priority]}`}>
      {labelize(priority)}
    </span>
  );
}

export function Alert({ message }: { message: string | null }) {
  if (!message) {
    return null;
  }
  return (
    <p className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-800" role="alert">
      {message}
    </p>
  );
}
