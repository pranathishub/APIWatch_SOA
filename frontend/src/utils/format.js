export const ms = (n) =>
  n == null ? "—" : `${Math.round(n).toLocaleString()} ms`;
export const percent = (n) => (n == null ? "—" : `${Number(n).toFixed(2)}%`);
export const time = (v) =>
  v ? new Date(v).toLocaleString() : "Not checked yet";
