import { useState, useEffect } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ArrowLeft } from "lucide-react";

import { api, errorText } from "../services/api";

import { Loading, ErrorMessage, PageHeader } from "../components/UI";

const defaults = {
  name: "",
  url: "",
  httpMethod: "GET",
  description: "",
  expectedStatusCode: 200,
  monitoringInterval: 60,
  timeout: 10,
  active: true,
};
export function ApiForm() {
  const { id } = useParams(),
    navigate = useNavigate();
  const [form, setForm] = useState(defaults),
    [loading, setLoading] = useState(!!id),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  useEffect(() => {
    let alive = true;
    if (id)
      api
        .get("/apis/" + id)
        .then((r) => {
          if (alive) setForm(r.data);
        })
        .catch((e) => {
          if (alive) setError(errorText(e));
        })
        .finally(() => {
          if (alive) setLoading(false);
        });
    return () => {
      alive = false;
    };
  }, [id]);
  function change(e) {
    const { name, type, value, checked } = e.target;
    setForm((f) => ({
      ...f,
      [name]:
        type === "checkbox"
          ? checked
          : type === "number"
            ? Number(value)
            : value,
    }));
  }
  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError("");
    try {
      const { data } = await api[id ? "put" : "post"](
        "/apis" + (id ? "/" + id : ""),
        form,
      );
      navigate("/apis/" + data.id);
    } catch (e) {
      setError(errorText(e));
    } finally {
      setBusy(false);
    }
  }
  return (
    <>
      <Link className="back-link" to="/apis">
        <ArrowLeft size={16} /> Back to monitors
      </Link>
      <PageHeader
        title={id ? "Edit monitor" : "Add monitor"}
        subtitle="Choose an endpoint and define what a healthy response looks like."
      />
      <ErrorMessage message={error} />
      {loading ? (
        <Loading />
      ) : (
        <form className="panel api-form" onSubmit={submit}>
          <div className="panel-heading">
            <h2>Endpoint details</h2>
          </div>
          <div className="form-grid">
            <label>
              Monitor name
              <input
                name="name"
                required
                maxLength={120}
                value={form.name}
                onChange={change}
                placeholder="e.g. Product catalog API"
              />
            </label>
            <label>
              HTTP method
              <select
                name="httpMethod"
                value={form.httpMethod}
                onChange={change}
              >
                {["GET", "POST", "PUT", "PATCH", "DELETE"].map((m) => (
                  <option key={m}>{m}</option>
                ))}
              </select>
            </label>
            <label className="span-two">
              Endpoint URL
              <input
                name="url"
                type="url"
                required
                maxLength={2048}
                value={form.url}
                onChange={change}
                placeholder="https://api.example.com/health"
              />
            </label>
            <label className="span-two">
              Description <span className="muted">(optional)</span>
              <textarea
                name="description"
                maxLength={2000}
                value={form.description || ""}
                onChange={change}
                placeholder="What does this endpoint do?"
              />
            </label>
            <label>
              Expected HTTP status
              <input
                name="expectedStatusCode"
                type="number"
                min="100"
                max="599"
                required
                value={form.expectedStatusCode}
                onChange={change}
              />
            </label>
            <label>
              Check interval (seconds)
              <input
                name="monitoringInterval"
                type="number"
                min="10"
                max="86400"
                required
                value={form.monitoringInterval}
                onChange={change}
              />
            </label>
            <label>
              Request timeout (seconds)
              <input
                name="timeout"
                type="number"
                min="1"
                max="30"
                required
                value={form.timeout}
                onChange={change}
              />
            </label>
            <label className="checkbox">
              <input
                name="active"
                type="checkbox"
                checked={form.active}
                onChange={change}
              />{" "}
              Enable monitoring
            </label>
          </div>
          {form.httpMethod !== "GET" && (
            <p className="notice">
              This monitor repeatedly sends {form.httpMethod} requests with an
              empty body. Use only a dedicated test endpoint where repeated
              requests are safe.
            </p>
          )}
          <div className="form-actions">
            <Link className="button" to="/apis">
              Cancel
            </Link>
            <button className="primary" disabled={busy}>
              {busy ? "Saving…" : id ? "Save changes" : "Create monitor"}
            </button>
          </div>
        </form>
      )}
    </>
  );
}
