import { useState } from "react";
import {
  Routes,
  Route,
  Navigate,
  NavLink,
  Outlet,
  useLocation,
} from "react-router-dom";
import {
  Activity,
  LayoutDashboard,
  Server,
  History,
  ChartNoAxesCombined,
  Bell,
  Settings,
  Sun,
  Moon,
  LogOut,
  Menu,
} from "lucide-react";
import { useAuth } from "./context/AuthContext";
import { Loading } from "./components/UI";
import {
  AuthPage,
  Dashboard,
  ApiList,
  ApiForm,
  ApiDetail,
  HistoryPage,
  Analytics,
  Alerts,
  SettingsPage,
} from "./pages/Pages";
const navigation = [
  ["/", "Overview", LayoutDashboard],
  ["/apis", "API monitors", Server],
  ["/history", "Monitoring history", History],
  ["/analytics", "Analytics", ChartNoAxesCombined],
  ["/alerts", "Alerts", Bell],
  ["/settings", "Settings", Settings],
];
function Layout() {
  const { user, loading, logout } = useAuth();
  const [open, setOpen] = useState(false),
    [theme, setTheme] = useState(
      () => localStorage.getItem("apiwatch.theme") || "dark",
    );
  const location = useLocation();
  if (loading) return <Loading />;
  if (!user) return <Navigate to="/login" replace />;
  function toggle() {
    const next = theme === "dark" ? "light" : "dark";
    setTheme(next);
    localStorage.setItem("apiwatch.theme", next);
  }
  return (
    <div className="app" data-theme={theme}>
      <aside className={open ? "sidebar open" : "sidebar"}>
        <div className="brand">
          <span className="brand-icon">
            <Activity size={24} />
          </span>
          APIWatch<span className="version">v1.0</span>
        </div>
        <div className="workspace-label">MONITORING WORKSPACE</div>
        <nav>
          {navigation.map(([to, label, Icon]) => (
            <NavLink
              key={to}
              to={to}
              end={to === "/"}
              onClick={() => setOpen(false)}
            >
              <Icon size={19} />
              {label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <div className="local-note">
            <span className="live-dot" /> Automatic checks enabled
            <small>Monitor intervals are configurable</small>
          </div>
          <div className="account">
            <span className="avatar">
              {user.username.slice(0, 2).toUpperCase()}
            </span>
            <div>
              <b>{user.username}</b>
              <small>Administrator</small>
            </div>
            <button aria-label="Log out" title="Log out" onClick={logout}>
              <LogOut size={18} />
            </button>
          </div>
        </div>
      </aside>
      {open && (
        <div className="mobile-overlay" onClick={() => setOpen(false)} />
      )}
      <div className="main">
        <header className="topbar">
          <div>
            <button
              className="mobile-menu"
              aria-label="Toggle navigation"
              onClick={() => setOpen(!open)}
            >
              <Menu size={20} />
            </button>
            <span className="breadcrumb">
              Workspace <span>/</span>{" "}
              <b>
                {navigation.find(([to]) =>
                  to === "/"
                    ? location.pathname === "/"
                    : location.pathname.startsWith(to),
                )?.[1] || "API monitors"}
              </b>
            </span>
          </div>
          <div className="top-actions">
            <span className="live-label">
              <span className="live-dot" /> Refreshes every 10s
            </span>
            <button
              aria-label="Toggle dark/light mode"
              title="Toggle theme"
              onClick={toggle}
            >
              {theme === "dark" ? <Sun size={18} /> : <Moon size={18} />}
            </button>
            <NavLink className="icon-button" aria-label="Alerts" to="/alerts">
              <Bell size={18} />
            </NavLink>
          </div>
        </header>
        <main>
          <Outlet />
        </main>
        <footer>
          APIWatch <span>API Monitoring & Performance Tracking</span>
        </footer>
      </div>
    </div>
  );
}
export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<AuthPage />} />
      <Route path="/register" element={<AuthPage register />} />
      <Route element={<Layout />}>
        <Route index element={<Dashboard />} />
        <Route path="apis" element={<ApiList />} />
        <Route path="apis/new" element={<ApiForm />} />
        <Route path="apis/:id/edit" element={<ApiForm />} />
        <Route path="apis/:id" element={<ApiDetail />} />
        <Route path="history" element={<HistoryPage />} />
        <Route path="analytics" element={<Analytics />} />
        <Route path="alerts" element={<Alerts />} />
        <Route path="settings" element={<SettingsPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
