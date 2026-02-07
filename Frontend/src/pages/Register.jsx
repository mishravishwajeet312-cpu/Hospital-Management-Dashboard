import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Register() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    setLoading(true);

    try {
      await register({ name, email, password });
      navigate("/login", { replace: true });
    } catch (err) {
      const message = err?.response?.data?.message || "Registration failed";
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
            <h1 className="text-2xl font-display">Create account</h1>
            <p className="text-sm text-slate-500">Register as a patient</p>
          </div>
        </div>

        <form className="mt-8 space-y-4" onSubmit={handleSubmit}>
          <div className="relative">
            <input
              id="register-name"
              className={`input peer pt-5 ${error ? "border-red-300 focus:ring-red-200" : ""}`}
              placeholder=" "
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
            <label
              htmlFor="register-name"
              className="absolute left-3 top-2 text-xs text-slate-400 transition-all peer-placeholder-shown:top-3.5 peer-placeholder-shown:text-sm peer-focus:top-2 peer-focus:text-xs peer-focus:text-brand"
            >
              Name
            </label>
          </div>
          <div className="relative">
            <input
              id="register-email"
              className={`input peer pt-5 ${error ? "border-red-300 focus:ring-red-200" : ""}`}
              placeholder=" "
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <label
              htmlFor="register-email"
              className="absolute left-3 top-2 text-xs text-slate-400 transition-all peer-placeholder-shown:top-3.5 peer-placeholder-shown:text-sm peer-focus:top-2 peer-focus:text-xs peer-focus:text-brand"
            >
              Email
            </label>
          </div>
          <div className="relative">
            <input
              id="register-password"
              className={`input peer pt-5 ${error ? "border-red-300 focus:ring-red-200" : ""}`}
              placeholder=" "
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
            <label
              htmlFor="register-password"
              className="absolute left-3 top-2 text-xs text-slate-400 transition-all peer-placeholder-shown:top-3.5 peer-placeholder-shown:text-sm peer-focus:top-2 peer-focus:text-xs peer-focus:text-brand"
            >
              Password
            </label>
          </div>
          {error ? <div className="text-sm text-red-600">{error}</div> : null}
          <button className="btn-primary w-full" type="submit" disabled={loading}>
            {loading ? "Creating..." : "Create Account"}
          </button>
        </form>

        <div className="mt-6 text-sm text-slate-600">
          Already have an account?{" "}
          <Link to="/login" className="text-brand font-semibold">
            Login
          </Link>
        </div>
      </div>
    </div>
  );
}
