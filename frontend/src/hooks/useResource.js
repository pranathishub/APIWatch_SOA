import { useEffect, useState, useCallback } from "react";
import { api, errorText } from "../services/api";
export function useResource(url, refresh = 10000) {
  const [data, setData] = useState(null),
    [error, setError] = useState(""),
    [loading, setLoading] = useState(true),
    [version, setVersion] = useState(0);
  const reload = useCallback(() => setVersion((v) => v + 1), []);
  useEffect(() => {
    let alive = true,
      busy = false;
    setData(null);
    setLoading(true);
    async function load() {
      if (busy) return;
      busy = true;
      try {
        const r = await api.get(url);
        if (alive) {
          setData(r.data);
          setError("");
        }
      } catch (e) {
        if (alive) setError(errorText(e));
      } finally {
        busy = false;
        if (alive) setLoading(false);
      }
    }
    load();
    const timer = refresh ? setInterval(load, refresh) : null;
    return () => {
      alive = false;
      clearInterval(timer);
    };
  }, [url, refresh, version]);
  return { data, error, loading, reload };
}
