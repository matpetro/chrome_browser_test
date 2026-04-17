# Chrome Maximize Diagnostic

Standalone project to diagnose the Chrome 147 `maximize()` bug.
Replicates the exact `launchApplication` sequence from `UIActionSteps.java` (develop branch):
1. `new ChromeDriver(options)` — same options as `ChromeDriverManager`
2. `driver.manage().deleteAllCookies()`
3. `driver.manage().window().maximize()` — bare, no sleep, no retry

Runs 15 times and logs Chrome version, driver version, and PASS/FAIL per run to a timestamped file in `target/`.

## How to run

```powershell
cd chrome-maximize-test

# Chrome 147 (system default)
mvn compile exec:java

# Specific Chrome binary (e.g. Chrome 146)
mvn compile exec:java "-Dchrome.binary=C:/path/to/chrome.exe"
```

## Output

Results are printed to the console and saved to:
```
target/maximize-diagnostic-<timestamp>.log
```

Each log includes the Chrome and driver versions detected, and a per-run PASS/FAIL summary:

```
=======================================================
  Chrome Maximize Diagnostic  (15 runs)
  Timestamp:      2026-04-17_11-00-00
  Chrome binary:  (system default)
  Chrome version: 147.0.7727.101
  Driver version: 147.0.7727.57
=======================================================

--- Run  1 / 15 ---
  ChromeDriver created
  Cookies cleared
  maximize() PASS in 12ms
...

=======================================================
  RESULTS SUMMARY
  Chrome 147.0.7727.101  |  Driver 147.0.7727.57
=======================================================
Run  1: PASS
Run  2: FAIL  -- unknown error: unhandled inspector error: ...
...
  PASSED: 14 / 15
  FAILED:  1 / 15
=======================================================
```
