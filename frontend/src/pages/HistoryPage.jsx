import { useState } from "react";
import { Link } from "react-router-dom";

import { useResource } from "../hooks/useResource";

import { ms, time } from "../utils/format";
import {
  Badge,
  Loading,
  Empty,
  ErrorMessage,
  PageHeader,
  Pagination,
} from "../components/UI";

export function HistoryPage() {
  const monitors = useResource("/apis");
  const [filters, setFilters] = useState({
      api: "",
      date: "",
      status: "",
      statusCode: "",
    }),
    [page, setPage] = useState(0);
  const params = new URLSearchParams({
    page,
    size: 20,
    ...Object.fromEntries(Object.entries(filters).filter(([, v]) => v)),
  });
  const resource = useResource("/monitoring?" + params);
  function change(e) {
    setFilters((f) => ({ ...f, [e.target.name]: e.target.value }));
    setPage(0);
  }
  return (
    <>
      <PageHeader
        title="Monitoring history"
        subtitle="Inspect every check, response, and failure. Date filters use UTC."
      />
      <ErrorMessage
        message={resource.error || monitors.error}
        retry={resource.reload}
      />
      <section className="panel">
        <div className="toolbar filters">
          <label>
            API
            <select name="api" value={filters.api} onChange={change}>
              <option value="">All APIs</option>
              {monitors.data?.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Date (UTC)
            <input
              name="date"
              type="date"
              value={filters.date}
              onChange={change}
            />
          </label>
          <label>
            Status
            <select name="status" value={filters.status} onChange={change}>
              <option value="">All statuses</option>
              <option>UP</option>
              <option>DOWN</option>
            </select>
          </label>
          <label>
            HTTP status code
            <input
              name="statusCode"
              type="number"
              min="100"
              max="599"
              placeholder="Any code"
              value={filters.statusCode}
              onChange={change}
            />
          </label>
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
                      "TIMESTAMP",
                      "API",
                      "STATUS",
                      "RESPONSE TIME",
                      "HTTP CODE",
                      "ERROR",
                    ].map((h) => (
                      <th key={h}>{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {resource.data.content.map((l) => (
                    <tr key={l.id}>
                      <td className="small">{time(l.checkedAt)}</td>
                      <td>
                        <Link to={"/apis/" + l.apiId}>{l.apiName}</Link>
                      </td>
                      <td>
                        <Badge status={l.status} />
                      </td>
                      <td className="mono">{ms(l.responseTime)}</td>
                      <td>{l.statusCode || "—"}</td>
                      <td className="error-cell">{l.errorMessage || "—"}</td>
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
          <Empty title="No checks match these filters" />
        )}
      </section>
    </>
  );
}
