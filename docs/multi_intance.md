# Multi instance

## Objectif
Permettre au plugin de fonctionner entre plusieurs serveurs.

## Résultat reflexion:
AtomicRedisQuestExecutor :
 - redis lock
 - load from db
 - execute
 - sync pub sub
 - save to db
 - unlock

Batching progress:
 - flush 1x/sec