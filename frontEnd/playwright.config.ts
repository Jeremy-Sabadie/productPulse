
import { defineConfig, devices } from '@playwright/test';

/**
 * Configuration des tests fonctionnels ProductPulse.
 *
 * Les tests UI utilisent Chromium.
 * Les tests API utilisent le client HTTP de Playwright.
 */
export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  forbidOnly: !!process.env['CI'],
  retries: process.env['CI'] ? 2 : 0,
  workers: process.env['CI'] ? 2 : undefined,

  reporter: [
    ['list'],
    ['html', { open: 'never' }],
  ],

  use: {
    baseURL: process.env['FRONTEND_URL'] ?? 'http://localhost:4200',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },

  projects: [
    {
      name: 'ui-chromium',
      testMatch: '**/ui/**/*.spec.ts',
      use: {
        ...devices['Desktop Chrome'],
      },
    },
    {
      name: 'api',
      testMatch: '**/api/**/*.spec.ts',
      use: {
        baseURL: process.env['API_URL'] ?? 'http://localhost:8080',
      },
    },
  ],

  webServer: {
    command: 'npm start -- --host 127.0.0.1',
    url: 'http://localhost:4200',
    reuseExistingServer: !process.env['CI'],
    timeout: 120_000,
  },
});
