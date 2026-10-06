import { useState } from "react";
import { Link } from "react-router-dom";
import { CheckCircle2 } from "lucide-react";

import { useResource } from "../hooks/useResource";
import { api, errorText } from "../services/api";
import { time } from "../utils/format";
import {
  Badge,
  Loading,
  Empty,
  ErrorMessage,
  PageHeader,
  Pagination,
} from "../components/UI";

export function Alerts() {
  const [page, setPage] = useState(0),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(null);
  const resource = useResource("/alerts?page=" + page);
  async function resolve(id) {
    setBusy(id);
    setError("");
    try {
      await api.put("/alerts/" + id + "/resolve");
      resource.reload();
    } catch (e) {
      setError(errorText(e));
    } finally {
      setBusy(null);
    }
  }
  return (
    <>
      <PageHeader
        title="Alerts & incidents"
        subtitle="Failures open an incident. Recovery automatically resolves it."
      />
      <ErrorMessage message={error || resource.error} retry={resource.reload} />
      <section className="panel">
        <div className="panel-heading">
          <h2>Incident log</h2>
          <span className="muted">
            {resource.data?.totalElements || 0} recorded alerts
          </span>
        </div>
        {resource.loading ? (
          <Loading />
        ) : resource.data?.content.length ? (
          <>
            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    {[
                      "API / MESSAGE",
                      "TYPE",
                      "OPENED",
                      "STATUS",
                      "ACTION",
                    ].map((h) => (
                      <th key={h}>{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {resource.data.content.map((a) => (
                    <tr key={a.id}>
                      <td>
                        <Link className="api-name" to={"/apis/" + a.apiId}>
                          {a.apiName}
                        </Link>
                        <p className="alert-message">{a.message}</p>
                      </td>
                      <td>
                        <Badge status={a.alertType} />
                      </td>
                      <td className="small">{time(a.createdAt)}</td>
                      <td>
                        <Badge
                          status={a.resolved ? "RESOLVED" : "UNRESOLVED"}
                        />
                        {a.resolved && (
                          <small className="resolved-time">
                            {time(a.resolvedAt)}
                          </small>
                        )}
                      </td>
                      <td>
                        {a.resolved ? (
                          <CheckCircle2 className="green" size={18} />
                        ) : (
                          <button
                            disabled={busy === a.id}
                            onClick={() => resolve(a.id)}
                          >
                            {busy === a.id ? "Resolving…" : "Resolve"}
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Pagination
              page={page}
              setPage={setPage}
              totalPages={resource.data.totalPages}
            />
          </>
        ) : (
          <Empty title="No incidents recorded">
            Alerts appear when a monitor first fails or changes from UP to DOWN.
          </Empty>
        )}
      </section>
    </>
  );
}
