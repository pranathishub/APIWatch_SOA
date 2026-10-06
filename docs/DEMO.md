# College demonstration

1. Start PostgreSQL and APIWatch locally. Set `ALLOW_PRIVATE_TARGETS=true` in the backend environment for this trusted loopback demo.
2. In another terminal run `node scripts/demo-target.mjs`.
3. Register an account. Add `http://127.0.0.1:9090/health`, GET, expected 200, interval 10 seconds, timeout 2 seconds.
4. Wait for the first check and dashboard refresh (about 20 seconds). Show UP, response time, and history.
5. Make the real endpoint fail in PowerShell:

```powershell
Invoke-RestMethod http://127.0.0.1:9090/control -Method Post -ContentType application/json -Body '{"status":503,"delay":0}'
```

6. After the next check, show DOWN, the incident, 503 history rows, and analytics. Consecutive failures do not create duplicate alerts.
7. Restore the endpoint:

```powershell
Invoke-RestMethod http://127.0.0.1:9090/control -Method Post -ContentType application/json -Body '{"status":200,"delay":0}'
```

8. Show UP and automatic incident resolution.
9. Set `{"status":200,"delay":4000}` to demonstrate a timeout with the two-second monitor timeout. Restore zero delay.
10. Pause/resume a monitor, filter history, open per-API analytics, switch themes, and resize to mobile width.
11. Explain: scheduler → HTTP request → log/alert transaction → PostgreSQL → authenticated REST → React polling.

The demo target binds to loopback. This walkthrough uses the locally run backend; a Docker container cannot reach the host's loopback address. For Docker, use a public test endpoint you control or configure a reachable test service.

## Viva questions

- Why BCrypt instead of encrypting passwords?
- How does a signed JWT authenticate requests?
- Why are ownership checks necessary with valid tokens?
- Why do timeouts have no HTTP status code?
- How is sample-based uptime different from time-based uptime?
- How do database locks prevent duplicate checks?
- Why are redirects disabled?
- What changes would support thousands of monitors?
