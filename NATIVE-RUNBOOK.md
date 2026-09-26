# Search native setup and transport boundary

The product has two original endpoints: `GET /search` and `GET /popularKeyword`. No account/admin/product modules are invented. All browser explorer code is new compatibility code; the Java controllers, DTOs, repositories and provider services are the original source being maintained.

## Reproduce locally

Use Java 17, Node 20+ and the included Gradle wrapper. From the repository root:

```sh
npm --prefix portfolio-demo ci
npm --prefix portfolio-demo run test:all
GRADLE_USER_HOME=/tmp/search-api-gradle-home bash SearchExtension/gradlew -p SearchExtension clean test bootJar --no-daemon
node scripts/local-provider-stub.mjs
```

The last command is a separate loopback-only process. In another terminal, start the built application with local provider addresses:

```sh
KAKAO_BASE_URL=http://127.0.0.1:9091 NAVER_BASE_URL=http://127.0.0.1:9091 java -jar SearchExtension/build/libs/SearchExtension-0.0.1-SNAPSHOT.jar
```

Open `/console/live.html` on that native server. It calls `/search` and `/popularKeyword` on the same origin, using actual HTTP and the real Java/H2 pipeline. `/console/index.html` is the independent synthetic mode; it blocks network connections with CSP. The static public demo is still synthetic by default. A standalone static host cannot supply native endpoints: live mode fails explicitly there and never substitutes fixtures.

`.env.native.example` names the configuration. It is a template, not a shell script; its JDBC value contains a semicolon and must be quoted when exported in a shell. Defaults bind loopback, use a disposable H2 database and point unconfigured providers at closed loopback port 9. The historical checked-in database is excluded from resource packaging. For restart-persistent local storage, explicitly choose an isolated H2 file URL and `SEARCH_DDL_MODE=update`; existing/operational databases require a separately reviewed migration policy.

The stub emits synthetic JSON only and accepts `SEARCH_STUB_SCENARIO=normal`, `kakao-down`, or `both-down`. It makes no external requests. Real provider keys and addresses belong only in server environment configuration; they are never sent by the browser. This delivery did not call real providers or deploy the application.

## Corrected behavior

- Naver `start` is `(page - 1) * pageSize + 1`, so later pages no longer overlap. Requests with start above 1000 use the existing 402 error. Returned page navigation is capped to reachable provider offsets. The existing total-item cap remains 2500. Source: [Naver's official blog-search guide](https://github.com/naver/naver-openapi-guide/blob/master/ko/service-apis/search/blog/blog.md).
- Each provider has a bounded timeout (default 3000ms; clamped 100–10000ms). Provider failures can trigger fallback; history persistence happens afterward and never triggers another provider call. Persistence failure uses the existing 500 envelope.
- The original 401 sort, 402 page and 501 provider-failure envelopes remain. Generic server exceptions no longer expose internal exception messages.
- Popular replacement is one repeatable-read transaction. Delete, insert and flush either commit as one snapshot or roll back. The scheduler runs a single managed periodic task. Counts are scheduled server aggregates, not instant browser counts; concurrent searches can appear on a later refresh.
- Live transport timeout/cancel/network/malformed failures remain console errors, separate from native API error envelopes. New requests cancel old requests; old responses cannot replace newer state. History is scoped to mode and server origin, stores only request metadata, and does not replay automatically.
- Demo provider scenarios, example-count controls and keyword reset cannot execute in live mode. Live result links accept only HTTP(S), with no embedded credentials, and open only on explicit selection.

## Verification

Unit/DOM tests cover the original synthetic console and added live isolation/staleness/abort behavior. Native tests exercise actual loopback provider HTTP, the real Spring HTTP endpoints and packaged live page, H2 concurrent searches, aggregate visibility and rollback, plus persistence failures. Default Gradle tests exclude explicitly tagged external integration tests. No browser rendering or screenshot was performed; responsive/visual behavior remains unverified under the user's browser restriction.
