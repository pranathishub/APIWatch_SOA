import { Link, useParams } from "react-router-dom";
import { Pencil, ArrowLeft } from "lucide-react";

import { useResource } from "../hooks/useResource";

import { Badge, Loading, ErrorMessage, PageHeader } from "../components/UI";
import { ResponseChart, OutcomeChart, UptimeChart } from "../components/Charts";

import { AnalyticsStats } from "../components/AnalyticsStats";
export function ApiDetail() {
  const { id } = useParams(),
    resource = useResource("/apis/" + id),
    stats = useResource("/analytics/" + id);
  return (
    <>
      <Link className="back-link" to="/apis">
        <ArrowLeft size={16} /> Back to monitors
      </Link>
      <ErrorMessage
        message={resource.error || stats.error}
        retry={() => {
          resource.reload();
          stats.reload();
        }}
      />
      {resource.loading || stats.loading ? (
        <Loading />
      ) : (
        resource.data &&
        stats.data && (
          <>
            <PageHeader
              title={resource.data.name}
              subtitle={resource.data.description || resource.data.url}
            >
              <Badge
                status={
                  resource.data.active ? resource.data.lastStatus : "PAUSED"
                }
              />
              <Link className="button" to={"/apis/" + id + "/edit"}>
                <Pencil size={16} /> Edit monitor
              </Link>
            </PageHeader>
            <section className="panel endpoint-details">
              <div>
                <small>ENDPOINT</small>
                <p className="mono">
                  {resource.data.httpMethod} {resource.data.url}
                </p>
              </div>
              <div>
                <small>INTERVAL</small>
                <p>{resource.data.monitoringInterval}s</p>
              </div>
              <div>
                <small>TIMEOUT</small>
                <p>{resource.data.timeout}s</p>
              </div>
              <div>
                <small>EXPECTED STATUS</small>
                <p>{resource.data.expectedStatusCode}</p>
              </div>
            </section>
            <AnalyticsStats stats={stats.data} />
            <ResponseChart series={stats.data.series} />
            <div className="two-columns">
              <UptimeChart stats={stats.data} />
              <OutcomeChart stats={stats.data} />
            </div>
          </>
        )
      )}
    </>
  );
}
