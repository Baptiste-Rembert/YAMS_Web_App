# YAMS_Web_App

Racine du projet pour l'application Yams (backend Spring Boot + frontend React/Vite).

## Commandes utiles

- Lancer le backend (Windows PowerShell):

```powershell
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
cd YAMS_Web_App
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