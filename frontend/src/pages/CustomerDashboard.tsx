import { useEffect, useState } from "react";
import DashboardLayout from "../layouts/DashboardLayout";
import CreateTicketForm from "../components/CreateTicketForm";
import {
  getMyTickets,
  type TicketResponse,
} from "../services/ticketService";
import { useNavigate } from "react-router-dom";

function CustomerDashboard() {
  const navigate = useNavigate();

  const [tickets, setTickets] = useState<TicketResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadTickets = async () => {
      try {
        const data = await getMyTickets();
        setTickets(data);
      } catch (error) {
        console.error(error);
        setError("Failed to load tickets.");
      } finally {
        setLoading(false);
      }
    };

    loadTickets();
  }, []);

  const handleTicketCreated = (ticket: TicketResponse) => {
    setTickets((previousTickets) => [
      ticket,
      ...previousTickets,
    ]);
  };

  const openTickets = tickets.filter(
    (ticket) => ticket.status !== "RESOLVED"
  ).length;

  const resolvedTickets = tickets.filter(
    (ticket) => ticket.status === "RESOLVED"
  ).length;

  const inProgressTickets = tickets.filter(
    (ticket) => ticket.status === "IN_PROGRESS"
  ).length;

  return (
    <DashboardLayout>

      <div>

        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">
              Customer Dashboard
            </h1>

            <p className="text-gray-500 mt-2">
              Manage your support requests and conversations.
            </p>
          </div>
        </div>

        {/* Statistics */}

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-8">

          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <p className="text-sm text-gray-500">
              Open Tickets
            </p>

            <p className="text-3xl font-bold text-gray-900 mt-2">
              {openTickets}
            </p>
          </div>

          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <p className="text-sm text-gray-500">
              In Progress
            </p>

            <p className="text-3xl font-bold text-gray-900 mt-2">
              {inProgressTickets}
            </p>
          </div>

          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <p className="text-sm text-gray-500">
              Resolved
            </p>

            <p className="text-3xl font-bold text-gray-900 mt-2">
              {resolvedTickets}
            </p>
          </div>

        </div>

        {/* Create Ticket */}

        <div className="mt-8">
          <CreateTicketForm
            onTicketCreated={handleTicketCreated}
          />
        </div>

        {/* Tickets */}

        <div className="mt-8">

          <h2 className="text-xl font-semibold text-gray-900 mb-4">
            My Tickets
          </h2>

          {loading && (
            <p className="text-gray-500">
              Loading tickets...
            </p>
          )}

          {error && (
            <div className="bg-red-50 text-red-600 px-4 py-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          {!loading && !error && tickets.length === 0 && (
            <div className="bg-white border border-gray-200 rounded-xl p-8 text-center">
              <p className="text-gray-500">
                You haven't created any tickets yet.
              </p>
            </div>
          )}

          <div className="space-y-4">

            {tickets.map((ticket) => (
              <div
                key={ticket.id}
                onClick={() =>
                  navigate(`/customer/tickets/${ticket.id}`)
                }
                className="bg-white border border-gray-200 rounded-xl p-6"
              >

                <div className="flex items-start justify-between">

                  <div>
                    <h3 className="text-lg font-semibold text-gray-900">
                      {ticket.title}
                    </h3>

                    <p className="text-sm text-gray-500 mt-2">
                      {ticket.description}
                    </p>
                  </div>

                  <span className="text-xs font-medium bg-gray-100 text-gray-700 px-3 py-1 rounded-full">
                    {ticket.priority}
                  </span>

                </div>

                <div className="flex items-center gap-4 mt-4 text-sm">

                  <span className="text-gray-500">
                    Status:
                  </span>

                  <span className="font-medium text-gray-700">
                    {ticket.status}
                  </span>

                </div>

              </div>
            ))}

          </div>

        </div>

      </div>

    </DashboardLayout>
  );
}

export default CustomerDashboard;