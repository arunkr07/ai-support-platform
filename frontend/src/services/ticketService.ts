import api from "./api";

export type TicketPriority =
  | "LOW"
  | "MEDIUM"
  | "HIGH"
  | "URGENT";

export interface CreateTicketRequest {
  title: string;
  description: string;
  priority: TicketPriority;
}

export interface TicketResponse {
  assignedAgentId: number;
  id: number;
  title: string;
  description: string;
  status: string;
  priority: string;
  customerId: number;
  customerName: string;
  customerEmail: string;
  createdAt: string;
  updatedAt: string;
}

export interface TicketMessageResponse {
  id: number;
  content: string;
  senderId: number;
  senderName: string;
  senderRole: string;
  createdAt: string;
}

export interface SendMessageRequest {
  content: string;
}

export const createTicket = async (
  data: CreateTicketRequest
): Promise<TicketResponse> => {
  const response = await api.post<TicketResponse>(
    "/api/tickets",
    data
  );

  return response.data;
};

export const getMyTickets = async (): Promise<TicketResponse[]> => {
  const response = await api.get<TicketResponse[]>(
    "/api/tickets"
  );

  return response.data;
};

export const getTicketById = async (
  id: number
): Promise<TicketResponse> => {
  const response = await api.get<TicketResponse>(
    `/api/tickets/${id}`
  );

  return response.data;
};

export const updateTicket = async (
  id: number,
  data: CreateTicketRequest
): Promise<TicketResponse> => {
  const response = await api.put<TicketResponse>(
    `/api/tickets/${id}`,
    data
  );

  return response.data;
};

export const deleteTicket = async (
  id: number
): Promise<void> => {
  await api.delete(`/api/tickets/${id}`);
};

export const reopenTicket = async (
  id: number
): Promise<TicketResponse> => {
  const response = await api.put<TicketResponse>(
    `/api/tickets/${id}/reopen`
  );

  return response.data;
};

export const getTicketMessages = async (
  id: number
): Promise<TicketMessageResponse[]> => {
  const response = await api.get<TicketMessageResponse[]>(
    `/api/tickets/${id}/messages`
  );

  return response.data;
};

export const sendTicketMessage = async (
  id: number,
  data: SendMessageRequest
): Promise<TicketMessageResponse> => {
  const response = await api.post<TicketMessageResponse>(
    `/api/tickets/${id}/messages`,
    data
  );

  return response.data;
};