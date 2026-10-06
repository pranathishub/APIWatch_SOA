import { Link } from "react-router-dom";
import { Plus, ArrowUpRight } from "lucide-react";

import { useResource } from "../hooks/useResource";

import { Loading, Empty, ErrorMessage, PageHeader } from "../components/UI";
import {
  ResponseChart,
  Distribution,
  OutcomeChart,
  UptimeChart,
} from "../components/Charts";
import { Stats } from "../components/Stats";
import { ApiTable } from "../components/ApiTable";

export function Dashboard() {
  const monitors = useResource("/apis"),
    stats = useResource("/analytics"),
    alerts = useResource("/alerts/unresolved-count");
  return (
    <>
      <PageHeader
        title="Overview"
        subtitle="A clear view of your API health and performance."
      >
        <Link className="primary" to="/apis/new">
          <Plus size={17} /> Add monitor
        </Link>
      </PageHeader>
      <ErrorMessage
        message={monitors.error || stats.error || alerts.error}
        retry={() => {
          monitors.reload();
          stats.reload();
          alerts.reload();
        }}
      />
      {monitors.loading || stats.loading ? (
        <Loading />
      ) : (
        monitors.data &&
        stats.data && (
          <>
            <Stats apis={monitors.data} stats={stats.data} />
            {alerts.data?.count > 0 && (
              <Link className="incident" to="/alerts">
                <span className="badge down">ATTENTION</span> Unresolved
                incidents need a look <ArrowUpRight size={17} />
              </Link>
            )}
            <div className="chart-grid">
              <ResponseChart series={stats.data.series} />
              <Distribution apis={monitors.data} />
            </div>
            <section className="panel">
              <div className="panel-heading">
                <h2>
                  API monitors{" "}
                  <span className="count">{monitors.data.length}</span>
                </h2>
                <Link to="/apis">View all monitors →</Link>
              </div>
              {monitors.data.length ? (
                <ApiTable items={monitors.data.slice(0, 6)} />
              ) : (
                <Empty title="Your first monitor starts here">
                  Add an API endpoint to start collecting real availability and
                  performance data. <Link to="/apis/new">Add monitor →</Link>
                </Empty>
              )}
            </section>
            <div className="two-columns">
              <OutcomeChart stats={stats.data} />
              <UptimeChart stats={stats.data} />
            </div>
          </>
        )
      )}
    </>
  );
}
