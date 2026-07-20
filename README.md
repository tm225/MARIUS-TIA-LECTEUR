# MARIUS TIA MUSIQUE

Lecteur de musique Android natif (Kotlin + Jetpack Compose + Media3/ExoPlayer),
avec design "informatique" (thème sombre, police monospace, accents néon).

## Fonctionnalités
- Scan automatique de **toutes les pistes audio** du téléphone (via MediaStore)
- Lecture en arrière-plan avec notification (MediaSessionService)
- Écran Bibliothèque avec recherche + onglet **Playlists** (création, ajout/retrait de pistes)
- Écran Lecteur : pochette d'album réelle en **disque rotatif animé**, barres d'égalisation vivantes, bouton lecture pulsant, lecture/pause, suivant/précédent, aléatoire, répétition, **minuteur de sommeil** (15/30/45/60 min)
- **Égaliseur audio 5 bandes** avec préréglages (accessible depuis Paramètres)
- Écran Paramètres : changement de la couleur du lecteur (8 teintes néon), appliquée à toute l'app
- Nom "MARIUS TIA MUSIQUE" affiché en haut à gauche, dans la couleur choisie

## Compiler l'APK (via GitHub Actions — sans PC)

1. Crée un nouveau dépôt GitHub (ex: `marius-tia-musique`), vide.
2. Depuis l'app GitHub mobile ou l'interface web, envoie tout le contenu de ce
   dossier dans le dépôt (en conservant l'arborescence, notamment le dossier
   `.github/workflows/`).
3. Va dans l'onglet **Actions** de ton dépôt : le workflow
   **"Build APK - Marius Tia Musique"** se lance automatiquement à chaque
   envoi sur la branche `main` (ou lance-le manuellement via
   **Run workflow**).
4. Une fois le workflow terminé (icône verte), ouvre le run → section
   **Artifacts** → télécharge `MariusTiaMusique-debug-apk`.
5. Décompresse le `.zip` téléchargé : il contient `app-debug.apk`.
   Installe-le sur ton téléphone (autorise l'installation depuis une source
   inconnue si demandé).

## Structure du projet
```
MariusTiaMusique/
├── app/
│   └── src/main/
│       ├── java/com/mariustia/musique/
│       │   ├── MainActivity.kt        (écran principal, navigation)
│       │   ├── MusicService.kt        (lecture en arrière-plan)
│       │   ├── PreferencesManager.kt  (sauvegarde couleur choisie)
│       │   ├── data/                  (scan MediaStore, modèle Track)
│       │   └── ui/                    (Bibliothèque, Lecteur, Paramètres)
│       ├── res/
│       └── AndroidManifest.xml
└── .github/workflows/build.yml        (compilation automatique)
```

## Personnalisation
- Pour changer les 8 couleurs proposées dans Paramètres, modifie
  `ColorSwatches` dans `ui/theme/Color.kt`.
- Pour changer le nom affiché, modifie `strings.xml` (nom de l'app) et le
  texte dans `AppHeader` (`MainActivity.kt`).
