import { useState } from "react";

import { useResource } from "../hooks/useResource";

import { Loading, ErrorMessage, PageHeader } from "../components/UI";
import { ResponseChart, OutcomeChart, UptimeChart } from "../components/Charts";

import { AnalyticsStats } from "../components/AnalyticsStats";
export function Analytics() {
  const monitors = useResource("/apis"),
    [selected, setSelected] = useState("");
  const resource = useResource("/analytics" + (selected ? "/" + selected : ""));
  return (
    <>
      <PageHeader
        title="Performance analytics"
        subtitle="Availability and response metrics across all recorded checks."
      >
        <select
          aria-label="Select API"
          value={selected}
          onChange={(e) => setSelected(e.target.value)}
        >
          <option value="">All APIs</option>
          {monitors.data?.map((a) => (
            <option key={a.id} value={a.id}>
              {a.name}
            </option>
          ))}
        </select>
      </PageHeader>
      <ErrorMessage
        message={resource.error || monitors.error}
        retry={resource.reload}
      />
      {resource.loading ? (
        <Loading />
      ) : (
        resource.data && (
          <>
            <AnalyticsStats stats={resource.data} />
            <ResponseChart series={resource.data.series} />
            <div className="two-columns">
              <UptimeChart stats={resource.data} />
              <OutcomeChart stats={resource.data} />
            </div>
            <p className="muted small">
              Uptime is based on successful checks, not elapsed time. Paused
              periods have no checks and do not affect this percentage.
            </p>
          </>
        )
      )}
    </>
  );
}
