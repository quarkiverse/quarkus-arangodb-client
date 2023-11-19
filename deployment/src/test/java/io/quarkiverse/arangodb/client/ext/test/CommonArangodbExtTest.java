package io.quarkiverse.arangodb.client.ext.test;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;

public abstract class CommonArangodbExtTest {
    @Inject
    protected ArangoDB arangoDB;
    protected ArangoDatabase db;
    protected ArangoCollection collection;

    @BeforeEach
    public void setup() {
        db = arangoDB.db("test");
        db.create();
        collection = db.collection("persons");
        collection.create();
    }

    @AfterEach
    public void tearDown() {
        db.drop();
    }

}
