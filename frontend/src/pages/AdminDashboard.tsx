import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import DashboardLayout from "../layouts/DashboardLayout";

import {
  getAdminDashboard,
  getAllAdminTickets,
  getAllAgents,
  getUnassignedTickets,
  assignTicket,
  reassignTicket,
  updateAdminTicketStatus,
  deleteAdminTicket,
  type AdminDashboardResponse,
  type AdminUserResponse,
  type AdminTicketStatus,
  type AdminTicketPriority,
} from "../services/adminService";

import type { TicketResponse } from "../services/ticketService";

function AdminDashboard() {
  const navigate = useNavigate();

  const [dashboard, setDashboard] =
    useState<AdminDashboardResponse | null>(null);

  const [tickets, setTickets] =
    useState<TicketResponse[]>([]);

  const [agents, setAgents] =
    useState<AdminUserResponse[]>([]);

  const [statusFilter, setStatusFilter] =
    useState<AdminTicketStatus | "">("");

  const [priorityFilter, setPriorityFilter] =
    useState<AdminTicketPriority | "">("");

  const [search, setSearch] = useState("");
  const [sort, setSort] = useState("createdAt");

  const [unassignedTicketIds, setUnassignedTicketIds] =
    useState<Set<number>>(new Set());

  const [loading, setLoading] = useState(true);
  const [ticketsLoading, setTicketsLoading] =
    useState(false);

  const [deletingTicketId, setDeletingTicketId] =
    useState<number | null>(null);

  const [error, setError] = useState("");

  const loadDashboard = useCallback(async () => {
    try {
      const data = await getAdminDashboard();
      setDashboard(data);
    } catch {
      setError("Failed to load dashboard.");
    }
  }, []);

  const loadAgents = useCallback(async () => {
    try {
      const data = await getAllAgents();
      setAgents(data);
    } catch {
      setError("Failed to load agents.");
    }
  }, []);

  const loadUnassignedTickets = useCallback(async () => {
    try {
      const data = await getUnassignedTickets();

      setUnassignedTicketIds(
        new Set(data.map((ticket) => ticket.id))
      );
    } catch {
      setError("Failed to load unassigned tickets.");
    }
  }, []);

  const loadTickets = useCallback(async () => {
    try {
      setTicketsLoading(true);
      setError("");

      const data = await getAllAdminTickets(
        statusFilter || undefined,
        priorityFilter || undefined,
        search.trim() || undefined,
        sort || undefined
      );

      setTickets(data);
    } catch {
      setError("Failed to load tickets.");
    } finally {
      setTicketsLoading(false);
    }
  }, [
    statusFilter,
    priorityFilter,
    search,
    sort,
  ]);

  const initialize = useCallback(async () => {
    setLoading(true);

    await Promise.all([
      loadDashboard(),
      loadTickets(),
      loadAgents(),
      loadUnassignedTickets(),
    ]);

    setLoading(false);
  }, [
    loadDashboard,
    loadTickets,
    loadAgents,
    loadUnassignedTickets,
  ]);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    initialize();
  }, [initialize]);

  useEffect(() => {
    if (!loading) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      loadTickets();
    }
  }, [
    statusFilter,
    priorityFilter,
    sort,
    loading,
    loadTickets,
  ]);

  const handleSearch = () => {
    loadTickets();
  };

  const handleAssign = async (
    ticketId: number,
    agentId: number
  ) => {
    try {
      setError("");

      if (!agentId) {
        return;
      }

      const isUnassigned =
        unassignedTicketIds.has(ticketId);

      const updatedTicket = isUnassigned
        ? await assignTicket(ticketId, agentId)
        : await reassignTicket(ticketId, agentId);

      setTickets((current) =>
        current.map((item) =>
          item.id === ticketId
            ? updatedTicket
            : item
        )
      );

      setUnassignedTicketIds((current) => {
        const updated = new Set(current);
        updated.delete(ticketId);
        return updated;
      });

      await Promise.all([
        loadDashboard(),
        loadUnassignedTickets(),
      ]);
    } catch {
      setError(
        "Failed to assign or reassign ticket."
      );
    }
  };

  const handleStatusChange = async (
    ticketId: number,
    status: AdminTicketStatus
  ) => {
    try {
      setError("");

      const updatedTicket =
        await updateAdminTicketStatus(
          ticketId,
          status
        );

      setTickets((current) =>
        current.map((item) =>
          item.id === ticketId
            ? updatedTicket
            : item
        )
      );

      await loadDashboard();
    } catch {
      setError("Invalid status transition.");
      await loadTickets();
    }
  };

  const handleDelete = async (ticketId: number) => {
    if (deletingTicketId !== null) {
      return;
    }

    const confirmed = window.confirm(
      "Are you sure you want to permanently delete this ticket?"
    );

    if (!confirmed) {
      return;
    }

    try {
      setError("");
      setDeletingTicketId(ticketId);

      /*
       * IMPORTANT:
       * This is the actual DELETE request.
       *
       * Backend returns HTTP 204 No Content.
       * Axios treats 204 as a successful response.
       */
      await deleteAdminTicket(ticketId);

      /*
       * Remove the ticket immediately from the UI.
       */
      setTickets((current) =>
        current.filter(
          (ticket) => ticket.id !== ticketId
        )
      );

      /*
       * Remove it from the unassigned set as well.
       */
      setUnassignedTicketIds((current) => {
        const updated = new Set(current);
        updated.delete(ticketId);
        return updated;
      });

      /*
       * The ticket has already been deleted successfully.
       *
       * Dashboard refresh is intentionally separate so that
       * a dashboard-refresh failure does NOT make a successful
       * deletion look like a failed deletion.
       */
      try {
        await loadDashboard();
      } catch {
        console.error(
          "Ticket deleted successfully, but dashboard refresh failed."
        );
      }

    } catch (error) {
      console.error(
        "Delete ticket request failed:",
        error
      );

      setError(
        "Failed to delete ticket. Please try again."
      );
    } finally {
      setDeletingTicketId(null);
    }
  };

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

  const getStatusClass = (status: string) => {
    switch (status) {
      case "OPEN":
        return "bg-blue-100 text-blue-700";

      case "IN_PROGRESS":
        return "bg-yellow-100 text-yellow-700";

      case "RESOLVED":
        return "bg-green-100 text-green-700";

      case "CLOSED":
        return "bg-gray-200 text-gray-700";

      default:
        return "bg-gray-100 text-gray-700";
    }
  };

  const getPriorityClass = (priority: string) => {
    switch (priority) {
      case "LOW":
        return "bg-gray-100 text-gray-700";

      case "MEDIUM":
        return "bg-blue-100 text-blue-700";

      case "HIGH":
        return "bg-orange-100 text-orange-700";

      case "URGENT":
        return "bg-red-100 text-red-700";

      default:
        return "bg-gray-100 text-gray-700";
    }
  };

  if (loading) {
    return (
      <DashboardLayout>
        <div className="flex min-h-[400px] items-center justify-center">
          <p className="text-gray-500">
            Loading admin dashboard...
          </p>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="space-y-8">

        <div>
          <h1 className="text-3xl font-bold text-gray-900">
            Admin Dashboard
          </h1>

          <p className="mt-1 text-gray-500">
            Manage tickets, agents, customers and
            support operations.
          </p>
        </div>

        {error && (
          <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        {/* Statistics */}

        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">

          <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
            <p className="text-sm font-medium text-gray-500">
              Total Tickets
            </p>

            <p className="mt-2 text-3xl font-bold text-gray-900">
              {dashboard?.totalTickets ?? 0}
            </p>
          </div>

          <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
            <p className="text-sm font-medium text-gray-500">
              Open Tickets
            </p>

            <p className="mt-2 text-3xl font-bold text-blue-600">
              {dashboard?.openTickets ?? 0}
            </p>
          </div>

          <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
            <p className="text-sm font-medium text-gray-500">
              In Progress
            </p>

            <p className="mt-2 text-3xl font-bold text-yellow-600">
              {dashboard?.inProgressTickets ?? 0}
            </p>
          </div>

          <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
            <p className="text-sm font-medium text-gray-500">
              Resolved
            </p>

            <p className="mt-2 text-3xl font-bold text-green-600">
              {dashboard?.resolvedTickets ?? 0}
            </p>
          </div>

        </div>

        {/* People */}

        <div className="grid grid-cols-1 gap-5 sm:grid-cols-3">

          <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
            <p className="text-sm font-medium text-gray-500">
              Unassigned Tickets
            </p>

            <p className="mt-2 text-2xl font-bold text-red-600">
              {dashboard?.unassignedTickets ?? 0}
            </p>
          </div>

          <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
            <p className="text-sm font-medium text-gray-500">
              Agents
            </p>

            <p className="mt-2 text-2xl font-bold text-gray-900">
              {dashboard?.totalAgents ?? 0}
            </p>
          </div>

          <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
            <p className="text-sm font-medium text-gray-500">
              Customers
            </p>

            <p className="mt-2 text-2xl font-bold text-gray-900">
              {dashboard?.totalCustomers ?? 0}
            </p>
          </div>

        </div>

        {/* Ticket Management */}

        <div className="rounded-xl border border-gray-200 bg-white shadow-sm">

          <div className="border-b border-gray-200 p-6">

            <div className="flex flex-col gap-4 lg:flex-row lg:items-end">

              <div className="flex-1">

                <label className="mb-1 block text-sm font-medium text-gray-700">
                  Search
                </label>

                <div className="flex gap-2">

                  <input
                    type="text"
                    value={search}
                    onChange={(event) =>
                      setSearch(event.target.value)
                    }
                    onKeyDown={(event) => {
                      if (event.key === "Enter") {
                        handleSearch();
                      }
                    }}
                    placeholder="Search title or description..."
                    className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none focus:border-gray-500 focus:ring-1 focus:ring-gray-500"
                  />

                  <button
                    type="button"
                    onClick={handleSearch}
                    className="rounded-lg bg-gray-900 px-4 py-2 text-sm font-medium text-white hover:bg-gray-800"
                  >
                    Search
                  </button>

                </div>

              </div>

              <div>

                <label className="mb-1 block text-sm font-medium text-gray-700">
                  Status
                </label>

                <select
                  value={statusFilter}
                  onChange={(event) =>
                    setStatusFilter(
                      event.target.value as
                        | AdminTicketStatus
                        | ""
                    )
                  }
                  className="rounded-lg border border-gray-300 px-3 py-2 text-sm"
                >
                  <option value="">
                    All statuses
                  </option>
                  <option value="OPEN">
                    Open
                  </option>
                  <option value="IN_PROGRESS">
                    In Progress
                  </option>
                  <option value="RESOLVED">
                    Resolved
                  </option>
                  <option value="CLOSED">
                    Closed
                  </option>
                </select>

              </div>

              <div>

                <label className="mb-1 block text-sm font-medium text-gray-700">
                  Priority
                </label>

                <select
                  value={priorityFilter}
                  onChange={(event) =>
                    setPriorityFilter(
                      event.target.value as
                        | AdminTicketPriority
                        | ""
                    )
                  }
                  className="rounded-lg border border-gray-300 px-3 py-2 text-sm"
                >
                  <option value="">
                    All priorities
                  </option>
                  <option value="LOW">
                    Low
                  </option>
                  <option value="MEDIUM">
                    Medium
                  </option>
                  <option value="HIGH">
                    High
                  </option>
                  <option value="URGENT">
                    Urgent
                  </option>
                </select>

              </div>

              <div>

                <label className="mb-1 block text-sm font-medium text-gray-700">
                  Sort
                </label>

                <select
                  value={sort}
                  onChange={(event) =>
                    setSort(event.target.value)
                  }
                  className="rounded-lg border border-gray-300 px-3 py-2 text-sm"
                >
                  <option value="createdAt">
                    Created
                  </option>
                  <option value="updatedAt">
                    Updated
                  </option>
                  <option value="title">
                    Title
                  </option>
                  <option value="id">
                    ID
                  </option>
                </select>

              </div>

            </div>

          </div>

          <div className="overflow-x-auto">

            {ticketsLoading ? (
              <div className="p-10 text-center text-gray-500">
                Loading tickets...
              </div>
            ) : tickets.length === 0 ? (
              <div className="p-10 text-center text-gray-500">
                No tickets found.
              </div>
            ) : (
              <table className="min-w-full divide-y divide-gray-200">

                <thead className="bg-gray-50">

                  <tr>

                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                      Ticket
                    </th>

                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                      Customer
                    </th>

                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                      Priority
                    </th>

                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                      Status
                    </th>

                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                      Assignment
                    </th>

                    <th className="px-6 py-3 text-right text-xs font-semibold uppercase tracking-wider text-gray-500">
                      Actions
                    </th>

                  </tr>

                </thead>

                <tbody className="divide-y divide-gray-200 bg-white">

                  {tickets.map((ticket) => {

                    const availableStatuses =
                      getAvailableStatuses(
                        ticket.status
                      );

                    const isUnassigned =
                      unassignedTicketIds.has(
                        ticket.id
                      );

                    const isDeleting =
                      deletingTicketId === ticket.id;

                    return (
                      <tr
                        key={ticket.id}
                        className="hover:bg-gray-50"
                      >

                        <td className="px-6 py-4">

                          <button
                            type="button"
                            onClick={() =>
                              navigate(
                                `/admin/tickets/${ticket.id}`
                              )
                            }
                            className="max-w-xs text-left"
                          >

                            <p className="font-medium text-blue-600 hover:text-blue-800">
                              #{ticket.id}{" "}
                              {ticket.title}
                            </p>

                            <p className="mt-1 truncate text-sm text-gray-500">
                              {ticket.description}
                            </p>

                          </button>

                        </td>

                        <td className="px-6 py-4">

                          <p className="text-sm font-medium text-gray-900">
                            {ticket.customerName}
                          </p>

                          <p className="text-xs text-gray-500">
                            {ticket.customerEmail}
                          </p>

                        </td>

                        <td className="px-6 py-4">

                          <span
                            className={`rounded-full px-2.5 py-1 text-xs font-medium ${getPriorityClass(
                              ticket.priority
                            )}`}
                          >
                            {ticket.priority}
                          </span>

                        </td>

                        <td className="px-6 py-4">

                          <select
                            value={ticket.status}
                            disabled={
                              ticket.status ===
                                "CLOSED" ||
                              isDeleting
                            }
                            onChange={(event) =>
                              handleStatusChange(
                                ticket.id,
                                event.target
                                  .value as AdminTicketStatus
                              )
                            }
                            className={`rounded-full border-0 px-2.5 py-1 text-xs font-medium ${getStatusClass(
                              ticket.status
                            )}`}
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

                        </td>

                        <td className="px-6 py-4">

                          <select
                            defaultValue=""
                            disabled={isDeleting}
                            onChange={(event) => {

                              const agentId =
                                Number(
                                  event.target.value
                                );

                              if (!agentId) {
                                return;
                              }

                              handleAssign(
                                ticket.id,
                                agentId
                              );

                              event.target.value = "";

                            }}
                            className="rounded-lg border border-gray-300 px-2 py-1.5 text-xs"
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
                                {agent.name}
                              </option>
                            ))}

                          </select>

                        </td>

                        <td className="px-6 py-4 text-right">

                          <button
                            type="button"
                            disabled={deletingTicketId !== null}
                            onClick={() =>
                              handleDelete(
                                ticket.id
                              )
                            }
                            className={`text-sm font-medium ${
                              isDeleting
                                ? "cursor-not-allowed text-gray-400"
                                : "text-red-600 hover:text-red-800"
                            }`}
                          >
                            {isDeleting
                              ? "Deleting..."
                              : "Delete"}
                          </button>

                        </td>

                      </tr>
                    );
                  })}

                </tbody>

              </table>
            )}

          </div>

        </div>

      </div>
    </DashboardLayout>
  );
}

export default AdminDashboard;