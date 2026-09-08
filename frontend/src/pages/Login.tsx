import { type FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { login, getCurrentUser } from "../services/authService";

function Login() {
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();

    setError("");
    setLoading(true);

    try {
      const token = await login({
        email,
        password,
      });

      localStorage.setItem("token", token);

      const user = await getCurrentUser();

      localStorage.setItem("user", JSON.stringify(user));

      if (user.role === "CUSTOMER") {
        navigate("/customer");
      } else if (user.role === "AGENT") {
        navigate("/agent");
      } else if (user.role === "ADMIN") {
        navigate("/admin");
      } else {
        setError("Unknown user role.");
      }

    } catch (error) {
      console.error(error);
      setError("Invalid email or password.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center px-4">

      <div className="w-full max-w-md bg-white rounded-2xl shadow-lg p-8">

        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900">
            Welcome Back
          </h1>

          <p className="text-gray-500 mt-2">
            Sign in to your support account
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5">

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Email
            </label>

            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="you@example.com"
              required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Password
            </label>

            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="••••••••"
              required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {error && (
            <div className="bg-red-50 text-red-600 px-4 py-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white font-medium py-3 rounded-lg transition"
          >
            {loading ? "Signing in..." : "Sign In"}
          </button>

        </form>

        <div className="text-center mt-6 text-sm text-gray-500">
          Don't have an account?{" "}
          <Link
            to="/register"
            className="text-blue-600 hover:text-blue-700 font-medium"
          >
            Create one
          </Link>
        </div>

      </div>

    </div>
  );
}

export default Login;