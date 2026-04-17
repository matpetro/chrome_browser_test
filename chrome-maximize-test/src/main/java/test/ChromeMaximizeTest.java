package main.java.test;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChromeMaximizeTest {

    private static final int RUNS = 10;

    private static final String OPT_TEST_TYPE          = "test-type";
    private static final String OPT_IGNORE_CERT        = "ignore-certificate-errors";
    private static final String OPT_ALLOW_INSECURE     = "allow-running-insecure-content";
    private static final String OPT_DISABLE_EXTENSIONS = "disable-extensions";
    private static final String OPT_DOWNLOAD_BUBBLE    = "disable-features=download-bubble@2,download-bubble-v2@2";
    private static final String OPT_INCOGNITO          = "incognito";

    public static void main(String[] args) throws Exception {
        String chromeBinary = System.getProperty("chrome.binary", "").trim();
        String timestamp    = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        File logFile = new File("target/maximize-diagnostic-" + timestamp + ".log");
        logFile.getParentFile().mkdirs();

        try (PrintWriter log = new PrintWriter(new FileWriter(logFile))) {

            String chromeVersion  = "unknown";
            String driverVersion  = "unknown";
            try {
                WebDriver probe = createDriver(chromeBinary);
                Capabilities caps = ((RemoteWebDriver) probe).getCapabilities();
                chromeVersion = caps.getBrowserVersion();
                driverVersion = (String) ((Map<?, ?>) caps.getCapability("chrome")).get("chromedriverVersion");
                if (driverVersion != null && driverVersion.contains(" "))
                    driverVersion = driverVersion.substring(0, driverVersion.indexOf(' '));
                probe.quit();
                Thread.sleep(1500);
            } catch (Exception e) {
                chromeVersion = "(probe failed: " + e.getMessage().split("\n")[0] + ")";
            }

            print(log, "=======================================================");
            print(log, "  Chrome Maximize Diagnostic  (" + RUNS + " runs)");
            print(log, "  Timestamp:      " + timestamp);
            print(log, "  Chrome binary:  " + (chromeBinary.isEmpty() ? "(system default)" : chromeBinary));
            print(log, "  Chrome version: " + chromeVersion);
            print(log, "  Driver version: " + driverVersion);
            print(log, "=======================================================");
            print(log, "");

            List<String> results = new ArrayList<>();
            int passed = 0;
            int failed = 0;

            for (int i = 1; i <= RUNS; i++) {
                print(log, String.format("--- Run %2d / %d ---", i, RUNS));
                WebDriver driver = null;
                try {
                    driver = createDriver(chromeBinary);
                    print(log, "  ChromeDriver created");

                    driver.manage().deleteAllCookies();
                    print(log, "  Cookies cleared");

                    long t0 = System.currentTimeMillis();
                    driver.manage().window().maximize();
                    print(log, String.format("  maximize() PASS in %dms", System.currentTimeMillis() - t0));

                    results.add(String.format("Run %2d: PASS", i));
                    passed++;

                } catch (Exception e) {
                    String msg = e.getMessage() == null ? "(no message)" : e.getMessage().split("\n")[0];
                    print(log, "  maximize() FAIL: " + msg);
                    results.add(String.format("Run %2d: FAIL  -- %s", i, msg));
                    failed++;
                } finally {
                    if (driver != null) try { driver.quit(); } catch (Exception ignored) {}
                    Thread.sleep(1500);
                }
                print(log, "");
            }

            print(log, "=======================================================");
            print(log, "  RESULTS SUMMARY");
            print(log, "  Chrome " + chromeVersion + "  |  Driver " + driverVersion);
            print(log, "=======================================================");
            results.forEach(r -> print(log, r));
            print(log, "");
            print(log, String.format("  PASSED: %d / %d", passed, RUNS));
            print(log, String.format("  FAILED: %d / %d", failed, RUNS));
            print(log, "=======================================================");
            System.out.println("\nLog written to: " + logFile.getAbsolutePath());
        }
    }

    private static void print(PrintWriter log, String line) {
        System.out.println(line);
        log.println(line);
        log.flush();
    }

    private static WebDriver createDriver(String chromeBinary) {
        ChromeOptions options = new ChromeOptions();

        options.addArguments(OPT_TEST_TYPE);
        options.addArguments(OPT_IGNORE_CERT);
        options.addArguments(OPT_ALLOW_INSECURE);
        options.addArguments(OPT_DISABLE_EXTENSIONS);
        options.addArguments(OPT_DOWNLOAD_BUBBLE);
        options.addArguments(OPT_INCOGNITO);

        String userDataDir = System.getProperty("java.io.tmpdir") + File.separator
                + "chromeProfile_" + Thread.currentThread().getId()
                + "_" + System.currentTimeMillis()
                + "_" + UUID.randomUUID();
        new File(userDataDir).mkdirs();
        options.addArguments("--user-data-dir=" + userDataDir);

        options.setExperimentalOption("useAutomationExtension", false);
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("password_manager_enabled", false);
        prefs.put("profile.default_content_settings.popups", 0);
        prefs.put("autofill.passwords_compromised_prompt_enabled", false);
        prefs.put("autofill.profile_enabled", false);
        options.setExperimentalOption("prefs", prefs);

        if (!chromeBinary.isEmpty()) {
            options.setBinary(chromeBinary);
        }

        return new ChromeDriver(options);
    }
}
