import { createContext, useContext, useState, useEffect } from "react";
import { api } from "../services/api";
const Context = createContext();
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null),
    [loading, setLoading] = useState(true);
  useEffect(() => {
    const logout = () => setUser(null);
    window.addEventListener("apiwatch:logout", logout);
    if (sessionStorage.getItem("apiwatch.token"))
      api
        .get("/auth/me")
        .then((r) => setUser(r.data))
        .catch(() => sessionStorage.removeItem("apiwatch.token"))
        .finally(() => setLoading(false));
    else setLoading(false);
    return () => window.removeEventListener("apiwatch:logout", logout);
  }, []);
  async function authenticate(mode, values) {
    const { data } = await api.post(`/auth/${mode}`, values);
    sessionStorage.setItem("apiwatch.token", data.token);
    setUser(data.user);
  }
  function logout() {
    sessionStorage.removeItem("apiwatch.token");
    setUser(null);
  }
  return (
    <Context.Provider value={{ user, loading, authenticate, logout }}>
      {children}
    </Context.Provider>
  );
}
export const useAuth = () => useContext(Context);
