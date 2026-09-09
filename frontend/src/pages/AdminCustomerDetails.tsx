import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import DashboardLayout from "../layouts/DashboardLayout";

import {
  getUserById,
  type AdminUserResponse,
} from "../services/adminService";

function AdminCustomerDetails() {
  const { id } = useParams();
  const navigate = useNavigate();

  const customerId = Number(id);

  const [customer, setCustomer] =
    useState<AdminUserResponse | null>(null);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] = useState("");

  const loadCustomer = useCallback(async () => {
    try {
      setLoading(true);
      setError("");

      const data =
        await getUserById(customerId);

      setCustomer(data);
    } catch {
      setError(
        "Failed to load customer."
      );
    } finally {
      setLoading(false);
    }
  }, [customerId]);

  useEffect(() => {
    if (customerId) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      loadCustomer();
    }
  }, [customerId, loadCustomer]);

  if (loading) {
    return (
      <DashboardLayout>
        <div className="flex min-h-[400px] items-center justify-center">
          <p className="text-gray-500">
            Loading customer...
          </p>
        </div>
      </DashboardLayout>
    );
  }

  if (!customer) {
    return (
      <DashboardLayout>
        <div className="space-y-4">

          <button
            onClick={() =>
              navigate("/admin/customers")
            }
            className="text-sm font-medium text-gray-600 hover:text-gray-900"
          >
            ← Back to Customers
          </button>

          <div className="rounded-lg border border-red-200 bg-red-50 p-6 text-red-700">
            {error || "Customer not found."}
          </div>

        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">

        <button
          onClick={() =>
            navigate("/admin/customers")
          }
          className="text-sm font-medium text-gray-600 hover:text-gray-900"
        >
          ← Back to Customers
        </button>

        {error && (
          <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        <div>

          <p className="text-sm font-medium text-gray-500">
            Customer #{customer.id}
          </p>

          <h1 className="mt-1 text-3xl font-bold text-gray-900">
            {customer.name}
          </h1>

        </div>

        <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">

          <h2 className="text-lg font-semibold text-gray-900">
            Customer Information
          </h2>

          <div className="mt-6 grid grid-cols-1 gap-6 md:grid-cols-3">

            <div>
              <p className="text-sm text-gray-500">
                ID
              </p>

              <p className="mt-1 font-medium text-gray-900">
                {customer.id}
              </p>
            </div>

            <div>
              <p className="text-sm text-gray-500">
                Name
              </p>

              <p className="mt-1 font-medium text-gray-900">
                {customer.name}
              </p>
            </div>

            <div>
              <p className="text-sm text-gray-500">
                Email
              </p>

              <p className="mt-1 text-sm text-gray-700">
                {customer.email}
              </p>
            </div>

          </div>

        </div>

      </div>
    </DashboardLayout>
  );
}

export default AdminCustomerDetails;