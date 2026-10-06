import axios from "axios";
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "/api",
  timeout: 35000,
});
api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem("apiwatch.token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
api.interceptors.response.use(
  (r) => r,
  (e) => {
    if (e.response?.status === 401 && !e.config?.url?.includes("/auth/login")) {
      sessionStorage.removeItem("apiwatch.token");
      window.dispatchEvent(new Event("apiwatch:logout"));
    }
    return Promise.reject(e);
  },
);
export const errorText = (e) =>
  e.response?.data?.message ||
  (e.code === "ECONNABORTED"
    ? "The server took too long to respond. Try again; if you were registering, try signing in."
    : e.code === "ERR_NETWORK"
      ? "Cannot connect to APIWatch. Check that the backend is running."
      : e.message || "Something went wrong.");
