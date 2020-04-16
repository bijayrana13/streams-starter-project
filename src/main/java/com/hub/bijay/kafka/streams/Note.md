KAFKA STREAMS (High Level DSL)
It is both a kafka consumer and producer

First librry in the world to provide exactly once capabilities is kafka streams.
One record at a time, no batching, true streaming.....
input is kafka and output is also kafka.....so kafka to kafka

Configuration required for kafkastreams
bootstrap.servers
auto.offset.reset.config
application.id
   Consuer group.id = application.id
   Default client.id prefix
   Prefix to internal changelog topics
default.key/value.serde

The data type of key and value are inferred during compile time.

Streams = ----
Processers = nodes

C:\Users\bijrana\Desktop\bigdata\kafka_2.11-2.2.0\

Kafka topics should be created before starting kafka streams application.

Kafka Stream application create two internal topics= ChangeLog(in case of key transformation) and Repartition (in case aggregation)

1)Start zookeeper and kafka
cd C:\Users\bijrana\Desktop\bigdata\kafka_2.11-2.2.0\bin\windows
set JAVA_HOME=C:\Program Files\Java\jre1.8.0_241
set PATH=C:\Program Files\Java\jre1.8.0_241\bin;%PATH%

c:
cd C:\Users\bijrana\Desktop\bigdata\kafka_2.11-2.2.0\bin\windows
zookeeper-server-start.bat ..\..\config\zookeeper.properties
kafka-server-start.bat ..\..\config\server.properties

2)Create below topics
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 2 --topic word-count-input
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 2 --topic word-count-output

3)List the topics
bin/kafka-topics.sh --list --zookeeper localhost:2181

4)Start stream application

5)Read data from output topic

bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
--topic word-count-output \
--from-beginning \
--formatter kafka.tools.DefaultMessageFormatter \
--property print.key=true \
--property print.value=true \
--property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
--property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer


6)Write data to input topic
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic word-count-input

7)List topic again to check changelog and repartition topics
bin/kafka-topics.sh --list --zookeeper localhost:2181
******************************

Learn Debugging code
Run as fat jar
scale application
******************************

KStreams
--------
All inserts
Infinite
Unbounded data streams

use while reading data from not compacted topic
use if new data is partial/transaction info
both Map and MapValues can be used here.

KTable
------
upserts on same key and non null values
deletes on same key and null values

If same key comes again with new value then KTable will be updated with this record rather than insert
If same key comes again with null value then that Key data from KTable will be deleted

use while reading data from log-compacted(aggregated) topic
use if new records are complete self sufficient and need a structured format..

only MapValues can be used here

NOTE: Any function with a key always triggers a re-partition.

Filter can be used in both .
FlatMapValues and FlatMap cant be used for KTable.

KStream Branch --
Branch(split) a KStream based on one or more predicates.

KStream<String,String>[] branches = stream.branch(
(key, value) -> value > 100,  /* first predicate */
(key, value) -> value > 10,  /* second predicate */
(key, value) -> value > 0,  /* third predicate */
)

KStream :

KStream<String,Long> Streams = builder.stream(
Serdes.String(),
Serdes.Long(),
"Word-Count-Input-Topic"
)

KTable :

KTable<String,Long> Streams = builder.table(
Serdes.String(),
Serdes.Long(),
"Word-Count-Input-Topic");

KStream :

KStream<String,Long> Streams = builder.globalTable(
Serdes.String(),
Serdes.Long(),
"Word-Count-Input-Topic");


stream.to("topic name") --> write kStream/KTable data to topic.
KStream<String,Long> newStream = stream.through("topic name")  --> Write KStream  to topic and create a KStream out of it again
KTable<String,Long> newTable = table.through("topic name")

Used functions which modifies values only(e.g FlatMapValues) if possible to avoid repartitioning.

LOG COMPACTION:

Log Compaction(not for de-duplication) :
Log compaction ensures to have latest known record in kafka topic.
NOTE: Since de-duplication/log-compaction is done after segment is committed , Consumer/producer still read all data inclusing duplicates and write them as well.
Compaction is only for new consumer/producer so that they dont read all history data of key.

tail broker.log and TRY compaction as below ==>

kafka-topics --zookeeper localhost:2181 --create \
             --topic  employee-salary-compact \
             --partitions 1 --replication-factor 1 \
             --config cleanup.policy=compact \
             --config min.cleanable.dirty.ratio=0.005 \
             --config segment.ms=10000
kafka-console-consumer --bootstrap-server localhost:9092 \
                       --topic employee-salary-compact \
                       --from-beginning \
                       --property print.key=true \
                       --property key.separator=,

kafka-console-producer --broker-list localhost:9092 \
                       --topic employee-salary-compact \
                       --property parse.key=true \
                       --property key.separator=,


#123,{"JHON":"80000"}
#456,{"MARK":"90000"}
#764,{"LISA":"100000"}

#764,{"LISA":"110000"}
#456,{"MARK":"140000"}


STream and Table Duality ----
Stream is the changelog of a table which captures the statechange of a table.
Table is a snapshot of all latest value of each key

Try converting a table to stream or vice versa.

Transforming KTable to a KStream (if we need all updates/deletes/inserts)
KStream<byte[], String> stream = table.toStream();


Transforming KStream to a KTable in two ways
1)Chain a groupByKey() and an aggregation step (count, aggregate, reduce)
   KTable<String, String> table = usersAndColours.groupByKey().count();
2)Write back to kafka and read as table
   stream.to("intermediate-topic");
   KTable<String, String> table = builder.table("intermediate-topic");













