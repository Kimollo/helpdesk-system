export type Role = 'CUSTOMER' | 'AGENT' | 'ADMIN';
export type TicketStatus = 'OPEN' | 'IN_PROGRESS' | 'WAITING_ON_CUSTOMER' | 'RESOLVED' | 'CLOSED';
export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
export type TicketCategory = 'HARDWARE' | 'SOFTWARE' | 'NETWORK' | 'ACCESS' | 'BILLING' | 'OTHER';

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: Role;
  enabled: boolean;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresAt: string;
  user: User;
}

export interface Ticket {
  id: number;
  subject: string;
  description: string;
  status: TicketStatus;
  priority: TicketPriority;
  category: TicketCategory;
  requester: User;
  assignee: User | null;
  createdAt: string;
  updatedAt: string;
  resolvedAt: string | null;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface TicketStats {
  open: number;
  inProgress: number;
  waitingOnCustomer: number;
  resolved: number;
  closed: number;
  total: number;
}

export interface Comment {
  id: number;
  body: string;
  internalNote: boolean;
  author: User;
  createdAt: string;
}

export interface ApiErrorBody {
  message?: string;
  fieldErrors?: { field: string; message: string }[];
}

export const STATUSES: TicketStatus[] = ['OPEN', 'IN_PROGRESS', 'WAITING_ON_CUSTOMER', 'RESOLVED', 'CLOSED'];
export const PRIORITIES: TicketPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];
export const CATEGORIES: TicketCategory[] = ['HARDWARE', 'SOFTWARE', 'NETWORK', 'ACCESS', 'BILLING', 'OTHER'];
export const ROLES: Role[] = ['CUSTOMER', 'AGENT', 'ADMIN'];
