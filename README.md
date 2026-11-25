# Hadoop TP1 — Installation et configuration

## 📌 Présentation

Ce dépôt contient les étapes d'installation et de configuration d'un environnement Hadoop pseudo-distribué sur macOS Intel, dans le cadre du TP1 d'introduction à MapReduce et Hadoop.

L'objectif est de disposer d'un environnement local fonctionnel permettant d'utiliser HDFS, YARN et MapReduce.

## 🖥️ Environnement

- **OS :** macOS
- **Architecture :** Intel x86_64
- **Java :** Temurin JDK 17
- **Hadoop :** 3.5.0
- **Mode :** Pseudo-distribué
- **Shell :** Zsh

## 1. Vérification de Java

Vérification de la version installée :

```bash
java -version
```

Configuration de Java 17 :

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

## 2. Installation de Hadoop

Téléchargement de Hadoop 3.5.0 :

```bash
curl -L -O https://dlcdn.apache.org/hadoop/common/hadoop-3.5.0/hadoop-3.5.0.tar.gz
```

Extraction de l'archive :

```bash
tar -xzf hadoop-3.5.0.tar.gz
```

Hadoop est installé dans :

```text
~/Applications/hadoop-3.5.0
```

## 3. Configuration des variables d'environnement

Modification du fichier Zsh :

```bash
nano ~/.zshrc
```

Ajout des variables :

```bash
# Java 17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Hadoop
export HADOOP_HOME="$HOME/Applications/hadoop-3.5.0"

# PATH
export PATH="$JAVA_HOME/bin:$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$PATH"
```

Chargement de la configuration :

```bash
source ~/.zshrc
```

Vérification :

```bash
echo $JAVA_HOME
echo $HADOOP_HOME
hadoop version
```

## 4. Configuration SSH localhost

Activation de la connexion SSH locale depuis les paramètres macOS.

Configuration de l'authentification par clé :

```bash
cat ~/.ssh/id_ed25519.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
chmod 700 ~/.ssh
```

Test de connexion :

```bash
ssh localhost
```

La connexion doit fonctionner sans demander de mot de passe.

## 5. Configuration Hadoop

Les fichiers de configuration sont situés dans :

```text
$HADOOP_HOME/etc/hadoop/
```

### core-site.xml

Configuration du NameNode :

```xml
<configuration>
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://localhost:9001</value>
    </property>
</configuration>
```

Le port 9001 est utilisé car le port 9000 était déjà occupé par PHP-FPM sur la machine.

### hdfs-site.xml

Configuration du stockage HDFS :

```xml
<configuration>

    <property>
        <name>dfs.replication</name>
        <value>1</value>
    </property>

    <property>
        <name>dfs.namenode.name.dir</name>
        <value>file:///Users/guississ/hadoop_data/namenode</value>
    </property>

    <property>
        <name>dfs.datanode.data.dir</name>
        <value>file:///Users/guississ/hadoop_data/datanode</value>
    </property>

</configuration>
```

### mapred-site.xml

Configuration de MapReduce avec YARN :

```xml
<configuration>
    <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
    </property>
</configuration>
```

### yarn-site.xml

Configuration du service auxiliaire nécessaire à MapReduce :

```xml
<configuration>
    <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>
</configuration>
```

### hadoop-env.sh

Configuration de Java :

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
```

## 6. Création des dossiers de stockage

```bash
mkdir -p ~/hadoop_data/namenode
mkdir -p ~/hadoop_data/datanode
```

Ces dossiers sont utilisés pour stocker :

- Les métadonnées du NameNode
- Les blocs de données du DataNode

## 7. Formatage du NameNode

Initialisation du système de fichiers HDFS :

```bash
hdfs namenode -format
```

⚠️ Cette commande est normalement exécutée uniquement lors de l'initialisation du NameNode.

## 8. Démarrage de HDFS

```bash
start-dfs.sh
```

Vérification des processus :

```bash
jps
```

Processus attendus :

- NameNode
- DataNode
- SecondaryNameNode

## 9. Démarrage de YARN

```bash
start-yarn.sh
```

Processus attendus :

- ResourceManager
- NodeManager

## 10. Vérification du cluster

Commande :

```bash
jps
```

Les principaux processus attendus :

```text
NameNode
DataNode
SecondaryNameNode
ResourceManager
NodeManager
```

Test HDFS :

```bash
hdfs dfs -ls /
```

## 11. Interfaces Web

### NameNode

http://localhost:9870

Permet de consulter l'état de HDFS, les DataNodes et les informations du système de fichiers.

### ResourceManager

http://localhost:8088

Permet de consulter l'état de YARN, les ressources disponibles et les applications exécutées.

## 12. Problème rencontré : port 9000 occupé

Lors du démarrage du NameNode, l'erreur suivante est apparue :

```text
java.net.BindException: Address already in use
```

Identification du processus :

```bash
lsof -i :9000
```

Le port 9000 était utilisé par PHP-FPM.

Solution : modifier l'adresse du NameNode dans `core-site.xml` :

```xml
<value>hdfs://localhost:9001</value>
```

Après cette modification, HDFS a démarré correctement.

## ✅ État actuel

- [x] Java 17 installé et configuré
- [x] Hadoop 3.5.0 installé
- [x] Variables d'environnement configurées
- [x] SSH localhost configuré
- [x] Configuration Hadoop réalisée
- [x] NameNode formaté
- [x] HDFS démarré
- [x] YARN démarré
- [x] NameNode Web UI accessible
- [x] ResourceManager Web UI accessible
- [x] Processus Hadoop vérifiés avec `jps`
- [x] HDFS testé avec `hdfs dfs -ls /`

## 🚀 Suite du TP

- Manipulations HDFS
- Import de données dans HDFS
- Exécution de jobs MapReduce
- WordCount
- Analyse statistique BMI

## Auteur
Mohamed Guissi
Master 2 — Data Analytics & Big Data  
Avignon Université

