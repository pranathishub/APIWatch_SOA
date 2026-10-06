import { useState } from "react";
import { Link } from "react-router-dom";
import { Plus, Search } from "lucide-react";

import { useResource } from "../hooks/useResource";
import { api, errorText } from "../services/api";

import {
  Loading,
  Empty,
  ErrorMessage,
  PageHeader,
  Modal,
} from "../components/UI";

import { ApiTable } from "../components/ApiTable";

export function ApiList() {
  const resource = useResource("/apis");
  const [search, setSearch] = useState(""),
    [filter, setFilter] = useState("ALL"),
    [deleting, setDeleting] = useState(null),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  async function toggle(a) {
    setBusy(true);
    setError("");
    try {
      await api.put("/apis/" + a.id, { ...a, active: !a.active });
      resource.reload();
    } catch (e) {
      setError(errorText(e));
    } finally {
      setBusy(false);
    }
  }
  async function remove() {
    setBusy(true);
    setError("");
    try {
      await api.delete("/apis/" + deleting.id);
      setDeleting(null);
      resource.reload();
    } catch (e) {
      setError(errorText(e));
    } finally {
      setBusy(false);
    }
  }
  const items = (resource.data || []).filter(
    (a) =>
      (a.name + " " + a.url).toLowerCase().includes(search.toLowerCase()) &&
      (filter === "ALL" || (a.active ? a.lastStatus : "PAUSED") === filter),
  );
  return (
    <>
      <PageHeader
        title="API monitors"
        subtitle="Manage endpoints and configure their monitoring schedules."
      >
        <Link className="primary" to="/apis/new">
          <Plus size={17} /> Add monitor
        </Link>
      </PageHeader>
      <ErrorMessage message={resource.error || error} retry={resource.reload} />
      <section className="panel">
        <div className="toolbar">
          <div className="search">
            <Search size={17} />
            <input
              aria-label="Search APIs"
              placeholder="Search name or endpoint…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <select
            aria-label="Filter by status"
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
          >
            {["ALL", "UP", "DOWN", "PENDING", "PAUSED"].map((v) => (
              <option key={v}>{v}</option>
            ))}
          </select>
        </div>
        {resource.loading ? (
          <Loading />
        ) : items.length ? (
          <ApiTable
            items={items}
            onToggle={toggle}
            onDelete={setDeleting}
            busy={busy}
          />
        ) : (
          <Empty
            title={
              search || filter !== "ALL"
                ? "No matching monitors"
                : "No monitors yet"
            }
          >
            Add an API to begin monitoring its availability.
          </Empty>
        )}
      </section>
      {deleting && (
        <Modal
          title="Delete monitor?"
          onClose={() => !busy && setDeleting(null)}
        >
          <p>
            Delete <strong>{deleting.name}</strong> and all its monitoring
            history and alerts? This cannot be undone.
          </p>
          <ErrorMessage message={error} />
          <div className="form-actions">
            <button onClick={() => setDeleting(null)} disabled={busy}>
              Cancel
            </button>
            <button className="danger" onClick={remove} disabled={busy}>
              {busy ? "Deleting…" : "Delete monitor"}
            </button>
          </div>
        </Modal>
      )}
    </>
  );
}
