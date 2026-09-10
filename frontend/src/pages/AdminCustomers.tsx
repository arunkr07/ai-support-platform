import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import DashboardLayout from "../layouts/DashboardLayout";

import {
  getAllCustomers,
  deleteUser,
  type AdminUserResponse,
} from "../services/adminService";

function AdminCustomers() {
  const navigate = useNavigate();

  const [customers, setCustomers] =
    useState<AdminUserResponse[]>([]);

  const [loading, setLoading] =
    useState(true);

  const [actionLoading, setActionLoading] =
    useState(false);

  const [error, setError] = useState("");

  const loadCustomers =
    useCallback(async () => {
      try {
        setLoading(true);
        setError("");

        const data =
          await getAllCustomers();

        setCustomers(data);
      } catch {
        setError(
          "Failed to load customers."
        );
      } finally {
        setLoading(false);
      }
    }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadCustomers();
  }, [loadCustomers]);

  const handleDelete = async (
    customer: AdminUserResponse
  ) => {
    const confirmed = window.confirm(
      `Are you sure you want to delete customer "${customer.name}"?`
    );

    if (!confirmed) {
      return;
    }

    try {
      setActionLoading(true);
      setError("");

      await deleteUser(customer.id);

      await loadCustomers();
    } catch {
      setError(
        "Failed to delete customer. The customer may still have tickets or message history."
      );
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <DashboardLayout>
      <div className="space-y-6">

        <div>
          <h1 className="text-3xl font-bold text-gray-900">
            Customers
          </h1>

          <p className="mt-1 text-gray-500">
            View and manage registered customers.
          </p>
        </div>

        {error && (
          <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">

          {loading ? (
            <div className="p-10 text-center text-gray-500">
              Loading customers...
            </div>
          ) : customers.length === 0 ? (
            <div className="p-10 text-center text-gray-500">
              No customers found.
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

                  {customers.map((customer) => (
                    <tr
                      key={customer.id}
                      className="hover:bg-gray-50"
                    >

                      <td className="px-6 py-4 text-sm text-gray-700">
                        {customer.id}
                      </td>

                      <td className="px-6 py-4 text-sm font-medium text-gray-900">
                        {customer.name}
                      </td>

                      <td className="px-6 py-4 text-sm text-gray-600">
                        {customer.email}
                      </td>

                      <td className="px-6 py-4">

                        <span className="rounded-full bg-gray-100 px-2.5 py-1 text-xs font-medium text-gray-700">
                          {customer.role}
                        </span>

                      </td>

                      <td className="px-6 py-4 text-right">

                        <div className="flex justify-end gap-3">

                          <button
                            onClick={() =>
                              navigate(
                                `/admin/customers/${customer.id}`
                              )
                            }
                            className="text-sm font-medium text-blue-600 hover:text-blue-800"
                          >
                            View
                          </button>

                          <button
                            onClick={() =>
                              handleDelete(customer)
                            }
                            disabled={actionLoading}
                            className="text-sm font-medium text-red-600 hover:text-red-800 disabled:cursor-not-allowed disabled:opacity-50"
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

export default AdminCustomers;