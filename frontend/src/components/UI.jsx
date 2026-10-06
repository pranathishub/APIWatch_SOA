import { Activity, RefreshCw, X } from "lucide-react";
export function Badge({ status }) {
  return (
    <span className={`badge ${status?.toLowerCase()}`}>
      <i />
      {status || "PENDING"}
    </span>
  );
}
export function Loading() {
  return (
    <div className="empty">
      <RefreshCw className="spin" size={22} /> Loading monitoring data…
    </div>
  );
}
export function ErrorMessage({ message, retry }) {
  return message ? (
    <div role="alert" className="error">
      {message}
      {retry && <button onClick={retry}>Try again</button>}
    </div>
  ) : null;
}
export function Empty({ title = "No data yet", children }) {
  return (
    <div className="empty">
      <Activity size={30} />
      <h3>{title}</h3>
      <p>
        {children || "Results will appear after the first scheduled check."}
      </p>
    </div>
  );
}
export function Stat({ label, value, detail, accent }) {
  return (
    <div className="stat">
      <span>{label}</span>
      <strong className={accent || ""}>{value}</strong>
      <small>{detail}</small>
    </div>
  );
}
export function PageHeader({
  eyebrow = "WORKSPACE",
  title,
  subtitle,
  children,
}) {
  return (
    <div className="page-heading">
      <div>
        <div className="eyebrow">{eyebrow}</div>
        <h1>{title}</h1>
        <p>{subtitle}</p>
      </div>
      <div className="header-actions">{children}</div>
    </div>
  );
}
export function Pagination({ page, setPage, totalPages = 0 }) {
  return (
    <div className="pagination">
      <span>
        Page {Math.min(page + 1, Math.max(1, totalPages))} of{" "}
        {Math.max(1, totalPages)}
      </span>
      <button disabled={page === 0} onClick={() => setPage(page - 1)}>
        Previous
      </button>
      <button
        disabled={page + 1 >= totalPages}
        onClick={() => setPage(page + 1)}
      >
        Next
      </button>
    </div>
  );
}
export function Modal({ title, children, onClose }) {
  return (
    <div className="modal-backdrop" onClick={onClose}>
      <section
        role="dialog"
        aria-modal="true"
        aria-label={title}
        className="modal"
        onClick={(e) => e.stopPropagation()}
        onKeyDown={(e) => {
          if (e.key === "Escape") onClose();
          if (e.key === "Tab") {
            const nodes = e.currentTarget.querySelectorAll(
              "button:not(:disabled),input,a[href]",
            );
            const first = nodes[0],
              last = nodes[nodes.length - 1];
            if (e.shiftKey && document.activeElement === first) {
              e.preventDefault();
              last.focus();
            } else if (!e.shiftKey && document.activeElement === last) {
              e.preventDefault();
              first.focus();
            }
          }
        }}
      >
        <div className="panel-heading">
          <h2>{title}</h2>
          <button autoFocus aria-label="Close" onClick={onClose}>
            <X size={18} />
          </button>
        </div>
        {children}
      </section>
    </div>
  );
}
