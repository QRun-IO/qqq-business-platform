import { execFileSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const webDirectory = path.dirname(path.dirname(fileURLToPath(import.meta.url)));
const repository = path.dirname(webDirectory);

try {
  execFileSync('git', ['rev-parse', '--show-toplevel'], { cwd: repository, stdio: 'ignore' });
  execFileSync('git', ['config', 'core.hooksPath', '.githooks'], { cwd: repository, stdio: 'ignore' });
  console.log('Git hooks enabled: staged platform changes will run the real E2E suite before commit.');
} catch {
  console.log('Git repository not available; hook installation skipped.');
}