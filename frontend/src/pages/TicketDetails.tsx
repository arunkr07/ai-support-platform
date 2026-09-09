import { type FormEvent, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import DashboardLayout from "../layouts/DashboardLayout";

import {
  getTicketById,
  getTicketMessages,
  sendTicketMessage,
  updateTicket,
  reopenTicket,
  type TicketMessageResponse,
  type TicketResponse,
  type TicketPriority,
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

  /* ============================= */
  /* EDIT TICKET                   */
  /* ============================= */

  const [editing, setEditing] = useState(false);

  const [editTitle, setEditTitle] = useState("");
  const [editDescription, setEditDescription] =
    useState("");

  const [editPriority, setEditPriority] =
    useState<TicketPriority>("MEDIUM");

  const [updatingTicket, setUpdatingTicket] =
    useState(false);

  const [updateError, setUpdateError] = useState("");

  /* ============================= */
  /* REOPEN TICKET                 */
  /* ============================= */

  const [reopeningTicket, setReopeningTicket] =
    useState(false);

  const [reopenError, setReopenError] = useState("");

  /* ============================= */
  /* LOAD TICKET                   */
  /* ============================= */

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

  /* ============================= */
  /* LOAD MESSAGES                 */
  /* ============================= */

  useEffect(() => {
    const loadMessages = async () => {
      if (!id) {
        setMessagesLoading(false);
        return;
      }

      try {
        setMessageError("");

        const data =
          await getTicketMessages(Number(id));

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

  /* ============================= */
  /* START EDITING                 */
  /* ============================= */

  const handleStartEditing = () => {
    if (!ticket) {
      return;
    }

    setEditTitle(ticket.title);
    setEditDescription(ticket.description);
    setEditPriority(
      ticket.priority as TicketPriority
    );

    setUpdateError("");
    setEditing(true);
  };

  /* ============================= */
  /* CANCEL EDITING                */
  /* ============================= */

  const handleCancelEditing = () => {
    setEditing(false);
    setUpdateError("");
  };

  /* ============================= */
  /* UPDATE TICKET                 */
  /* ============================= */

  const handleUpdateTicket = async (
    event: FormEvent<HTMLFormElement>
  ) => {
    event.preventDefault();

    if (!ticket) {
      return;
    }

    const title = editTitle.trim();
    const description = editDescription.trim();

    if (!title) {
      setUpdateError("Ticket title is required.");
      return;
    }

    if (!description) {
      setUpdateError(
        "Ticket description is required."
      );
      return;
    }

    try {
      setUpdatingTicket(true);
      setUpdateError("");

      const updatedTicket =
        await updateTicket(ticket.id, {
          title,
          description,
          priority: editPriority,
        });

      setTicket(updatedTicket);

      setEditing(false);
    } catch (error) {
      console.error(error);

      setUpdateError(
        "Failed to update ticket. Please try again."
      );
    } finally {
      setUpdatingTicket(false);
    }
  };

  /* ============================= */
  /* REOPEN TICKET                 */
  /* ============================= */

  const handleReopenTicket = async () => {
    if (!ticket) {
      return;
    }

    try {
      setReopeningTicket(true);
      setReopenError("");

      const updatedTicket =
        await reopenTicket(ticket.id);

      setTicket(updatedTicket);
    } catch (error) {
      console.error(error);

      setReopenError(
        "Failed to reopen ticket. Please try again."
      );
    } finally {
      setReopeningTicket(false);
    }
  };

  /* ============================= */
  /* SEND MESSAGE                  */
  /* ============================= */

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

      const newMessage =
        await sendTicketMessage(
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

  /* ============================= */
  /* LOADING                       */
  /* ============================= */

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

  /* ============================= */
  /* ERROR                         */
  /* ============================= */

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

  const isResolved =
    ticket.status === "RESOLVED";

  const isClosed =
    ticket.status === "CLOSED";

  return (
    <DashboardLayout>

      <div>

        {/* ============================= */}
        {/* BACK BUTTON                   */}
        {/* ============================= */}

        <button
          onClick={() => navigate("/customer")}
          className="text-sm text-blue-600 hover:text-blue-700 mb-6"
        >
          ← Back to My Tickets
        </button>

        {/* ============================= */}
        {/* TICKET DETAILS                */}
        {/* ============================= */}

        <div className="bg-white border border-gray-200 rounded-xl p-6">

          {!editing ? (
            <>
              {/* Header */}

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

                <div className="flex gap-3 flex-wrap">

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

              {/* Description */}

              <div className="mt-8 pt-6 border-t border-gray-100">

                <h2 className="text-sm font-semibold text-gray-900 mb-3">
                  Description
                </h2>

                <p className="text-gray-600 whitespace-pre-wrap">
                  {ticket.description}
                </p>

              </div>

              {/* Actions */}

              <div className="mt-6 pt-6 border-t border-gray-100 flex flex-wrap gap-3">

                {/* Edit */}

                {!isClosed && (
                  <button
                    onClick={handleStartEditing}
                    className="px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition"
                  >
                    Edit Ticket
                  </button>
                )}

                {/* Reopen */}

                {isResolved && (
                  <button
                    onClick={handleReopenTicket}
                    disabled={reopeningTicket}
                    className="px-5 py-2.5 bg-green-600 hover:bg-green-700 disabled:bg-green-300 text-white font-medium rounded-lg transition"
                  >
                    {reopeningTicket
                      ? "Reopening..."
                      : "Reopen Ticket"}
                  </button>
                )}

              </div>

              {/* Reopen Error */}

              {reopenError && (
                <div className="mt-4 bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg text-sm">
                  {reopenError}
                </div>
              )}

            </>
          ) : (

            /* ============================= */
            /* EDIT FORM                      */
            /* ============================= */

            <form
              onSubmit={handleUpdateTicket}
              className="space-y-6"
            >

              <div className="flex items-center justify-between">

                <div>
                  <h1 className="text-2xl font-bold text-gray-900">
                    Edit Ticket
                  </h1>

                  <p className="text-sm text-gray-500 mt-1">
                    Update the details of your support request.
                  </p>
                </div>

                <span className="text-sm text-gray-400">
                  #{ticket.id}
                </span>

              </div>

              {/* Update Error */}

              {updateError && (
                <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg text-sm">
                  {updateError}
                </div>
              )}

              {/* Title */}

              <div>

                <label
                  htmlFor="edit-title"
                  className="block text-sm font-medium text-gray-700 mb-2"
                >
                  Title
                </label>

                <input
                  id="edit-title"
                  type="text"
                  value={editTitle}
                  onChange={(event) =>
                    setEditTitle(event.target.value)
                  }
                  maxLength={255}
                  disabled={updatingTicket}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
                />

              </div>

              {/* Description */}

              <div>

                <label
                  htmlFor="edit-description"
                  className="block text-sm font-medium text-gray-700 mb-2"
                >
                  Description
                </label>

                <textarea
                  id="edit-description"
                  value={editDescription}
                  onChange={(event) =>
                    setEditDescription(
                      event.target.value
                    )
                  }
                  rows={6}
                  maxLength={5000}
                  disabled={updatingTicket}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-blue-500 resize-none disabled:bg-gray-100"
                />

                <p className="text-xs text-gray-400 mt-1">
                  {editDescription.length}/5000
                </p>

              </div>

              {/* Priority */}

              <div>

                <label
                  htmlFor="edit-priority"
                  className="block text-sm font-medium text-gray-700 mb-2"
                >
                  Priority
                </label>

                <select
                  id="edit-priority"
                  value={editPriority}
                  onChange={(event) =>
                    setEditPriority(
                      event.target.value as TicketPriority
                    )
                  }
                  disabled={updatingTicket}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
                >
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

              {/* Buttons */}

              <div className="flex justify-end gap-3">

                <button
                  type="button"
                  onClick={handleCancelEditing}
                  disabled={updatingTicket}
                  className="px-5 py-2.5 border border-gray-300 text-gray-700 hover:bg-gray-50 disabled:bg-gray-100 font-medium rounded-lg transition"
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  disabled={updatingTicket}
                  className="px-5 py-2.5 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white font-medium rounded-lg transition"
                >
                  {updatingTicket
                    ? "Saving..."
                    : "Save Changes"}
                </button>

              </div>

            </form>
          )}

        </div>

        {/* ============================= */}
        {/* CONVERSATION                  */}
        {/* ============================= */}

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
                disabled={
                  sendingMessage ||
                  isClosed
                }
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
                    !message.trim() ||
                    isClosed
                  }
                  className="bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white font-medium px-5 py-2.5 rounded-lg transition"
                >
                  {sendingMessage
                    ? "Sending..."
                    : "Send Message"}
                </button>

              </div>

            </form>

            {isClosed && (
              <p className="text-sm text-gray-500 mt-3">
                This ticket is closed and can no longer receive messages.
              </p>
            )}

          </div>

        </div>

      </div>

    </DashboardLayout>
  );
}

export default TicketDetails;