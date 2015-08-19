package json.benchmark.immutables;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.ServiceLoader;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import json.application.TestDataSupplier;
import json.application.immutables.ImmutablesSupplier;
import json.application.immutables.Response;
import json.benchmark.BenchmarkBase;
import json.benchmark.BenchmarkBase.ParserBenchmarkBase;
import json.benchmark.BenchmarkBase.SerializerBenchmarkBase;

public class GsonBenchmark {

	public static class ParserBenchmark extends ParserBenchmarkBase {
		TypeAdapter<Response>	adapter;
		TypeAdapter<Map<?, ?>>	mapAdapter;

		@Setup
		public void prepare() {
			GsonBuilder builder = new GsonBuilder();
			ServiceLoader.load(TypeAdapterFactory.class).forEach(builder::registerTypeAdapterFactory);
			Gson gson = builder.create();
			this.adapter = gson.getAdapter(Response.class);
			this.mapAdapter = gson.getAdapter(new TypeToken<Map<?, ?>>() {
			});
		}

		@Benchmark
		public Response reader() throws Exception {
			try (InputStream in = this.json.openStream()) {
				return this.adapter.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8));
			}
		}

		@Benchmark
		@SuppressWarnings("rawtypes")
		public Map parse2Map() throws Exception {
			try (InputStream in = this.json.openStream()) {
				return this.mapAdapter.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8));
			}
		}

		@Benchmark
		public Response bufferedReader() throws Exception {
			try (InputStream in = this.json.openStream()) {
				return this.adapter.fromJson(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
			}
		}
	}

	public static class SerializerBenchmark extends SerializerBenchmarkBase<Response> {

		TypeAdapter<Response> adapter;

		@Setup
		public void prepare() {
			GsonBuilder builder = new GsonBuilder();
			ServiceLoader.load(TypeAdapterFactory.class).forEach(builder::registerTypeAdapterFactory);
			Gson gson = builder.create();
			this.adapter = gson.getAdapter(Response.class);
		}

		@Override
		protected TestDataSupplier<Response> newSupplier() {
			return new ImmutablesSupplier();
		}

		@Benchmark
		public String string() throws Exception {
			return this.adapter.toJson(this.testdata);
		}
	}

	public static void main(String[] args) throws Exception {
		BenchmarkBase.runForDebug(GsonBenchmark.class);
	}

}
