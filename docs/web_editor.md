# How the web editor works

## Flow:
- [Server] the user types the command /quest editor
- [Server] sends a POST request to the web API with the server config
- [API] saves the config, associates a token with it, and replies with the token
- [Server] opens a browser with the web editor URL and the token
- [Web client] fetches the config with the token via the API, and displays it in the editor
- [Web client] the user edits the config, and sends it to the API
- [Server] runs the command /quest applyedits <token> to apply the changes

## API
Symfony REST API

## Client:
- Nuxt

### How the web client works:

- The user enters an OpenAI API key
- The interface: one part handles the display, the other is a conversation with an API
that edits the quests based on the conversation.

Flow:
- The configuration is loaded on the client from the API
- The user enters an OpenAI API key -> localstorage
- The user enters a prompt, and the AI returns a structured response.
One or more structured response templates can be sent by the API.
Response type:
- Feedback (simple message, for example to ask for clarification)
- Action (PUT, PATCH, DELETE), on a JSON object (Quest, Pool, Category...)

A modal opens with the response. 
If it is an action, the user can accept it or not. They can also edit the response.

On the server side, a snapshot of the config is saved on every change so it can be rolled back.