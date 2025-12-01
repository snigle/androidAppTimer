# AppTimer

AppTimer est une application Android conçue pour vous aider à gérer votre temps d'écran. Elle surveille les applications que vous utilisez et vous permet de définir des limites de temps pour chacune d'elles.

<a href="https://play.google.com/store/apps/details?id=com.github.snigle.apptimer"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" width="200"/></a>

## Logo

![Logo AppTimer](docs/images/presentation.png)

## Fonctionnalités

*   **Surveillance de l'utilisation des applications** : AppTimer s'exécute en arrière-plan pour suivre le temps que vous passez sur chaque application.
*   **Minuteries personnalisables** : Définissez des limites de temps quotidiennes pour des applications spécifiques.
*   **Alertes de temps** : Une bulle flottante discrète vous avertit lorsque votre temps est presque écoulé.
*   **Prolongation du temps** : Besoin de quelques minutes de plus ? Prolongez facilement la minuterie lorsque vous en avez besoin.
*   **Contrôle de l'écran** : AppTimer met intelligemment les minuteries en pause lorsque votre écran est éteint pour préserver la batterie.

## Comment ça marche

L'application utilise les `UsageStatsManager` d'Android pour suivre l'application au premier plan. Un service d'arrière-plan surveille en permanence l'application en cours d'exécution et gère les minuteries. Lorsque le temps alloué pour une application est presque écoulé, une bulle flottante apparaît, vous donnant la possibilité de prolonger votre session ou de fermer l'application.

## Captures d'écran

<img alt="Capture d'écran 1" src="docs/images/Screenshot_20251124-215541.jpg" width="200"/> <img alt="Capture d'écran 2" src="docs/images/Screenshot_20251124-215554.jpg" width="200"/> <img alt="Capture d'écran 3" src="docs/images/Screenshot_20251124-220151.jpg" width="200"/>
