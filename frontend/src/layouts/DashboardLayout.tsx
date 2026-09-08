import type { ReactNode } from "react";
import { useNavigate } from "react-router-dom";

interface DashboardLayoutProps {
  children: ReactNode;
}

function DashboardLayout({ children }: DashboardLayoutProps) {
  const navigate = useNavigate();

  const userData = localStorage.getItem("user");
  const user = userData ? JSON.parse(userData) : null;

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");

    navigate("/login");
  };

  return (
    <div className="min-h-screen bg-gray-50 flex">

      {/* Sidebar */}
      <aside className="w-64 bg-white border-r border-gray-200 flex flex-col">

        {/* Logo */}
        <div className="h-16 flex items-center px-6 border-b border-gray-200">
          <h1 className="text-xl font-bold text-gray-900">
            AI Support
          </h1>
        </div>

        {/* Navigation */}
        <nav className="flex-1 p-4 space-y-2">

          <button
            onClick={() => {
              if (user?.role === "CUSTOMER") {
                navigate("/customer");
              } else if (user?.role === "AGENT") {
                navigate("/agent");
              } else if (user?.role === "ADMIN") {
                navigate("/admin");
              }
            }}
            className="w-full text-left px-4 py-3 rounded-lg text-gray-700 hover:bg-gray-100 transition"
          >
            Dashboard
          </button>

          <button
            className="w-full text-left px-4 py-3 rounded-lg text-gray-700 hover:bg-gray-100 transition"
          >
            Tickets
          </button>

          <button
            className="w-full text-left px-4 py-3 rounded-lg text-gray-700 hover:bg-gray-100 transition"
          >
            Messages
          </button>

        </nav>

        {/* User section */}
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
            className="w-full px-4 py-2 text-sm text-red-600 hover:bg-red-50 rounded-lg transition"
          >
            Logout
          </button>

        </div>

      </aside>


      {/* Main content */}
      <main className="flex-1">

        {/* Top bar */}
        <header className="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-8">

          <div>
            <h2 className="text-lg font-semibold text-gray-900">
              Support Center
            </h2>
          </div>

          <div className="text-sm text-gray-500">
            {user?.email}
          </div>

        </header>


        {/* Page content */}
        <section className="p-8">
          {children}
        </section>

      </main>

    </div>
  );
}

export default DashboardLayout;