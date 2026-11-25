# Hadoop TP1 — Installation, configuration et WordCount

TP d'introduction à **Hadoop**, **HDFS**, **YARN** et **MapReduce**.  
Ce dépôt documente l'installation d'un environnement Hadoop pseudo-distribué sur macOS ainsi que les exercices de développement Java et d'analyse des performances.

## Environnement

| Élément | Version / configuration |
| ------- | ----------------------- |
| Système | macOS Intel (x86_64)    |
| Java    | Temurin JDK 17          |
| Hadoop  | 3.5.0                   |
| Mode    | Pseudo-distribué        |
| Shell   | Zsh                     |

## Structure du dépôt

```text
hadoop-tp1-configuration/
├── configuration/       # Configuration Hadoop
├── src/
│   └── WordCount.java   # Programme MapReduce
├── README.md
└── .gitignore
```

Les datasets (`data_light/`), fichiers compilés (`.class`), archives JAR et résultats Hadoop ne sont pas versionnés.

# Partie A — Installation et configuration

## 1. Java et Hadoop

Vérification de Java :

```bash
java -version
```

Configuration dans `~/.zshrc` :

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export HADOOP_HOME="$HOME/Applications/hadoop-3.5.0"
export PATH="$JAVA_HOME/bin:$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$PATH"
```

Recharger les variables :

```bash
source ~/.zshrc
hadoop version
```

## 2. Configuration Hadoop

Les fichiers de configuration utilisés :

- `core-site.xml` : adresse HDFS (`hdfs://localhost:9001`).
- `hdfs-site.xml` : stockage HDFS et réplication.
- `mapred-site.xml` : MapReduce sur YARN.
- `yarn-site.xml` : configuration YARN.
- `hadoop-env.sh` : configuration de Java.

Le port **9001** a été choisi car le port 9000 était occupé.

## 3. Démarrage et vérification

```bash
start-dfs.sh
start-yarn.sh
jps
hdfs dfs -ls /
```

Interfaces Web :

| Service         | URL                   |
| --------------- | --------------------- |
| NameNode        | http://localhost:9870 |
| ResourceManager | http://localhost:8088 |

# Partie B — Exercices

## Exercice 1 — Arborescence HDFS

Création du répertoire utilisateur HDFS :

```bash
hdfs dfs -mkdir -p /user/guississ
hdfs dfs -ls /user/guississ
```

## Exercice 2 — Premier job MapReduce

Exécution de l'exemple `grep` fourni par Hadoop sur les fichiers XML de configuration :

```bash
hdfs dfs -mkdir -p input
hdfs dfs -put $HADOOP_HOME/etc/hadoop/*.xml input

hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-3.5.0.jar \
  grep input output 'dfs[a-z.]+'
```

Le job s'est terminé avec succès. Le répertoire `output` contient les résultats et `_SUCCESS`.

## Exercice 3 — Transfert des données dans HDFS

Datasets disponibles dans `data_light` :

| Dataset  | Contenu                             |
| -------- | ----------------------------------- |
| `wp_20`  | 20 articles Wikipédia               |
| `wp_200` | 200 articles Wikipédia              |
| `socio`  | Dataset utilisé pour la suite du TP |

Les datasets `wp_20` et `wp_200` ont été transférés dans HDFS.

## Exercice 4 — Bibliothèques Hadoop pour Java

Les bibliothèques Hadoop sont disponibles sous :

```text
$HADOOP_HOME/share/hadoop/
```

API utilisée :

```java
org.apache.hadoop.mapreduce
```

Il s'agit de la nouvelle API MapReduce, contrairement à l'ancienne API `org.apache.hadoop.mapred`.

## Exercice 5 — Mapper WordCount

Le Mapper :

1. lit une ligne de texte ;
2. convertit le texte en minuscules ;
3. découpe la ligne en mots ;
4. ignore les tokens vides ;
5. émet `(mot, 1)`.

La tokenisation utilise `[^\\p{L}]+`, qui considère les caractères non alphabétiques comme séparateurs et prend en compte les lettres Unicode.

## Exercice 6 — Reducer WordCount

Le Reducer reçoit les valeurs associées à un mot, les additionne et produit :

```text
(mot, nombre_total_occurrences)
```

## Exercice 7 — Configuration du Job

La méthode `main()` configure :

- la `Configuration` ;
- le `Job` ;
- les classes Mapper et Reducer ;
- les formats d'entrée et de sortie ;
- les types de sortie ;
- les chemins HDFS.

Les chemins sont transmis en arguments : `args[0]` pour l'entrée et `args[1]` pour la sortie.

---

## Exercice 8 — Compilation et exécution de WordCount

### Objectif

Compiler notre programme MapReduce Java, créer une archive JAR et exécuter WordCount sur `wp_20`.

### Compilation

Depuis le dossier `src` :

```bash
cd ~/Documents/hadoop-tp1-configuration/src
hadoop com.sun.tools.javac.Main WordCount.java
```

La compilation génère les classes Java :

- `WordCount.class`
- `WordCount$WordCountMapper.class`
- `WordCount$WordCountReducer.class`

### Création du JAR

```bash
jar cf wc.jar WordCount*.class
```

### Vérification de l'entrée HDFS

```bash
hdfs dfs -ls wp_20
```

### Exécution

Si le répertoire de sortie existe déjà, le supprimer :

```bash
hdfs dfs -rm -r output
```

Lancer WordCount :

```bash
hadoop jar wc.jar WordCount wp_20 output
```

### Résultats

Le job s'est terminé avec succès (`completed successfully`).

