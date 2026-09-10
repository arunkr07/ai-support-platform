import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";

import Login from "./pages/Login";
import Register from "./pages/Register";

import CustomerDashboard from "./pages/CustomerDashboard";
import AgentDashboard from "./pages/AgentDashboard";
import AdminDashboard from "./pages/AdminDashboard";

import TicketDetails from "./pages/TicketDetails";
import AgentTicketDetails from "./pages/AgentTicketDetails";
import AdminTicketDetails from "./pages/AdminTicketDetails";

import AdminAgents from "./pages/AdminAgents";
import AdminCustomers from "./pages/AdminCustomers";
import AdminCustomerDetails from "./pages/AdminCustomerDetails";

import ProtectedRoute from "./components/ProtectedRoute";

function App() {
  return (
    <BrowserRouter>

      <Routes>


        <Route
          path="/login"
          element={<Login />}
        />

        <Route
          path="/register"
          element={<Register />}
        />


        <Route
          element={
            <ProtectedRoute
              allowedRoles={["CUSTOMER"]}
            />
          }
        >

          <Route
            path="/customer"
            element={<CustomerDashboard />}
          />

          <Route
            path="/customer/tickets/:id"
            element={<TicketDetails />}
          />

        </Route>


        <Route
          element={
            <ProtectedRoute
              allowedRoles={["AGENT"]}
            />
          }
        >

          <Route
            path="/agent"
            element={<AgentDashboard />}
          />

          <Route
            path="/agent/tickets/:id"
            element={<AgentTicketDetails />}
          />

        </Route>


        <Route
          element={
            <ProtectedRoute
              allowedRoles={["ADMIN"]}
            />
          }
        >

          <Route
            path="/admin"
            element={<AdminDashboard />}
          />

          <Route
            path="/admin/tickets/:id"
            element={<AdminTicketDetails />}
          />

          <Route
            path="/admin/agents"
            element={<AdminAgents />}
          />

          <Route
            path="/admin/customers"
            element={<AdminCustomers />}
          />

          <Route
            path="/admin/customers/:id"
            element={<AdminCustomerDetails />}
          />

        </Route>


        <Route
          path="*"
          element={
            <Navigate
              to="/login"
              replace
            />
          }
        />

      </Routes>

    </BrowserRouter>
  );
}

export default App;