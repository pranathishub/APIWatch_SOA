import { ShieldCheck } from "lucide-react";
import { useAuth } from "../context/AuthContext";

import { time } from "../utils/format";
import { PageHeader } from "../components/UI";

export function SettingsPage() {
  const { user, logout } = useAuth();
  return (
    <>
      <PageHeader
        title="Profile & settings"
        subtitle="Your account and monitoring preferences."
      />
      <section className="panel settings-panel">
        <div className="panel-heading">
          <h2>Administrator profile</h2>
          <ShieldCheck size={20} className="green" />
        </div>
        <dl>
          <dt>Username</dt>
          <dd>{user.username}</dd>
          <dt>Email</dt>
          <dd>{user.email}</dd>
          <dt>Role</dt>
          <dd>{user.role}</dd>
          <dt>Joined</dt>
          <dd>{time(user.createdAt)}</dd>
        </dl>
        <button onClick={logout}>Sign out</button>
      </section>
      <section className="panel settings-panel">
        <h2>Workspace preferences</h2>
        <p>
          Use the sun or moon button in the top bar to switch themes. Your theme
          is saved on this device.
        </p>
        <p>
          Dashboard data refreshes every 10 seconds. Configure check intervals
          and timeouts separately for each monitor.
        </p>
        <p>
          Alerts are available in this dashboard. Email notifications are not
          enabled in this version.
        </p>
      </section>
    </>
  );
}