| Compteur                               |  Valeur |
| -------------------------------------- | ------: |
| Fichiers en entrée                     |      20 |
| Tâches Map                             |      20 |
| Tâches Reduce                          |       1 |
| Map input records                      |   1 003 |
| Map output records                     |  35 968 |
| Reduce output records / mots distincts |   6 672 |
| Failed Shuffles                        |       0 |
| Bytes Read                             | 235 808 |
| Bytes Written                          |  73 713 |

### Vérification des résultats

```bash
hdfs dfs -ls output
hdfs dfs -cat output/part-r-00000
```

Le dossier contient `part-r-00000`, les résultats du Reducer, et `_SUCCESS`, qui confirme la réussite du job.

Pour afficher seulement les premières lignes :

```bash
hdfs dfs -cat output/part-r-00000 | head -20
```

Exemples de résultats :

```text
a       159
abord   3
abondamment 1
```

Le résultat contient **6 672 mots distincts**.

---

## Exercice 9 — Analyse des logs et mesure du temps d'exécution

### Objectif

Analyser les logs Hadoop afin de relever le nombre de fichiers, de splits, de tâches Map et Reduce ainsi que leurs durées. Mesurer également le temps réel d'exécution.

### Informations des logs

Pour le dataset `wp_20` :

```text
Total input files to process : 20
number of splits:20
Launched map tasks=20
Launched reduce tasks=1
Total time spent by all map tasks (ms)=116848
Total time spent by all reduce tasks (ms)=14203
```

### Temps cumulé des tâches

| Phase  | Temps en ms | Temps en secondes |
| ------ | ----------: | ----------------: |
| Map    |     116 848 |         116,848 s |
| Reduce |      14 203 |          14,203 s |

Ces valeurs correspondent à la somme des durées des tâches, pas au temps global réellement écoulé.

### Mesure du temps réel

Supprimer l'ancien output, puis exécuter le programme avec `time` :

```bash
hdfs dfs -rm -r output
time hadoop jar wc.jar WordCount wp_20 output
```

Résultat obtenu :

```text
6,93s user
0,73s system
20% cpu
37,309 total
```

Le temps réel d'exécution est **37,309 secondes**.

### Interprétation

Les temps cumulés Map et Reduce sont supérieurs au temps réel, car plusieurs tâches Map peuvent s'exécuter en parallèle. Le temps cumulé représente la somme des durées des tâches, tandis que le temps réel mesure la durée totale du job.

### Vérification avec YARN

Interface : http://localhost:8088

Informations observées :

- 5 applications soumises ;
- 5 applications terminées ;
- 0 application en cours ;
- 1 nœud actif ;
- ResourceManager `STARTED` ;
- Hadoop 3.5.0.

---

## Exercice 10 — Comparaison des performances

### Objectif

Comparer WordCount sur deux datasets de tailles différentes : `wp_20` et `wp_200`.

Les indicateurs étudiés sont le nombre de fichiers, de splits, de tâches Map/Reduce et les temps de traitement.

### Dataset `wp_20`

```bash
hdfs dfs -rm -r output
time hadoop jar wc.jar WordCount wp_20 output
```

| Indicateur          |  Résultat |
| ------------------- | --------: |
| Fichiers            |        20 |
| Splits              |        20 |
| Maps                |        20 |
| Reducers            |         1 |
| Temps Map cumulé    | 116,848 s |
| Temps Reduce cumulé |  14,203 s |
| Temps réel          |  37,309 s |

### Dataset `wp_200`

```bash
hdfs dfs -rm -r output
time hadoop jar wc.jar WordCount wp_200 output
```

| Indicateur                             |                Résultat |
| -------------------------------------- | ----------------------: |
| Fichiers                               |                     200 |
| Splits                                 |                     200 |
| Maps                                   |                     200 |
| Reducers                               |                       1 |
| Map output records                     |                 483 649 |
| Reduce output records / mots distincts |                  34 465 |
| Temps Map cumulé                       |             1 581,719 s |
| Temps Reduce cumulé                    |               277,008 s |
| Temps réel                             | 348,25 s (≈ 5 min 48 s) |

### Tableau comparatif

| Dataset  | Fichiers | Splits | Maps | Reducers |  Map cumulé | Reduce cumulé | Temps réel |
| -------- | -------: | -----: | ---: | -------: | ----------: | ------------: | ---------: |
| `wp_20`  |       20 |     20 |   20 |        1 |   116,848 s |      14,203 s |   37,309 s |
| `wp_200` |      200 |    200 |  200 |        1 | 1 581,719 s |     277,008 s |   348,25 s |

### Interprétation

`wp_200` contient 10 fois plus de fichiers que `wp_20`.

Le temps réel passe de 37,309 secondes à 348,25 secondes :

```text
348,25 / 37,309 ≈ 9,33
```

Le traitement de `wp_200` est donc environ **9,33 fois plus long**.

L'augmentation du volume de données entraîne davantage de splits et de tâches Map. Hadoop répartit les traitements, mais le lancement et la gestion des tâches ont également un coût. Dans notre environnement local, le temps total augmente donc avec la taille du dataset.

Les temps cumulés Map et Reduce ne représentent pas le temps réel du job, notamment en raison de l'exécution parallèle des tâches Map.

### Conclusion

Cette expérience permet de constater l'impact de la taille des données sur les performances d'un job MapReduce et de comprendre le rôle du parallélisme.

---

## Conclusion générale

Ce TP nous a permis de :

- installer et configurer Hadoop en mode pseudo-distribué ;
- manipuler HDFS ;
- exécuter un premier job MapReduce ;
- développer, compiler et exécuter un programme MapReduce Java ;
- analyser les logs et les compteurs Hadoop ;
- mesurer les temps d'exécution ;
- comparer les performances selon la taille des datasets.

## Auteur

**Mohamed Guissi**  
Master 2 — ILSEN  
Avignon Université
