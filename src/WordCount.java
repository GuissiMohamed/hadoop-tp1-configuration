import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.fs.Path;
public class WordCount {

    // Classe Mapper : transforme chaque ligne en paires (mot, 1)
    public static class WordCountMapper
            extends Mapper<LongWritable, Text, Text, IntWritable> {

        // Valeur envoyée pour chaque mot : 1
        private final IntWritable value = new IntWritable(1);

        // Clé de sortie : le mot
        private final Text key = new Text();

        @Override
        protected void map(LongWritable keyInput,
                           Text valueInput,
                           Context context)
                throws IOException, InterruptedException {

            // Récupération de la ligne de texte
            String line = valueInput.toString();

            // Passage en minuscules pour ne pas distinguer la casse
            line = line.toLowerCase();

            // Découpage du texte : tout ce qui n'est pas une lettre
            // est considéré comme un séparateur
            String[] words = line.split("[^\\p{L}]+");

            // Parcours des mots trouvés
            for (String word : words) {

                // On ignore les chaînes vides
                if (!word.isEmpty()) {

                    // La clé correspond au mot
                    key.set(word);

                    // On écrit la paire (mot, 1)
                    context.write(key, value);
                }
            }
        }
    }

    // Classe Reducer : additionne les valeurs associées à chaque mot
public static class WordCountReducer
        extends Reducer<Text, IntWritable, Text, IntWritable> {

    // Méthode reduce appelée pour chaque mot
    @Override
    protected void reduce(Text key,
                          Iterable<IntWritable> values,
                          Context context)
            throws IOException, InterruptedException {

        // Initialisation du compteur
        int sum = 0;

        // Parcours de toutes les valeurs associées au mot
        for (IntWritable value : values) {

            // Addition des valeurs
            sum += value.get();
        }

        // Écriture du résultat final : (mot, nombre d'occurrences)
        context.write(key, new IntWritable(sum));
    }
}
// Méthode principale : configure et lance le Job Hadoop
public static void main(String[] args) throws Exception {

    // Configuration Hadoop
    Configuration conf = new Configuration();

    // Création du Job
    Job job = Job.getInstance(conf, "Custom Word Count Program");

    // Indique la classe principale
    job.setJarByClass(WordCount.class);

    // Configuration du Mapper et du Reducer
    job.setMapperClass(WordCountMapper.class);
    job.setReducerClass(WordCountReducer.class);

    // Format des fichiers d'entrée
    job.setInputFormatClass(TextInputFormat.class);

    // Format des fichiers de sortie
    job.setOutputFormatClass(TextOutputFormat.class);

    // Types des clés et valeurs finales
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);

    // Chemin d'entrée passé en argument
    FileInputFormat.addInputPath(job, new Path(args[0]));

    // Chemin de sortie passé en argument
    FileOutputFormat.setOutputPath(job, new Path(args[1]));

    // Lancement du Job et retour du code d'exécution
    System.exit(job.waitForCompletion(true) ? 0 : 1);
}
}

