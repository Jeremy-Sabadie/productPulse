
import { test, expect, type Page } from '@playwright/test';

const DASHBOARD_URL = '/admin/feedback';
const FEEDBACK_API = '**/api/admin/feedback**';

interface Feedback {
  id: number;
  title: string;
  description: string;
  applicationCode: string;
  authorId: number;
  createdAt: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
}

const feedbacks: Feedback[] = [
  {
    id: 1,
    title: 'Ajouter un mode sombre',
    description: 'Pouvoir utiliser My Popote avec un thème sombre.',
    applicationCode: 'MY_POPOTE',
    authorId: 99,
    createdAt: '2026-10-15T10:00:00Z',
    status: 'PENDING',
  },
  {
    id: 2,
    title: 'Corriger la liste de courses',
    description: 'Un ingrédient apparaît plusieurs fois.',
    applicationCode: 'MY_POPOTE',
    authorId: 98,
    createdAt: '2026-10-15T11:00:00Z',
    status: 'PENDING',
  },
];

/**
 * Simule une réponse paginée de l'API administrateur.
 */
function paginatedResponse(
  items: Feedback[],
  page = 0,
  totalPages = 1,
): object {
  return {
    content: items,
    number: page,
    size: 10,
    totalElements: totalPages === 1 ? items.length : 20,
    totalPages,
    first: page === 0,
    last: page === totalPages - 1,
    empty: items.length === 0,
  };
}

/**
 * Simule la consultation des feedbacks en attente.
 */
async function mockFeedbackList(
  page: Page,
  items: Feedback[],
): Promise<void> {
  await page.route(FEEDBACK_API, async (route) => {
    if (route.request().method() !== 'GET') {
      await route.fallback();
      return;
    }

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(paginatedResponse(items)),
    });
  });
}

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => {
    sessionStorage.setItem('productpulse.accessToken', 'playwright-test-token');
  });

  await page.route('**/api/auth/me', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ role: 'ADMIN' }),
    });
  });
});

test.describe('Dashboard administrateur - Feedbacks', () => {
  test('affiche les feedbacks en attente', async ({ page }) => {
    await mockFeedbackList(page, feedbacks);

    await page.goto(DASHBOARD_URL);

    await expect(
      page.getByRole('heading', {
        name: 'Gestion des feedbacks',
      }),
    ).toBeVisible();

    await expect(
      page.getByText('Ajouter un mode sombre'),
    ).toBeVisible();

    await expect(
      page.getByText('Corriger la liste de courses'),
    ).toBeVisible();

    await expect(
      page.getByText('Pouvoir utiliser My Popote avec un thème sombre.'),
    ).toBeVisible();

    await expect(
      page.getByText('En attente').first(),
    ).toBeVisible();
  });

  test('affiche un message lorsque la liste est vide', async ({
    page,
  }) => {
    await mockFeedbackList(page, []);

    await page.goto(DASHBOARD_URL);

    await expect(
      page.getByRole('heading', { name: 'Aucun retour trouvé' }),
    ).toBeVisible();
  });

  test('permet d’approuver un feedback', async ({ page }) => {
    let approved = false;

    await page.route(FEEDBACK_API, async (route) => {
      const request = route.request();

      if (
        request.method() === 'PATCH' &&
        request.url().endsWith('/1/approve')
      ) {
        approved = true;

        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            ...feedbacks[0],
            status: 'APPROVED',
          }),
        });

        return;
      }

      if (request.method() === 'GET') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(
            paginatedResponse(
              approved ? [feedbacks[1]] : feedbacks,
            ),
          ),
        });

        return;
      }

      await route.fallback();
    });

    await page.goto(DASHBOARD_URL);

    const feedback = page.getByRole('article').filter({
      hasText: 'Ajouter un mode sombre',
    });

    await feedback.getByRole('button', {
      name: 'Approuver',
    }).click();

    await expect(
      page.getByText('Ajouter un mode sombre'),
    ).toHaveCount(0);

    await expect(
      page.getByText('Corriger la liste de courses'),
    ).toBeVisible();

    expect(approved).toBe(true);
  });

  test('permet de rejeter un feedback', async ({ page }) => {
    let rejected = false;

    await page.route(FEEDBACK_API, async (route) => {
      const request = route.request();

      if (
        request.method() === 'PATCH' &&
        request.url().endsWith('/1/reject')
      ) {
        rejected = true;

        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            ...feedbacks[0],
            status: 'REJECTED',
          }),
        });

        return;
      }

      if (request.method() === 'GET') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify(
            paginatedResponse(
              rejected ? [feedbacks[1]] : feedbacks,
            ),
          ),
        });

        return;
      }

      await route.fallback();
    });

    await page.goto(DASHBOARD_URL);

    const feedback = page.getByRole('article').filter({
      hasText: 'Ajouter un mode sombre',
    });

    await feedback.getByRole('button', {
      name: 'Rejeter',
    }).click();

    await expect(
      page.getByText('Ajouter un mode sombre'),
    ).toHaveCount(0);

    expect(rejected).toBe(true);
  });

  test('permet de naviguer entre les pages', async ({ page }) => {
    const requestedPages: number[] = [];

    await page.route(FEEDBACK_API, async (route) => {
      if (route.request().method() !== 'GET') {
        await route.fallback();
        return;
      }

      const url = new URL(route.request().url());
      const pageNumber = Number(
        url.searchParams.get('page') ?? '0',
      );

      requestedPages.push(pageNumber);

      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(
          paginatedResponse(
            pageNumber === 0 ? [feedbacks[0]] : [feedbacks[1]],
            pageNumber,
            2,
          ),
        ),
      });
    });

    await page.goto(DASHBOARD_URL);

    await expect(
      page.getByText('Ajouter un mode sombre'),
    ).toBeVisible();

    await page.getByRole('button', {
      name: 'Suivant',
    }).click();

    await expect(
      page.getByText('Corriger la liste de courses'),
    ).toBeVisible();

    expect(requestedPages).toContain(1);
  });

  test('affiche une erreur lorsque l’API échoue', async ({ page }) => {
    await page.route(FEEDBACK_API, async (route) => {
      await route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({
          message: 'Erreur interne',
        }),
      });
    });

    await page.goto(DASHBOARD_URL);

    await expect(
      page.getByRole('alert'),
    ).toContainText('Impossible de charger les feedbacks');
  });
});
