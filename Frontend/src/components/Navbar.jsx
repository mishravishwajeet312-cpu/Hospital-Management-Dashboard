import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Greeting from "./Greeting";
import RoleBadge from "./RoleBadge";

export default function Navbar({ onMenuClick }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const initials = user?.email ? user.email.charAt(0).toUpperCase() : "G";

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  return (
    <header className="sticky top-0 z-20 flex items-center justify-between bg-brand text-white px-6 py-4 shadow-md">
      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={onMenuClick}
          className="md:hidden btn-ghost border border-white/20"
        >
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            className="h-5 w-5"
          >
            <line x1="3" y1="6" x2="21" y2="6" />
            <line x1="3" y1="12" x2="21" y2="12" />
            <line x1="3" y1="18" x2="21" y2="18" />
          </svg>
        </button>
        <div>
          <div className="text-xs uppercase tracking-[0.3em] text-white/70">Hospital</div>
          <div className="text-lg font-display">Dashboard</div>
          <Greeting className="mt-1 text-white/85" />
        </div>
      </div>
      <div className="flex items-center gap-4">
        <div className="hidden sm:flex items-center gap-3 rounded-full bg-white/10 px-4 py-2">
          <div className="h-9 w-9 rounded-full bg-white/20 flex items-center justify-center font-semibold">
            {initials}
          </div>
          <div>
            <div className="text-sm font-semibold">{user?.email || "guest"}</div>
            <div className="mt-1">
              <RoleBadge role={user?.role} />
            </div>
          </div>
        </div>
        <button
          onClick={handleLogout}
          className="btn-ghost border border-white/20"
          type="button"
        >
          Logout
        </button>
      </div>
    </header>
  );
}
