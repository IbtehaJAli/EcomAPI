package com.ibtehaj.Ecom;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.RuntimeWiring;

import java.io.IOException;
import java.io.InputStream;

public class GraphqlSchemaLoader {
    private static final String SCHEMA_FILE = "/schema.graphqls";

    public static GraphQLSchema loadSchema(DataFetchers dataFetchers) throws IOException {
        // Load the schema file from the resources
        InputStream inputStream = GraphqlSchemaLoader.class.getResourceAsStream(SCHEMA_FILE);

        // Parse the schema file into a TypeDefinitionRegistry
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(inputStream);

        // Wire up the data fetchers using the DataFetchers object
        RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
        	    .type("Query", builder -> builder
        	        .dataFetcher("products", dataFetchers::products)// all products
        	        .dataFetcher("product", dataFetchers::product)// single product by id 
        	        .dataFetcher("productStock", dataFetchers::productStock) // single product stock by id
        	        .dataFetcher("allProductStocks", dataFetchers::allProductStocks) // all product stocks 
        	        .dataFetcher("productStocks", dataFetchers::productStocks))// product stocks for a product
        	    .build();

        // Generate a GraphQLSchema from the TypeDefinitionRegistry and RuntimeWiring
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
    }
}