# End-to-end (e2e) tests

End-to-end UI tests for Recurring Expense Tracker, written with [Kane CLI](https://www.testmuai.com/kane-cli/) by TestMu AI. Each test is a `_test.md` file in natural language, with no selectors or test IDs. Kane CLI installs the APK on an Android emulator, taps through the steps and checks the result on screen.

| Test | What it covers |
|---|---|
| `add_expense_test.md` | Adding a monthly expense (Netflix, 15.99) and seeing it in the list and in the Monthly summary |
| `edit_delete_expense_test.md` | Adding an expense, changing its price, and deleting it after the confirmation dialog |

## Get the APK

The tests run against `e2e/kane/app.apk`, which is gitignored. Download the latest `RecurringExpenseTracker_x.y.z.apk` from [Releases](https://github.com/DennisBauer/RecurringExpenseTracker/releases) and save it as `e2e/kane/app.apk`, or build one with Gradle and copy it there.

## Run the tests

```bash
npm install -g @testmuai/kane-cli
kane-cli login
cd e2e/kane
```

On an Apple Silicon Mac with Android Studio and an arm64 emulator (AVD):

```bash
kane-cli doctor --target emulator --install
kane-cli testrun run add_expense_test.md edit_delete_expense_test.md
```

From any OS, on a cloud emulator (needs a TestMu AI account with HyperExecute):

```bash
kane-cli plugin install remote-execution
kane-cli testrun run add_expense_test.md edit_delete_expense_test.md --remote --device-name "Pixel 7" --os-version 14
```

A run exits with `0` when every test passes, `1` when a test fails or breaks, `2` on a usage, plan or sign-in error, and `3` when cancelled, so it can gate CI. For pipelines, see the [CI/CD recipes](https://github.com/LambdaTest/kane-cli/blob/main/docs/user-guide/cicd.md) for GitHub Actions, GitLab CI, Jenkins and Docker; with `--remote` the runner only needs Node and the Kane CLI plugin.

Each test gets an `output-<test>/Result.md`, and the run leaves an evidence pack (steps, screenshots, logs) in `.testmuai/evidence/` that `kane-cli evidence serve <pack>` opens. The run also records each step in `output-<test>/`, so the next run replays quickly; if the UI changes and a replay stops matching, Kane CLI re-plans that step (adaptive healing, on by default).

## Editing a test

Change the step text in a `_test.md` file and run it again. If you use a coding agent such as Claude Code, Codex or Gemini CLI, `npx @testmuai/kane-cli-skill` teaches it to run and extend these tests.
