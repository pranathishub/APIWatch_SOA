import { ms, percent } from "../utils/format";
import { Stat } from "../components/UI";

export function AnalyticsStats({ stats }) {
  return (
    <div className="stats analytics-stats">
      <Stat label="Uptime" value={percent(stats.uptime)} accent="green" />
      <Stat label="Downtime" value={percent(stats.downtime)} accent="red" />
      <Stat
        label="Average response"
        value={stats.totalRequests ? ms(stats.averageResponseTime) : "—"}
      />
      <Stat
        label="Minimum response"
        value={stats.totalRequests ? ms(stats.minimumResponseTime) : "—"}
      />
      <Stat
        label="Maximum response"
        value={stats.totalRequests ? ms(stats.maximumResponseTime) : "—"}
      />
      <Stat
        label="Total requests"
        value={stats.totalRequests.toLocaleString()}
      />
      <Stat
        label="Successful"
        value={stats.successfulRequests.toLocaleString()}
        accent="green"
      />
      <Stat
        label="Failed"
        value={stats.failedRequests.toLocaleString()}
        accent="red"
      />
    </div>
  );
}
