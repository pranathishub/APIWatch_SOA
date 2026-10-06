import { Link } from "react-router-dom";
import { Activity, Pause, Play, Pencil, Trash2 } from "lucide-react";

import { ms, time } from "../utils/format";
import { Badge } from "../components/UI";

export function ApiTable({ items, onToggle, onDelete, busy }) {
  return (
    <div className="table-scroll">
      <table>
        <thead>
          <tr>
            <th>API NAME / ENDPOINT</th>
            <th>STATUS</th>
            <th>RESPONSE TIME</th>
            <th>HTTP CODE</th>
            <th>LAST CHECKED</th>
            <th>ACTIONS</th>
          </tr>
        </thead>
        <tbody>
          {items.map((a) => (
            <tr key={a.id}>
              <td>
                <Link className="api-name" to={"/apis/" + a.id}>
                  <span className="endpoint-icon">
                    <Activity size={17} />
                  </span>
                  {a.name}
                </Link>
                <div className="endpoint-url">
                  <span>{a.httpMethod}</span> {a.url}
                </div>
              </td>
              <td>
                <Badge status={a.active ? a.lastStatus : "PAUSED"} />
              </td>
              <td className="mono">{ms(a.lastResponseTime)}</td>
              <td className="mono">{a.lastStatusCode || "—"}</td>
              <td className="muted small">{time(a.lastChecked)}</td>
              <td>
                <div className="row-actions">
                  <Link
                    aria-label={"Edit " + a.name}
                    title="Edit monitor"
                    to={"/apis/" + a.id + "/edit"}
                  >
                    <Pencil size={16} />
                  </Link>
                  {onToggle && (
                    <button
                      disabled={busy}
                      aria-label={(a.active ? "Pause " : "Resume ") + a.name}
                      title={a.active ? "Pause" : "Resume"}
                      onClick={() => onToggle(a)}
                    >
                      {a.active ? <Pause size={16} /> : <Play size={16} />}
                    </button>
                  )}
                  {onDelete && (
                    <button
                      disabled={busy}
                      aria-label={"Delete " + a.name}
                      title="Delete monitor"
                      onClick={() => onDelete(a)}
                    >
                      <Trash2 size={16} />
                    </button>
                  )}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
