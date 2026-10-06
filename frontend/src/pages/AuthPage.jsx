import { useState, useEffect } from "react";
import { Link, Navigate } from "react-router-dom";
import { Activity, ArrowUpRight, ShieldCheck } from "lucide-react";
import { useAuth } from "../context/AuthContext";

import { errorText } from "../services/api";

import { ErrorMessage } from "../components/UI";

export function AuthPage({ register = false }) {
  const { user, authenticate } = useAuth();
  const [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  useEffect(() => setError(""), [register]);
  if (user) return <Navigate to="/" replace />;
  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError("");
    try {
      await authenticate(
        register ? "register" : "login",
        Object.fromEntries(new FormData(e.currentTarget)),
      );
    } catch (e) {
      setError(errorText(e));
    } finally {
      setBusy(false);
    }
  }
  return (
    <div className="auth-page" data-theme="dark">
      <div className="auth-story">
        <div className="brand">
          <span className="brand-icon">
            <Activity />
          </span>
          APIWatch
        </div>
        <div>
          <span className="eyebrow">KNOW BEFORE YOUR USERS DO</span>
          <h1>
            Every endpoint.
            <br />
            Every heartbeat.
          </h1>
          <p>
            Keep a clear view of your APIs. Track availability, understand
            performance, and catch failures as they happen.
          </p>
          <div className="auth-features">
            <span>
              <Activity size={18} /> Continuous monitoring
            </span>
            <span>
              <ShieldCheck size={18} /> Your APIs, securely managed
            </span>
          </div>
        </div>
        <small>API Monitoring & Performance Tracking System</small>
      </div>
      <div className="auth-panel">
        <form key={register ? "register" : "login"} onSubmit={submit}>
          <span className="eyebrow">YOUR MONITORING WORKSPACE</span>
          <h2>{register ? "Create your account" : "Welcome back"}</h2>
          <p>
            {register
              ? "Start monitoring your first API."
              : "Sign in to see how your APIs are doing."}
          </p>
          <ErrorMessage message={error} />
          <label>
            Username
            <input
              name="username"
              autoComplete="username"
              required
              minLength={register ? 3 : 1}
              maxLength={40}
              pattern={register ? "[A-Za-z0-9_]{3,40}" : undefined}
              placeholder="Your username"
            />
          </label>
          {register && (
            <label>
              Email address
              <input
                name="email"
                type="email"
                autoComplete="email"
                required
                placeholder="you@example.com"
              />
            </label>
          )}
          <label>
            Password
            <input
              name="password"
              type="password"
              autoComplete={register ? "new-password" : "current-password"}
              required
              minLength={register ? 8 : 1}
              maxLength={72}
              placeholder={
                register ? "At least 8 characters" : "Enter your password"
              }
            />
          </label>
          <button className="primary full" disabled={busy}>
            {busy ? "Please wait…" : register ? "Create account" : "Sign in"}
            <ArrowUpRight size={18} />
          </button>
          <div className="auth-switch">
            {register ? "Already have an account?" : "New to APIWatch?"}{" "}
            <Link to={register ? "/login" : "/register"}>
              {register ? "Sign in" : "Create an account"}
            </Link>
          </div>
        </form>
      </div>
    </div>
  );
}
