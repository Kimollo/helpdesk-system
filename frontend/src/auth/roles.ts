import type { Role, User } from '../types';

const KNOWN_ROLES: readonly Role[] = ['CUSTOMER', 'AGENT', 'ADMIN'];

export function normalizeRole(role: string | null | undefined): Role | null {
  if (!role) {
    return null;
  }
  const bare = role.toUpperCase().replace('ROLE_', '') as Role;
  return KNOWN_ROLES.includes(bare) ? bare : null;
}

export function withNormalizedRole(user: User): User {
  const role = normalizeRole(user.role);
  return role ? { ...user, role } : user;
}

export function hasRole(role: string | null | undefined, allowed: readonly Role[]): boolean {
  const normalized = normalizeRole(role);
  return normalized !== null && allowed.includes(normalized);
}
