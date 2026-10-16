
import { createPrivateKey, sign } from 'node:crypto';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const ISSUER = 'https://auth.my-popote.example';
const AUDIENCE = 'productpulse';

const PRIVATE_KEY_PATH = resolve(
  process.cwd(),
  'tests/fixtures/keys/private.pem',
);

/**
 * Génère un JWT RS256 pour les tests d'intégration ProductPulse.
 *
 * @param userId Identifiant de l'utilisateur simulé.
 * @returns JWT signé au format compact.
 */
export function createTestJwt(userId: string): string {
  const now = Math.floor(Date.now() / 1000);

  const header = {
    alg: 'RS256',
    typ: 'JWT',
  };

  const payload = {
    sub: userId,
    iss: ISSUER,
    aud: AUDIENCE,
    iat: now,
    exp: now + 3600,
  };

  const encode = (value: object): string =>
    Buffer.from(JSON.stringify(value)).toString('base64url');

  const signingInput = `${encode(header)}.${encode(payload)}`;

  const privateKey = createPrivateKey(
    readFileSync(PRIVATE_KEY_PATH),
  );

  const signature = sign(
    'RSA-SHA256',
    Buffer.from(signingInput),
    privateKey,
  ).toString('base64url');

  return `${signingInput}.${signature}`;
}
