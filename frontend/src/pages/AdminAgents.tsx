import { useCallback, useEffect, useState } from "react";

import DashboardLayout from "../layouts/DashboardLayout";

import {
  getAllAgents,
  createAgent,
  updateAgent,
  deleteUser,
  type AdminUserResponse,
} from "../services/adminService";

function AdminAgents() {
  const [agents, setAgents] =
    useState<AdminUserResponse[]>([]);

  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] =
    useState(false);

  const [error, setError] = useState("");

  const [showCreateForm, setShowCreateForm] =
    useState(false);

  const [editingAgentId, setEditingAgentId] =
    useState<number | null>(null);

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] =
    useState("");

  const loadAgents = useCallback(async () => {
    try {
      setLoading(true);
      setError("");

      const data = await getAllAgents();

      setAgents(data);
    } catch {
      setError("Failed to load agents.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadAgents();
  }, [loadAgents]);

  const resetForm = () => {
    setName("");
    setEmail("");
    setPassword("");
    setEditingAgentId(null);
    setShowCreateForm(false);
  };

  const handleCreate = async (
    event: React.FormEvent
  ) => {
    event.preventDefault();

    try {
      setActionLoading(true);
      setError("");

      await createAgent({
        name: name.trim(),
        email: email.trim(),
        password,
      });

      resetForm();
      await loadAgents();
    } catch {
      setError("Failed to create agent.");
    } finally {
      setActionLoading(false);
    }
  };

  const handleEdit = (
    agent: AdminUserResponse
  ) => {
    setEditingAgentId(agent.id);
    setName(agent.name);
    setEmail(agent.email);
    setPassword("");
    setShowCreateForm(true);
  };

  const handleUpdate = async (
    event: React.FormEvent
  ) => {
    event.preventDefault();

    if (!editingAgentId) {
      return;
    }

    try {
      setActionLoading(true);
      setError("");

      await updateAgent(
        editingAgentId,
        {
          name: name.trim(),
          email: email.trim(),
        }
      );

      resetForm();
      await loadAgents();
    } catch {
      setError("Failed to update agent.");
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelete = async (
    agent: AdminUserResponse
  ) => {
    const confirmed = window.confirm(
      `Are you sure you want to delete agent "${agent.name}"?`
    );

    if (!confirmed) {
      return;
    }

    try {
      setActionLoading(true);
      setError("");

      await deleteUser(agent.id);

      await loadAgents();
    } catch {
      setError("Failed to delete agent.");
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <DashboardLayout>
      <div className="space-y-6">

        <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">

          <div>
            <h1 className="text-3xl font-bold text-gray-900">
              Agents
            </h1>

            <p className="mt-1 text-gray-500">
              Manage your support agents.
            </p>
          </div>

          <button
            onClick={() => {
              resetForm();
              setShowCreateForm(true);
            }}
            className="rounded-lg bg-gray-900 px-4 py-2 text-sm font-medium text-white hover:bg-gray-800"
          >
            Create Agent
          </button>

        </div>

        {error && (
          <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        {showCreateForm && (
          <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">

            <h2 className="text-lg font-semibold text-gray-900">
              {editingAgentId
                ? "Edit Agent"
                : "Create Agent"}
            </h2>

            <form
              onSubmit={
                editingAgentId
                  ? handleUpdate
                  : handleCreate
              }
              className="mt-6 space-y-4"
            >

              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700">
                  Name
                </label>

                <input
                  type="text"
                  value={name}
                  onChange={(event) =>
                    setName(event.target.value)
                  }
                  required
                  className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"
                />
              </div>

              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700">
                  Email
                </label>

                <input
                  type="email"
                  value={email}
                  onChange={(event) =>
                    setEmail(event.target.value)
                  }
                  required
                  className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"
                />
              </div>

              {!editingAgentId && (
                <div>
                  <label className="mb-1 block text-sm font-medium text-gray-700">
                    Password
                  </label>

                  <input
                    type="password"
                    value={password}
                    onChange={(event) =>
                      setPassword(
                        event.target.value
                      )
                    }
                    minLength={8}
                    required
                    className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"
                  />

                  <p className="mt-1 text-xs text-gray-500">
                    Password must contain at least
                    8 characters.
                  </p>
                </div>
              )}

              <div className="flex gap-3">

                <button
                  type="submit"
                  disabled={actionLoading}
                  className="rounded-lg bg-gray-900 px-4 py-2 text-sm font-medium text-white hover:bg-gray-800 disabled:opacity-50"
                >
                  {actionLoading
                    ? "Saving..."
                    : editingAgentId
                    ? "Update Agent"
                    : "Create Agent"}
                </button>

                <button
                  type="button"
                  onClick={resetForm}
                  className="rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>

              </div>

            </form>

          </div>
        )}

        <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">

          {loading ? (
            <div className="p-10 text-center text-gray-500">
              Loading agents...
            </div>
          ) : agents.length === 0 ? (
            <div className="p-10 text-center text-gray-500">
              No agents found.
            </div>
          ) : (
            <div className="overflow-x-auto">

              <table className="min-w-full divide-y divide-gray-200">

                <thead className="bg-gray-50">

                  <tr>

                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                      ID
                    </th>

                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                      Name
                    </th>

                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                      Email
                    </th>

                    <th className="px-6 py-3 text-left text-xs font-semibold uppercase tracking-wider text-gray-500">
                      Role
                    </th>

                    <th className="px-6 py-3 text-right text-xs font-semibold uppercase tracking-wider text-gray-500">
                      Actions
                    </th>

                  </tr>

                </thead>

                <tbody className="divide-y divide-gray-200">

                  {agents.map((agent) => (
                    <tr
                      key={agent.id}
                      className="hover:bg-gray-50"
                    >

                      <td className="px-6 py-4 text-sm text-gray-700">
                        {agent.id}
                      </td>

                      <td className="px-6 py-4 text-sm font-medium text-gray-900">
                        {agent.name}
                      </td>

                      <td className="px-6 py-4 text-sm text-gray-600">
                        {agent.email}
                      </td>

                      <td className="px-6 py-4">

                        <span className="rounded-full bg-blue-100 px-2.5 py-1 text-xs font-medium text-blue-700">
                          {agent.role}
                        </span>

                      </td>

                      <td className="px-6 py-4 text-right">

                        <div className="flex justify-end gap-3">

                          <button
                            onClick={() =>
                              handleEdit(agent)
                            }
                            className="text-sm font-medium text-blue-600 hover:text-blue-800"
                          >
                            Edit
                          </button>

                          <button
                            onClick={() =>
                              handleDelete(agent)
                            }
                            disabled={actionLoading}
                            className="text-sm font-medium text-red-600 hover:text-red-800 disabled:opacity-50"
                          >
                            Delete
                          </button>

                        </div>

                      </td>

                    </tr>
                  ))}

                </tbody>

              </table>

            </div>
          )}

        </div>

      </div>
    </DashboardLayout>
  );
}

export default AdminAgents;