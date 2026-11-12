// Runs the web tests in a single headless browser instead of every registered launcher.
// Default: ChromeHeadless. Override with KARMA_BROWSER=FirefoxHeadless.
config.browsers = [process.env.KARMA_BROWSER || 'ChromeHeadless'];
