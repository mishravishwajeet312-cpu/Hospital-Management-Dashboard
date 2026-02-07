import { useEffect, useMemo, useState } from "react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";
import Greeting from "../components/Greeting";
import { CalendarIcon, ClipboardIcon, StethoscopeIcon, UserIcon } from "../components/Icons";

export default function Dashboard() {
  const { user } = useAuth();
  const role = user?.role;

  const [stats, setStats] = useState({
    patients: 0,
    doctors: 0,
    appointments: 0,
    records: 0,
  });
  const [loading, setLoading] = useState(true);

  const cards = useMemo(() => {
    if (role === "PATIENT") {
      return [
        { key: "appointments", label: "My Appointments", icon: CalendarIcon },
        { key: "records", label: "My Records", icon: ClipboardIcon },
        { key: "doctors", label: "Doctors", icon: StethoscopeIcon },
      ];
    }
    return [
      { key: "patients", label: "Patients", icon: UserIcon },
      { key: "doctors", label: "Doctors", icon: StethoscopeIcon },
      { key: "appointments", label: "Appointments", icon: CalendarIcon },
    ];
  }, [role]);

  useEffect(() => {
    let mounted = true;

    const loadStats = async () => {
      try {
        setLoading(true);

        if (role === "PATIENT") {
          const [myAppointments, myRecords, doctors] = await Promise.all([
            api.get("/appointments/my"),
            api.get("/medical-records/my"),
            api.get("/doctors", { params: { page: 0, size: 1 } }),
          ]);

          if (!mounted) {
            return;
          }

          setStats({
            appointments: myAppointments.data?.length || 0,
            records: myRecords.data?.length || 0,
            doctors: doctors.data?.totalItems || 0,
            patients: 0,
          });
          return;
        }

        const [patients, doctors, appointments] = await Promise.all([
          api.get("/patients", { params: { page: 0, size: 1 } }),
          api.get("/doctors", { params: { page: 0, size: 1 } }),
          api.get("/appointments", { params: { page: 0, size: 1 } }),
        ]);

        if (!mounted) {
          return;
        }

        setStats({
          patients: patients.data?.totalItems || 0,
          doctors: doctors.data?.totalItems || 0,
          appointments: appointments.data?.totalItems || 0,
          records: 0,
        });
      } catch {
        if (mounted) {
          setStats({ patients: 0, doctors: 0, appointments: 0, records: 0 });
        }
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    };

    loadStats();

    return () => {
      mounted = false;
    };
  }, [role]);

  return (
    <div>
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-display">Dashboard</h1>
          <p className="text-sm text-slate-500 mt-1">Quick overview of today.</p>
        </div>
        <div className="card px-4 py-3 text-ink">
          <Greeting className="text-ink" />
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mt-6">
        {cards.map((card) => {
          const Icon = card.icon;
          return (
            <div key={card.key} className="card card-hover p-5">
              <div className="flex items-center justify-between">
                <div className="section-title">Total {card.label}</div>
                <div className="h-10 w-10 rounded-xl bg-brand/10 text-brand flex items-center justify-center">
                  <Icon className="h-5 w-5" />
                </div>
              </div>
              <div className="text-3xl font-display mt-3">
                {loading ? "?" : stats[card.key]}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
