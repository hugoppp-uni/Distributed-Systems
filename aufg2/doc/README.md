# Verteilte Primzahlfaktorisierung im Aktormodell

## Kommunikationsschritte passend zum Problem

### Komponenten:
- n Client Actor
- 1 Server für Gruppenkommunikation
- n Worker Actor

### Message types:
- Task
  - Number
- Result
  - Number
  - Result
  - Cpu-zeit
  - Summe der Rho-Zyklendurchläufe
- IdleRequest
- IdleResponse
  - RandomTask

### Ablauf
- Client
  - schickt
    - Task
  - empfängt
    - Result
     
- Worker
  - empfängt
    - Task
    - IdleRequest
    - IdleResponse
  - schickt
    - IdleRequest
    - IdleResponse
    - Result

## Verteilung

### Client

### Worker

Worker
- taskSet: Set\<int>


Bearbeite Nachrichten:
- Task t erhalten -> taskSet.Add(t.Number)
  - mit p = (1 / taskSet.count) aktuelle Berechnung abbrechen, t wählen
  - mit p = 1 - (1 / taskSet.count) aktuelle Berechnung fortführen
- Result r erhalten -> taskSet.Remove(r.Number)
  - if (currentTask = r.Number) -> aktuelle Berechnung abbrechen
- IdleRequest iReq erhalten -> send( IdleResponse{taskSet.randomElement().Number} )
- IdleResponse iRes erhalten -> taskSet.add(iRes.Number)


Wenn lokal das Ergebnis gefunden wurde:
  - Result r{Number = n, Result = r}
  - taskSet.Remove(r.Number)
  - send(r)
  - if (taskSet.empty())
    - send(IdleRequest{})
  - else
      - fange mit zufälligen Task aus dem Set an

Während Berechnung periodisch:
- if(mailbox().empty())
  - berechne weiter
- else
  - bearbeite Nachrichten

## Qualität Ihrer Lösungsideen anhand Qualitätseigenschaften verteilter Systeme messen und benutzen für Konzeptbegründung
### Skalierungsverhalten

Die Größe der Nachrichten ist immer konstant, es werden keine Listen o.Ä. verschickt. Pro IdleRequest
entstehen n mit n = Anzahl Worker Nachrichten, dies könnte evtl. verbessert werden.

- Normales Verhalten 
    - Mit sinkender Anzahl von Tasks steigt die Wahrscheinlichkeit p = (1 / taskSet.count), dass ein Worker einen
      neuen Task übernimmt. Dadurch arbeiten im Mittel immer die gleiche Anzahl an Workern an einem Task

- Ungünstiges Verhalten beim Edge Case 'viele Tasks wenig Worker':
  - Wenn bereits b Tasks im backlog (noch nicht abgeschlossene Tasks) liegen, und dann ein neuer Worker beitritt,
    erhält dieser nur maximal n Tasks bei n Workern. Sobald die n Tasks abgeschlossen sind, schickt dieser
    einen IdleRequest los, wodurch wieder maximal n Tasks in das taskSet hinzufügt werden. Wenn nun ein neuer Worker 
    beitritt, während b > n ist, entsteht hierdurch bei dem neuen Worker ein Bias auf neu hinzukommende Tasks, da die 
    Wahrscheinlichkeit, einen neuen Task auszuwählen bei p = (1 / n) liegt. Dies liegt daran, dass der Backlog nicht 
    vollständig propagiert wird. Sobald der Backlog abgearbeitet wird, verringert sich der Bias.

### Fehlertoleranz

- Ein Worker schmiert ab 
    - nicht schlimm, da die anderen Worker dieses erst aus dem Backlog nehmen, sobald sie das Ergebnis erhalten.
    - somit wird dieser Task irgendwann von einem anderen Worker übernommen. (probalistic load balancing)
    - Client bekommt nichts mit

### Gemeinsame Ressourcennutzung

Es gibt keine gemeinsame Ressourcennutzung.

### Offenheit

Es können jederzeit neue Worker und neue Clients gespawnt werden.

### Nebenläufigkeit
Nebenläufigkeit wird durch das C++ Actor Framework (CAF) sichergestellt.

### Sicherheit

Ein Teil dieser Antworten würde die Bevölkerung verunsichern

### Transparenz

Worker-Verteilung gegenüber Clients transparent. Clients und Worker sind lose gekoppelt. 
Fehler beim Worker sind dem Client gegenüber transparent, außer wenn alle Worker abstürzen.

## Leistungsfähigkeit Ihrer Lösung mithilfe einer verteilten Laufzeitmessung.

// TODO

## Wie erhalten neu hinzukommender Worker Aufgaben?
Probabilistic Load Balancing

## Wie entscheiden Worker fuer welchen Client sie jetzt arbeiten?
Probabilistic Load Balancing

## Wie verhindern Sie, dass mehrere Clients sich gegenseitig die Worker wegnehmen und letztlich kein Client Fortschritt machen kann?
Probabilistic Load Balancing
