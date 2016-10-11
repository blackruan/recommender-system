import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;


public class MultiplicationMapperJoin {

    public static class CoOccurrenceMapper extends Mapper<LongWritable, Text, IntWritable, Text> {

        // map method
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            //input movieA: movieB \t normalizedRelation
            //output movieA \t coOccurrence:movieB:normalizedRelation

            String[] tokens = value.toString().trim().split("\t");
            String[] movies = tokens[0].split(":");

            int movie1 = Integer.parseInt(movies[0]);
            int movie2 = Integer.parseInt(movies[1]);
            double relation = Double.parseDouble(tokens[1]);

            context.write(new IntWritable(movie1), new Text("coOccurrence:" + movie2 + ":" + relation));
        }
    }

    public static class RateMapper extends Mapper<LongWritable, Text, IntWritable, Text> {

        // map method
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            //input user,movie,rating
            //output movie \t rate:user:rating
            String[] tokens = value.toString().trim().split(",");
            int user = Integer.parseInt(tokens[0]);
            int movie = Integer.parseInt(tokens[1]);
            double rating = Double.parseDouble(tokens[2]);

            context.write(new IntWritable(movie), new Text("rate:" + user + ":" + rating));
        }
    }

    public static class MapperJoinReducer extends Reducer<IntWritable, Text, Text, DoubleWritable> {
        // reduce method
        @Override
        public void reduce(IntWritable key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            // if the calculated score is smaller than the threshold, do not write this result out in order to optimize
            // the file size of the intermediate jobs
            double threshold = 0.1;

            Map<Integer, Double> movieRelationMap = new HashMap<Integer, Double>();
            Map<Integer, Double> ratingMap = new HashMap<Integer, Double>();

            //user:movie score
            while(values.iterator().hasNext()) {
                String[] tokens =  values.iterator().next().toString().trim().split(":");
                if(tokens[0].equals("coOccurrence")) {
                    movieRelationMap.put(Integer.valueOf(tokens[1]), Double.valueOf(tokens[2]));
                } else {
                    ratingMap.put(Integer.valueOf(tokens[1]), Double.valueOf(tokens[2]));
                }
            }

            for(Map.Entry<Integer, Double> ratingEntry: ratingMap.entrySet()) {

                for(Map.Entry<Integer, Double> relationEntry: movieRelationMap.entrySet()) {

                    double score = relationEntry.getValue() * ratingEntry.getValue(); //5 * 8 = 40
                    if(score >= threshold) {
                        DecimalFormat df = new DecimalFormat("#.00");
                        score = Double.valueOf(df.format(score));
                        context.write(new Text(ratingEntry.getKey() + ":" + relationEntry.getKey()), new DoubleWritable(score));
                    }
                    // user:movie score
                }

            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf);

        job.setJarByClass(MultiplicationMapperJoin.class);
        job.setReducerClass(MapperJoinReducer.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(Text.class);

        MultipleInputs.addInputPath(job, new Path(args[0]), TextInputFormat.class, CoOccurrenceMapper.class);
        MultipleInputs.addInputPath(job, new Path(args[1]), TextInputFormat.class, RateMapper.class);

        TextOutputFormat.setOutputPath(job, new Path(args[2]));

        job.waitForCompletion(true);
    }
}
