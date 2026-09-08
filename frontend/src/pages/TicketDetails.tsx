import { type FormEvent, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import DashboardLayout from "../layouts/DashboardLayout";

import {
  getTicketById,
  getTicketMessages,
  sendTicketMessage,
  type TicketMessageResponse,
  type TicketResponse,
} from "../services/ticketService";

function TicketDetails() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [ticket, setTicket] =
    useState<TicketResponse | null>(null);

  const [messages, setMessages] =
    useState<TicketMessageResponse[]>([]);

  const [loading, setLoading] = useState(true);
  const [messagesLoading, setMessagesLoading] = useState(true);

  const [error, setError] = useState("");
  const [messageError, setMessageError] = useState("");

  const [message, setMessage] = useState("");
  const [sendingMessage, setSendingMessage] = useState(false);

  useEffect(() => {
    const loadTicket = async () => {
      if (!id) {
        setError("Invalid ticket ID.");
        setLoading(false);
        return;
      }

      try {
        setError("");

        const data = await getTicketById(Number(id));

        setTicket(data);
      } catch (error) {
        console.error(error);
        setError("Failed to load ticket.");
      } finally {
        setLoading(false);
      }
    };

    loadTicket();
  }, [id]);

  useEffect(() => {
    const loadMessages = async () => {
      if (!id) {
        setMessagesLoading(false);
        return;
      }

      try {
        setMessageError("");

        const data = await getTicketMessages(Number(id));

        setMessages(data);
      } catch (error) {
        console.error(error);
        setMessageError(
          "Failed to load conversation."
        );
      } finally {
        setMessagesLoading(false);
      }
    };

    loadMessages();
  }, [id]);

  const handleSendMessage = async (
    event: FormEvent
  ) => {
    event.preventDefault();

    if (!id) {
      return;
    }

    const trimmedMessage = message.trim();

    if (!trimmedMessage) {
      return;
    }

    try {
      setMessageError("");
      setSendingMessage(true);

      const newMessage = await sendTicketMessage(
        Number(id),
        {
          content: trimmedMessage,
        }
      );

      setMessages((previousMessages) => [
        ...previousMessages,
        newMessage,
      ]);

      setMessage("");
    } catch (error) {
      console.error(error);

      setMessageError(
        "Failed to send message. Please try again."
      );
    } finally {
      setSendingMessage(false);
    }
  };

  if (loading) {
    return (
      <DashboardLayout>
        <div className="bg-white border border-gray-200 rounded-xl p-8 text-center">
          <p className="text-gray-500">
            Loading ticket...
          </p>
        </div>
      </DashboardLayout>
    );
  }

  if (error || !ticket) {
    return (
      <DashboardLayout>
        <div>

          <button
            onClick={() => navigate("/customer")}
            className="text-sm text-blue-600 hover:text-blue-700 mb-6"
          >
            ← Back to My Tickets
          </button>

          <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg">
            {error || "Ticket not found."}
          </div>

        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>

      <div>

        {/* Back Button */}

        <button
          onClick={() => navigate("/customer")}
          className="text-sm text-blue-600 hover:text-blue-700 mb-6"
        >
          ← Back to My Tickets
        </button>

        {/* Ticket Details */}

        <div className="bg-white border border-gray-200 rounded-xl p-6">

          <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-4">

            <div>

              <div className="flex items-center gap-3">

                <h1 className="text-2xl font-bold text-gray-900">
                  {ticket.title}
                </h1>

                <span className="text-sm text-gray-400">
                  #{ticket.id}
                </span>

              </div>

              <p className="text-gray-500 mt-2">
                Created on{" "}
                {new Date(
                  ticket.createdAt
                ).toLocaleDateString()}
              </p>

            </div>

            <div className="flex gap-3">

              <span className="px-3 py-1.5 bg-gray-100 text-gray-700 rounded-full text-sm font-medium">
                {ticket.status}
              </span>

              <span
                className={`px-3 py-1.5 rounded-full text-sm font-medium ${
                  ticket.priority === "URGENT"
                    ? "bg-red-100 text-red-700"
                    : ticket.priority === "HIGH"
                    ? "bg-orange-100 text-orange-700"
                    : ticket.priority === "MEDIUM"
                    ? "bg-yellow-100 text-yellow-700"
                    : "bg-green-100 text-green-700"
                }`}
              >
                {ticket.priority}
              </span>

            </div>

          </div>

          <div className="mt-8 pt-6 border-t border-gray-100">

            <h2 className="text-sm font-semibold text-gray-900 mb-3">
              Description
            </h2>

            <p className="text-gray-600 whitespace-pre-wrap">
              {ticket.description}
            </p>

          </div>

        </div>

        {/* Conversation */}

        <div className="bg-white border border-gray-200 rounded-xl mt-6">

          <div className="px-6 py-5 border-b border-gray-200">

            <h2 className="text-xl font-semibold text-gray-900">
              Conversation
            </h2>

            <p className="text-sm text-gray-500 mt-1">
              Communicate with the support team about this ticket.
            </p>

          </div>

          {/* Messages */}

          <div className="p-6">

            {messagesLoading && (
              <div className="py-8 text-center">
                <p className="text-gray-500">
                  Loading conversation...
                </p>
              </div>
            )}

            {!messagesLoading &&
              messageError &&
              messages.length === 0 && (
                <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg">
                  {messageError}
                </div>
              )}

            {!messagesLoading &&
              !messageError &&
              messages.length === 0 && (
                <div className="bg-gray-50 rounded-lg p-8 text-center">
                  <h3 className="text-sm font-semibold text-gray-700">
                    No messages yet
                  </h3>

                  <p className="text-sm text-gray-500 mt-1">
                    Send a message to start the conversation.
                  </p>
                </div>
              )}

            {!messagesLoading &&
              messages.length > 0 && (
                <div className="space-y-5">

                  {messages.map((item) => {

                    const isCustomer =
                      item.senderRole === "CUSTOMER";

                    return (
                      <div
                        key={item.id}
                        className={`flex ${
                          isCustomer
                            ? "justify-end"
                            : "justify-start"
                        }`}
                      >

                        <div
                          className={`max-w-[80%] ${
                            isCustomer
                              ? "items-end"
                              : "items-start"
                          } flex flex-col`}
                        >

                          <div
                            className={`flex items-center gap-2 mb-1 ${
                              isCustomer
                                ? "flex-row-reverse"
                                : ""
                            }`}
                          >

                            <span className="text-sm font-medium text-gray-700">
                              {item.senderName}
                            </span>

                            <span className="text-xs text-gray-400">
                              {item.senderRole}
                            </span>

                          </div>

                          <div
                            className={`px-4 py-3 rounded-xl ${
                              isCustomer
                                ? "bg-blue-600 text-white rounded-br-none"
                                : "bg-gray-100 text-gray-800 rounded-bl-none"
                            }`}
                          >
                            <p className="text-sm whitespace-pre-wrap">
                              {item.content}
                            </p>
                          </div>

                          <span className="text-xs text-gray-400 mt-1">
                            {new Date(
                              item.createdAt
                            ).toLocaleString()}
                          </span>

                        </div>

                      </div>
                    );
                  })}

                </div>
              )}

          </div>

          {/* Send Message */}

          <div className="border-t border-gray-200 p-6">

            {messageError &&
              messages.length > 0 && (
                <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg text-sm mb-4">
                  {messageError}
                </div>
              )}

            <form
              onSubmit={handleSendMessage}
              className="space-y-3"
            >

              <textarea
                value={message}
                onChange={(event) =>
                  setMessage(event.target.value)
                }
                placeholder="Type your message..."
                rows={4}
                maxLength={2000}
                disabled={sendingMessage}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-blue-500 resize-none disabled:bg-gray-100"
              />

              <div className="flex items-center justify-between">

                <span className="text-xs text-gray-400">
                  {message.length}/2000
                </span>

                <button
                  type="submit"
                  disabled={
                    sendingMessage ||
                    !message.trim()
                  }
                  className="bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white font-medium px-5 py-2.5 rounded-lg transition"
                >
                  {sendingMessage
                    ? "Sending..."
                    : "Send Message"}
                </button>

              </div>

            </form>

          </div>

        </div>

      </div>

    </DashboardLayout>
  );
}

export default TicketDetails;