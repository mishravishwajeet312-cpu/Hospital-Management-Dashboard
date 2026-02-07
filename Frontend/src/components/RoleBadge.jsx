import { ShieldIcon, StethoscopeIcon, UserIcon } from "./Icons";

const roleMap = {
  ADMIN: { label: "Admin", icon: ShieldIcon, className: "bg-indigo-100 text-indigo-700" },
  DOCTOR: { label: "Doctor", icon: StethoscopeIcon, className: "bg-emerald-100 text-emerald-700" },
  RECEPTIONIST: { label: "Reception", icon: UserIcon, className: "bg-sky-100 text-sky-700" },
  PATIENT: { label: "Patient", icon: UserIcon, className: "bg-amber-100 text-amber-700" },
};

export default function RoleBadge({ role }) {
  if (!role) {
    return null;
  }

  const config = roleMap[role] || roleMap.PATIENT;
  const Icon = config.icon;

  return (
    <span className={`inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-xs font-semibold ${config.className}`}>
      <Icon className="h-3.5 w-3.5" />
      {config.label}
    </span>
  );
}
