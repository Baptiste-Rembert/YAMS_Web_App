# YAMS_Web_App

Racine du projet pour l'application Yams (backend Spring Boot + frontend React/Vite).

## État d'avancement du projet

Le projet suit une méthodologie de développement itérative. Voici l'état actuel des modules :

*   **Logique métier (Backend) :** 100% - Moteur de jeu complet, calcul des scores et règles du Yams validés par tests unitaires.
*   **Persistance des données :** 80% - Couche JPA/SQL en cours de finalisation (gestion des historiques et persistance des sessions via JDBC).
*   **Mode Multijoueur :** 40% - Développement de la couche temps-réel via WebSockets (STOMP). La synchronisation des états de jeu entre clients est l'objectif majeur de la version actuelle.

## Commandes utiles

- Lancer le backend (Windows PowerShell):

- Lancer le backend (Linux/macOS):

```bash
# (optionnel) export JAVA_HOME si nécessaire, par ex. :
# export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
# depuis la racine du projet :
mvn -DskipTests spring-boot:run
```

- Lancer le frontend (dans `frontend`):

```bash
cd frontend
npm install
npm run dev
```

- Construire et exécuter les tests (backend):

```bash
# depuis la racine du projet :
mvn test
```

## Endpoints utiles

- `GET /api/hello` — test simple du backend
- `POST /api/auth/login` — body JSON `{ "username": "alice" }`
- `POST /api/auth/register` — body JSON `{ "username": "alice" }`
- `POST /api/games` — créer une partie
- `GET  /api/games` — lister les parties
- `POST /api/games/{id}/join` — body JSON `{ "username": "player1" }`
- `GET  /api/games/{id}` — détails de la partie
- `GET  /api/chat/ping` — test REST du chat
 - `POST /api/games/{id}/start` — démarrer / initialiser la partie

## WebSocket / Chat

- STOMP endpoint (SockJS): `/ws`
- Envoyer messages: destination `/app/chat.send`
- S'abonner (broadcast global): `/topic/chat`
- S'abonner à un salon de jeu: `/topic/game.{gameId}`

## Itérations & Tests

- J'ai ajouté des tests d'intégration pour les controllers et WebSocket. Lancez `mvn test` pour exécuter tous les tests.

Je mettrai à jour ce README au fur et à mesure des prochaines itérations (auth avancée, scorecards, règles du jeu, UI examples).