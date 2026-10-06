import { ms, percent } from "../utils/format";
import { Stat } from "../components/UI";

export function Stats({ apis, stats }) {
  const active = apis.filter((a) => a.active);
  return (
    <div className="stats">
      <Stat
        label="Total APIs"
        value={apis.length}
        detail={active.length + " actively monitored"}
      />
      <Stat
        label="APIs up"
        value={active.filter((a) => a.lastStatus === "UP").length}
        detail="Latest check succeeded"
        accent="green"
      />
      <Stat
        label="APIs down"
        value={active.filter((a) => a.lastStatus === "DOWN").length}
        detail="Needs your attention"
        accent="red"
      />
      <Stat
        label="Avg. response time"
        value={stats.totalRequests ? ms(stats.averageResponseTime) : "—"}
        detail="Across all recorded checks"
      />
      <Stat
        label="Uptime"
        value={percent(stats.uptime)}
        detail="Successful checks / total"
        accent="green"
      />
    </div>
  );
}
