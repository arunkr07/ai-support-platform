import { type FormEvent, useState } from "react";
import {
  createTicket,
  type TicketPriority,
  type TicketResponse,
} from "../services/ticketService";

interface CreateTicketFormProps {
  onTicketCreated: (ticket: TicketResponse) => void;
}

function CreateTicketForm({
  onTicketCreated,
}: CreateTicketFormProps) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [priority, setPriority] = useState<TicketPriority>("MEDIUM");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();

    setError("");
    setLoading(true);

    try {
      const ticket = await createTicket({
        title,
        description,
        priority,
      });

      onTicketCreated(ticket);

      setTitle("");
      setDescription("");
      setPriority("MEDIUM");

    } catch (error) {
      console.error(error);
      setError("Failed to create ticket.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white border border-gray-200 rounded-xl p-6">

      <h2 className="text-xl font-semibold text-gray-900">
        Create Support Ticket
      </h2>

      <p className="text-sm text-gray-500 mt-1 mb-6">
        Describe your issue and our support team will help you.
      </p>

      <form onSubmit={handleSubmit} className="space-y-5">

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Title
          </label>

          <input
            type="text"
            value={title}
            onChange={(event) => setTitle(event.target.value)}
            placeholder="e.g. Unable to reset password"
            maxLength={100}
            required
            className="w-full px-4 py-3 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-blue-500"
          />

          <p className="text-xs text-gray-400 mt-1">
            {title.length}/100
          </p>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Description
          </label>

          <textarea
            value={description}
            onChange={(event) => setDescription(event.target.value)}
            placeholder="Describe your problem in detail..."
            maxLength={1000}
            required
            rows={6}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-blue-500 resize-none"
          />

          <p className="text-xs text-gray-400 mt-1">
            {description.length}/1000
          </p>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Priority
          </label>

          <select
            value={priority}
            onChange={(event) =>
              setPriority(event.target.value as TicketPriority)
            }
            className="w-full px-4 py-3 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="URGENT">Urgent</option>
          </select>
        </div>

        {error && (
          <div className="bg-red-50 text-red-600 px-4 py-3 rounded-lg text-sm">
            {error}
          </div>
        )}

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white font-medium py-3 rounded-lg transition"
        >
          {loading ? "Creating Ticket..." : "Create Ticket"}
        </button>

      </form>

    </div>
  );
}

export default CreateTicketForm;