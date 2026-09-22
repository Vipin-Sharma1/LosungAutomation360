# LosungAutomation360

Selenium + TestNG automation for the Losung360 order workflow.

## Project layout

- `src/main/java/org/example/base`: browser lifecycle and shared test setup
- `src/main/java/org/example/pages`: Login, Create Order, and Manage Orders page objects
- `src/main/java/org/example/utils`: environment configuration, waits, and screenshots
- `src/main/java/org/example/constants`: shared test data
- `src/test/java/org/example`: focused TestNG tests
- `testng.xml`: suite entry point

## Run locally

Requirements: Java 21, Maven, and Chrome started with remote debugging on port `9222`.

```powershell
mvn clean test
```

Local login reuses the Chrome session on `127.0.0.1:9222`. If the session is not authenticated, the test asks for the OTP in the terminal.

## Run in CI

Set these GitHub Actions secrets:

- `LOSUNG_LOGIN_MOBILE`
- `LOSUNG_OTP`

CI automatically runs Chrome headlessly. Screenshots are written to `screenshots/` when a test fails.

## Useful environment variables

- `LOSUNG_LOGIN_MOBILE`: login phone/email
- `LOSUNG_OTP`: six-digit non-interactive OTP for CI
- `LOSUNG_TIMEOUT_SECONDS`: Selenium wait timeout, default `30`
- `CI_MODE`: set to `true` to force headless mode locally
