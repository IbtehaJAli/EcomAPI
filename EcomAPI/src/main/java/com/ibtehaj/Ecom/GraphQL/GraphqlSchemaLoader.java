package com.ibtehaj.Ecom.GraphQL;

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
        	        .dataFetcher("products", dataFetchers::products) // all products
        	        .dataFetcher("product", dataFetchers::product) // single product by id 
        	        .dataFetcher("productStock", dataFetchers::productStock) // single product stock by id
        	        .dataFetcher("allProductStocks", dataFetchers::allProductStocks) // all product stocks 
        	        .dataFetcher("productStocks", dataFetchers::productStocks) // product stocks for a product
        	        .dataFetcher("getCustomerProfileById", dataFetchers::getCustomerProfileById) // customer profile by customer ID
        	        .dataFetcher("getCustomerProfileByEmail", dataFetchers::getCustomerProfileByEmail) // customer profile by email
        	        .dataFetcher("getAllCustomerProfiles", dataFetchers::getAllCustomerProfiles) // all customer profiles
        	        .dataFetcher("getSaleById", dataFetchers::getSaleById) // sale by sale ID
        	        .dataFetcher("getSalesByCustomerId", dataFetchers::getSalesByCustomerId) // sales by customer ID
        	        .dataFetcher("getAllSales", dataFetchers::getAllSales) // all sales
        	        .dataFetcher("getSaleItemById", dataFetchers::getSaleItemById) // sale item by sale item ID
        	        .dataFetcher("getAllSaleItemsBySaleId", dataFetchers::getAllSaleItemsBySaleId) // sale items by sale ID
        	        .dataFetcher("getAllSaleItems", dataFetchers::getAllSaleItems)) // all sale items
        	    .build();

        // Generate a GraphQLSchema from the TypeDefinitionRegistry and RuntimeWiring
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
    }
}