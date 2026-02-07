import { useEffect, useMemo, useState } from "react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";
import { SearchIcon, XIcon } from "../components/Icons";

const emptyForm = {
  name: "",
  email: "",
  password: "",
  specialization: "",
  phone: "",
};

export default function Doctors() {
  const { user } = useAuth();
  const role = user?.role;
  const isAdmin = role === "ADMIN";

  const [items, setItems] = useState([]);
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [specialization, setSpecialization] = useState("");

  const [form, setForm] = useState(emptyForm);
  const [formError, setFormError] = useState("");
  const [formLoading, setFormLoading] = useState(false);
  const [editing, setEditing] = useState(null);

  const load = async () => {
    try {
      setLoading(true);
      setError("");
      const response = await api.get("/doctors", {
        params: {
          page,
          size,
          sort: "id,desc",
          specialization: specialization || undefined,
        },
      });

      setItems(response.data?.data || []);
      setTotalPages(response.data?.totalPages || 0);
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to load doctors");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, [page, size, specialization]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setFormError("");
    setFormLoading(true);

    try {
      if (editing) {
        const payload = {};
        if (form.name) payload.name = form.name;
        if (form.email) payload.email = form.email;
        if (form.password) payload.password = form.password;
        if (form.specialization) payload.specialization = form.specialization;
        if (form.phone) payload.phone = form.phone;

        await api.put(`/admin/users/doctor/${editing.userId}`, payload);
      } else {
        await api.post("/admin/users/doctor", {
          name: form.name,
          email: form.email,
          password: form.password,
          specialization: form.specialization,
          phone: form.phone || undefined,
        });
      }

      setForm(emptyForm);
      setEditing(null);
      await load();
    } catch (err) {
      setFormError(err?.response?.data?.message || "Failed to save doctor");
    } finally {
      setFormLoading(false);
    }
  };

  const startEdit = (doctor) => {
    setEditing(doctor);
    setForm({
      name: doctor.name || "",
      email: doctor.email || "",
      password: "",
      specialization: doctor.specialization || "",
      phone: doctor.phone || "",
    });
  };

  const handleDelete = async (doctor) => {
    if (!window.confirm("Delete this doctor?")) {
      return;
    }

    try {
      await api.delete(`/admin/users/${doctor.userId}`);
      await load();
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to delete doctor");
    }
  };

  const formTitle = useMemo(() => (editing ? "Update Doctor" : "Create Doctor"), [editing]);
  const filtersActive = Boolean(specialization);

  return (
    <div>
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-display">Doctors</h1>
          <p className="text-sm text-slate-500 mt-1">Manage medical staff.</p>
        </div>
      </div>

      {isAdmin ? (
        <form className="mt-6 card p-5" onSubmit={handleSubmit}>
          <div className="section-title">{formTitle}</div>
          <div className="mt-4 grid grid-cols-1 md:grid-cols-3 gap-3">
            <input
              className="input"
              placeholder="Name"
              value={form.name}
              onChange={(e) => setForm((prev) => ({ ...prev, name: e.target.value }))}
              required={!editing}
            />
            <input
              className="input"
              placeholder="Email"
              type="email"
              value={form.email}
              onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))}
              required={!editing}
            />
            <input
              className="input"
              placeholder="Password"
              type="password"
              value={form.password}
              onChange={(e) => setForm((prev) => ({ ...prev, password: e.target.value }))}
              required={!editing}
            />
            <input
              className="input"
              placeholder="Specialization"
              value={form.specialization}
              onChange={(e) => setForm((prev) => ({ ...prev, specialization: e.target.value }))}
              required={!editing}
            />
            <input
              className="input"
              placeholder="Phone"
              value={form.phone}
              onChange={(e) => setForm((prev) => ({ ...prev, phone: e.target.value }))}
            />
          </div>
          {formError ? <div className="mt-3 text-sm text-red-600">{formError}</div> : null}
          <div className="mt-4 flex items-center gap-3">
            <button className="btn-primary" type="submit" disabled={formLoading}>
              {formLoading ? "Saving..." : editing ? "Update" : "Create"}
            </button>
            {editing ? (
              <button
                type="button"
                className="btn-secondary"
                onClick={() => {
                  setEditing(null);
                  setForm(emptyForm);
                }}
              >
                Cancel
              </button>
            ) : null}
          </div>
        </form>
      ) : null}

      <div className="mt-6 card p-4">
        <div className="flex items-center justify-between mb-3">
          <div className="section-title">Search</div>
          {filtersActive ? (
            <button
              className="btn-secondary px-3 py-1.5 text-xs"
              type="button"
              onClick={() => setSpecialization("")}
            >
              <span className="inline-flex items-center gap-1">
                <XIcon className="h-3.5 w-3.5" />
                Clear
              </span>
            </button>
          ) : null}
        </div>
        <div className="relative">
          <SearchIcon className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <input
            className="input pl-9 pr-8"
            placeholder="Search by specialization"
            value={specialization}
            onChange={(e) => setSpecialization(e.target.value)}
          />
        </div>
      </div>

      <div className="mt-6 table-wrap">
        <div className="px-4 py-3 border-b border-slate-200 text-sm text-slate-500">
          {loading ? "Loading..." : `${items.length} doctor(s)`}
        </div>
        {error ? <div className="px-4 py-3 text-sm text-red-600">{error}</div> : null}

        <table className="w-full text-sm">
          <thead className="table-head">
            <tr>
              <th className="text-left px-4 py-3">ID</th>
              <th className="text-left px-4 py-3">Name</th>
              <th className="text-left px-4 py-3">Email</th>
              <th className="text-left px-4 py-3">Specialization</th>
              <th className="text-left px-4 py-3">Phone</th>
              {isAdmin ? <th className="text-left px-4 py-3">Actions</th> : null}
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id} className="table-row even:bg-slate-50/60">
                <td className="px-4 py-3">{item.id}</td>
                <td className="px-4 py-3">{item.name}</td>
                <td className="px-4 py-3">{item.email}</td>
                <td className="px-4 py-3">{item.specialization || "-"}</td>
                <td className="px-4 py-3">{item.phone || "-"}</td>
                {isAdmin ? (
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2">
                      <button
                        className="text-sm font-semibold text-brand hover:text-brandDark transition"
                        type="button"
                        onClick={() => startEdit(item)}
                      >
                        Edit
                      </button>
                      <button
                        className="text-sm font-semibold text-red-600 hover:text-red-700 transition"
                        type="button"
                        onClick={() => handleDelete(item)}
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                ) : null}
              </tr>
            ))}
            {!loading && items.length === 0 ? (
              <tr>
                <td colSpan={isAdmin ? 6 : 5} className="px-4 py-6 text-center text-slate-500">
                  No doctors found.
                </td>
              </tr>
            ) : null}
          </tbody>
        </table>
      </div>

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
