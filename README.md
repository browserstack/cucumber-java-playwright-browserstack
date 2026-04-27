# cucumber-java-playwright-browserstack

A ready-to-run sample that runs [Cucumber](https://cucumber.io/docs/installation/java/) scenarios in [Playwright for Java](https://playwright.dev/java/) against real browsers on [BrowserStack](https://www.browserstack.com/automate/playwright). Uses the [BrowserStack Java SDK](https://www.browserstack.com/docs/automate/playwright/java-sdk-quickstart) to fan out across platforms, stream results to the dashboard, and manage the BrowserStack Local tunnel.

![BrowserStack Logo](https://d98b8t1nnulk5.cloudfront.net/production/images/layout/logo-header.png?1469004780)

## Prerequisites

- **JDK 8 or newer** (Temurin 8 / 11 / 17 are all fine).
- **Maven 3.6+**.
- A **BrowserStack account** — grab your [username and access key](https://www.browserstack.com/accounts/settings).
- Works on **macOS, Windows, and Linux** — the sample targets Java 8 so it compiles and runs everywhere.

No browsers or drivers to install locally. Playwright connects to BrowserStack's cloud over a WebSocket; browser binaries live in the cloud.

## Quick start

```bash
# 1. Clone
git clone https://github.com/browserstack/cucumber-java-playwright-browserstack.git
cd cucumber-java-playwright-browserstack

# 2. Set credentials (pick one method — see "Setting credentials" below)
export BROWSERSTACK_USERNAME=<your-username>
export BROWSERSTACK_ACCESS_KEY=<your-access-key>

# 3. Run against a public site (bstackdemo.com) on all configured platforms
mvn test -P sample-test
```

That's it. Maven downloads Cucumber, TestNG, Playwright for Java, and the BrowserStack SDK on first run, then fans the suite out to the 3 platforms in `browserstack.yml` in parallel.

**What you'll see:**

- Console: `Tests run: 3, Failures: 0, Errors: 0` (one test per platform).
- Dashboard: a new build at [automate.browserstack.com](https://automate.browserstack.com) named `browserstack build #N`, with one session per platform. Each session has a **video**, **network logs**, **Playwright logs**, and **console output**.
- Test Observability: intelligent report at [observability.browserstack.com](https://observability.browserstack.com).

## Setting credentials

Pick **one** of these three options. Env vars always override `browserstack.yml`.

| Option | When to use it | How |
|---|---|---|
| **Env vars** (recommended) | Any real workflow; keeps secrets out of the repo | see below |
| **Edit `browserstack.yml`** | Quick local experimentation | Replace `YOUR_USERNAME` / `YOUR_ACCESS_KEY` at the top of the file |
| **Inline on the command** | One-off debug | `BROWSERSTACK_USERNAME=... BROWSERSTACK_ACCESS_KEY=... mvn test -P sample-test` (bash/zsh only) |

Env var syntax per shell:

```bash
# macOS / Linux (bash, zsh)
export BROWSERSTACK_USERNAME=<your-username>
export BROWSERSTACK_ACCESS_KEY=<your-access-key>
```

```powershell
# Windows — PowerShell
$env:BROWSERSTACK_USERNAME = "<your-username>"
$env:BROWSERSTACK_ACCESS_KEY = "<your-access-key>"
```

```cmd
:: Windows — cmd.exe
set BROWSERSTACK_USERNAME=<your-username>
set BROWSERSTACK_ACCESS_KEY=<your-access-key>
```

> **CI / GitHub Actions:** the shipped `.github/workflows/cucumber-workflow-run.yml` reads `BROWSERSTACK_USERNAME` and `BROWSERSTACK_ACCESS_KEY` from repository secrets. Add those two secrets in your fork's **Settings → Secrets and variables → Actions** before triggering the workflow.

## Testing a page on `localhost` (BrowserStack Local)

If your app lives on `localhost`, a staging host, or behind a firewall, run the `sample-local-test` profile. It hits `http://bs-local.com:45454/` — `bs-local.com` is a hostname BrowserStack Local resolves to `localhost` inside the cloud browser.

You need **two things**:

1. **A server running on port 45454 on your machine** that serves a page whose `<title>` contains `BrowserStack Local`.
2. **`browserstackLocal: true`** in `browserstack.yml` (already set in this repo). The SDK launches and tears down the Local tunnel for you — no manual binary download.

Spin up a throwaway server in ~10 seconds:

```bash
# macOS / Linux — bash/zsh
mkdir -p /tmp/bstack-local-demo
printf '<!doctype html><html><head><title>BrowserStack Local demo</title></head><body>ok</body></html>' > /tmp/bstack-local-demo/index.html
python3 -m http.server 45454 --directory /tmp/bstack-local-demo &
mvn test -P sample-local-test
kill %1                                          # stop the server when done
```

```powershell
# Windows — PowerShell
New-Item -ItemType Directory -Force $env:TEMP\bstack-local-demo | Out-Null
'<!doctype html><html><head><title>BrowserStack Local demo</title></head><body>ok</body></html>' | Set-Content $env:TEMP\bstack-local-demo\index.html
Start-Process python -ArgumentList '-m','http.server','45454','--directory',"$env:TEMP\bstack-local-demo" -NoNewWindow
mvn test -P sample-local-test
Get-Process python | Stop-Process                # stop the server when done
```

Swap the demo URL for your real app whenever you're ready — just change the `Given I am on "…"` line in `src/test/resources/features/localtest/local.feature` and the assertion in `local.feature` / `StackLocalSteps.java` to something meaningful for your app.

## Project layout

```
cucumber-java-playwright-browserstack/
├── pom.xml                                         Maven deps + profiles (sample-test, sample-local-test)
├── browserstack.yml                                BrowserStack SDK config: credentials, platforms, Local, reporting
├── src/test/
│   ├── java/com/browserstack/
│   │   ├── RunCucumberTest.java                    TestNG runner for sample-test
│   │   ├── RunCucumberLocalTest.java               TestNG runner for sample-local-test
│   │   └── stepdefs/
│   │       ├── e2e/StackDemoSteps.java             Steps for bstackdemo.com add-to-cart
│   │       └── local/StackLocalSteps.java          Steps for BrowserStack Local connectivity check
│   └── resources/
│       ├── features/test/e2e.feature               @e2e — bstackdemo.com add-to-cart
│       ├── features/localtest/local.feature        @local — title contains "BrowserStack Local"
│       ├── testng.xml                              Suite XML for sample-test
│       └── testngLocal.xml                         Suite XML for sample-local-test
└── .github/workflows/                              CI workflow (matrix over Java × OS) + Semgrep
```

## Customizing the run

### Platforms

`browserstack.yml` declares the OS / browser matrix. The shipped config covers the three Playwright engines:

```yaml
platforms:
  - os: OS X
    osVersion: Ventura
    browserName: chrome               # chromium
    browserVersion: latest
  - os: Windows
    osVersion: 10
    browserName: playwright-firefox
    browserVersion: latest
  - os: OS X
    osVersion: Monterey
    browserName: playwright-webkit
    browserVersion: latest
```

Add, remove, or change entries freely — the full supported list is at [BrowserStack: browsers & platforms for Playwright](https://www.browserstack.com/list-of-browsers-and-platforms/playwright). The SDK spins up one session per `platforms:` entry × `parallelsPerPlatform:`.

### Parallelism

Tune `parallelsPerPlatform` in `browserstack.yml` for your plan's parallel capacity — use the [Parallel Test Calculator](https://www.browserstack.com/automate/parallel-calculator?ref=github) to pick a number.

### Reporting

`projectName`, `buildName`, and `buildIdentifier` in `browserstack.yml` control how runs group in the dashboard. `testObservability: true` is on by default.

### Debugging capabilities

Flip these in `browserstack.yml` when you need to reproduce a flaky failure:

| Key | What it does |
|---|---|
| `debug: true` | Step-by-step screenshots for each Playwright action |
| `networkLogs: true` | HAR capture for every request |
| `consoleLogs: verbose` | Full browser console stream (levels: `disable`, `errors`, `warnings`, `info`, `verbose`) |

## Adopting this in your own project

To port this pattern into an existing Cucumber-JVM + Playwright project:

1. **Add the SDK to `pom.xml`** and wire the `-javaagent:` into Surefire:
   ```xml
   <dependency>
       <groupId>com.browserstack</groupId>
       <artifactId>browserstack-java-sdk</artifactId>
       <version>LATEST</version>
       <scope>compile</scope>
   </dependency>
   ```
   ```xml
   <plugin>
       <artifactId>maven-surefire-plugin</artifactId>
       <configuration>
           <argLine>-javaagent:${com.browserstack:browserstack-java-sdk:jar}</argLine>
       </configuration>
   </plugin>
   ```
   Use this project's `pom.xml` as the full template — it includes `cucumber-testng`, the surefire `<suiteXmlFiles>` block, and Maven profiles.

2. **Copy `browserstack.yml`** into your project root. Set `userName`, `accessKey`, `projectName`, and your own `platforms:`. Set `framework: cucumber-testng`.

3. **Copy the `@Before` Playwright connection block** from `StackDemoSteps.java` into your step definitions. The SDK rewires the WebSocket URL per thread so the same block targets a different platform on each parallel run.

4. **Run** — the javaagent is already wired, so plain `mvn test` is enough:
   ```bash
   mvn test
   ```

## Troubleshooting

| Symptom | Fix |
|---|---|
| `401 Unauthorized` from `cdp.browserstack.com` | `BROWSERSTACK_USERNAME` / `BROWSERSTACK_ACCESS_KEY` not set, or placeholders still in `browserstack.yml`. Re-check credentials. |
| Tests hang at `browserType.connect(...)` | Network/firewall blocking WebSocket to `*.browserstack.com`. Confirm outbound `443` is open. |
| `sample-local-test` fails with `ERR_CONNECTION_REFUSED` | Nothing listening on `localhost:45454`. Start the demo server from the "Testing a page on localhost" section. |
| `sample-local-test` port conflict | Something else is using `45454`. Change it in `local.feature` and in the `python -m http.server` command; both must match. |
| `Could not find -javaagent:${com.browserstack:...:jar}` | The `maven-dependency-plugin` `properties` goal is missing from `pom.xml`. Keep it as shipped. |
| Dashboard missing the build | Credentials are valid but belong to a different account — double-check which account generated the key in use. |

## Notes

- Results: [BrowserStack Automate dashboard](https://www.browserstack.com/automate) · [Test Observability](https://observability.browserstack.com).
- The sample targets Java 8 (`maven.compiler.source: 1.8`). If your project is on a newer JDK, bump `source` / `target` — nothing else in the sample depends on a specific Java version.
- Playwright for Java connects to BrowserStack over a WebSocket; there's no local browser binary to install or update.
