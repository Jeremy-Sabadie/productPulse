
# 📊 ProductPulse

**Plateforme de collecte, de modération et de suivi des retours utilisateurs.**

ProductPulse est une application web full-stack développée pour accompagner [My Popote](https://github.com/Jeremy-Sabadie/my_popote), une application de planification des repas.

Son objectif est de transformer les avis, suggestions et signalements de bugs en informations exploitables pour améliorer un produit logiciel.

Ce projet personnel illustre une démarche **Product Builder**, associant conception fonctionnelle, développement, qualité logicielle et automatisation des tests.

---

## 🎯 Objectifs du projet

- Centraliser les retours utilisateurs
- Identifier les anomalies et demandes d'évolution
- Faciliter la modération des avis
- Donner de la visibilité sur le traitement des retours
- Aider à prioriser les améliorations du produit
- Relier les retours utilisateurs au suivi du développement

ProductPulse constitue un complément fonctionnel de My Popote et un exemple concret de démarche d'amélioration continue.

---

## ✨ Fonctionnalités

### Espace utilisateur

Le périmètre fonctionnel comprend :

- Dépôt d'avis, de suggestions et de signalements de bugs
- Catégorisation des retours
- Consultation des avis publiés après modération
- Consultation de l'historique personnel
- Suivi du statut des avis
- Possibilité de soutenir les retours d'autres utilisateurs

### Espace administrateur

- Tableau de bord de gestion des retours
- Consultation et modération des avis
- Gestion des statuts
- Filtrage par catégories
- Consultation des soutiens pour faciliter la priorisation
- Identification des retours pouvant devenir des tickets de développement

### Évolutions prévues

- Création de tickets GitHub Issues à partir des retours sélectionnés
- Amélioration de la traçabilité entre les retours et les corrections
- Export PDF des retours et de leur suivi
- Enrichissement progressif des parcours utilisateurs

Les fonctionnalités décrivent le périmètre du produit. Leur disponibilité effective dépend de l'avancement des développements.

---

## 🏗️ Architecture technique

ProductPulse repose sur une architecture full-stack séparant l'interface utilisateur, la logique métier et la persistance.

```text
Utilisateur / Administrateur
            |
            v
     Angular 22 / TypeScript
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

Cette organisation favorise la séparation des responsabilités, la testabilité, la maintenabilité et l'évolution de l'application.

Le **Repository Pattern** permet d'isoler la persistance de la logique métier.

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
| Tests E2E | Playwright |
| CI/CD | GitHub Actions, pipelines de tests automatisés |
| Infrastructure | Docker, Docker Compose, VPS Linux |
| Déploiement | HTTPS, reverse proxy Caddy |
| Versionnement | Git, GitHub |
| Intégration prévue | GitHub Issues |

---

## 🔐 Authentification et intégration avec My Popote

ProductPulse fonctionne comme une application complémentaire de My Popote, sans imposer la création d'un second compte utilisateur.

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

## 🧪 Qualité logicielle et tests automatisés

La qualité logicielle constitue un axe central de ProductPulse.

Elle est intégrée au cycle de développement grâce à des **tests automatisés et des pipelines GitHub Actions déjà opérationnels**.

### Stratégie de tests

La démarche de validation couvre notamment :

- Les règles métier de gestion des avis
- La validation des données
- Les endpoints REST
- Les contrôles d'accès et les autorisations
- La vérification des JWT signés en RS256
- Les comportements du frontend
- Les parcours utilisateurs avec Playwright
- La prévention des régressions

### Tests backend — JUnit et Spring Boot Test

Les tests backend vérifient les comportements applicatifs, les règles métier, les endpoints REST et les mécanismes de sécurité.

Ils contribuent à détecter les anomalies avant l'intégration des modifications.

### Tests end-to-end — Playwright

Playwright permet d'automatiser la validation des parcours utilisateurs dans le navigateur.

Ces tests complètent les vérifications backend en contrôlant les interactions entre les différents composants de l'application.

### Démarche QA

Le développement associe :

1. Analyse du besoin utilisateur
2. Définition des critères d'acceptation
3. Conception des cas de test
4. Développement des fonctionnalités
5. Exécution des tests automatisés
6. Analyse des résultats et correction des anomalies
7. Validation avant intégration

Cette démarche illustre une approche de la qualité logicielle allant de la conception des tests à leur automatisation.

---

## ⚙️ CI/CD — GitHub Actions

**Des workflows GitHub Actions sont en place et opérationnels sur le dépôt ProductPulse.**

Ils permettent d'automatiser les contrôles qualité et de détecter les régressions lors des évolutions du projet.

```text
Modification du code
        |
        v
Git / GitHub
        |
        v
GitHub Actions
        |
        v
Exécution des contrôles automatisés
        |
        v
Résultats des tests
        |
        v
Analyse et correction si nécessaire
        |
        v
Validation de l'intégration
```

### Apports de l'intégration continue

- Exécution reproductible des contrôles techniques
- Détection précoce des régressions
- Visibilité sur les résultats des tests
- Fiabilisation des modifications
- Traçabilité des validations
- Réduction des vérifications manuelles répétitives

L'intégration continue est une **réalisation effective du projet**, et non une fonctionnalité prévue.

Les suites de tests et les workflows restent évolutifs afin d'accompagner les nouvelles fonctionnalités.

---

## 📋 Gestion du cycle de vie des retours

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

Cette intégration constitue une évolution fonctionnelle, et non une automatisation déjà opérationnelle.

---

## 🐳 Docker et déploiement

ProductPulse est conteneurisé et déployé sur un **VPS Linux personnel**.

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

Le déploiement sur VPS et les workflows GitHub Actions constituent deux réalisations complémentaires : **exploitation de l'application et automatisation de sa validation qualité**.

---

## 🔄 Organisation du développement

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
CI GitHub Actions
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

**Application développée et déployée sur un VPS personnel, avec des tests automatisés et des workflows GitHub Actions opérationnels.**

Le projet continue d'évoluer pour compléter les parcours utilisateurs et renforcer la gestion du cycle de vie des retours.

### Prochaines étapes

- Finaliser et valider les parcours utilisateurs
- Intégrer GitHub Issues
- Maintenir et enrichir les suites de tests automatisés existantes
- Faire évoluer les workflows CI/CD selon les besoins du projet
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
