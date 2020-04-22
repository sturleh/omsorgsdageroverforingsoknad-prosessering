# Omsorgsdageroverføringsøknad-prosessering
![CI / CD](https://github.com/navikt/omsorgsdageroverforingsoknad-prosessering/workflows/CI%20/%20CD/badge.svg)

Tjeneste som prosesserer søknader om overføring av omsorgsdager
Leser søknader fra Kafka topic `privat-overfore-omsorgsdager-soknad-mottatt` som legges der av [omsorgsdageroverforingsoknad-mottak](https://github.com/navikt/omsorgsdageroverforingsoknad-mottak).

Filtrerer på version 2 i metadata

## Prosessering
- Genererer Søknad-PDF
- Oppretter Journalpost
- Oppretter Gosys Oppgave
- Sletter mellomlagrede dokumenter

## Feil i prosessering
Ved feil i en av streamene som håndterer prosesseringen vil streamen stoppe, og tjenesten gi 503 response på liveness etter 15 minutter.
Når tjenenesten restarter vil den forsøke å prosessere søknaden på ny og fortsette slik frem til den lykkes.

## Henvendelser
Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

Interne henvendelser kan sendes via Slack i kanalen #team-düsseldorf.
