import api from "./api";

import type {
  TicketResponse,
} from "./ticketService";

export interface AdminDashboardResponse {
  totalTickets: number;
  openTickets: number;
  inProgressTickets: number;
  resolvedTickets: number;
  unassignedTickets: number;
  totalAgents: number;
  totalCustomers: number;
}

export interface AdminUserResponse {
  id: number;
  name: string;
  email: string;
  role: string;
}

export type AdminTicketStatus =
  | "OPEN"
  | "IN_PROGRESS"
  | "RESOLVED"
  | "CLOSED";

export type AdminTicketPriority =
  | "LOW"
  | "MEDIUM"
  | "HIGH"
  | "URGENT";

export interface AssignTicketRequest {
  agentId: number;
}

export interface UpdateAdminTicketStatusRequest {
  status: AdminTicketStatus;
}

export interface CreateAgentRequest {
  name: string;
  email: string;
  password: string;
}

export interface UpdateAgentRequest {
  name: string;
  email: string;
}

export const getAdminDashboard =
  async (): Promise<AdminDashboardResponse> => {
    const response =
      await api.get<AdminDashboardResponse>(
        "/api/admin/dashboard"
      );

    return response.data;
  };

export const getAllAdminTickets = async (
  status?: AdminTicketStatus,
  priority?: AdminTicketPriority,
  search?: string,
  sort?: string
): Promise<TicketResponse[]> => {
  const response =
    await api.get<TicketResponse[]>(
      "/api/admin/tickets",
      {
        params: {
          ...(status ? { status } : {}),
          ...(priority ? { priority } : {}),
          ...(search ? { search } : {}),
          ...(sort ? { sort } : {}),
        },
      }
    );

  return response.data;
};

export const getAdminTicketById = async (
  id: number
): Promise<TicketResponse> => {
  const response =
    await api.get<TicketResponse>(
      `/api/admin/tickets/${id}`
    );

  return response.data;
};

export const getUnassignedTickets =
  async (): Promise<TicketResponse[]> => {
    const response =
      await api.get<TicketResponse[]>(
        "/api/admin/tickets/unassigned"
      );

    return response.data;
  };

export const getAllAgents =
  async (): Promise<AdminUserResponse[]> => {
    const response =
      await api.get<AdminUserResponse[]>(
        "/api/admin/agents"
      );

    return response.data;
  };

export const getAllCustomers =
  async (): Promise<AdminUserResponse[]> => {
    const response =
      await api.get<AdminUserResponse[]>(
        "/api/admin/customers"
      );

    return response.data;
  };

export const getUserById = async (
  id: number
): Promise<AdminUserResponse> => {
  const response =
    await api.get<AdminUserResponse>(
      `/api/admin/users/${id}`
    );

  return response.data;
};

export const assignTicket = async (
  ticketId: number,
  agentId: number
): Promise<TicketResponse> => {
  const response =
    await api.post<TicketResponse>(
      `/api/admin/tickets/${ticketId}/assign`,
      {
        agentId,
      }
    );

  return response.data;
};

export const reassignTicket = async (
  ticketId: number,
  agentId: number
): Promise<TicketResponse> => {
  const response =
    await api.put<TicketResponse>(
      `/api/admin/tickets/${ticketId}/reassign`,
      {
        agentId,
      }
    );

  return response.data;
};

export const updateAdminTicketStatus = async (
  ticketId: number,
  status: AdminTicketStatus
): Promise<TicketResponse> => {
  const response =
    await api.put<TicketResponse>(
      `/api/admin/tickets/${ticketId}/status`,
      {
        status,
      }
    );

  return response.data;
};

export const deleteAdminTicket = async (
  ticketId: number
): Promise<void> => {
  await api.delete(
    `/api/admin/tickets/${ticketId}`
  );
};

export const createAgent = async (
  data: CreateAgentRequest
): Promise<AdminUserResponse> => {
  const response =
    await api.post<AdminUserResponse>(
      "/api/admin/agents",
      data
    );

  return response.data;
};

export const updateAgent = async (
  id: number,
  data: UpdateAgentRequest
): Promise<AdminUserResponse> => {
  const response =
    await api.put<AdminUserResponse>(
      `/api/admin/agents/${id}`,
      data
    );

  return response.data;
};

export const deleteUser = async (
  id: number
): Promise<void> => {
  await api.delete(`/api/admin/users/${id}`);
};
