import { useEffect, useMemo, useState } from "react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";

const recordFormDefault = {
  patientId: "",
  diagnosis: "",
  notes: "",
  visitDate: "",
};

const prescriptionFormDefault = {
  medicalRecordId: "",
  medicineName: "",
  dosage: "",
  duration: "",
  instructions: "",
};

export default function MedicalRecords() {
  const { user } = useAuth();
  const role = user?.role;

  const [items, setItems] = useState([]);
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [reload, setReload] = useState(0);

  const [recordForm, setRecordForm] = useState(recordFormDefault);
  const [recordError, setRecordError] = useState("");
  const [recordLoading, setRecordLoading] = useState(false);

  const [prescriptionForm, setPrescriptionForm] = useState(prescriptionFormDefault);
  const [prescriptionError, setPrescriptionError] = useState("");
  const [prescriptionLoading, setPrescriptionLoading] = useState(false);

  const [fileRecordId, setFileRecordId] = useState("");
  const [fileToUpload, setFileToUpload] = useState(null);
  const [fileError, setFileError] = useState("");
  const [fileLoading, setFileLoading] = useState(false);

  const [downloadError, setDownloadError] = useState("");

  const [fileListRecordId, setFileListRecordId] = useState("");
  const [files, setFiles] = useState([]);
  const [fileListError, setFileListError] = useState("");

  const canCreateRecord = role === "DOCTOR" || role === "ADMIN";
  const canCreatePrescription = role === "DOCTOR" || role === "ADMIN";
  const canUploadFile = role === "DOCTOR";

  useEffect(() => {
    let mounted = true;

    const load = async () => {
      try {
        setLoading(true);
        setError("");

        if (role === "PATIENT") {
          const response = await api.get("/medical-records/my");
          if (mounted) {
            setItems(response.data || []);
            setTotalPages(1);
          }
          return;
        }

        const response = await api.get("/medical-records", {
          params: { page, size, sort: "visitDate,desc" },
        });

        if (mounted) {
          setItems(response.data?.data || []);
          setTotalPages(response.data?.totalPages || 0);
        }
      } catch (err) {
        if (mounted) {
          setError(err?.response?.data?.message || "Failed to load records");
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
  }, [role, page, size, reload]);

  const handleCreateRecord = async (event) => {
    event.preventDefault();
    setRecordError("");
    setRecordLoading(true);

    try {
      await api.post("/medical-records", {
        patientId: Number(recordForm.patientId),
        diagnosis: recordForm.diagnosis,
        notes: recordForm.notes || undefined,
        visitDate: recordForm.visitDate,
      });
      setRecordForm(recordFormDefault);
      setReload((v) => v + 1);
    } catch (err) {
      setRecordError(err?.response?.data?.message || "Failed to create record");
    } finally {
      setRecordLoading(false);
    }
  };

  const handleCreatePrescription = async (event) => {
    event.preventDefault();
    setPrescriptionError("");
    setPrescriptionLoading(true);

    try {
      await api.post("/prescriptions", {
        medicalRecordId: Number(prescriptionForm.medicalRecordId),
        medicineName: prescriptionForm.medicineName,
        dosage: prescriptionForm.dosage,
        duration: prescriptionForm.duration,
        instructions: prescriptionForm.instructions || undefined,
      });
      setPrescriptionForm(prescriptionFormDefault);
    } catch (err) {
      setPrescriptionError(err?.response?.data?.message || "Failed to create prescription");
    } finally {
      setPrescriptionLoading(false);
    }
  };

  const handleUpload = async (event) => {
    event.preventDefault();
    setFileError("");
    setFileLoading(true);

    try {
      if (!fileToUpload) {
        throw new Error("Please choose a file");
      }
      const formData = new FormData();
      formData.append("file", fileToUpload);
      formData.append("medicalRecordId", fileRecordId);

      await api.post("/files/upload", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });

      setFileRecordId("");
      setFileToUpload(null);
    } catch (err) {
      setFileError(err?.response?.data?.message || err.message || "Upload failed");
    } finally {
      setFileLoading(false);
    }
  };

  const handleDownload = async (fileId) => {
    setDownloadError("");
    try {
      const response = await api.get(`/files/${fileId}/download`, { responseType: "blob" });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.download = `file-${fileId}`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setDownloadError(err?.response?.data?.message || "Download failed");
    }
  };

  const handleFetchFiles = async () => {
    setFileListError("");
    try {
      const response = await api.get(`/files/record/${fileListRecordId}`);
      setFiles(response.data || []);
    } catch (err) {
      setFileListError(err?.response?.data?.message || "Failed to fetch files");
      setFiles([]);
    }
  };

  const recordTitle = useMemo(() => (canCreateRecord ? "Create Medical Record" : ""), [canCreateRecord]);

  return (
    <div>
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-display">Medical Records</h1>
          <p className="text-sm text-slate-500 mt-1">Track patient history and documents.</p>
        </div>
      </div>

      {canCreateRecord ? (
        <form className="mt-6 card p-5" onSubmit={handleCreateRecord}>
          <div className="section-title">{recordTitle}</div>
          <div className="mt-4 grid grid-cols-1 md:grid-cols-4 gap-3">
            <input
              className="input"
              placeholder="Patient ID"
              value={recordForm.patientId}
              onChange={(e) => setRecordForm((prev) => ({ ...prev, patientId: e.target.value }))}
              required
            />
            <input
              className="input"
              placeholder="Diagnosis"
              value={recordForm.diagnosis}
              onChange={(e) => setRecordForm((prev) => ({ ...prev, diagnosis: e.target.value }))}
              required
            />
            <input
              className="input"
              placeholder="Notes"
              value={recordForm.notes}
              onChange={(e) => setRecordForm((prev) => ({ ...prev, notes: e.target.value }))}
            />
            <input
              className="input"
              type="date"
              value={recordForm.visitDate}
              onChange={(e) => setRecordForm((prev) => ({ ...prev, visitDate: e.target.value }))}
              required
            />
          </div>
          {recordError ? <div className="mt-3 text-sm text-red-600">{recordError}</div> : null}
          <button className="mt-4 btn-primary" type="submit" disabled={recordLoading}>
            {recordLoading ? "Saving..." : "Create Record"}
          </button>
        </form>
      ) : null}

      {canCreatePrescription ? (
        <form className="mt-6 card p-5" onSubmit={handleCreatePrescription}>
          <div className="section-title">Create Prescription</div>
          <div className="mt-4 grid grid-cols-1 md:grid-cols-5 gap-3">
            <input
              className="input"
              placeholder="Medical Record ID"
              value={prescriptionForm.medicalRecordId}
              onChange={(e) => setPrescriptionForm((prev) => ({ ...prev, medicalRecordId: e.target.value }))}
              required
            />
            <input
              className="input"
              placeholder="Medicine"
              value={prescriptionForm.medicineName}
              onChange={(e) => setPrescriptionForm((prev) => ({ ...prev, medicineName: e.target.value }))}
              required
            />
            <input
              className="input"
              placeholder="Dosage"
              value={prescriptionForm.dosage}
              onChange={(e) => setPrescriptionForm((prev) => ({ ...prev, dosage: e.target.value }))}
              required
            />
            <input
              className="input"
              placeholder="Duration"
              value={prescriptionForm.duration}
              onChange={(e) => setPrescriptionForm((prev) => ({ ...prev, duration: e.target.value }))}
              required
            />
            <input
              className="input"
              placeholder="Instructions"
              value={prescriptionForm.instructions}
              onChange={(e) => setPrescriptionForm((prev) => ({ ...prev, instructions: e.target.value }))}
            />
          </div>
          {prescriptionError ? <div className="mt-3 text-sm text-red-600">{prescriptionError}</div> : null}
          <button className="mt-4 btn-primary" type="submit" disabled={prescriptionLoading}>
            {prescriptionLoading ? "Saving..." : "Create Prescription"}
          </button>
        </form>
      ) : null}

      {canUploadFile ? (
        <form className="mt-6 card p-5" onSubmit={handleUpload}>
          <div className="section-title">Upload Medical Document</div>
          <div className="mt-4 grid grid-cols-1 md:grid-cols-3 gap-3">
            <input
              className="input"
              placeholder="Medical Record ID"
              value={fileRecordId}
              onChange={(e) => setFileRecordId(e.target.value)}
              required
            />
            <input
              className="input-file"
              type="file"
              accept=".pdf,.jpg,.jpeg,.png"
              onChange={(e) => setFileToUpload(e.target.files?.[0] || null)}
              required
            />
          </div>
          {fileError ? <div className="mt-3 text-sm text-red-600">{fileError}</div> : null}
          <button className="mt-4 btn-primary" type="submit" disabled={fileLoading}>
            {fileLoading ? "Uploading..." : "Upload File"}
          </button>
        </form>
      ) : null}

      <div className="mt-6 card p-5">
        <div className="section-title">Files for Medical Record</div>
        <div className="mt-4 flex flex-col md:flex-row gap-3">
          <input
            className="input"
            placeholder="Medical Record ID"
            value={fileListRecordId}
            onChange={(e) => setFileListRecordId(e.target.value)}
          />
          <button className="btn-primary" type="button" onClick={handleFetchFiles}>
            Fetch Files
          </button>
        </div>
        {fileListError ? <div className="mt-3 text-sm text-red-600">{fileListError}</div> : null}

        {files.length ? (
          <div className="mt-4 table-wrap">
            <table className="w-full text-sm">
              <thead className="table-head">
                <tr>
                  <th className="text-left px-4 py-2">ID</th>
                  <th className="text-left px-4 py-2">Name</th>
                  <th className="text-left px-4 py-2">Type</th>
                  <th className="text-left px-4 py-2">Uploaded</th>
                  <th className="text-left px-4 py-2">Action</th>
                </tr>
              </thead>
              <tbody>
                {files.map((file) => (
                  <tr key={file.id} className="table-row even:bg-slate-50/60">
                    <td className="px-4 py-2">{file.id}</td>
                    <td className="px-4 py-2">{file.fileName}</td>
                    <td className="px-4 py-2">{file.fileType}</td>
                    <td className="px-4 py-2">{file.uploadedAt?.slice(0, 10) || "-"}</td>
                    <td className="px-4 py-2">
                      <button
                        className="text-sm font-semibold text-brand hover:text-brandDark transition"
                        type="button"
                        onClick={() => handleDownload(file.id)}
                      >
                        Download
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : null}
      </div>

      <div className="mt-6 table-wrap">
        <div className="px-4 py-3 border-b border-slate-200 text-sm text-slate-500">
          {loading ? "Loading..." : `${items.length} record(s)`}
        </div>
        {error ? <div className="px-4 py-3 text-sm text-red-600">{error}</div> : null}
        {downloadError ? <div className="px-4 py-3 text-sm text-red-600">{downloadError}</div> : null}

        <table className="w-full text-sm">
          <thead className="table-head">
            <tr>
              <th className="text-left px-4 py-3">ID</th>
              <th className="text-left px-4 py-3">Patient</th>
              <th className="text-left px-4 py-3">Doctor</th>
              <th className="text-left px-4 py-3">Diagnosis</th>
              <th className="text-left px-4 py-3">Visit Date</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id} className="table-row even:bg-slate-50/60">
                <td className="px-4 py-3">{item.id}</td>
                <td className="px-4 py-3">{item.patientId}</td>
                <td className="px-4 py-3">{item.doctorId}</td>
                <td className="px-4 py-3">{item.diagnosis}</td>
                <td className="px-4 py-3">{item.visitDate}</td>
              </tr>
            ))}
            {!loading && items.length === 0 ? (
              <tr>
                <td colSpan="5" className="px-4 py-6 text-center text-slate-500">
                  No records found.
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
