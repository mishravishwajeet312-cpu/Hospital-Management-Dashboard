import { NavLink } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const navItems = [
  { to: "/", label: "Dashboard", roles: ["ADMIN", "DOCTOR", "RECEPTIONIST", "PATIENT"] },
  { to: "/appointments", label: "Appointments", roles: ["ADMIN", "DOCTOR", "RECEPTIONIST", "PATIENT"] },
  { to: "/patients", label: "Patients", roles: ["ADMIN", "DOCTOR", "RECEPTIONIST"] },
  { to: "/doctors", label: "Doctors", roles: ["ADMIN", "DOCTOR", "RECEPTIONIST", "PATIENT"] },
  { to: "/medical-records", label: "Medical Records", roles: ["ADMIN", "DOCTOR", "PATIENT"] },
  { to: "/admin-users", label: "Admin Users", roles: ["ADMIN"] },
];

export default function Sidebar({ open = false, onClose }) {
  const { user } = useAuth();
  const role = user?.role;

  const links = navItems.filter((item) => !role || item.roles.includes(role));

  return (
    <>
      <div
        className={`fixed inset-0 z-30 bg-slate-900/40 transition-opacity md:hidden ${
          open ? "opacity-100" : "pointer-events-none opacity-0"
        }`}
        onClick={onClose}
        role="presentation"
      />
      <aside
        className={`fixed inset-y-0 left-0 z-40 w-64 transform bg-sidebar text-white px-6 py-8 transition-transform duration-300 md:translate-x-0 ${
          open ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 rounded-xl bg-white/10 flex items-center justify-center font-display text-lg">
              H
            </div>
            <div>
              <div className="text-sm font-semibold">Hospital</div>
              <div className="text-xs text-white/60">Dashboard</div>
            </div>
          </div>
          <button type="button" className="md:hidden btn-ghost" onClick={onClose}>
            Close
          </button>
        </div>
        <nav className="mt-10 space-y-2">
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              end={link.to === "/"}
              className={({ isActive }) =>
                `group flex items-center justify-between rounded-lg px-3 py-2 text-sm font-semibold transition-all ${
                  isActive
                    ? "bg-white/15 text-white shadow-soft"
                    : "text-white/70 hover:text-white hover:bg-white/10 hover:translate-x-1"
                }`
              }
            >
              <span>{link.label}</span>
              <span className="h-2 w-2 rounded-full bg-white/30 group-hover:bg-accent" />
            </NavLink>
          ))}
        </nav>
      </aside>
    </>
  );
}
