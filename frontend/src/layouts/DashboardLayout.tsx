import type { ReactNode } from "react";
import { useNavigate, useLocation } from "react-router-dom";

interface DashboardLayoutProps {
  children: ReactNode;
}

function DashboardLayout({
  children,
}: DashboardLayoutProps) {
  const navigate = useNavigate();
  const location = useLocation();

  const userData =
    localStorage.getItem("user");

  const user = userData
    ? JSON.parse(userData)
    : null;

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");

    navigate("/login");
  };

  const isAdmin = user?.role === "ADMIN";

  const isActive = (path: string) =>
    location.pathname === path ||
    location.pathname.startsWith(
      `${path}/`
    );

  return (
    <div className="flex min-h-screen bg-gray-50">

      {/* Sidebar */}

      <aside className="flex w-64 flex-col border-r border-gray-200 bg-white">

        <div className="flex h-16 items-center border-b border-gray-200 px-6">

          <h1 className="text-xl font-bold text-gray-900">
            AI Support
          </h1>

        </div>

        <nav className="flex-1 space-y-2 p-4">

          <button
            onClick={() => {

              if (user?.role === "CUSTOMER") {
                navigate("/customer");
              }

              if (user?.role === "AGENT") {
                navigate("/agent");
              }

              if (user?.role === "ADMIN") {
                navigate("/admin");
              }

            }}
            className={`w-full rounded-lg px-4 py-3 text-left text-sm font-medium transition ${
              isActive(
                user?.role === "ADMIN"
                  ? "/admin"
                  : user?.role === "AGENT"
                  ? "/agent"
                  : "/customer"
              )
                ? "bg-gray-900 text-white"
                : "text-gray-700 hover:bg-gray-100"
            }`}
          >
            Dashboard
          </button>

          {isAdmin ? (
            <>
              <button
                onClick={() =>
                  navigate("/admin")
                }
                className={`w-full rounded-lg px-4 py-3 text-left text-sm font-medium transition ${
                  isActive("/admin/tickets")
                    ? "bg-gray-100 text-gray-900"
                    : "text-gray-700 hover:bg-gray-100"
                }`}
              >
                Tickets
              </button>

              <button
                onClick={() =>
                  navigate("/admin/agents")
                }
                className={`w-full rounded-lg px-4 py-3 text-left text-sm font-medium transition ${
                  isActive("/admin/agents")
                    ? "bg-gray-100 text-gray-900"
                    : "text-gray-700 hover:bg-gray-100"
                }`}
              >
                Agents
              </button>

              <button
                onClick={() =>
                  navigate("/admin/customers")
                }
                className={`w-full rounded-lg px-4 py-3 text-left text-sm font-medium transition ${
                  isActive(
                    "/admin/customers"
                  )
                    ? "bg-gray-100 text-gray-900"
                    : "text-gray-700 hover:bg-gray-100"
                }`}
              >
                Customers
              </button>
            </>
          ) : (
            <>
              <button
                className="w-full rounded-lg px-4 py-3 text-left text-sm font-medium text-gray-700 hover:bg-gray-100"
              >
                Tickets
              </button>

              <button
                className="w-full rounded-lg px-4 py-3 text-left text-sm font-medium text-gray-700 hover:bg-gray-100"
              >
                Messages
              </button>
            </>
          )}

        </nav>

        {/* User */}

        <div className="border-t border-gray-200 p-4">

          <div className="mb-3">

            <p className="font-medium text-gray-900">
              {user?.name || "User"}
            </p>

            <p className="text-sm text-gray-500">
              {user?.role || ""}
            </p>

          </div>

          <button
            onClick={handleLogout}
            className="w-full rounded-lg px-4 py-2 text-sm text-red-600 transition hover:bg-red-50"
          >
            Logout
          </button>

        </div>

      </aside>

      {/* Main */}

      <main className="flex-1">

        <header className="flex h-16 items-center justify-between border-b border-gray-200 bg-white px-8">

          <div>
            <h2 className="text-lg font-semibold text-gray-900">
              Support Center
            </h2>
          </div>

          <div className="text-sm text-gray-500">
            {user?.email}
          </div>

        </header>

        <section className="p-8">
          {children}
        </section>

      </main>

    </div>
  );
}

export default DashboardLayout;