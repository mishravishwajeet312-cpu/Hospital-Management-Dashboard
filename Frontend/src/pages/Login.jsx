import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      await login({ email, password });
      navigate("/", { replace: true });
    } catch (err) {
      const message = err?.response?.data?.message || "Login failed";
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-6">
      <div className="card w-full max-w-md p-8">
        <div className="flex items-center gap-3">
          <div className="h-10 w-10 rounded-xl bg-brand/15 text-brand flex items-center justify-center font-display text-lg">
            H
          </div>
          <div>
            <h1 className="text-2xl font-display">Welcome back</h1>
            <p className="text-sm text-slate-500">Access your hospital dashboard</p>
          </div>
        </div>

        <form className="mt-8 space-y-4" onSubmit={handleSubmit}>
          <div className="relative">
            <input
              id="login-email"
              className={`input peer pt-5 ${error ? "border-red-300 focus:ring-red-200" : ""}`}
              placeholder=" "
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <label
              htmlFor="login-email"
              className="absolute left-3 top-2 text-xs text-slate-400 transition-all peer-placeholder-shown:top-3.5 peer-placeholder-shown:text-sm peer-focus:top-2 peer-focus:text-xs peer-focus:text-brand"
            >
              Email
            </label>
          </div>
          <div className="relative">
            <input
              id="login-password"
              className={`input peer pt-5 ${error ? "border-red-300 focus:ring-red-200" : ""}`}
              placeholder=" "
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
            <label
              htmlFor="login-password"
              className="absolute left-3 top-2 text-xs text-slate-400 transition-all peer-placeholder-shown:top-3.5 peer-placeholder-shown:text-sm peer-focus:top-2 peer-focus:text-xs peer-focus:text-brand"
            >
              Password
            </label>
          </div>
          {error ? <div className="text-sm text-red-600">{error}</div> : null}
          <button className="btn-primary w-full" type="submit" disabled={loading}>
            {loading ? "Signing in..." : "Sign In"}
          </button>
        </form>

        <div className="mt-6 text-sm text-slate-600">
          No account?{" "}
          <Link to="/register" className="text-brand font-semibold">
            Register
          </Link>
        </div>
      </div>
    </div>
  );
}
