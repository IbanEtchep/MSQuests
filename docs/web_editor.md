# Fonctionnement éditeur web

## Flux:
- [Serveur] l'utilisateur tape la commande /quest editor
- [Serveur] envoi une requête POST à l'API web avec la config du serveur
- [API] sauvegarde la config, lui associe un token, et répond avec le token
- [Serveur] ouvre un navigateur avec l'URL de l'éditeur web et le token
- [Client web] récupère la config avec le token via l'API, et l'affiche dans l'éditeur
- [Client web] l'utilisateur modifie la config, et l'envoi à l'API
- [Serveur] tape la commande /quest applyedits <token> pour appliquer les modifications

## API
Symfony API REST

## Client:
- Nuxt

### Fonctionnement client web:

- L'utilisateur entre une clé API OpenAI
- L'interface: Une partie gère l'affichage, l'autre est une conversation avec une api
qui modifie les quetes en fonction de la conversation.

Flux:
- La configuration est chargée sur le client depuis l'API
- L'utilisateur entre une clé API OpenAI -> localstorage
- L'utilisateur entre un prompt, et l'IA renvoi une réponse structurée.
Une ou plusieurs templates de réponse structurée peuvent être envoyées par l'API.
Réponse de type:
- Feedback (simple message, par exemple pour demander des précisions)
- Action (PUT, PATCH, DELETE), d'un objet JSON (Quête, Pool, Catégorie...)

Une modal d'ouvre avec la réponse. 
Si c'est une action, l'utilisateur peut la valider ou non. Il a aussi la possibilité de modifier la réponse.

Côté serveur, une snapshot de la config est sauvegardée à chaque modification pour pouvoir revenir en arrière.