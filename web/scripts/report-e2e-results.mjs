import { appendFile, readFile } from 'node:fs/promises';

const reportPath = 'test-results/junit.xml';
const summaryPath = process.env.GITHUB_STEP_SUMMARY;

let markdown;
try {
  const xml = await readFile(reportPath, 'utf8');
  const suites = [...xml.matchAll(/<testsuite\b([^>]*)>/g)];
  const value = (attributes, name) => attributes.match(new RegExp(`${name}="([^"]*)"`))?.[1] ?? '0';
  const totals = suites.reduce(
    (result, suite) => {
      result.tests += Number(value(suite[1], 'tests'));
      result.failures += Number(value(suite[1], 'failures'));
      result.errors += Number(value(suite[1], 'errors'));
      result.skipped += Number(value(suite[1], 'skipped'));
      return result;
    },
    { tests: 0, failures: 0, errors: 0, skipped: 0 },
  );
  if (!suites.length || !totals.tests) throw new Error('JUnit report contains no executed tests');
  const failed = totals.failures + totals.errors;
  const passed = totals.tests - failed - totals.skipped;
  if (!passed && !failed) throw new Error('JUnit report contains no completed tests');
  markdown = `## Playwright live-service E2E results\n\n| Passed | Failed | Skipped | Total |\n|---:|---:|---:|---:|\n| ${passed} | ${failed} | ${totals.skipped} | ${totals.tests} |\n`;
  if (failed) console.log(`::error title=E2E validation failed::${failed} test(s) failed; deployment is blocked. Download the Playwright artifact for traces.`);
} catch (error) {
  markdown = `## Playwright live-service E2E results\n\nNo JUnit report was produced. Setup or service startup failed, so deployment remains blocked.\n`;
  console.log(`::error title=E2E report unavailable::${error.message}`);
}

console.log(markdown);
if (summaryPath) await appendFile(summaryPath, markdown);