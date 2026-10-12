
#!/usr/bin/env bash
set -euo pipefail

# Corriger les conflits de bibliothèques avec VS Code Snap
unset GTK_PATH GIO_MODULE_DIR LD_LIBRARY_PATH LD_PRELOAD

# Se placer à la racine du projet
cd "$(dirname "$(realpath "$0")")"
PROJECT_DIR="$PWD"

echo "=== ProductPulse - Environnement de développement ==="

# 1. Vérifier les outils nécessaires
for cmd in docker openssl gnome-terminal npm; do
    if ! command -v "$cmd" >/dev/null 2>&1; then
        echo "ERREUR : $cmd n'est pas installé."
        exit 1
    fi
done

if [ ! -f docker-compose.yml ]; then
    echo "ERREUR : docker-compose.yml introuvable."
    exit 1
fi

if [ ! -f backend/mvnw ]; then
    echo "ERREUR : backend/mvnw introuvable."
    exit 1
fi

if [ ! -d frontEnd ]; then
    echo "ERREUR : dossier frontEnd introuvable."
    exit 1
fi

chmod +x backend/mvnw

# 2. Protéger les secrets
touch .gitignore

if ! grep -qxF '.env' .gitignore; then
    printf '\n# Secrets locaux\n.env\n' >> .gitignore
fi

if git ls-files --error-unmatch .env >/dev/null 2>&1; then
    echo "ERREUR : .env est déjà suivi par Git."
    echo "Exécute : git rm --cached .env"
    exit 1
fi

# 3. Créer les identifiants au premier lancement
if [ ! -f .env ]; then
    echo "Création de la configuration MariaDB..."

    (
        umask 077
        printf 'DB_PASSWORD=%s\n' "$(openssl rand -hex 24)"
        printf 'DB_ROOT_PASSWORD=%s\n' "$(openssl rand -hex 24)"
    ) > .env

    echo "Fichier .env créé."
else
    echo "Configuration .env existante conservée."
fi

# 4. Charger les variables d'environnement
set -a
source "$PROJECT_DIR/.env"
set +a

# 5. Démarrer MariaDB
echo "Démarrage de MariaDB..."
docker compose up -d --wait db

echo "MariaDB est prête."

# 6. Démarrer l'API Spring Boot
echo "Démarrage de l'API..."

gnome-terminal \
    --working-directory="$PROJECT_DIR/backend" \
    -- bash -c './mvnw spring-boot:run; exec bash'

# 7. Démarrer le frontend Angular
echo "Démarrage du frontend Angular..."

gnome-terminal \
    --working-directory="$PROJECT_DIR/frontEnd" \
    -- bash -c 'npm start; exec bash'

echo ""
echo "=== ProductPulse - Démarrage lancé ==="
echo "Frontend : http://localhost:4200"
echo "API      : http://localhost:8080"
echo "MariaDB  : localhost:3308"
echo ""
echo "Vérifie les terminaux pour confirmer le démarrage."
