export function labelize(value: string) {
  return value
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');
}

export function fullName(person: { firstName: string; lastName: string }) {
  return `${person.firstName} ${person.lastName}`;
}

export function timeAgo(iso: string) {
  const seconds = Math.round((Date.now() - new Date(iso).getTime()) / 1000);
  if (seconds < 45) {
    return 'just now';
  }
  const minutes = Math.round(seconds / 60);
  if (minutes < 60) {
    return `${minutes}m ago`;
  }
  const hours = Math.round(minutes / 60);
  if (hours < 24) {
    return `${hours}h ago`;
  }
  const days = Math.round(hours / 24);
  if (days < 7) {
    return `${days}d ago`;
  }
  return formatWhen(iso);
}

export function formatWhen(iso: string | null) {
  if (!iso) {
    return '—';
  }
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(iso));
}
