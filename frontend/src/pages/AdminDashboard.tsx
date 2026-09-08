import DashboardLayout from "../layouts/DashboardLayout";

function AdminDashboard() {
  return (
    <DashboardLayout>

      <div>
        <h1 className="text-3xl font-bold text-gray-900">
          Admin Dashboard
        </h1>

        <p className="text-gray-500 mt-2">
          Monitor users, agents, tickets, and platform activity.
        </p>

        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mt-8">

          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <p className="text-sm text-gray-500">
              Total Users
            </p>

            <p className="text-3xl font-bold text-gray-900 mt-2">
              0
            </p>
          </div>

          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <p className="text-sm text-gray-500">
              Active Agents
            </p>

            <p className="text-3xl font-bold text-gray-900 mt-2">
              0
            </p>
          </div>

          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <p className="text-sm text-gray-500">
              Open Tickets
            </p>

            <p className="text-3xl font-bold text-gray-900 mt-2">
              0
            </p>
          </div>

          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <p className="text-sm text-gray-500">
              Resolved
            </p>

            <p className="text-3xl font-bold text-gray-900 mt-2">
              0
            </p>
          </div>

        </div>

      </div>

    </DashboardLayout>
  );
}

export default AdminDashboard;