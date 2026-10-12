
# ProductPulse — User Stories

Version : 1.0 — 10 octobre 2026
Statut : cadrage fonctionnel validé, détails techniques à préciser

## 1. Vision du produit

ProductPulse est une application indépendante permettant de recueillir, modérer et qualifier les retours des utilisateurs de plusieurs applications.

L'objectif est d'identifier les besoins réels des utilisateurs et de sélectionner les évolutions pertinentes.

ProductPulse n'est pas un outil de ticketing ni un clone de Jira.

Première application intégrée : My Popote.

## 2. Rôles

### Utilisateur
- Se connecte avec son compte de l'application d'origine.
- Soumet des avis et suggestions.
- Consulte les avis approuvés de la communauté.
- Soutient ou retire son soutien à un avis.
- Consulte l'historique de ses propres avis.

### Administrateur
- Modère les avis.
- Consulte l'ensemble des avis et leurs soutiens.
- Gère les statuts.
- Identifie les évolutions intéressantes.
- Marque les avis « Futur ticket ».
- Exporte les avis sélectionnés au format PDF.

## 3. User Stories

### US01 — Authentification

**En tant qu'utilisateur**, je veux me connecter à ProductPulse avec mon compte My Popote afin de ne pas créer un nouveau compte.

**Critères d'acceptation :**
- L'utilisateur peut accéder à ProductPulse depuis My Popote.
- Un accès direct à ProductPulse est également prévu.
- ProductPulse ne stocke pas les mots de passe My Popote.
- Les droits administrateur sont vérifiés côté serveur.
- Le mécanisme d'authentification sera défini après analyse de Spring Security dans My Popote.

### US02 — Déposer un avis

**En tant qu'utilisateur**, je veux soumettre un avis afin de signaler un problème ou proposer une amélioration.

**Critères d'acceptation :**
- Le titre est obligatoire.
- La description est obligatoire.
- L'avis est associé à l'utilisateur authentifié.
- L'application concernée est identifiée.
- L'avis est soumis à modération avant publication.
- Un message confirme l'enregistrement.

### US03 — Catégoriser un avis

**En tant qu'utilisateur**, je veux sélectionner une catégorie afin de préciser la nature de mon retour.

**Critères d'acceptation :**
- La catégorie est obligatoire.
- Les catégories initiales sont : anomalie, amélioration, nouvelle fonctionnalité.
- Les catégories pourront évoluer selon les besoins.
- L'administrateur peut filtrer les avis par catégorie.

### US04 — Horodatage

**En tant qu'administrateur**, je veux connaître la date et l'heure de chaque avis afin d'assurer sa traçabilité.

**Critères d'acceptation :**
- La date et l'heure sont générées côté serveur.
- L'utilisateur ne peut pas les modifier.
- Les dates sont affichées dans un format lisible.

### US05 — Version de l'application

**En tant qu'administrateur**, je veux connaître la version de l'application concernée afin de replacer un retour dans son contexte.

**Critères d'acceptation :**
- La version est renseignée automatiquement lorsque possible.
- La version est conservée avec l'avis.
- Une mise à jour de l'application ne clôture pas automatiquement les avis existants.

### US06 — Capture d'écran

**En tant qu'utilisateur**, je veux joindre une capture d'écran afin d'illustrer mon avis.

**Critères d'acceptation :**
- Pièce jointe facultative.
- Une image maximum par avis.
- Formats acceptés : JPG, PNG et WebP.
- Taille maximale : 5 Mo.
- Aperçu et suppression avant envoi.
- Stockage et accès sécurisés.
- L'administrateur contrôle la publication éventuelle de la capture.

### US07 — Historique personnel

**En tant qu'utilisateur**, je veux retrouver mes avis afin de consulter leur état de traitement.

**Critères d'acceptation :**
- L'utilisateur retrouve tous ses propres avis.
- Les avis en attente de modération restent visibles par leur auteur.
- Chaque avis affiche sa date, sa catégorie et son statut.
- L'utilisateur ne peut pas modifier lui-même les statuts.

### US08 — Gestion administrative

**En tant qu'administrateur**, je veux consulter et gérer les avis afin d'organiser leur traitement.

**Critères d'acceptation :**
- Tableau de bord réservé aux administrateurs.
- Consultation de tous les avis.
- Filtres par application, catégorie et statut.
- Consultation du détail et des captures.
- Modification des statuts de traitement.
- Les statuts exacts seront stabilisés avant l'implémentation.

### US09 — Sélection « Futur ticket »

**En tant qu'administrateur**, je veux identifier les avis pouvant devenir des évolutions afin de les transmettre au responsable du projet.

**Critères d'acceptation :**
- Ajout ou retrait de l'étiquette « Futur ticket ».
- Filtrage des avis portant cette étiquette.
- Aucun ticket n'est créé automatiquement.
- Aucune notation manuelle de priorité n'est requise.
- La sélection reste une décision de l'administrateur.
- L'étiquette n'est pas affichée comme engagement public de développement.

### US10 — Export PDF

