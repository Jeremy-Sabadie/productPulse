
#!/usr/bin/env bash
set -euo pipefail

# Éviter les conflits avec les bibliothèques VS Code Snap
unset GTK_PATH GIO_MODULE_DIR LD_LIBRARY_PATH LD_PRELOAD

cd "$(dirname "$(realpath "$0")")"
PROJECT_DIR="$PWD"

API_PID=""
FRONT_PID=""

cleanup() {
    trap - INT TERM EXIT

    echo ""
    echo "Arrêt de ProductPulse..."

    if [ -n "$FRONT_PID" ]; then
        kill "$FRONT_PID" 2>/dev/null || true
        wait "$FRONT_PID" 2>/dev/null || true
    fi

    if [ -n "$API_PID" ]; then
        kill "$API_PID" 2>/dev/null || true
        wait "$API_PID" 2>/dev/null || true
    fi

    echo "Processus de développement arrêtés."
    echo "MariaDB reste disponible dans Docker."
}

trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

echo "=== ProductPulse - Développement local ==="

# 1. Vérifier les outils et les fichiers
for cmd in docker openssl npm; do
    if ! command -v "$cmd" >/dev/null 2>&1; then
        echo "ERREUR : $cmd n'est pas installé."
        exit 1
    fi
done

if [ ! -f docker-compose.yml ] ||
   [ ! -f backend/mvnw ] ||
   [ ! -d frontEnd ]; then
    echo "ERREUR : structure du projet incomplète."
    exit 1
fi

chmod +x backend/mvnw

# 2. Vérifier la protection des secrets
touch .gitignore

for entry in ".env" ".secrets/"; do
    if ! grep -qxF "$entry" .gitignore; then
        printf '\n%s\n' "$entry" >> .gitignore
    fi
done

if git ls-files --error-unmatch .env >/dev/null 2>&1; then
    echo "ERREUR : .env est suivi par Git."
    exit 1
fi

if git ls-files -- .secrets | grep -q .; then
    echo "ERREUR : une clé du dossier .secrets est suivie par Git."
    exit 1
fi

# 3. Conserver ou créer la configuration MariaDB
if [ ! -f .env ]; then
    echo "Création de la configuration MariaDB..."

    (
        umask 077
        printf 'DB_PASSWORD=%s\n' "$(openssl rand -hex 24)"
        printf 'DB_ROOT_PASSWORD=%s\n' "$(openssl rand -hex 24)"
    ) > .env
fi

set -a
source "$PROJECT_DIR/.env"
set +a

# 4. Charger la clé publique de My Popote en production
MY_POPOTE_PUBLIC_KEY="$PROJECT_DIR/.secrets/jwt-public.pem"

if [ ! -r "$MY_POPOTE_PUBLIC_KEY" ]; then
    echo "ERREUR : clé publique My Popote introuvable."
    exit 1
fi

export PRODUCTPULSE_JWT_PUBLIC_KEY
PRODUCTPULSE_JWT_PUBLIC_KEY="$(cat "$MY_POPOTE_PUBLIC_KEY")"
export PRODUCTPULSE_JWT_ISSUER="my-popote"

echo "JWT RS256 : clé publique chargée."

# 5. Démarrer MariaDB
echo "Démarrage de MariaDB..."
docker compose up -d --wait db

# 6. Démarrer l'API
echo "Démarrage de Spring Boot..."

(
    cd "$PROJECT_DIR/backend"
    exec ./mvnw spring-boot:run
) > "$PROJECT_DIR/api-dev.log" 2>&1 &

API_PID=$!

# 7. Démarrer Angular
echo "Démarrage d'Angular..."

(
    cd "$PROJECT_DIR/frontEnd"
    exec npm start
) > "$PROJECT_DIR/front-dev.log" 2>&1 &

FRONT_PID=$!

echo ""
echo "=== ProductPulse lancé ==="
echo "Frontend : http://localhost:4200"
echo "API      : http://localhost:8080"
echo "MariaDB  : localhost:3308"
echo ""
echo "Logs API     : api-dev.log"
echo "Logs Angular : front-dev.log"
echo ""
echo "Ctrl + C pour arrêter."
echo ""

# 8. Surveiller les processus
while true; do
    if ! kill -0 "$API_PID" 2>/dev/null; then
        echo "ERREUR : le processus API s'est arrêté."
        tail -n 25 "$PROJECT_DIR/api-dev.log"
        exit 1
    fi

    if ! kill -0 "$FRONT_PID" 2>/dev/null; then
        echo "ERREUR : le processus Angular s'est arrêté."
        tail -n 25 "$PROJECT_DIR/front-dev.log"
        exit 1
    fi

    sleep 2
done
