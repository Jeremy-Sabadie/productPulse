
import { test, expect } from '@playwright/test';
import { createTestJwt } from '../fixtures/jwt.fixture';

test.describe('API - Administration des feedbacks', () => {
  test('refuse la consultation sans authentification', async ({
    request,
  }) => {
    const response = await request.get('/api/admin/feedback');

    expect(response.status()).toBe(401);
  });

  test('refuse un utilisateur non administrateur', async ({
    request,
  }) => {
    const response = await request.get('/api/admin/feedback', {
      headers: {
        Authorization: `Bearer ${createTestJwt('99')}`,
      },
    });

    expect(response.status()).toBe(403);
  });

  test('autorise un administrateur authentifié', async ({
    request,
  }) => {
    const response = await request.get('/api/admin/feedback', {
      headers: {
        Authorization: `Bearer ${createTestJwt('42')}`,
      },
    });

    expect(response.status()).toBe(200);
    expect(response.headers()['content-type']).toContain('application/json');

    const body = await response.json();

    expect(body).toHaveProperty('content');
    expect(Array.isArray(body.content)).toBe(true);
  });
});
