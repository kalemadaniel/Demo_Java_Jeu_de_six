# 🎲 Demo_Java_Jeu_de_six

Un jeu de **petits chevaux (Ludo)** développé en **Java Swing natif**, avec le thème **Nimbus**.

Jouez à plusieurs sur le même ordinateur ou affrontez la machine en solo. Le projet fonctionne **sans Maven ni bibliothèque externe**.

## ✨ Fonctionnalités

- De **1 à 4 joueurs humains**.
- Un adversaire automatique en mode solo.
- Trois niveaux de difficulté : **Facile, Moyen et Difficile**.
- Personnalisation du nom et de la couleur de chaque joueur.
- Quatre pions par joueur.
- Lancer de dé, captures et cases protégées.
- Indication des pions disponibles et suivi des arrivées.
- Détection de la victoire.
- Boutons pour consulter les règles ou commencer une nouvelle partie.

## 🖼️ Aperçu
La configuration du jeu
<img width="445" height="378" alt="Capture d&#39;écran 2026-11-13 143432" src="https://github.com/user-attachments/assets/aef3f248-0827-40e0-b621-fe69314d40a8" />

Le jeu
<img width="983" height="792" alt="Capture d&#39;écran 2026-11-13 144735" src="https://github.com/user-attachments/assets/e594ac73-846b-48e8-902d-fbcff287844a" />


### Sous Windows

1. Téléchargez et extrayez le projet.
2. Double-cliquez sur **Lancer.bat**.

### Depuis un terminal

Dans le dossier contenant le fichier JAR :

```bash
java -jar Demo_Java_Jeu_de_six.jar
```

## 💻 Ouvrir le projet dans NetBeans

1. Téléchargez ou clonez ce dépôt.
2. Ouvrez **NetBeans IDE 8.0.2**.
3. Sélectionnez **File → Open Project**.
4. Choisissez le dossier contenant `build.xml`, `nbproject` et `src`.
5. Cliquez sur **Open Project**.
6. Faites un clic droit sur le projet, puis choisissez **Run**.

Si NetBeans demande la classe principale, sélectionnez :

```text
Demo_Java_Jeu_de_six
```

Si nécessaire, configurez le **JDK 8** dans les propriétés du projet.

## 🎮 Comment jouer

### Configuration

Choisissez le nombre de joueurs humains, puis attribuez un nom et une couleur différente à chacun.

- **1 joueur humain :** la machine est ajoutée automatiquement.
- **2 à 4 joueurs humains :** les participants jouent à tour de rôle sur le même ordinateur.

### Règles de cette variante

1. Chaque joueur possède **quatre pions** dans sa base.
2. Il faut obtenir un **6** pour sortir un pion.
3. Un pion déjà sorti avance du nombre indiqué par le dé.
4. Obtenir un **6** permet de rejouer, même sans déplacement possible.
5. Atterrir sur un adversaire le renvoie à sa base, sauf sur une case protégée.
6. Les cases marquées d’une **étoile** protègent tous les pions qui s’y trouvent.
7. Plusieurs pions peuvent partager une case sans bloquer le passage.
8. Un résultat exact est nécessaire pour terminer le couloir d’arrivée.
9. Le premier joueur à rentrer ses **quatre pions** gagne.

Cette variante ne pénalise pas trois 6 successifs et n’accorde pas de lancer supplémentaire après une capture.

### Commandes

- Cliquez sur **Lancer le dé**.
- Cliquez sur un pion disponible du plateau ou sur son bouton.
- Les pions jouables sont indiqués par un contour sombre.
- Si aucun déplacement n’est possible, le jeu poursuit automatiquement.

## 🤖 Niveaux de difficulté

Les niveaux concernent uniquement la machine en mode solo.

| Niveau | Comportement |
|---|---|
| Facile | Choisit au hasard parmi les coups possibles. |
| Moyen | Privilégie les arrivées, les captures et la progression. |
| Difficile | Prend aussi en compte les refuges et les menaces immédiates. |

La machine utilise des règles de décision simples, sans recherche exhaustive. Le dé reste aléatoire pour tous les joueurs, quel que soit le niveau.


## 🔧 Compiler le projet

### Dans NetBeans

Faites un clic droit sur le projet, puis sélectionnez **Clean and Build**.

Le fichier JAR est généré dans :

```text
dist/Demo_Java_Jeu_de_six.jar
```

### Sous Windows

Exécutez **Compiler.bat** avec un JDK configuré dans `JAVA_HOME` ou accessible depuis le `PATH`.

### Avec Ant

Si Ant est installé et accessible depuis le terminal :

```bash
ant clean
ant jar
ant run
```

## 🧪 Vérifications

Le projet comprend des tests intégrés pour vérifier :

- La sortie des pions sur un 6.
- L’alternance des tours.
- Les captures et les cases protégées.
- L’arrivée avec un résultat exact.
- La détection de la victoire.
- Les coordonnées du parcours.
- Neuf parties simulées couvrant les trois niveaux avec deux, trois et quatre joueurs.

Pour les exécuter avec le JAR fourni :

```bash
java -jar Demo_Java_Jeu_de_six.jar --test
```

Ou avec Ant :

```bash
ant test
```

## 🤝 Contributions

Les suggestions et contributions sont les bienvenues.

Pour signaler un problème, ouvrez une **issue** en précisant :

- Votre version de Java et votre système d’exploitation.
- Les étapes permettant de reproduire le problème.
- Le résultat attendu et le résultat observé.
- Le message d’erreur éventuel.
