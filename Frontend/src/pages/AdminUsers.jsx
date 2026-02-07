import { useEffect, useState } from "react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";
import { SearchIcon, XIcon } from "../components/Icons";
import RoleBadge from "../components/RoleBadge";

const roleOptions = ["ADMIN", "DOCTOR", "RECEPTIONIST", "PATIENT"];

export default function AdminUsers() {
  const { user } = useAuth();
  const isAdmin = user?.role === "ADMIN";

  const [items, setItems] = useState([]);
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [filters, setFilters] = useState({ name: "", email: "", role: "" });

  const load = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/admin/users", {
        params: {
          page,
          size,
          sort: "id,desc",
          name: filters.name || undefined,
          email: filters.email || undefined,
          role: filters.role || undefined,
        },
      });

      setItems(response.data?.data || []);
      setTotalPages(response.data?.totalPages || 0);
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to load users");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!isAdmin) {
      setLoading(false);
      return;
    }

    load();
  }, [page, size, filters, isAdmin]);

  const handleDelete = async (item) => {
    if (!window.confirm("Delete this user?")) {
      return;
    }

    try {
      await api.delete(`/admin/users/${item.id}`);
      await load();
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to delete user");
    }
  };

  const filtersActive = Boolean(filters.name || filters.email || filters.role);

  if (!isAdmin) {
    return (
      <div>
        <h1 className="text-3xl font-display">Admin Users</h1>
        <div className="mt-4 text-sm text-slate-600">Only admins can access this page.</div>
      </div>
    );
  }

  return (
    <div>
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-display">Admin Users</h1>
          <p className="text-sm text-slate-500 mt-1">View and manage system users.</p>
        </div>
      </div>

      <div className="mt-6 card p-4">
        <div className="flex items-center justify-between mb-3">
          <div className="section-title">Search</div>
          {filtersActive ? (
            <button
              className="btn-secondary px-3 py-1.5 text-xs"
              type="button"
              onClick={() => setFilters({ name: "", email: "", role: "" })}
            >
              <span className="inline-flex items-center gap-1">
                <XIcon className="h-3.5 w-3.5" />
                Clear
              </span>
            </button>
          ) : null}
        </div>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          <div className="relative">
            <SearchIcon className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              className="input pl-9 pr-8"
              placeholder="Search by name"
              value={filters.name}
              onChange={(e) => setFilters((prev) => ({ ...prev, name: e.target.value }))}
            />
          </div>
          <div className="relative">
            <SearchIcon className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
              className="input pl-9 pr-8"
              placeholder="Search by email"
              value={filters.email}
              onChange={(e) => setFilters((prev) => ({ ...prev, email: e.target.value }))}
            />
          </div>
          <select
            className="input"
            value={filters.role}
            onChange={(e) => setFilters((prev) => ({ ...prev, role: e.target.value }))}
          >
            <option value="">All Roles</option>
            {roleOptions.map((role) => (
              <option key={role} value={role}>
                {role}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="mt-6 table-wrap">
        <div className="px-4 py-3 border-b border-slate-200 text-sm text-slate-500">
          {loading ? "Loading..." : `${items.length} user(s)`}
        </div>
        {error ? <div className="px-4 py-3 text-sm text-red-600">{error}</div> : null}

        <table className="w-full text-sm">
          <thead className="table-head">
            <tr>
              <th className="text-left px-4 py-3">ID</th>
              <th className="text-left px-4 py-3">Name</th>
              <th className="text-left px-4 py-3">Email</th>
              <th className="text-left px-4 py-3">Role</th>
              <th className="text-left px-4 py-3">Actions</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id} className="table-row even:bg-slate-50/60">
                <td className="px-4 py-3">{item.id}</td>
                <td className="px-4 py-3">{item.name}</td>
                <td className="px-4 py-3">{item.email}</td>
                <td className="px-4 py-3">
                  <RoleBadge role={item.role} />
                </td>
                <td className="px-4 py-3">
                  <button
                    className="text-sm font-semibold text-red-600 hover:text-red-700 transition"
                    type="button"
                    onClick={() => handleDelete(item)}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
            {!loading && items.length === 0 ? (
              <tr>
                <td colSpan="5" className="px-4 py-6 text-center text-slate-500">
                  No users found.
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
