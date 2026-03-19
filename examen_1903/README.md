# Plateforme de réservation de salles de coworking — Microservices

Architecture Spring Boot / Spring Cloud / Apache Kafka

## Prérequis

- Java 17+
- Maven 3.8+
- Apache Kafka + Zookeeper *(optionnel — voir section Kafka)*

## Ports

| Service              | Port |
|----------------------|------|
| config-server        | 8888 |
| discovery-server     | 8761 |
| api-gateway          | 8080 |
| room-service         | 8081 |
| member-service       | 8082 |
| reservation-service  | 8083 |

## Ordre de lancement

> Lancer chaque commande dans un terminal séparé depuis `examen_1903/`

```bash
# 1. Config Server (attendre ~20s qu'il soit UP)
cd config-server && mvn spring-boot:run

# 2. Discovery Server / Eureka (attendre ~20s)
cd discovery-server && mvn spring-boot:run

# 3. Services métier + Gateway (ordre libre, en parallèle)
cd api-gateway && mvn spring-boot:run
cd room-service && mvn spring-boot:run
cd member-service && mvn spring-boot:run
cd reservation-service && mvn spring-boot:run
```

> **Important** : chaque service utilise une base H2 **en mémoire**. Redémarrer un service remet sa base à zéro.

## Kafka — Comportement sans broker

Les services fonctionnent **sans Kafka**. Kafka est utilisé uniquement pour la propagation asynchrone de certains événements. Sans broker :

| Fonctionnalité | Sans Kafka | Avec Kafka |
|----------------|-----------|-----------|
| CRUD rooms / members / reservations | ✅ | ✅ |
| Disponibilité salle (available) lors d'une réservation/annulation/completion | ✅ synchrone | ✅ |
| Suspension membre (`suspended=true`) après quota atteint | ❌ manuel via PUT | ✅ auto |
| Désuspension membre (`suspended=false`) après annulation | ❌ manuel via PUT | ✅ auto |
| Annulation des réservations CONFIRMED quand une salle est supprimée | ❌ reste CONFIRMED | ✅ auto |
| Suppression des réservations quand un membre est supprimé | ❌ restent en base | ✅ auto |

## Kafka — Démarrage local (Windows)

```bash
# Dans le dossier d'installation Kafka
.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties
.\bin\windows\kafka-server-start.bat .\config\server.properties
```

## Topics Kafka

| Topic | Producteur | Consommateur | Action |
|-------|-----------|-------------|--------|
| `room-deleted` | room-service | reservation-service | Annule toutes les réservations CONFIRMED de la salle |
| `member-deleted` | member-service | reservation-service | Supprime toutes les réservations du membre + libère les salles |
| `member-suspend` | reservation-service | member-service | Passe `suspended=true` |
| `member-unsuspend` | reservation-service | member-service | Passe `suspended=false` |
| `reservation-cancelled-for-member` | reservation-service | member-service | Réévalue la suspension après annulation en cascade |

## Swagger UI

| Service | URL |
|---------|-----|
| room-service | http://localhost:8081/swagger-ui.html |
| member-service | http://localhost:8082/swagger-ui.html |
| reservation-service | http://localhost:8083/swagger-ui.html |

## Eureka Dashboard

http://localhost:8761

## API — Endpoints principaux

### Rooms (port 8081)
| Méthode | URL | Description |
|---------|-----|-------------|
| GET | /rooms | Lister les salles |
| POST | /rooms | Créer une salle |
| GET | /rooms/{id} | Détail d'une salle |
| PUT | /rooms/{id} | Modifier une salle |
| PATCH | /rooms/{id}/availability | Changer la disponibilité |
| DELETE | /rooms/{id} | Supprimer (+ événement Kafka `room-deleted`) |

### Members (port 8082)
| Méthode | URL | Description |
|---------|-----|-------------|
| GET | /members | Lister les membres |
| POST | /members | Créer un membre (maxConcurrentBookings auto selon subscriptionType) |
| GET | /members/{id} | Détail d'un membre |
| PUT | /members/{id} | Modifier un membre |
| DELETE | /members/{id} | Supprimer (+ événement Kafka `member-deleted`) |

### Reservations (port 8083)
| Méthode | URL | Description |
|---------|-----|-------------|
| GET | /reservations | Lister les réservations |
| POST | /reservations | Créer (valide salle dispo + membre non suspendu) |
| GET | /reservations/{id} | Détail |
| PATCH | /reservations/{id}/cancel | Annuler (CONFIRMED → CANCELLED) |
| PATCH | /reservations/{id}/complete | Compléter (CONFIRMED → COMPLETED) |

## Collection Bruno — Tests par étapes

Importer le dossier `bruno/` dans Bruno. **Sélectionner l'environnement `local`** avant de lancer.

### Scenario complet (IDs prévisibles avec base H2 vide)

| Étape | Dossier Bruno | Requête | Résultat attendu |
|-------|--------------|---------|-----------------|
| 2a | step2a-rooms | 02, 03, 04 | 3 salles créées (id=1,2,3) |
| 2b | step2b-members | 01, 02, 03 | Alice BASIC (max=2), Bob PRO (max=5), Carol ENTERPRISE (max=10) |
| 2c | step2c-reservations | 01 | Réservation id=1 (Alice, salle 1), salle 1 → available=false |
| 2c | step2c-reservations | 02 | GET salle 1 → available=**false** |
| 2c | step2c-reservations | 03 | Doublon même créneau → **409** |
| 3 | step3-kafka | 01 | PUT Alice → suspended=true *(simule Kafka)* |
| 3 | step3-kafka | 02 | GET Alice → suspended=**true** |
| 3 | step3-kafka | 03 | Alice suspendue essaie salle 2 → **409** |
| 3 | step3-kafka | 04 | PUT Alice → suspended=false |
| 3 | step3-kafka | 05 | PATCH cancel réservation 1 → CANCELLED, salle 1 → available=true |
| 3 | step3-kafka | 06 | GET salle 1 → available=**true** |
| 3 | step3-kafka | 07 | Bob réserve salle 1 → id=2, CONFIRMED |
| 3 | step3-kafka | 09 | DELETE salle 1 → Kafka `room-deleted` |
| 3 | step3-kafka | 10 | Réservation Bob : CANCELLED *(Kafka)* ou CONFIRMED *(sans Kafka)* |
| 3 | step3-kafka | 11 | DELETE Carol → Kafka `member-deleted` |
| 4 | step4-state-pattern | 01 | Bob réserve salle 2 → id=3, CONFIRMED |
| 4 | step4-state-pattern | 02 | PATCH cancel id=3 → CANCELLED |
| 4 | step4-state-pattern | 03 | Re-cancel id=3 → **422** *(State Pattern)* |
| 4 | step4-state-pattern | 04 | Bob réserve salle 3 → id=4, CONFIRMED |
| 4 | step4-state-pattern | 05 | PATCH complete id=4 → COMPLETED |
| 4 | step4-state-pattern | 06 | Re-complete id=4 → **422** *(State Pattern)* |
| 6 | step4-state-pattern | 08-10 | Swagger → 200 OK sur les 3 services |

> **Note sur les IDs** : les IDs sont auto-incrémentés par H2 depuis 1 à chaque démarrage.
> Si tu as des données existantes, les IDs seront différents. Relancer les services (CTRL+C puis `mvn spring-boot:run`) remet les bases à zéro.
