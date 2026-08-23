import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';

test('Data Integrity Audit — Zero Mock Data / Zero Fabricated Content in Production Code', () => {
  const libDir = path.resolve('lib');
  const appDir = path.resolve('app');

  function scanDirectory(dir, forbiddenPatterns) {
    const entries = fs.readdirSync(dir, { withFileTypes: true });
    for (const entry of entries) {
      const fullPath = path.join(dir, entry.name);
      if (entry.isDirectory()) {
        scanDirectory(fullPath, forbiddenPatterns);
      } else if (entry.isFile() && (entry.name.endsWith('.ts') || entry.name.endsWith('.tsx'))) {
        const content = fs.readFileSync(fullPath, 'utf-8');
        for (const pattern of forbiddenPatterns) {
          const matches = content.match(pattern);
          if (matches) {
            assert.fail(`Found forbidden mock pattern "${pattern}" in production file: ${fullPath}`);
          }
        }
      }
    }
  }

  // Forbidden patterns representing hardcoded mock collections in production runtime
  const forbidden = [
    /const INITIAL_DEMAND_SIGNALS\s*=\s*\[\s*\{/i,
    /const MOCK_BOOKS\s*=\s*\[/i,
    /const FAKE_ANALYTICS\s*=\s*\[/i,
    /const DEMO_CHAPTERS\s*=\s*\[/i,
  ];

  scanDirectory(libDir, forbidden);
  scanDirectory(appDir, forbidden);

  assert.ok(true, 'Zero production mock collections found across lib/ and app/');
});
