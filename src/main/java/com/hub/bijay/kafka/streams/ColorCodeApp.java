package com.hub.bijay.kafka.streams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;

import java.util.Arrays;
import java.util.Properties;

public class ColorCodeApp {

    public static void main(String[] args) {

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG,"ColorCount-Application");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");

        KStreamBuilder builder = new KStreamBuilder();

        KStream<String, String> ColorCountInput = builder.stream("color-count-input");

        KStream<String, String> ColorCounts = ColorCountInput.filter((k,v) -> v.contains(","))
                .selectKey((k, v) -> v.split(",")[0].toLowerCase())
                .mapValues(value -> value.split(",")[1].toLowerCase())
                .filter((key, value) -> Arrays.asList("red","green","blue").contains(value));


        ColorCounts.to(Serdes.String(), Serdes.String(),"color-count-intermediate");

        KTable<String, String> colorTable = builder.table("color-count-intermediate");

        KTable<String, Long> CountStreamFinalTable = colorTable.groupBy((k,v) -> new KeyValue<>(v,v)).count("COUNTS");

        CountStreamFinalTable.to(Serdes.String(),Serdes.Long(),"color-count-output");


        KafkaStreams streams = new KafkaStreams(builder, config);

        streams.cleanUp();
        streams.start();
        //Printing topology
        System.out.println(streams.toString());

        //Shutdown hook to correctly close the stream application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));


    }
}
