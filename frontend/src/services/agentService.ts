import api from "./api";

import {
  type TicketResponse,
  type TicketMessageResponse,
} from "./ticketService";

export interface AgentDashboardResponse {
  totalTickets: number;
  openTickets: number;
  inProgressTickets: number;
  resolvedTickets: number;
}

export type AgentTicketStatus =
  | "OPEN"
  | "IN_PROGRESS"
  | "RESOLVED"
  | "CLOSED";

export type AgentTicketPriority =
  | "LOW"
  | "MEDIUM"
  | "HIGH"
  | "URGENT";

export interface UpdateTicketStatusRequest {
  status: AgentTicketStatus;
}

export interface SendAgentMessageRequest {
  content: string;
}

export interface SuggestedReplyResponse {
  suggestedReply: string;
}

export interface TicketAiAnalysisResponse {
  id: number;
  ticketId: number;
  summary: string;
  category: string;
  suggestedPriority: string;
  createdAt: string;
}

export const getAgentDashboard =
  async (): Promise<AgentDashboardResponse> => {
    const response =
      await api.get<AgentDashboardResponse>(
        "/api/agent/dashboard"
      );

    return response.data;
  };

export const getAgentTickets = async (
  status?: AgentTicketStatus,
  priority?: AgentTicketPriority,
  search?: string
): Promise<TicketResponse[]> => {
  const response = await api.get<TicketResponse[]>(
    "/api/agent/tickets",
    {
      params: {
        ...(status ? { status } : {}),
        ...(priority ? { priority } : {}),
        ...(search ? { search } : {}),
      },
    }
  );

  return response.data;
};

export const getAgentTicketById = async (
  id: number
): Promise<TicketResponse> => {
  const response = await api.get<TicketResponse>(
    `/api/agent/tickets/${id}`
  );

  return response.data;
};

export const claimTicket = async (
  id: number
): Promise<TicketResponse> => {
  const response = await api.put<TicketResponse>(
    `/api/agent/tickets/${id}/claim`
  );

  return response.data;
};

export const updateTicketStatus = async (
  id: number,
  status: AgentTicketStatus
): Promise<TicketResponse> => {
  const response = await api.put<TicketResponse>(
    `/api/agent/tickets/${id}/status`,
    {
      status,
    }
  );

  return response.data;
};

export const releaseTicket = async (
  id: number
): Promise<TicketResponse> => {
  const response = await api.put<TicketResponse>(
    `/api/agent/tickets/${id}/release`
  );

  return response.data;
};

export const getAgentMessages = async (
  id: number
): Promise<TicketMessageResponse[]> => {
  const response =
    await api.get<TicketMessageResponse[]>(
      `/api/agent/tickets/${id}/messages`
    );

  return response.data;
};

export const sendAgentMessage = async (
  id: number,
  data: SendAgentMessageRequest
): Promise<TicketMessageResponse> => {
  const response =
    await api.post<TicketMessageResponse>(
      `/api/agent/tickets/${id}/messages`,
      data
    );

  return response.data;
};

/* ============================= */
/* AI ASSISTANCE                 */
/* ============================= */

export const generateSuggestedReply = async (
  id: number
): Promise<SuggestedReplyResponse> => {
  const response =
    await api.post<SuggestedReplyResponse>(
      `/api/agent/tickets/${id}/suggest-reply`,
      {}
    );

  return response.data;
};

export const getTicketAiAnalysis = async (
  id: number
): Promise<TicketAiAnalysisResponse> => {
  const response =
    await api.get<TicketAiAnalysisResponse>(
      `/api/agent/tickets/${id}/ai-analysis`
    );

  return response.data;
};