import { useEffect, useMemo, useState } from "react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";
import { FilterIcon, XIcon } from "../components/Icons";

const statusOptions = ["PENDING", "ACCEPTED", "REJECTED", "CANCELLED", "COMPLETED"];
const dayNames = ["SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"];

const rescheduleDefault = {
  id: "",
  appointmentDate: "",
  appointmentTime: "",
};

const normalizeTime = (value) => {
  if (!value) return "";
  const parts = value.split(":");
  return parts.length >= 2 ? `${parts[0].padStart(2, "0")}:${parts[1].padStart(2, "0")}` : value;
};

const parseToMinutes = (value) => {
  const [h, m] = value.split(":").map((v) => Number(v));
  return h * 60 + m;
};

const formatMinutes = (minutes) => {
  const hours = Math.floor(minutes / 60)
    .toString()
    .padStart(2, "0");
  const mins = (minutes % 60).toString().padStart(2, "0");
  return `${hours}:${mins}`;
};

const generateSlotsForDate = (availabilityList, dateString) => {
  if (!dateString) {
    return [];
  }
  const date = new Date(`${dateString}T00:00:00`);
  if (Number.isNaN(date.getTime())) {
    return [];
  }

  const dayOfWeek = dayNames[date.getDay()];
  const windows = availabilityList.filter((item) => item.dayOfWeek === dayOfWeek);
  const slots = [];

  windows.forEach((window) => {
    const start = normalizeTime(window.startTime);
    const end = normalizeTime(window.endTime);
    const duration = Number(window.slotDuration || 0);
    if (!start || !end || !duration) {
      return;
    }

    let current = parseToMinutes(start);
    const endMinutes = parseToMinutes(end);

    while (current + duration <= endMinutes) {
      slots.push(formatMinutes(current));
      current += duration;
    }
  });

  return Array.from(new Set(slots)).sort();
};

