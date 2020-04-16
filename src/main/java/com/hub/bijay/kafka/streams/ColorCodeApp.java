package com.hub.bijay.kafka.streams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;

import java.util.Arrays;
import java.util.Properties;

public class ColorCodeApp {

    public static void main(String[] args) {

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG,"Wordcount-Application");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        KStreamBuilder builder = new KStreamBuilder();

        KStream<String, String> WordCountInput = builder.stream("word-count-input");
        KTable<String, Long> WordCounts = WordCountInput.mapValues(value -> value.toLowerCase())
                .flatMapValues(value -> Arrays.asList(value.split(" ")))
                .selectKey((key,value) -> value)
                .groupByKey()
                .count("Counts");

        WordCounts.to(Serdes.String(), Serdes.Long(),"word-count-output");

        KafkaStreams streams = new KafkaStreams(builder, config);

        streams.start();
        //Printing topology
        System.out.println(streams.toString());

        //Shutdown hook to correctly close the stream application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));


    }
}
