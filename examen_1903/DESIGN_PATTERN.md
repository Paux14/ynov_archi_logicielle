# Design Pattern — State (Comportemental)

## Pattern choisi : **State**

## Localisation

`reservation-service/src/main/java/com/ynov/coworking/reservation/pattern/`

## Justification

Le cycle de vie d'une réservation suit un automate d'états bien défini :

```
CONFIRMED ──────► CANCELLED
     │
     └──────────► COMPLETED
```

Les transitions ne sont valides que depuis l'état **CONFIRMED** :
- Une réservation **CANCELLED** ne peut plus être annulée ni complétée.
- Une réservation **COMPLETED** ne peut plus être modifiée.

Le pattern **State** modélise naturellement cet automate :
- Chaque état (`ConfirmedState`, `CancelledState`, `CompletedState`) implémente l'interface `ReservationState`.
- Les transitions `cancel()` et `complete()` lancent une `IllegalStateException` si la transition est interdite, sans aucun `if/switch` dispersé dans le service.
- `ReservationStateFactory` instancie l'état courant à partir du statut persisté en base.

## Alternatives envisagées

| Pattern | Raison du rejet |
|---------|----------------|
| **Builder** | Pertinent pour la construction, mais ne couvre pas la gestion des transitions de statut |
| **Strategy** | Moins adapté car les transitions dépendent de l'état *courant*, pas d'un algorithme interchangeable |
| **Chain of Responsibility** | Utile pour des validations en cascade, mais ici la logique est liée à l'état, non à une chaîne de validateurs |

## Classes

| Classe | Rôle |
|--------|------|
| `ReservationState` | Interface — déclare `cancel()` et `complete()` |
| `ConfirmedState` | Autorise les deux transitions |
| `CancelledState` | Rejette toute transition |
| `CompletedState` | Rejette toute transition |
| `ReservationStateFactory` | Retourne l'état correspondant au statut persisté |
