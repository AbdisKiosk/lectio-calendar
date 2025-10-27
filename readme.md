# Skema Kalender Integration

## Opsætning

1. Opret en `.env`-fil i projektets rodmappe, baseret på `.env.example`.
2. Udfyld `SKOLEID` i `.env`-filen. Dette er det tal, der står ved alle links, når du er logget ind på skolens system.

Eksempel på `.env`:
SKOLEID=631
AUTH=et-eller-andet

## Brug

For at abonnere på kalenderen i f.eks. iCloud, skal du bruge et link som dette:
https://<din-server-ip>/schedule?auth=et-eller-andet

**Bemærk:** Du skal have din egen server. Personligt bruger jeg en Ubuntu VM med GUI, så jeg kan logge ind med MitID.

## Fremtidig funktionalitet

Der arbejdes på en browser extension, som kan hente dine cookies direkte fra din browser, så du kun skal være logget ind dér, og den derved skal kunne køres headless.