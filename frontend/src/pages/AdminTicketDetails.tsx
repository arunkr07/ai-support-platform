import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import DashboardLayout from "../layouts/DashboardLayout";

import {
  getAdminTicketById,
  getAllAgents,
  getUnassignedTickets,
  assignTicket,
  reassignTicket,
  updateAdminTicketStatus,
  deleteAdminTicket,
  type AdminUserResponse,
  type AdminTicketStatus,
} from "../services/adminService";

import type { TicketResponse } from "../services/ticketService";

function AdminTicketDetails() {
  const { id } = useParams();
  const navigate = useNavigate();

  const ticketId = Number(id);

  const [ticket, setTicket] =
    useState<TicketResponse | null>(null);

  const [agents, setAgents] =
    useState<AdminUserResponse[]>([]);

  const [isUnassigned, setIsUnassigned] =
    useState(false);

  const [loading, setLoading] =
    useState(true);

  const [actionLoading, setActionLoading] =
    useState(false);

  const [error, setError] = useState("");

  const loadTicket = useCallback(async () => {
    try {
      const data =
        await getAdminTicketById(ticketId);

      setTicket(data);
    } catch {
      setError("Failed to load ticket.");
    }
  }, [ticketId]);

  const loadAgents = useCallback(async () => {
    try {
      const data = await getAllAgents();
      setAgents(data);
    } catch {
      setError("Failed to load agents.");
    }
  }, []);

  const loadAssignmentState =
    useCallback(async () => {
      try {
        const data =
          await getUnassignedTickets();

        setIsUnassigned(
          data.some(
            (item) => item.id === ticketId
          )
        );
      } catch {
        setError(
          "Failed to load assignment information."
        );
      }
    }, [ticketId]);

  const initialize = useCallback(async () => {
    if (!ticketId) {
      setError("Invalid ticket ID.");
      setLoading(false);
      return;
    }

    setLoading(true);

    await Promise.all([
      loadTicket(),
      loadAgents(),
      loadAssignmentState(),
    ]);

    setLoading(false);
  }, [
    ticketId,
    loadTicket,
    loadAgents,
    loadAssignmentState,
  ]);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    initialize();
  }, [initialize]);

  const getAvailableStatuses = (
    status: string
  ): AdminTicketStatus[] => {
    switch (status) {
      case "OPEN":
        return ["OPEN", "IN_PROGRESS"];

      case "IN_PROGRESS":
        return [
          "IN_PROGRESS",
          "OPEN",
          "RESOLVED",
        ];

      case "RESOLVED":
        return [
          "RESOLVED",
          "IN_PROGRESS",
          "CLOSED",
        ];

      case "CLOSED":
        return ["CLOSED"];

      default:
        return [];
    }
  };

  const handleAssignment = async (
    agentId: number
  ) => {
    if (!ticket || !agentId) {
      return;
    }

    try {
      setActionLoading(true);
      setError("");

      const updatedTicket = isUnassigned
        ? await assignTicket(
            ticket.id,
            agentId
          )
        : await reassignTicket(
            ticket.id,
            agentId
          );

      setTicket(updatedTicket);
      setIsUnassigned(false);
    } catch {
      setError(
        "Failed to assign or reassign ticket."
      );
    } finally {
      setActionLoading(false);
    }
  };

  const handleStatusChange = async (
    status: AdminTicketStatus
  ) => {
    if (!ticket) {
      return;
    }

    try {
      setActionLoading(true);
      setError("");

      const updatedTicket =
        await updateAdminTicketStatus(
          ticket.id,
          status
        );

      setTicket(updatedTicket);
    } catch {
      setError("Invalid status transition.");
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!ticket) {
      return;
    }

    const confirmed = window.confirm(
      "Are you sure you want to permanently delete this ticket?"
    );

    if (!confirmed) {
      return;
    }

    try {
      setActionLoading(true);
      setError("");

      await deleteAdminTicket(ticket.id);

      navigate("/admin");
    } catch {
      setError("Failed to delete ticket.");
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <DashboardLayout>
        <div className="flex min-h-100 items-center justify-center">
          <p className="text-gray-500">
            Loading ticket...
          </p>
        </div>
      </DashboardLayout>
    );
  }

  if (!ticket) {
    return (
      <DashboardLayout>
        <div className="space-y-4">

          <button
            onClick={() => navigate("/admin")}
            className="text-sm font-medium text-gray-600 hover:text-gray-900"
          >
            ← Back to Admin Dashboard
          </button>

          <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-red-700">
            {error || "Ticket not found."}
          </div>

        </div>
      </DashboardLayout>
    );
  }

  const availableStatuses =
    getAvailableStatuses(ticket.status);

  return (
    <DashboardLayout>
      <div className="space-y-6">

        <button
          onClick={() => navigate("/admin")}
          className="text-sm font-medium text-gray-600 hover:text-gray-900"
        >
          ← Back to Admin Dashboard
        </button>

        {error && (
          <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}


        <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-start">

          <div>

            <p className="text-sm font-medium text-gray-500">
              Ticket #{ticket.id}
            </p>

            <h1 className="mt-1 text-3xl font-bold text-gray-900">
              {ticket.title}
            </h1>

          </div>

          <button
            onClick={handleDelete}
            disabled={actionLoading}
            className="rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            Delete Ticket
          </button>

        </div>


        <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">

          <h2 className="text-lg font-semibold text-gray-900">
            Ticket Information
          </h2>

          <div className="mt-6 grid grid-cols-1 gap-6 md:grid-cols-2">

            <div>
              <p className="text-sm text-gray-500">
                Status
              </p>

              <select
                value={ticket.status}
                disabled={
                  actionLoading ||
                  ticket.status === "CLOSED"
                }
                onChange={(event) =>
                  handleStatusChange(
                    event.target
                      .value as AdminTicketStatus
                  )
                }
                className="mt-2 rounded-lg border border-gray-300 px-3 py-2 text-sm"
              >
                {availableStatuses.map(
                  (status) => (
                    <option
                      key={status}
                      value={status}
                    >
                      {status.replace(
                        "_",
                        " "
                      )}
                    </option>
                  )
                )}
              </select>
            </div>

            <div>
              <p className="text-sm text-gray-500">
                Priority
              </p>

              <p className="mt-2 font-medium text-gray-900">
                {ticket.priority}
              </p>
            </div>

            <div>
              <p className="text-sm text-gray-500">
                Created
              </p>

              <p className="mt-2 text-sm text-gray-900">
                {new Date(
                  ticket.createdAt
                ).toLocaleString()}
              </p>
            </div>

            <div>
              <p className="text-sm text-gray-500">
                Last Updated
              </p>

              <p className="mt-2 text-sm text-gray-900">
                {new Date(
                  ticket.updatedAt
                ).toLocaleString()}
              </p>
            </div>

          </div>

          <div className="mt-6">

            <p className="text-sm text-gray-500">
              Description
            </p>

            <div className="mt-2 rounded-lg bg-gray-50 p-4 text-sm leading-6 text-gray-700">
              {ticket.description}
            </div>

          </div>

        </div>


        <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">

          <h2 className="text-lg font-semibold text-gray-900">
            Customer
          </h2>

          <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-3">

            <div>
              <p className="text-sm text-gray-500">
                Customer ID
              </p>

              <p className="mt-1 font-medium text-gray-900">
                {ticket.customerId}
              </p>
            </div>

            <div>
              <p className="text-sm text-gray-500">
                Name
              </p>

              <p className="mt-1 font-medium text-gray-900">
                {ticket.customerName}
              </p>
            </div>

            <div>
              <p className="text-sm text-gray-500">
                Email
              </p>

              <p className="mt-1 text-sm text-gray-700">
                {ticket.customerEmail}
              </p>
            </div>

          </div>

        </div>


        <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">

          <h2 className="text-lg font-semibold text-gray-900">
            Assignment
          </h2>

          <div className="mt-4 rounded-lg bg-gray-50 p-4">

            {ticket.assignedAgentId ? (
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">

                <div>
                  <p className="text-sm text-gray-500">
                    Agent ID
                  </p>

                  <p className="mt-1 font-medium text-gray-900">
                    {ticket.assignedAgentId}
                  </p>
                </div>

                <div>
                  <p className="text-sm text-gray-500">
                    Name
                  </p>

                  <p className="mt-1 font-medium text-gray-900">
                    {ticket.assignedAgentName}
                  </p>
                </div>

                <div>
                  <p className="text-sm text-gray-500">
                    Email
                  </p>

                  <p className="mt-1 text-sm text-gray-700">
                    {ticket.assignedAgentEmail}
                  </p>
                </div>

              </div>
            ) : (
              <p className="text-sm text-gray-600">
                This ticket is currently unassigned.
              </p>
            )}

          </div>

          <div className="mt-4 flex flex-col gap-3 sm:flex-row">

            <select
              defaultValue=""
              disabled={actionLoading}
              onChange={(event) => {
                const agentId =
                  Number(event.target.value);

                if (agentId) {
                  handleAssignment(agentId);
                }

                event.target.value = "";
              }}
              className="rounded-lg border border-gray-300 px-3 py-2 text-sm"
            >

              <option value="">
                {isUnassigned
                  ? "Assign agent"
                  : "Reassign agent"}
              </option>

              {agents.map((agent) => (
                <option
                  key={agent.id}
                  value={agent.id}
                >
                  {agent.name} — {agent.email}
                </option>
              ))}

            </select>

          </div>

        </div>

      </div>
    </DashboardLayout>
  );
}

export default AdminTicketDetails;