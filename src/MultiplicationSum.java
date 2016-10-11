import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class MultiplicationSum {

    public static class MultiplicationSumMapper extends Mapper<LongWritable, Text, Text, DoubleWritable> {

        // map method
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            //input user:movie \t score
            //output user:movie score
            String[] tokens = value.toString().trim().split("\t");
            double score = Double.parseDouble(tokens[1]);
            context.write(new Text(tokens[0]), new DoubleWritable(score));
        }
    }

    public static class MultiplicationSumReducer extends Reducer<Text, DoubleWritable, IntWritable, Text> {
        // reduce method
        @Override
        public void reduce(Text key, Iterable<DoubleWritable> values, Context context)
                throws IOException, InterruptedException {

            // if the calculated score is smaller than the threshold, do not write this result out in order to optimize
            // the file size of the intermediate jobs
            double threshold = 1;

            //user:movie score
            double sum = 0;
            while(values.iterator().hasNext()) {
                sum += values.iterator().next().get();
            }

            if(sum >= threshold) {
                String[] tokens = key.toString().split(":");
                int user = Integer.parseInt(tokens[0]);
                context.write(new IntWritable(user), new Text(tokens[1] + ":" + sum));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf);
        job.setMapperClass(MultiplicationSumMapper.class);
        job.setReducerClass(MultiplicationSumReducer.class);

        job.setJarByClass(MultiplicationSum.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(Text.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(DoubleWritable.class);

        TextInputFormat.setInputPaths(job, new Path(args[0]));
        TextOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
    }

}