export default function Appointments() {
  const { user } = useAuth();
  const role = user?.role;
  const isDoctor = role === "DOCTOR";

  const [items, setItems] = useState([]);
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [reload, setReload] = useState(0);

  const [filters, setFilters] = useState({
    status: "",
    doctorId: "",
    patientId: "",
    date: "",
  });

  const [booking, setBooking] = useState({
    doctorId: "",
    patientId: "",
    appointmentDate: "",
    appointmentTime: "",
    reason: "",
  });

  const [availabilityList, setAvailabilityList] = useState([]);
  const [availabilityLoading, setAvailabilityLoading] = useState(false);
  const [availabilityError, setAvailabilityError] = useState("");

  const [availableSlots, setAvailableSlots] = useState([]);
  const [slotsLoading, setSlotsLoading] = useState(false);
  const [bookingError, setBookingError] = useState("");
  const [bookingSuccess, setBookingSuccess] = useState("");
  const [bookingLoading, setBookingLoading] = useState(false);

  const [actionError, setActionError] = useState("");
  const [actionSuccess, setActionSuccess] = useState("");
  const [actionLoading, setActionLoading] = useState(false);
  const [rescheduleForm, setRescheduleForm] = useState(rescheduleDefault);

  const [doctorDate, setDoctorDate] = useState(() => new Date().toISOString().slice(0, 10));
  const [patientLookup, setPatientLookup] = useState({});

  useEffect(() => {
    let mounted = true;

    const load = async () => {
      try {
        setLoading(true);
        setError("");

        if (role === "PATIENT") {
          const response = await api.get("/appointments/my");
          if (mounted) {
            setItems(response.data || []);
            setTotalPages(1);
          }
          return;
        }

        if (role === "DOCTOR") {
          const response = await api.get("/appointments/doctor");
          if (mounted) {
            setItems(response.data || []);
            setTotalPages(1);
          }
          return;
        }

        const response = await api.get("/appointments", {
          params: {
            page,
            size,
            sort: "appointmentDate,desc",
            status: filters.status || undefined,
            doctorId: filters.doctorId || undefined,
            patientId: filters.patientId || undefined,
            date: filters.date || undefined,
          },
        });

        if (mounted) {
          setItems(response.data?.data || []);
          setTotalPages(response.data?.totalPages || 0);
        }
      } catch (err) {
        if (mounted) {
          setError(err?.response?.data?.message || "Failed to load appointments");
        }
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    };

    load();

    return () => {
      mounted = false;
    };
  }, [role, page, size, filters, reload]);

  useEffect(() => {
    let mounted = true;

    const loadAvailability = async () => {
      if (!booking.doctorId) {
        if (mounted) {
          setAvailabilityList([]);
          setAvailabilityError("");
        }
        return;
      }

      setAvailabilityLoading(true);
      setAvailabilityError("");

      try {
        const response = await api.get(`/availability/doctor/${booking.doctorId}`);
        if (mounted) {
          setAvailabilityList(response.data || []);
        }
      } catch (err) {
        if (mounted) {
          setAvailabilityError(err?.response?.data?.message || "Failed to load availability");
          setAvailabilityList([]);
        }
      } finally {
        if (mounted) {
          setAvailabilityLoading(false);
        }
      }
    };

    loadAvailability();

    return () => {
      mounted = false;
    };
  }, [booking.doctorId]);

  useEffect(() => {
    let mounted = true;

    const loadSlots = async () => {
      if (!booking.doctorId || !booking.appointmentDate) {
        if (mounted) {
          setAvailableSlots([]);
        }
        return;
      }

      setSlotsLoading(true);

      try {
        const response = await api.get(`/availability/doctor/${booking.doctorId}/slots`, {
          params: { date: booking.appointmentDate },
        });
        if (mounted) {
          const slots = (response.data?.slots || []).map(normalizeTime);
          setAvailableSlots(slots);
          if (!slots.includes(booking.appointmentTime)) {
            setBooking((prev) => ({ ...prev, appointmentTime: "" }));
          }
        }
      } catch {
        if (mounted) {
          setAvailableSlots([]);
        }
      } finally {
        if (mounted) {
          setSlotsLoading(false);
        }
      }
    };

    loadSlots();

    return () => {
      mounted = false;
    };
  }, [booking.doctorId, booking.appointmentDate]);

  useEffect(() => {
    let mounted = true;

    const loadPatients = async () => {
      if (!isDoctor || items.length === 0) {
        return;
      }

      try {
        const response = await api.get("/patients", {
          params: { page: 0, size: 1000 },
        });
        if (mounted) {
          const map = {};
          (response.data?.data || []).forEach((patient) => {
            map[patient.id] = patient.name;
          });
          setPatientLookup(map);
        }
      } catch {
        if (mounted) {
          setPatientLookup({});
        }
      }
    };

    loadPatients();

    return () => {
      mounted = false;
    };
  }, [isDoctor, items]);

  const canBook = role === "PATIENT" || role === "RECEPTIONIST" || role === "ADMIN";
  const canUpdateStatus = role === "ADMIN";
  const canManageAppointments = role === "PATIENT" || role === "RECEPTIONIST" || role === "ADMIN";

  const allSlots = useMemo(
    () => generateSlotsForDate(availabilityList, booking.appointmentDate),
    [availabilityList, booking.appointmentDate]
  );

  const availableSlotSet = useMemo(() => new Set(availableSlots), [availableSlots]);

  const doctorAppointments = useMemo(() => {
    if (!isDoctor) {
      return [];
    }
    if (!doctorDate) {
      return items;
    }
    return items.filter((item) => item.appointmentDate === doctorDate);
  }, [items, doctorDate, isDoctor]);

  const handleBook = async (event) => {
    event.preventDefault();
    setBookingError("");
    setBookingSuccess("");
    setBookingLoading(true);

    try {
      await api.post("/appointments/book", {
        doctorId: Number(booking.doctorId),
        patientId: booking.patientId ? Number(booking.patientId) : undefined,
        appointmentDate: booking.appointmentDate,
        appointmentTime: booking.appointmentTime,
        reason: booking.reason || undefined,
      });
      setBooking({ doctorId: "", patientId: "", appointmentDate: "", appointmentTime: "", reason: "" });
      setAvailableSlots([]);
      setAvailabilityList([]);
      setBookingSuccess("Appointment request submitted successfully.");
      setReload((v) => v + 1);
    } catch (err) {
      setBookingError(err?.response?.data?.message || "Failed to book appointment");
    } finally {
      setBookingLoading(false);
    }
  };

  const handleStatusUpdate = async (id, status) => {
    try {
      await api.put(`/appointments/${id}/status`, { status });
      setActionSuccess("Appointment status updated.");
      setReload((v) => v + 1);
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to update status");
    }
  };

  const handleAccept = async (id) => {
    setActionError("");
    setActionSuccess("");
    setActionLoading(true);

    try {
      await api.put(`/appointments/${id}/accept`);
      setActionSuccess("Appointment accepted.");
      setReload((v) => v + 1);
    } catch (err) {
      setActionError(err?.response?.data?.message || "Failed to accept appointment");
    } finally {
      setActionLoading(false);
    }
  };

  const handleReject = async (id) => {
    setActionError("");
    setActionSuccess("");
    setActionLoading(true);

    try {
      await api.put(`/appointments/${id}/reject`);
      setActionSuccess("Appointment rejected.");
      setReload((v) => v + 1);
    } catch (err) {
      setActionError(err?.response?.data?.message || "Failed to reject appointment");
    } finally {
      setActionLoading(false);
    }
  };

  const handleCancel = async (id) => {
    setActionError("");
    setActionSuccess("");
    setActionLoading(true);

    try {
      await api.put(`/appointments/${id}/cancel`);
      setActionSuccess("Appointment cancelled.");
      setReload((v) => v + 1);
    } catch (err) {
      setActionError(err?.response?.data?.message || "Failed to cancel appointment");
    } finally {
      setActionLoading(false);
    }
  };

  const handleRescheduleStart = (item) => {
    setActionError("");
    setRescheduleForm({
      id: item.id,
      appointmentDate: item.appointmentDate || "",
      appointmentTime: normalizeTime(item.appointmentTime) || "",
    });
  };

  const handleRescheduleSubmit = async (event) => {
    event.preventDefault();
    if (!rescheduleForm.id) {
      return;
    }

    setActionError("");
    setActionSuccess("");
    setActionLoading(true);

    try {
      await api.put(`/appointments/${rescheduleForm.id}/reschedule`, {
        appointmentDate: rescheduleForm.appointmentDate,
        appointmentTime: rescheduleForm.appointmentTime,
      });
      setRescheduleForm(rescheduleDefault);
      setActionSuccess("Appointment rescheduled.");
      setReload((v) => v + 1);
    } catch (err) {
      setActionError(err?.response?.data?.message || "Failed to reschedule appointment");
    } finally {
      setActionLoading(false);
    }
  };

  const clearReschedule = () => {
    setRescheduleForm(rescheduleDefault);
  };

  const showFilters = role === "ADMIN" || role === "RECEPTIONIST";
  const filtersActive = Boolean(filters.status || filters.doctorId || filters.patientId || filters.date);
  const totalColumns = 6 + (canUpdateStatus ? 1 : 0) + (canManageAppointments ? 1 : 0);

  const statusBadge = (status) => {
    if (status === "ACCEPTED") return "badge-success";
    if (status === "PENDING") return "badge-warning";
    if (status === "REJECTED" || status === "CANCELLED") return "badge-danger";
    if (status === "COMPLETED") return "badge-muted";
    return "badge-muted";
  };

  return (
    <div>
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-display">Appointments</h1>
          <p className="text-sm text-slate-500 mt-1">Manage schedules and status.</p>
        </div>
      </div>

      {canBook ? (
        <form className="mt-6 card p-5" onSubmit={handleBook}>
          <div className="section-title">Book Appointment</div>
          <div className="mt-4 grid grid-cols-1 md:grid-cols-4 gap-3">
            <input
              className="input"
              placeholder="Doctor ID"
              value={booking.doctorId}
              onChange={(e) => setBooking((prev) => ({ ...prev, doctorId: e.target.value }))}
              required
            />
            {role === "ADMIN" || role === "RECEPTIONIST" ? (
              <input
                className="input"
                placeholder="Patient ID"
                value={booking.patientId}
                onChange={(e) => setBooking((prev) => ({ ...prev, patientId: e.target.value }))}
                required
              />
            ) : null}
            <input
              className="input"
              type="date"
              value={booking.appointmentDate}
              onChange={(e) => setBooking((prev) => ({ ...prev, appointmentDate: e.target.value }))}
              required
            />
            <input
              className="input"
              placeholder="Reason (optional)"
              value={booking.reason}
              onChange={(e) => setBooking((prev) => ({ ...prev, reason: e.target.value }))}
            />
          </div>

          <div className="mt-5">
            <div className="section-title">Doctor Availability</div>
            {availabilityLoading ? (
              <div className="mt-2 text-sm text-slate-500">Loading availability...</div>
            ) : availabilityError ? (
              <div className="mt-2 text-sm text-red-600">{availabilityError}</div>
            ) : availabilityList.length ? (
              <div className="mt-3 flex flex-wrap gap-2">
                {availabilityList.map((item) => (
                  <span key={item.id} className="badge-muted">
                    {item.dayOfWeek} {normalizeTime(item.startTime)}-{normalizeTime(item.endTime)}
                    {item.slotDuration ? ` (${item.slotDuration}m)` : ""}
                  </span>
                ))}
              </div>
            ) : booking.doctorId ? (
              <div className="mt-2 text-sm text-slate-500">No availability set for this doctor.</div>
            ) : (
              <div className="mt-2 text-sm text-slate-500">Select a doctor to view availability.</div>
            )}
          </div>

          <div className="mt-5">
            <div className="section-title">Select Time Slot</div>
            {slotsLoading ? (
              <div className="mt-2 text-sm text-slate-500">Loading time slots...</div>
            ) : booking.appointmentDate ? (
              allSlots.length ? (
                <div className="mt-3 grid grid-cols-2 sm:grid-cols-4 md:grid-cols-6 gap-2">
                  {allSlots.map((slot) => {
                    const isAvailable = availableSlotSet.has(slot);
                    const isSelected = booking.appointmentTime === slot;
                    return (
                      <button
                        key={slot}
                        type="button"
                        disabled={!isAvailable}
                        onClick={() =>
                          setBooking((prev) => ({
                            ...prev,
                            appointmentTime: slot,
                          }))
                        }
                        className={`px-2 py-2 rounded-lg text-xs font-semibold border transition ${
                          isSelected
                            ? "bg-brand text-white border-brand"
                            : isAvailable
                            ? "bg-white text-ink border-slate-200 hover:border-brand/60"
                            : "bg-slate-100 text-slate-400 border-slate-200 cursor-not-allowed"
                        }`}
                      >
                        {slot}
                      </button>
                    );
                  })}
                </div>
              ) : (
                <div className="mt-2 text-sm text-slate-500">
                  No slots available for selected date.
                </div>
              )
            ) : (
              <div className="mt-2 text-sm text-slate-500">Pick a date to see available slots.</div>
            )}
          </div>

          {bookingError ? <div className="mt-3 text-sm text-red-600">{bookingError}</div> : null}
          {bookingSuccess ? <div className="mt-3 text-sm text-emerald-600">{bookingSuccess}</div> : null}
          <button
            className="mt-4 btn-primary"
            type="submit"
            disabled={bookingLoading || !booking.appointmentTime}
          >
            {bookingLoading ? "Booking..." : "Book Appointment"}
          </button>
        </form>
      ) : null}

      {isDoctor ? (
        <div className="mt-8 card p-5">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <div className="section-title">Doctor Dashboard</div>
              <div className="text-sm text-slate-600">Daily appointments and requests</div>
            </div>
            <input
              className="input sm:w-48"
              type="date"
              value={doctorDate}
              onChange={(e) => setDoctorDate(e.target.value)}
            />
          </div>

          <div className="mt-4 table-wrap">
            <div className="px-4 py-3 border-b border-slate-200 text-sm text-slate-500">
              {loading ? "Loading..." : `${doctorAppointments.length} appointment(s)`}
            </div>
            {actionError ? <div className="px-4 py-3 text-sm text-red-600">{actionError}</div> : null}
            {actionSuccess ? <div className="px-4 py-3 text-sm text-emerald-600">{actionSuccess}</div> : null}

            <table className="w-full text-sm">
              <thead className="table-head">
                <tr>
                  <th className="text-left px-4 py-3">Time</th>
                  <th className="text-left px-4 py-3">Patient</th>
                  <th className="text-left px-4 py-3">Reason</th>
                  <th className="text-left px-4 py-3">Status</th>
                  <th className="text-left px-4 py-3">Actions</th>
                </tr>
              </thead>
              <tbody>
                {doctorAppointments.map((item) => (
                  <tr key={item.id} className="table-row even:bg-slate-50/60">
                    <td className="px-4 py-3">{normalizeTime(item.appointmentTime)}</td>
                    <td className="px-4 py-3">
                      {patientLookup[item.patientId] || `Patient #${item.patientId}`}
                    </td>
                    <td className="px-4 py-3">{item.reason || "-"}</td>
                    <td className="px-4 py-3">
                      <span className={statusBadge(item.status)}>{item.status}</span>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-2">
                        <button
                          className="btn-secondary px-3 py-1.5 text-xs"
                          type="button"
                          onClick={() => handleAccept(item.id)}
                          disabled={actionLoading || item.status !== "PENDING"}
                        >
                          Accept
                        </button>
                        <button
                          className="btn-secondary px-3 py-1.5 text-xs text-red-600 border-red-200"
                          type="button"
                          onClick={() => handleReject(item.id)}
                          disabled={actionLoading || item.status !== "PENDING"}
                        >
                          Reject
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
                {!loading && doctorAppointments.length === 0 ? (
                  <tr>
                    <td colSpan="5" className="px-4 py-6 text-center text-slate-500">
                      No appointments for this date.
                    </td>
                  </tr>
                ) : null}
              </tbody>
            </table>
          </div>
        </div>
      ) : null}

      {showFilters ? (
        <div className="mt-6 card p-4">
          <div className="flex items-center justify-between mb-3">
            <div className="section-title flex items-center gap-2">
              <FilterIcon className="h-4 w-4" />
              Filters
            </div>
            {filtersActive ? (
              <button
                className="btn-secondary px-3 py-1.5 text-xs"
                type="button"
                onClick={() => setFilters({ status: "", doctorId: "", patientId: "", date: "" })}
              >
                <span className="inline-flex items-center gap-1">
                  <XIcon className="h-3.5 w-3.5" />
                  Clear
                </span>
              </button>
            ) : null}
          </div>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
            <select
              className="input"
              value={filters.status}
              onChange={(e) => setFilters((prev) => ({ ...prev, status: e.target.value }))}
            >
              <option value="">All Status</option>
              {statusOptions.map((status) => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </select>
            <input
              className="input"
              placeholder="Doctor ID"
              value={filters.doctorId}
              onChange={(e) => setFilters((prev) => ({ ...prev, doctorId: e.target.value }))}
            />
            <input
              className="input"
              placeholder="Patient ID"
              value={filters.patientId}
              onChange={(e) => setFilters((prev) => ({ ...prev, patientId: e.target.value }))}
            />
            <input
              className="input"
              type="date"
              value={filters.date}
              onChange={(e) => setFilters((prev) => ({ ...prev, date: e.target.value }))}
            />
          </div>
        </div>
      ) : null}

      <div className="mt-6 table-wrap">
        <div className="px-4 py-3 border-b border-slate-200 text-sm text-slate-500">
          {loading ? "Loading..." : `${items.length} appointment(s)`}
        </div>
        {error ? <div className="px-4 py-3 text-sm text-red-600">{error}</div> : null}

        <table className="w-full text-sm">
          <thead className="table-head">
            <tr>
              <th className="text-left px-4 py-3">ID</th>
              <th className="text-left px-4 py-3">Doctor</th>
              <th className="text-left px-4 py-3">Patient</th>
              <th className="text-left px-4 py-3">Date</th>
              <th className="text-left px-4 py-3">Time</th>
              <th className="text-left px-4 py-3">Status</th>
              {canUpdateStatus ? <th className="text-left px-4 py-3">Update</th> : null}
              {canManageAppointments ? <th className="text-left px-4 py-3">Actions</th> : null}
            </tr>
          </thead>
          <tbody>
            {items.map((item) => {
              const isFinal = item.status === "CANCELLED" || item.status === "COMPLETED";

              return (
                <tr key={item.id} className="table-row even:bg-slate-50/60">
                  <td className="px-4 py-3">{item.id}</td>
                  <td className="px-4 py-3">{item.doctorId}</td>
                  <td className="px-4 py-3">{item.patientId}</td>
                  <td className="px-4 py-3">{item.appointmentDate}</td>
                  <td className="px-4 py-3">{normalizeTime(item.appointmentTime)}</td>
                  <td className="px-4 py-3">
                    <span className={statusBadge(item.status)}>{item.status}</span>
                  </td>
                  {canUpdateStatus ? (
                    <td className="px-4 py-3">
                      <select
                        className="input w-36"
                        defaultValue={item.status}
                        onChange={(e) => handleStatusUpdate(item.id, e.target.value)}
                      >
                        {statusOptions.map((status) => (
                          <option key={status} value={status}>
                            {status}
                          </option>
                        ))}
                      </select>
                    </td>
                  ) : null}
                  {canManageAppointments ? (
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-3">
                        <button
                          className="text-sm font-semibold text-brand hover:text-brandDark transition disabled:opacity-50"
                          type="button"
                          onClick={() => handleRescheduleStart(item)}
                          disabled={actionLoading || isFinal}
                        >
                          Reschedule
                        </button>
                        <button
                          className="text-sm font-semibold text-red-600 hover:text-red-700 transition disabled:opacity-50"
                          type="button"
                          onClick={() => handleCancel(item.id)}
                          disabled={actionLoading || isFinal}
                        >
                          Cancel
                        </button>
                      </div>
                    </td>
                  ) : null}
                </tr>
              );
            })}
            {!loading && items.length === 0 ? (
              <tr>
                <td colSpan={totalColumns} className="px-4 py-6 text-center text-slate-500">
                  No appointments found.
                </td>
              </tr>
            ) : null}
          </tbody>
        </table>
      </div>

      {canManageAppointments && rescheduleForm.id ? (
        <form className="mt-6 card p-5" onSubmit={handleRescheduleSubmit}>
          <div className="section-title">Reschedule Appointment #{rescheduleForm.id}</div>
          <div className="mt-4 grid grid-cols-1 md:grid-cols-2 gap-3">
            <input
              className="input"
              type="date"
              value={rescheduleForm.appointmentDate}
              onChange={(e) =>
                setRescheduleForm((prev) => ({ ...prev, appointmentDate: e.target.value }))
              }
              required
            />
            <input
              className="input"
              type="time"
              value={rescheduleForm.appointmentTime}
              onChange={(e) =>
                setRescheduleForm((prev) => ({ ...prev, appointmentTime: e.target.value }))
              }
              required
            />
          </div>
          {actionError ? <div className="mt-3 text-sm text-red-600">{actionError}</div> : null}
          {actionSuccess ? <div className="mt-3 text-sm text-emerald-600">{actionSuccess}</div> : null}
          <div className="mt-4 flex items-center gap-3">
            <button className="btn-primary" type="submit" disabled={actionLoading}>
              {actionLoading ? "Saving..." : "Save Changes"}
            </button>
            <button type="button" className="btn-secondary" onClick={clearReschedule}>
              Cancel
            </button>
          </div>
        </form>
      ) : null}

      {totalPages > 1 ? (
        <div className="mt-4 flex items-center gap-2">
          <button
            className="btn-secondary px-3 py-1.5 text-xs"
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0}
            type="button"
          >
            Prev
          </button>
          <div className="text-sm text-slate-600">
            Page {page + 1} of {totalPages}
          </div>
          <button
            className="btn-secondary px-3 py-1.5 text-xs"
            onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
            disabled={page >= totalPages - 1}
            type="button"
          >
            Next
          </button>
        </div>
      ) : null}
    </div>
  );
}
