# 📊 ProductPulse

**Plateforme de collecte, de modération et de suivi des retours utilisateurs.**

ProductPulse est une application web full-stack développée pour accompagner [My Popote](https://github.com/Jeremy-Sabadie/my_popote), une application de planification des repas.

Son objectif est de transformer les avis, suggestions et signalements de bugs en informations exploitables pour améliorer un produit logiciel.

Ce projet personnel illustre une démarche **Product Builder**, à la croisée de la conception fonctionnelle, du développement et de la qualité logicielle.

---

## 🎯 Objectifs

- Centraliser les retours utilisateurs
- Identifier les anomalies et demandes d'évolution
- Faciliter la modération des avis
- Donner de la visibilité sur le traitement des retours
- Aider à prioriser les améliorations du produit
- Relier les retours utilisateurs au suivi du développement

---

## ✨ Fonctionnalités

### Espace utilisateur

Le périmètre fonctionnel comprend :

- Dépôt d'avis, de suggestions et de signalements de bugs
- Catégorisation des retours
- Consultation des avis publiés après modération
- Consultation de l'historique personnel
- Suivi du statut des avis
- Soutien des retours d'autres utilisateurs

### Espace administrateur

- Tableau de bord de gestion des retours
- Consultation et modération des avis
- Gestion des statuts
- Filtrage par catégories
- Consultation des soutiens pour aider à la priorisation
- Identification des retours pouvant devenir des tickets de développement

### Évolutions prévues

- Création de tickets GitHub Issues depuis les retours sélectionnés
- Suivi des corrections et évolutions
- Export PDF des retours
- Renforcement des tests automatisés et de la CI/CD

Les fonctionnalités décrivent le périmètre du produit ; leur disponibilité effective dépend de l'avancement des développements.

---

## 🏗️ Architecture

ProductPulse repose sur une architecture full-stack séparant l'interface, la logique métier et la persistance.

```text
Utilisateur / Administrateur
            |
            v
     Angular / TypeScript
            |
        REST / JSON
            |
            v
     Spring Boot / Java 21
            |
       Spring Data JPA
            |
            v
          MariaDB
```

Le backend suit une architecture en couches :

```text
Controller
    |
    v
Service / Business Logic
    |
    v
Repository
    |
    v
JPA / MariaDB
```

Le **Repository Pattern** permet d'isoler la persistance de la logique métier, facilitant les tests, la maintenance et l'évolution de l'application.

---

## 🛠️ Stack technique

| Domaine | Technologies |
|---|---|
| Frontend | Angular 22, TypeScript |
| Backend | Java 21, Spring Boot |
| API | REST / JSON |
| Persistance | MariaDB, Spring Data JPA / Hibernate |
| Migrations | Flyway |
| Sécurité | Spring Security, JWT RS256 |
| Tests backend | JUnit, Spring Boot Test |
| Tests E2E | Playwright (automatisation à renforcer) |
| Infrastructure | Docker, Docker Compose, VPS Linux |
| Déploiement | HTTPS, reverse proxy Caddy |
| Versionnement | Git, GitHub |
| Intégration prévue | GitHub Issues |

---

## 🔐 Authentification et intégration avec My Popote

ProductPulse est conçu comme une application complémentaire de My Popote.

L'objectif est de proposer une expérience cohérente sans imposer la création d'un second compte utilisateur.

### Authentification partagée

Le mécanisme repose sur des **JSON Web Tokens signés en RS256**.

```text
      My Popote
  Authentification
          |
          v
     JWT signé
   (clé privée)
          |
          v
     ProductPulse
          |
          v
 Vérification JWT
   (clé publique)
          |
          v
  Accès aux ressources
```

Principes de sécurité :

- My Popote assure l'authentification des utilisateurs
- ProductPulse vérifie la signature des jetons
- La clé privée reste détenue par My Popote
- Les audiences JWT identifient les applications destinataires
- Les autorisations sont contrôlées côté backend
- Les secrets sont externalisés de la configuration versionnée

Cette architecture permet de réutiliser une identité commune entre plusieurs applications.

---

## 🧪 Qualité logicielle et stratégie de tests

La qualité logicielle fait partie intégrante du projet.

Les principaux axes de validation concernent :

- Les règles métier de gestion des avis
- La validation des données
- Les endpoints REST
- Les contrôles d'accès
- La vérification des JWT
- Les opérations de persistance
- Les parcours utilisateurs
- La prévention des régressions

### Démarche de développement

```text
Besoin utilisateur
        |
        v
Analyse fonctionnelle
        |
        v
Critères d'acceptation
        |
        v
Tests
        |
        v
Développement
        |
        v
Validation / Refactoring
```

L'objectif est de combiner conception fonctionnelle, tests techniques et validation des comportements attendus.

---

## 📋 Cycle de vie des retours

ProductPulse vise à assurer la traçabilité d'un retour depuis son dépôt jusqu'à son éventuelle prise en charge dans le développement.

```text
Retour utilisateur
        |
        v
Enregistrement
        |
        v
Modération
        |
        v
Analyse / Catégorisation
        |
        v
Priorisation
        |
        v
Ticket de développement
        |
        v
Correction ou évolution
```

L'intégration prévue avec **GitHub Issues** permettra de relier les retours sélectionnés aux tâches techniques correspondantes.

Ce processus constitue un objectif fonctionnel du projet, et non une chaîne entièrement automatisée à ce stade.

---

## 🐳 Docker et déploiement

ProductPulse est conteneurisé et a fait l'objet d'un déploiement sur un VPS Linux personnel.

```text
Internet
    |
  HTTPS
    |
    v
Caddy (reverse proxy)
    |
    +----> Frontend Angular
    |
    +----> API Spring Boot
                 |
                 v
              MariaDB
                 |
                 v
          Volume persistant
```

Le projet utilise Docker Compose pour orchestrer ses composants.

Le travail de déploiement comprend :

- Configuration des conteneurs
- Gestion des variables d'environnement
- Persistance des données
- Exposition HTTPS
- Communication frontend / backend
- Authentification interapplications
- Vérifications après mise en production

---

## 🔄 Méthode de développement

Le développement repose sur Git et GitHub, avec des branches dédiées aux fonctionnalités et corrections.

```text
Besoin / Anomalie
        |
        v
Analyse et cadrage
        |
        v
Branche feature / fix
        |
        v
Développement et tests
        |
        v
Revue et validation
        |
        v
Intégration
        |
        v
Déploiement
```

Cette organisation facilite la traçabilité des modifications et la maîtrise des régressions.

---

## 🤖 Utilisation de l'intelligence artificielle

L'intelligence artificielle est utilisée comme **assistant technique**, et non comme agent autonome chargé de développer l'application.

Elle intervient pour :

- Explorer des solutions d'architecture
- Analyser des erreurs et anomalies
- Proposer des pistes d'implémentation
- Identifier des cas limites
- Participer aux revues de code
- Accompagner les refactorings

Les décisions de conception, de sécurité, de stratégie de tests et de déploiement restent sous contrôle humain.

Les propositions de l'IA sont examinées, testées et validées avant intégration.

> **AI-assisted, developer-driven.**

---

## 📈 Démarche produit

ProductPulse met en pratique une approche d'amélioration continue fondée sur les retours utilisateurs.

Le cycle cible est le suivant :

1. Recueillir les besoins et difficultés rencontrées
2. Qualifier les retours
3. Identifier les anomalies et opportunités d'évolution
4. Prioriser les interventions
5. Transformer les besoins retenus en tâches de développement
6. Tester les corrections et évolutions
7. Améliorer le produit

Cette démarche associe **analyse fonctionnelle, conception produit, développement et QA**.

---

## 🚀 État du projet

**Application développée et déployée sur un VPS personnel.**

Le projet continue d'évoluer pour compléter les parcours utilisateurs et renforcer la gestion du cycle de vie des retours.

### Prochaines étapes

- Finaliser et valider les parcours utilisateurs
- Intégrer GitHub Issues
- Renforcer la couverture de tests automatisés
- Compléter les pipelines CI/CD
- Améliorer l'observabilité et le suivi des anomalies

---

## 🔗 Projet associé

**[My Popote](https://github.com/Jeremy-Sabadie/my_popote)** — Application de planification des repas, de gestion des recettes et de génération de listes de courses.

ProductPulse complète My Popote en apportant une démarche structurée de collecte et de traitement des retours utilisateurs.

---

## 👤 Auteur

**Jérémy Sabadie**

**Product Builder | Conception produit & qualité logicielle**

Développement Java / Angular · Analyse fonctionnelle · QA

Projet personnel illustrant une démarche complète : **de la collecte du besoin utilisateur à sa traduction en évolution logicielle, avec une attention particulière à la qualité, à la sécurité et à la traçabilité**.
