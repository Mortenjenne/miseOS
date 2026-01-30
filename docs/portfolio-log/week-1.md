
# Portfolio Log: MiseOS

## Uge 1: Idéfase og Vision
Dato: 2026-01-30

### Valg af projekt
Efter at have brainstormet på et par forskellige idéer, herunder et administrativt feriesystem, er valget faldet på MiseOS. Jeg har brugt ugen på at sparre med tidligere kollegaer fra mine 20 år i køkkenbranchen for at validere idéen. Konklusionen er klar: Der mangler et dedikeret "styresystem" til køkkenet, der kan erstatte manuelle Word-dokumenter og løse sedler. MiseOS skal fungere som den centrale hub for menuplanlægning og ingrediens styring.

### Hvad er MiseOS?
MiseOS er et værktøj, der digitaliserer og strukturerer den kreative planlægningsfase i professionelle køkkener. Systemet fungerer som bindeleddet mellem kokkens idéer og køkkenchefens drift.

#### Kreativt input:
Kokkene på de enkelte sektioner (f.eks. Varm, Kold eller Bageri) kan løbende indsende ugeforslag til menuer. Dette sikrer, at deres faglighed og idéer bliver en integreret del af planlægningen.

#### Råvareforespørgsel:
I forbindelse med menuforslagene kan kokken med det samme sende forespørgsler på de specifikke råvarer, der skal bruges til retterne.

#### Centraliseret varebestilling:
Køkkenchefen får et samlet overblik over alle sektioners forslag og råvareønsker. Herefter kan chefen godkende planerne og stå for den endelige, samlede varebestilling baseret på systemets data.

#### Effektivisering:
Ved at samle forslag og bestillinger ét sted, slipper man for fejlkilder som mistede sedler og mundtlige misforståelser, samtidig med at man sparer tid på manuel indtastning i f.eks. Word eller Excel.
Målet er at fjerne det overflødige tastearbejde og sikre, at informationen aldrig går tabt fra idé til servering.

### MVP (Minimum Viable Product)
Det har været vigtigt for mig at fastslå et skarpt MVP, så projektet forbliver realistisk inden for de 10 uger. Selvom jeg drømmer om at integrere både vejr-data, kunde statistik og takeaway, har jeg besluttet, at kernen i MiseOS skal være:

- **Digital uge menuplan:** Slut med Word-skabeloner og printede ark.

- **Godkendelses-flow:** Køkkenchefen samler trådene fra de forskellige partier.

- **Ingrediens bestilling:** Et simpelt system til at ønske råvarer direkte knyttet til menuen.

### Refleksioner og Beslutninger
Det sværeste i denne uge var faktisk at fravælge features. Man vil gerne løse alt på én gang, men erfaring fra tidligere projekter viser, at "less is more". Ved at fokusere benhårdt på et MVP fra start, kan jeg prioritere Clean Code og anvendelse af SOLID-principperne, så jeg opnår en høj teknisk kvalitet i min backend.

Jeg har struktureret mine tanker og min arkitektur sådan, at systemet kan bære fremtidige udvidelser uden at skulle bygges om senere. De ekstra features er ikke smidt væk, men er placeret på en "Nice-to-have" liste, som kan implementeres løbende, hvis der er overskud af tid i processen.
#### Næste skridt: 
Jeg har oprettet mit GitHub-repository og skrevet en grundig README, der forklarer projektets vision. Næste uge begynder det tekniske arbejde med at analysere datamodellen og fastlægge, hvordan de første data skal gemmes via JPA.
