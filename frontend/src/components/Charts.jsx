import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
} from "recharts";
import { Empty } from "./UI";
const colors = ["#26bd94", "#f06b79", "#dfb55d"];
export function ResponseChart({ series = [] }) {
  return (
    <section className="panel">
      <div className="panel-heading">
        <h2>Response time</h2>
        <span className="muted">Latest {series.length} checks · ms</span>
      </div>
      {!series.length ? (
        <Empty />
      ) : (
        <div className="chart">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart
              data={series.map((x) => ({
                ...x,
                label: new Date(x.checkedAt).toLocaleTimeString(),
              }))}
            >
              <defs>
                <linearGradient id="area" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#26bd94" stopOpacity={0.3} />
                  <stop offset="100%" stopColor="#26bd94" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid stroke="var(--border)" vertical={false} />
              <XAxis
                dataKey="label"
                minTickGap={60}
                tick={{ fill: "var(--muted)", fontSize: 11 }}
              />
              <YAxis tick={{ fill: "var(--muted)", fontSize: 11 }} />
              <Tooltip
                contentStyle={{
                  background: "var(--panel)",
                  border: "1px solid var(--border)",
                  borderRadius: 8,
                }}
              />
              <Area
                isAnimationActive={false}
                type="monotone"
                name="Response time (ms)"
                dataKey="responseTime"
                stroke="#26bd94"
                fill="url(#area)"
                strokeWidth={2}
              />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      )}
    </section>
  );
}
export function Distribution({ apis = [] }) {
  const data = ["UP", "DOWN", "PENDING"].map((name) => ({
    name,
    value: apis.filter((a) => a.lastStatus === name && a.active).length,
  }));
  return (
    <section className="panel">
      <div className="panel-heading">
        <h2>Monitor status</h2>
        <span className="muted">Active APIs</span>
      </div>
      {data.every((x) => !x.value) ? (
        <Empty title="No active monitors" />
      ) : (
        <>
          <div className="chart donut">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  isAnimationActive={false}
                  data={data}
                  dataKey="value"
                  innerRadius={65}
                  outerRadius={86}
                  paddingAngle={5}
                  stroke="none"
                >
                  {data.map((x, i) => (
                    <Cell key={x.name} fill={colors[i]} />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={{
                    background: "var(--panel)",
                    border: "1px solid var(--border)",
                  }}
                />
              </PieChart>
            </ResponsiveContainer>
          </div>
          <div className="legend">
            {data.map((x, i) => (
              <span key={x.name}>
                <i style={{ background: colors[i] }} />
                {x.name} <b>{x.value}</b>
              </span>
            ))}
          </div>
        </>
      )}
    </section>
  );
}
export function OutcomeChart({ stats }) {
  return (
    <section className="panel">
      <div className="panel-heading">
        <h2>Request outcomes</h2>
        <span className="muted">All recorded checks</span>
      </div>
      {!stats.totalRequests ? (
        <Empty />
      ) : (
        <div className="chart">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart
              data={[
                { name: "Successful", count: stats.successfulRequests },
                { name: "Failed", count: stats.failedRequests },
              ]}
            >
              <CartesianGrid vertical={false} stroke="var(--border)" />
              <XAxis dataKey="name" />
              <YAxis allowDecimals={false} />
              <Tooltip contentStyle={{ background: "var(--panel)" }} />
              <Bar
                isAnimationActive={false}
                dataKey="count"
                radius={[5, 5, 0, 0]}
                maxBarSize={75}
              >
                {colors.slice(0, 2).map((c) => (
                  <Cell key={c} fill={c} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}
    </section>
  );
}
export function UptimeChart({ stats }) {
  return (
    <section className="panel">
      <div className="panel-heading">
        <h2>Availability</h2>
        <span className="muted">Percentage of checks</span>
      </div>
      {!stats.totalRequests ? (
        <Empty />
      ) : (
        <>
          <div className="availability">
            {stats.uptime.toFixed(2)}
            <small>% uptime</small>
          </div>
          <div className="uptime-track">
            <div style={{ width: `${stats.uptime}%` }} />
          </div>
          <div className="legend">
            <span>UP {stats.uptime.toFixed(2)}%</span>
            <span>DOWN {stats.downtime.toFixed(2)}%</span>
          </div>
        </>
      )}
    </section>
  );
}
