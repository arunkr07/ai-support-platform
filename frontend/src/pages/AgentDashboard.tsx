import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import DashboardLayout from "../layouts/DashboardLayout";

import {
  getAgentDashboard,
  getAgentTickets,
  type AgentDashboardResponse,
  type AgentTicketStatus,
  type AgentTicketPriority,
} from "../services/agentService";

import { type TicketResponse } from "../services/ticketService";

function AgentDashboard() {
  const navigate = useNavigate();

  const [dashboard, setDashboard] =
    useState<AgentDashboardResponse | null>(null);

  const [tickets, setTickets] =
    useState<TicketResponse[]>([]);

  const [loading, setLoading] = useState(true);
  const [ticketsLoading, setTicketsLoading] =
    useState(true);

  const [error, setError] = useState("");

  const [search, setSearch] = useState("");

  const [status, setStatus] =
    useState<AgentTicketStatus | "">("");

  const [priority, setPriority] =
    useState<AgentTicketPriority | "">("");

  /*
   * Load agent dashboard
   */
  useEffect(() => {
    let cancelled = false;

    const loadDashboard = async () => {
      try {
        const data = await getAgentDashboard();

        if (!cancelled) {
          setDashboard(data);
        }
      } catch (error) {
        console.error(error);

        if (!cancelled) {
          setError("Failed to load dashboard.");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    loadDashboard();

    return () => {
      cancelled = true;
    };
  }, []);

  /*
   * Load assigned tickets
   *
   * Status and priority are dependencies because
   * changing either filter should reload the tickets.
   */
  useEffect(() => {
    let cancelled = false;

    const loadTickets = async () => {
      try {
        setTicketsLoading(true);

        const data = await getAgentTickets(
          status || undefined,
          priority || undefined,
          search.trim() || undefined
        );

        if (!cancelled) {
          setTickets(data);
        }
      } catch (error) {
        console.error(error);

        if (!cancelled) {
          setError("Failed to load tickets.");
        }
      } finally {
        if (!cancelled) {
          setTicketsLoading(false);
        }
      }
    };

    loadTickets();

    return () => {
      cancelled = true;
    };
  }, [status, priority]);

  const handleSearch = async () => {
    try {
      setTicketsLoading(true);
      setError("");

      const data = await getAgentTickets(
        status || undefined,
        priority || undefined,
        search.trim() || undefined
      );

      setTickets(data);
    } catch (error) {
      console.error(error);
      setError("Failed to search tickets.");
    } finally {
      setTicketsLoading(false);
    }
  };

  const getPriorityClass = (
    ticketPriority: string
  ) => {
    switch (ticketPriority) {
      case "URGENT":
        return "bg-red-100 text-red-700";

      case "HIGH":
        return "bg-orange-100 text-orange-700";

      case "MEDIUM":
        return "bg-yellow-100 text-yellow-700";

      case "LOW":
        return "bg-green-100 text-green-700";

      default:
        return "bg-gray-100 text-gray-700";
    }
  };

  const getStatusClass = (
    ticketStatus: string
  ) => {
    switch (ticketStatus) {
      case "OPEN":
        return "bg-blue-100 text-blue-700";

      case "IN_PROGRESS":
        return "bg-purple-100 text-purple-700";

      case "RESOLVED":
        return "bg-green-100 text-green-700";

      case "CLOSED":
        return "bg-gray-100 text-gray-700";

      default:
        return "bg-gray-100 text-gray-700";
    }
  };

  if (loading) {
    return (
      <DashboardLayout>
        <div className="bg-white border border-gray-200 rounded-xl p-8 text-center">
          <p className="text-gray-500">
            Loading dashboard...
          </p>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="space-y-8">

        {/* Header */}
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            Agent Dashboard
          </h1>

          <p className="text-gray-500 mt-1">
            Manage your assigned support tickets.
          </p>
        </div>

        {/* Error */}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}

        {/* Statistics */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">

          <div className="bg-white border border-gray-200 rounded-xl p-6">
            <p className="text-sm text-gray-500">
              Total Tickets
            </p>

            <p className="text-3xl font-bold text-gray-900 mt-2">
              {dashboard?.totalTickets ?? 0}
            </p>
          </div>

          <div className="bg-white border border-gray-200 rounded-xl p-6">
            <p className="text-sm text-gray-500">
              Open
            </p>

            <p className="text-3xl font-bold text-blue-600 mt-2">
              {dashboard?.openTickets ?? 0}
            </p>
          </div>

          <div className="bg-white border border-gray-200 rounded-xl p-6">
            <p className="text-sm text-gray-500">
              In Progress
            </p>

            <p className="text-3xl font-bold text-purple-600 mt-2">
              {dashboard?.inProgressTickets ?? 0}
            </p>
          </div>

          <div className="bg-white border border-gray-200 rounded-xl p-6">
            <p className="text-sm text-gray-500">
              Resolved
            </p>

            <p className="text-3xl font-bold text-green-600 mt-2">
              {dashboard?.resolvedTickets ?? 0}
            </p>
          </div>

        </div>

        {/* Filters */}
        <div className="bg-white border border-gray-200 rounded-xl p-6">

          <div className="flex flex-col lg:flex-row gap-4">

            {/* Search */}
            <div className="flex-1">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Search
              </label>

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
                placeholder="Search by ticket title..."
                className="w-full px-4 py-3 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            {/* Status */}
            <div className="lg:w-48">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Status
              </label>

              <select
                value={status}
                onChange={(event) =>
                  setStatus(
                    event.target.value as
                      | AgentTicketStatus
                      | ""
                  )
                }
                className="w-full px-4 py-3 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">
                  All Statuses
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

            {/* Priority */}
            <div className="lg:w-48">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Priority
              </label>

              <select
                value={priority}
                onChange={(event) =>
                  setPriority(
                    event.target.value as
                      | AgentTicketPriority
                      | ""
                  )
                }
                className="w-full px-4 py-3 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">
                  All Priorities
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

            {/* Search Button */}
            <div className="lg:self-end">
              <button
                onClick={handleSearch}
                className="w-full lg:w-auto px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition"
              >
                Search
              </button>
            </div>

          </div>

        </div>

        {/* Assigned Tickets */}
        <div>

          <div className="flex items-center justify-between mb-4">

            <div>
              <h2 className="text-xl font-semibold text-gray-900">
                Assigned Tickets
              </h2>

              <p className="text-sm text-gray-500 mt-1">
                Tickets currently assigned to you.
              </p>
            </div>

            <span className="text-sm text-gray-500">
              {tickets.length} ticket
              {tickets.length !== 1 ? "s" : ""}
            </span>

          </div>

          {ticketsLoading ? (
            <div className="bg-white border border-gray-200 rounded-xl p-8 text-center">
              <p className="text-gray-500">
                Loading tickets...
              </p>
            </div>
          ) : tickets.length === 0 ? (
            <div className="bg-white border border-gray-200 rounded-xl p-10 text-center">

              <h3 className="text-lg font-semibold text-gray-700">
                No assigned tickets
              </h3>

              <p className="text-sm text-gray-500 mt-1">
                You currently have no tickets matching these filters.
              </p>

            </div>
          ) : (
            <div className="space-y-4">

              {tickets.map((ticket) => (
                <div
                  key={ticket.id}
                  onClick={() =>
                    navigate(
                      `/agent/tickets/${ticket.id}`
                    )
                  }
                  className="bg-white border border-gray-200 rounded-xl p-6 hover:shadow-md transition cursor-pointer"
                >

                  <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-4">

                    <div className="flex-1">

                      <div className="flex items-center gap-3">

                        <h3 className="text-lg font-semibold text-gray-900">
                          {ticket.title}
                        </h3>

                        <span className="text-sm text-gray-400">
                          #{ticket.id}
                        </span>

                      </div>

                      <p className="text-sm text-gray-500 mt-2">
                        Customer:{" "}
                        <span className="font-medium text-gray-700">
                          {ticket.customerName}
                        </span>
                      </p>

                      <p className="text-sm text-gray-500 mt-1">
                        {ticket.customerEmail}
                      </p>

                      <p className="text-gray-600 mt-4 line-clamp-2">
                        {ticket.description}
                      </p>

                    </div>

                    <div className="flex gap-2 flex-wrap">

                      <span
                        className={`px-3 py-1.5 rounded-full text-xs font-medium ${getStatusClass(
                          ticket.status
                        )}`}
                      >
                        {ticket.status}
                      </span>

                      <span
                        className={`px-3 py-1.5 rounded-full text-xs font-medium ${getPriorityClass(
                          ticket.priority
                        )}`}
                      >
                        {ticket.priority}
                      </span>

                    </div>

                  </div>

                  <div className="mt-5 pt-4 border-t border-gray-100 flex items-center justify-between">

                    <span className="text-xs text-gray-400">
                      Created{" "}
                      {new Date(
                        ticket.createdAt
                      ).toLocaleString()}
                    </span>

                    <span className="text-sm text-blue-600 font-medium">
                      Open Ticket →
                    </span>

                  </div>

                </div>
              ))}

            </div>
          )}

        </div>

      </div>
    </DashboardLayout>
  );
}

export default AgentDashboard;