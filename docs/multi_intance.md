# Multi instance

## Objectif
Permettre au plugin de fonctionner entre plusieurs serveurs.

## Outcome of the analysis:
AtomicRedisQuestExecutor :
 - redis lock
 - load from db
 - execute
 - sync pub sub
 - save to db
 - unlock

Batching progress:
 - flush 1x/sec