**En tant qu'administrateur**, je veux exporter les avis sélectionnés « Futur ticket » afin de les transmettre au chef de projet.

**Critères d'acceptation :**
- Export accessible uniquement à l'administrateur.
- Sélection des avis à exporter.
- PDF lisible et professionnel.
- Informations : référence, titre, catégorie, description, date, application, version et statut.
- Les données personnelles et captures ne sont pas incluses par défaut.
- Aucun ticket n'est automatiquement créé.

### US11 — Consultation communautaire

**En tant qu'utilisateur authentifié**, je veux consulter les avis approuvés afin de découvrir les retours déjà proposés.

**Critères d'acceptation :**
- Seuls les avis approuvés sont visibles par la communauté.
- Les avis en attente ou rejetés ne sont pas publiés.
- Chaque avis affiche les informations utiles : titre, description, catégorie, date et statut.
- Les informations privées des utilisateurs ne sont pas exposées.
- Le nombre de soutiens n'est pas affiché aux utilisateurs.
- Aucune feuille de route publique des évolutions sélectionnées n'est prévue.

### US12 — Soutenir un avis

**En tant qu'utilisateur authentifié**, je veux soutenir un avis existant afin de signaler que je partage ce besoin.

**Critères d'acceptation :**
- Action « Je soutiens cet avis ».
- Un seul soutien par utilisateur et par avis.
- Le soutien peut être retiré.
- L'utilisateur peut voir s'il soutient déjà un avis.
- Aucun compteur de soutiens n'est visible publiquement.
- Les soutiens ne déclenchent aucune évolution automatiquement.

### US13 — Modération des avis

**En tant qu'administrateur**, je veux approuver ou rejeter les nouveaux avis afin de maîtriser les contenus publiés.

**Critères d'acceptation :**
- Chaque nouvel avis est initialement en attente de modération.
- Seul l'administrateur peut approuver ou rejeter un avis.
- Un avis approuvé devient visible par la communauté.
- Un avis rejeté reste accessible à l'administrateur et à son auteur.
- Le statut de modération est distinct du statut de traitement.
- Les utilisateurs ne peuvent soutenir que les avis publiés.

### US14 — Analyse des soutiens

**En tant qu'administrateur**, je veux consulter et trier les avis selon leur nombre de soutiens afin d'identifier les besoins partagés.

**Critères d'acceptation :**
- Le nombre de soutiens est visible uniquement dans l'administration.
- Tri décroissant ou croissant par nombre de soutiens.
- Filtres combinables avec la catégorie et l'application.
- Les soutiens constituent un indicateur d'intérêt, pas une priorité automatique.
- Aucun classement public des avis les plus soutenus.

## 4. UX/UI

Les deux maquettes validées servent de références :

- Dashboard administrateur ProductPulse.
- Formulaire utilisateur de dépôt d'avis.

Principes :
- Identité graphique indépendante de My Popote.
- Palette bleu, indigo et violet.
- Interface professionnelle, claire et responsive.
- Approche mobile-first.
- Navigation simple et accessible.
- Validation UX/UI au fil du développement.

Les données illustratives des maquettes ne remplacent pas les règles fonctionnelles de ce document.

## 5. Architecture technique

### Frontend
- Angular 19, sous réserve de la version réellement initialisée.
- Application responsive.
- Tests Playwright.

### Backend
- Java 21.
- Spring Boot, version réellement générée sur Spring Initializr.
- Spring Web.
- Spring Security.
- Spring Data JPA.
- Validation.
- MariaDB Driver.
- Flyway.
- Tests JUnit / Spring Boot Test.

### Base de données
- MariaDB.
- Docker Compose pour l'environnement local.
- Base et identifiants distincts entre développement et production.

### Déploiement
- Développement et tests en local.
- Déploiement ultérieur sur VPS OVH.
- Services Docker.
- Reverse proxy HTTPS.
- Surveillance des ressources du VPS.

## 6. Contraintes importantes

- Ne pas perturber My Popote en production.
- Analyser son authentification avant toute intégration.
- Travailler sur des branches Git dédiées.
- Tester les modifications avant fusion.
- Ne pas exposer les secrets dans GitHub.
- Sécuriser les fichiers téléchargés.
- Préserver la séparation des rôles utilisateur et administrateur.
- Garder ProductPulse réutilisable pour plusieurs applications.

## 7. Hors périmètre du MVP

- Création automatique de tickets Jira ou GitHub Issues.
- Gestion complète de projets et de sprints.
- Système de notation publique.
- Classement public des avis par popularité.
- Priorisation automatique.
- Feuille de route publique.
- Comptes et mots de passe propres à ProductPulse.

## 8. Démarche qualité

Chaque User Story doit pouvoir être associée à :
- Des critères d'acceptation vérifiables.
- Des tests fonctionnels.
- Des tests API lorsque pertinents.
- Des tests de sécurité pour les accès protégés.
- Des tests de non-régression.

L'objectif est de présenter un produit construit avec une démarche complète : cadrage, conception UX/UI, développement, tests, livraison et amélioration continue.
