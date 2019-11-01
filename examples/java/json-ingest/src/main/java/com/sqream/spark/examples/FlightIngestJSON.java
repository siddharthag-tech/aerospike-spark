package com.sqream.spark.examples;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class FlightIngestJSON {
	private static final String dataPath = "../../../data/json/flights";
	private static transient Logger log = LoggerFactory.getLogger(FlightIngestJSON.class);
	public static void main(String[] args) {
		FlightIngestJSON app = new FlightIngestJSON();
		app.start();
	}

	private boolean start() {
		log.info("Ingest flights from " + dataPath);
		SparkSession spark = SparkSession.builder()
				.appName("JSON to SQream")
				.master("local[*]").getOrCreate();

		Dataset<Row> jsontDf = spark.read()
				.format("json")
				.option("extensions", "json")
				.json(dataPath).where("Year = 2000").where("Month = 11").where("DayofMonth = 11").where("UniqueCarrier = 'UA'");

		jsontDf.printSchema();
		jsontDf.show(5);


		long start = System.currentTimeMillis();
		// Write it to SQream
		jsontDf.write()
		.mode(SaveMode.Append)
		.format("com.sqream.spark.sql")
		.option("sqream.host", "31.154.184.250")
		.option("sqream.port", 5000)
		.option("sqream.user", "sqream")
		.option("sqream.password", "sqream")
		.option("sqream.database", "master")
		.option("sqream.table", "spark.flights")
		.option("sqream.batch.insert.size", 10000)
		.save();
		long end = System.currentTimeMillis();
		log.info("Flights saved in " + (end-start) + " milliseconds");		
		return true;
	}
}

/*
|-- Year: long (nullable = true)
|-- Month: long (nullable = true)
|-- DayofMonth: long (nullable = true)
|-- DayOfWeek: long (nullable = true)
|-- DepTime: string (nullable = true)
|-- CRSDepTime: string (nullable = true)
|-- ArrTime: string (nullable = true)
|-- CRSArrTime: string (nullable = true)
|-- UniqueCarrier: string (nullable = true)
|-- FlightNum: string (nullable = true)
|-- TailNum: string (nullable = true)
|-- ActualElapsedTime: string (nullable = true)
|-- CRSElapsedTime: string (nullable = true)
|-- AirTime: long (nullable = true)
|-- ArrDelay: long (nullable = true)
|-- DepDelay: long (nullable = true)
|-- Origin: string (nullable = true)
|-- Dest: string (nullable = true)
|-- Distance: string (nullable = true)
|-- TaxiIn: long (nullable = true)
|-- TaxiOut: long (nullable = true)
|-- Cancelled: string (nullable = true)
|-- CancellationCode: string (nullable = true)
|-- Diverted: string (nullable = true)
|-- CarrierDelay: string (nullable = true)
|-- WeatherDelay: string (nullable = true)
|-- NASDelay: string (nullable = true)
|-- SecurityDelay: string (nullable = true)
|-- LateAircraftDelay: string (nullable = true)


 */
