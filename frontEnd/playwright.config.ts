import { defineConfig, devices } from '@playwright/test';

const isCI = Boolean(process.env['CI']);

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  forbidOnly: isCI,
  retries: isCI ? 2 : 0,
  workers: isCI ? 2 : undefined,

  reporter: isCI
    ? [['list'], ['html', { open: 'never' }]]
    : [['list'], ['html', { open: 'never' }]],

  timeout: 30_000,

  expect: {
    timeout: 5_000,
  },

  use: {
    baseURL: 'http://127.0.0.1:4200',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],

  webServer: {
    command: 'npm start -- --host 127.0.0.1 --port 4200',
    url: 'http://127.0.0.1:4200',
    reuseExistingServer: !isCI,
    timeout: 120_000,
  },
});
