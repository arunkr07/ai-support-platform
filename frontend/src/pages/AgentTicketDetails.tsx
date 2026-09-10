import { type FormEvent, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import DashboardLayout from "../layouts/DashboardLayout";

import {
  getAgentTicketById,
  getAgentMessages,
  sendAgentMessage,
  updateTicketStatus,
  releaseTicket,
  generateSuggestedReply,
  getTicketAiAnalysis,
  type AgentTicketStatus,
  type SuggestedReplyResponse,
  type TicketAiAnalysisResponse,
} from "../services/agentService";

import type {
    TicketMessageResponse,
    TicketResponse,
} from "../services/ticketService";

function AgentTicketDetails() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [ticket, setTicket] =
    useState<TicketResponse | null>(null);

  const [messages, setMessages] =
    useState<TicketMessageResponse[]>([]);

  const [loading, setLoading] = useState(true);
  const [messagesLoading, setMessagesLoading] =
    useState(true);

  const [error, setError] = useState("");
  const [messageError, setMessageError] =
    useState("");

  const [message, setMessage] = useState("");
  const [sendingMessage, setSendingMessage] =
    useState(false);

  const [statusUpdating, setStatusUpdating] =
    useState(false);

  const [releasing, setReleasing] =
    useState(false);


  const [aiAnalysis, setAiAnalysis] =
    useState<TicketAiAnalysisResponse | null>(
      null
    );

  const [suggestedReply, setSuggestedReply] =
    useState<SuggestedReplyResponse | null>(
      null
    );

  const [analysisLoading, setAnalysisLoading] =
    useState(false);

  const [replyLoading, setReplyLoading] =
    useState(false);

  const [aiError, setAiError] = useState("");

  useEffect(() => {
    const loadTicket = async () => {
      if (!id) {
        setError("Invalid ticket ID.");
        setLoading(false);
        return;
      }

      try {
        setError("");

        const data =
          await getAgentTicketById(Number(id));

        setTicket(data);
      } catch (error) {
        console.error(error);

        setError(
          "Failed to load ticket. Make sure this ticket is assigned to you."
        );
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

        const data =
          await getAgentMessages(Number(id));

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

    const trimmedMessage =
      message.trim();

    if (!trimmedMessage) {
      return;
    }

    try {
      setSendingMessage(true);
      setMessageError("");

      const newMessage =
        await sendAgentMessage(
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


  const handleStatusChange = async (
    newStatus: AgentTicketStatus
  ) => {
    if (!ticket) {
      return;
    }

    try {
      setStatusUpdating(true);
      setError("");

      const updatedTicket =
        await updateTicketStatus(
          ticket.id,
          newStatus
        );

      setTicket(updatedTicket);
    } catch (error) {
      console.error(error);

      setError(
        "Failed to update ticket status."
      );
    } finally {
      setStatusUpdating(false);
    }
  };


  const handleRelease = async () => {
    if (!ticket) {
      return;
    }

    const confirmed =
      window.confirm(
        "Are you sure you want to release this ticket?"
      );

    if (!confirmed) {
      return;
    }

    try {
      setReleasing(true);
      setError("");

      await releaseTicket(ticket.id);

      navigate("/agent");
    } catch (error) {
      console.error(error);

      setError(
        "Failed to release ticket."
      );
    } finally {
      setReleasing(false);
    }
  };


  const handleGenerateAnalysis =
    async () => {
      if (!ticket) {
        return;
      }

      try {
        setAnalysisLoading(true);
        setAiError("");

        const analysis =
          await getTicketAiAnalysis(
            ticket.id
          );

        setAiAnalysis(analysis);
      } catch (error) {
        console.error(error);

        setAiError(
          "Failed to generate AI analysis."
        );
      } finally {
        setAnalysisLoading(false);
      }
    };


  const handleGenerateSuggestedReply =
    async () => {
      if (!ticket) {
        return;
      }

      try {
        setReplyLoading(true);
        setAiError("");

        const reply =
          await generateSuggestedReply(
            ticket.id
          );

        setSuggestedReply(reply);
      } catch (error) {
        console.error(error);

        setAiError(
          "Failed to generate suggested reply."
        );
      } finally {
        setReplyLoading(false);
      }
    };


  const handleUseSuggestedReply =
    () => {
      if (!suggestedReply) {
        return;
      }

      setMessage(
        suggestedReply.suggestedReply
      );
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
            onClick={() =>
              navigate("/agent")
            }
            className="text-sm text-blue-600 hover:text-blue-700 mb-6"
          >
            ← Back to Agent Dashboard
          </button>

          <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg">
            {error ||
              "Ticket not found."}
          </div>
        </div>
      </DashboardLayout>
    );
  }

  const isClosed =
    ticket.status === "CLOSED";

  return (
    <DashboardLayout>
      <div>


        <button
          onClick={() =>
            navigate("/agent")
          }
          className="text-sm text-blue-600 hover:text-blue-700 mb-6"
        >
          ← Back to Agent Dashboard
        </button>


        {error && (
          <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg mb-6">
            {error}
          </div>
        )}


        <div className="bg-white border border-gray-200 rounded-xl p-6">

          <div className="flex flex-col xl:flex-row xl:items-start xl:justify-between gap-6">

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
                Customer:{" "}
                <span className="font-medium text-gray-700">
                  {ticket.customerName}
                </span>
              </p>

              <p className="text-sm text-gray-500 mt-1">
                {ticket.customerEmail}
              </p>

              <p className="text-sm text-gray-400 mt-2">
                Created{" "}
                {new Date(
                  ticket.createdAt
                ).toLocaleString()}
              </p>

            </div>

            <div className="flex flex-wrap gap-3">

              <span className="px-3 py-1.5 bg-gray-100 text-gray-700 rounded-full text-sm font-medium">
                {ticket.status}
              </span>

              <span
                className={`px-3 py-1.5 rounded-full text-sm font-medium ${getPriorityClass(
                  ticket.priority
                )}`}
              >
                {ticket.priority}
              </span>

            </div>

          </div>


          <div className="mt-8 pt-6 border-t border-gray-100">

            <h2 className="text-sm font-semibold text-gray-900 mb-3">
              Customer Issue
            </h2>

            <p className="text-gray-600 whitespace-pre-wrap">
              {ticket.description}
            </p>

          </div>


          <div className="mt-8 pt-6 border-t border-gray-100">

            <div className="flex flex-col lg:flex-row lg:items-end gap-4">

              <div>

                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Update Status
                </label>

                <select
                  value={ticket.status}
                  disabled={
                    statusUpdating ||
                    isClosed
                  }
                  onChange={(event) =>
                    handleStatusChange(
                      event.target.value as AgentTicketStatus
                    )
                  }
                  className="px-4 py-3 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
                >
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

              <button
                onClick={handleRelease}
                disabled={
                  releasing ||
                  isClosed
                }
                className="px-5 py-3 border border-red-300 text-red-600 hover:bg-red-50 disabled:bg-gray-100 disabled:text-gray-400 rounded-lg font-medium transition"
              >
                {releasing
                  ? "Releasing..."
                  : "Release Ticket"}
              </button>

            </div>

            {isClosed && (
              <p className="text-sm text-gray-500 mt-3">
                This ticket is closed and cannot be modified.
              </p>
            )}

          </div>

        </div>


        <div className="bg-white border border-gray-200 rounded-xl mt-6">

          <div className="px-6 py-5 border-b border-gray-200">

            <div className="flex items-center justify-between gap-4">

              <div>

                <h2 className="text-xl font-semibold text-gray-900">
                  AI Assistance
                </h2>

                <p className="text-sm text-gray-500 mt-1">
                  Use AI to analyze this ticket and assist with your response.
                </p>

              </div>

              <div className="text-2xl">
                🤖
              </div>

            </div>

          </div>

          <div className="p-6 space-y-6">


            {aiError && (
              <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg">
                {aiError}
              </div>
            )}


            <div className="flex flex-col sm:flex-row gap-3">

              <button
                onClick={
                  handleGenerateAnalysis
                }
                disabled={
                  analysisLoading
                }
                className="px-5 py-3 bg-purple-600 hover:bg-purple-700 disabled:bg-purple-300 text-white font-medium rounded-lg transition"
              >
                {analysisLoading
                  ? "Analyzing..."
                  : "Analyze Ticket"}
              </button>

              <button
                onClick={
                  handleGenerateSuggestedReply
                }
                disabled={
                  replyLoading ||
                  isClosed
                }
                className="px-5 py-3 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white font-medium rounded-lg transition"
              >
                {replyLoading
                  ? "Generating..."
                  : "Generate Suggested Reply"}
              </button>

            </div>


            {aiAnalysis && (
              <div className="border border-gray-200 rounded-xl overflow-hidden">

                <div className="bg-gray-50 px-5 py-4 border-b border-gray-200">

                  <h3 className="font-semibold text-gray-900">
                    Ticket Analysis
                  </h3>

                </div>

                <div className="p-5 space-y-5">

                  <div>

                    <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide">
                      Summary
                    </p>

                    <p className="text-gray-700 mt-2 whitespace-pre-wrap">
                      {aiAnalysis.summary}
                    </p>

                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">

                    <div className="bg-gray-50 rounded-lg p-4">

                      <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide">
                        Category
                      </p>

                      <p className="font-medium text-gray-900 mt-1">
                        {aiAnalysis.category}
                      </p>

                    </div>

                    <div className="bg-gray-50 rounded-lg p-4">

                      <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide">
                        AI Suggested Priority
                      </p>

                      <p className="font-medium text-gray-900 mt-1">
                        {aiAnalysis.suggestedPriority}
                      </p>

                    </div>

                  </div>

                  <p className="text-xs text-gray-400">
                    Generated{" "}
                    {new Date(
                      aiAnalysis.createdAt
                    ).toLocaleString()}
                  </p>

                </div>

              </div>
            )}

            {suggestedReply && (
              <div className="border border-blue-200 rounded-xl overflow-hidden">

                <div className="bg-blue-50 px-5 py-4 border-b border-blue-200">

                  <h3 className="font-semibold text-blue-900">
                    Suggested Reply
                  </h3>

                </div>

                <div className="p-5">

                  <p className="text-gray-700 whitespace-pre-wrap leading-relaxed">
                    {suggestedReply.suggestedReply}
                  </p>

                  <div className="mt-5 flex justify-end">

                    <button
                      onClick={
                        handleUseSuggestedReply
                      }
                      disabled={isClosed}
                      className="px-5 py-2.5 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white font-medium rounded-lg transition"
                    >
                      Use This Reply
                    </button>

                  </div>

                </div>

              </div>
            )}

          </div>

        </div>


        <div className="bg-white border border-gray-200 rounded-xl mt-6">

          <div className="px-6 py-5 border-b border-gray-200">

            <h2 className="text-xl font-semibold text-gray-900">
              Customer Conversation
            </h2>

            <p className="text-sm text-gray-500 mt-1">
              Communicate with the customer about this ticket.
            </p>

          </div>

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

                    const isAgent =
                      item.senderRole ===
                      "AGENT";

                    return (
                      <div
                        key={item.id}
                        className={`flex ${
                          isAgent
                            ? "justify-end"
                            : "justify-start"
                        }`}
                      >

                        <div
                          className={`max-w-[80%] ${
                            isAgent
                              ? "items-end"
                              : "items-start"
                          } flex flex-col`}
                        >

                          <div
                            className={`flex items-center gap-2 mb-1 ${
                              isAgent
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
                              isAgent
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


          <div className="border-t border-gray-200 p-6">

            {messageError &&
              messages.length > 0 && (
                <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-lg text-sm mb-4">
                  {messageError}
                </div>
              )}

            <form
              onSubmit={
                handleSendMessage
              }
              className="space-y-3"
            >

              <textarea
                value={message}
                onChange={(event) =>
                  setMessage(
                    event.target.value
                  )
                }
                placeholder={
                  isClosed
                    ? "Closed tickets cannot receive messages."
                    : "Type your reply to the customer..."
                }
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
                    isClosed ||
                    !message.trim()
                  }
                  className="bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white font-medium px-5 py-2.5 rounded-lg transition"
                >
                  {sendingMessage
                    ? "Sending..."
                    : "Send Reply"}
                </button>

              </div>

            </form>

          </div>

        </div>

      </div>
    </DashboardLayout>
  );
}

export default AgentTicketDetails;